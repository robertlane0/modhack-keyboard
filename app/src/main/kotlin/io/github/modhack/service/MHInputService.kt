package io.github.modhack.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import io.github.modhack.input.KeyActionHandler
import io.github.modhack.input.ModifierState
import io.github.modhack.input.WordComposer
import io.github.modhack.layout.LayoutCache
import io.github.modhack.model.InputMode
import io.github.modhack.model.KeyboardState
import io.github.modhack.model.Suggestion
import io.github.modhack.prefs.KeyboardPreferences
import io.github.modhack.prefs.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main InputMethodService implementation for ModHack Keyboard.
 *
 * Lifecycle:
 * - [onCreate]: initializes repositories, caches, and coroutine scope.
 * - [onCreateInputView]: returns a [ComposeView] for the keyboard UI.
 * - [onStartInput]: resets word composer, detects locale, applies auto-cap.
 * - [onFinishInput]: resets word composer and clears modifiers.
 * - [onUpdateSelection]: detects cursor movement and updates composing state.
 */
class MHInputService : InputMethodService(), CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var layoutCache: LayoutCache
    
    private lateinit var inputConnectionManager: InputConnectionManager
    private lateinit var wordComposer: WordComposer
    private lateinit var keyActionHandler: KeyActionHandler

    private val _keyboardState = MutableStateFlow(KeyboardState())
    val keyboardState: StateFlow<KeyboardState> = _keyboardState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val _currentLocale = MutableStateFlow("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

    lateinit var preferences: StateFlow<KeyboardPreferences>
        private set

    private var cachedInputView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(this)
        layoutCache = LayoutCache(this)
        
        val prefsFlow = MutableStateFlow(KeyboardPreferences())
        preferences = prefsFlow.asStateFlow()
        launch {
            preferencesRepository.preferences.collect { prefs ->
                prefsFlow.value = prefs
                updateLayout()
            }
        }
        
        inputConnectionManager = InputConnectionManager(this)
        wordComposer = WordComposer()
        keyActionHandler = KeyActionHandler(this, inputConnectionManager, wordComposer)
        
        launch {
            ModifierState.activeModifiers.collect { modifiers ->
                _keyboardState.value = _keyboardState.value.copy(activeModifiers = modifiers)
            }
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                io.github.modhack.ui.KeyboardUI(service = this@MHInputService)
            }
        }
        cachedInputView = composeView
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        wordComposer.reset()
        detectLocale(attribute)
        applyAutoCap(attribute)
        updateLayout()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        wordComposer.reset()
        ModifierState.clearAll()
        _suggestions.value = emptyList()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if (oldSelStart != newSelStart || oldSelEnd != newSelEnd) {
            wordComposer.reset()
            _suggestions.value = emptyList()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cachedInputView = null
        cancel()
    }

    /**
     * Detects the current locale from the editor info.
     */
    private fun detectLocale(attribute: EditorInfo?) {
        val locale = attribute?.inputType?.let { resolveLocaleFromInputType(it) }
            ?: resources.configuration.locales[0].language
            ?: "en"
        _currentLocale.value = locale
    }

    private fun resolveLocaleFromInputType(inputType: Int): String? {
        val fieldType = inputType and android.text.InputType.TYPE_MASK_CLASS
        return when (fieldType) {
            android.text.InputType.TYPE_CLASS_PHONE -> "en"
            else -> null
        }
    }

    /**
     * Applies auto-capitalization based on editor context and preferences.
     */
    private fun applyAutoCap(attribute: EditorInfo?) {
        val prefs = (preferences as? MutableStateFlow)?.value ?: KeyboardPreferences()
        if (!prefs.autoCap) return

        val ic = inputConnectionManager
        val textBefore = ic.getTextBeforeCursor(2)?.toString() ?: return

        if (textBefore.isEmpty() || textBefore.endsWith("\n") || textBefore.endsWith(". ") || textBefore.endsWith("? ") || textBefore.endsWith("! ")) {
            ModifierState.press(io.github.modhack.model.Modifier.SHIFT)
            _keyboardState.value = _keyboardState.value.copy(shiftState = io.github.modhack.model.ShiftState.ON)
        }
    }

    /**
     * Switches to the next available language subtype.
     */
    /**
     * Supported locales for cycling through with the globe key.
     * These should match the subtypes declared in res/xml/method.xml.
     */
    private val supportedLocales = listOf("en", "es", "fr", "de", "ru", "ar", "he")

    /**
     * Switches to the next available language subtype.
     * Cycles through the locales declared in method.xml.
     */
    fun switchToNextLanguage() {
        if (supportedLocales.size <= 1) return

        val currentLocale = _currentLocale.value
        val currentIdx = supportedLocales.indexOf(currentLocale)
        val nextIdx = if (currentIdx < 0) 0 else (currentIdx + 1) % supportedLocales.size
        _currentLocale.value = supportedLocales[nextIdx]
        updateLayout()
    }

    fun toggleSymbolsMode() {
        val newMode = if (_keyboardState.value.mode == InputMode.SYMBOLS) InputMode.TEXT else InputMode.SYMBOLS
        _keyboardState.value = _keyboardState.value.copy(mode = newMode)
        updateLayout()
    }

    private fun updateLayout() {
        val prefs = (preferences as? MutableStateFlow)?.value ?: KeyboardPreferences()
        val mode = _keyboardState.value.mode
        val isPortrait = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
        val fullModeStr = if (isPortrait) prefs.modePortrait else prefs.modeLandscape
        val locale = _currentLocale.value
        
        launch {
            val layout = layoutCache.getLayout(mode, locale, resources.configuration.orientation, fullModeStr)
            _keyboardState.value = _keyboardState.value.copy(layoutId = layout.id)
        }
    }
    
    /**
     * Entry point for key events from the UI.
     */
    fun onKey(primaryCode: Int) {
        keyActionHandler.onKey(primaryCode)
    }
}
