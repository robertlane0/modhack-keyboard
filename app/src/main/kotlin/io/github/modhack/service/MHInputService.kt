package io.github.modhack.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.modhack.input.ComposeSequence
import io.github.modhack.input.DeadAccentSequence
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
 * - [onCurrentInputMethodSubtypeChanged]: handles system-initiated subtype changes.
 */
class MHInputService : InputMethodService(), CoroutineScope, LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = _viewModelStore

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var layoutCache: LayoutCache
    
    private lateinit var inputConnectionManager: InputConnectionManager
    private lateinit var wordComposer: WordComposer
    private lateinit var keyActionHandler: KeyActionHandler

    /** X11 compose key sequence engine for compose key input. */
    val composeSequence = ComposeSequence()

    /** Dead key / accent combining engine for dead key input. */
    val deadAccentSequence = DeadAccentSequence()

    private val _keyboardState = MutableStateFlow(KeyboardState())
    val keyboardState: StateFlow<KeyboardState> = _keyboardState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val _currentLocale = MutableStateFlow("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

    /** The currently active InputMethodSubtype, or null if none. */
    private var currentSubtype: InputMethodSubtype? = null

    lateinit var preferences: StateFlow<KeyboardPreferences>
        private set

    private var cachedInputView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

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
        val composeView = ComposeView(this)

        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        // ViewTreeViewModelStoreOwner lives in lifecycle-viewmodel but the Kotlin
        // compiler cannot resolve it directly, so we invoke it via reflection.
        Class.forName("androidx.lifecycle.ViewTreeViewModelStoreOwner")
            .getMethod("set", View::class.java, ViewModelStoreOwner::class.java)
            .invoke(null, composeView, this)

        composeView.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        composeView.setContent {
            val themeId by preferences.collectAsState()
            io.github.modhack.ui.theme.MHTheme(themeId = themeId.theme) {
                io.github.modhack.ui.KeyboardUI(service = this@MHInputService)
            }
        }
        cachedInputView = composeView
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        wordComposer.reset()
        composeSequence.cancel()
        deadAccentSequence.cancel()
        detectLocale(attribute)
        applyAutoCap(attribute)
        updateLayout()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        wordComposer.reset()
        composeSequence.cancel()
        deadAccentSequence.cancel()
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
            composeSequence.cancel()
            deadAccentSequence.cancel()
            _suggestions.value = emptyList()
        }
    }

    /**
     * Called when the system switches to a different input method subtype.
     *
     * This handles both user-initiated subtype switches (via the globe key
     * or system language picker) and programmatic switches.
     *
     * @param newSubtype The new input method subtype being activated.
     */
    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        currentSubtype = newSubtype

        // Extract locale from the subtype's extra value or locale string
        @Suppress("DEPRECATION")
        val subtypeLocale = newSubtype.locale
        val locale = newSubtype.extraValue
            .split(",")
            .firstOrNull { it.startsWith("KeyboardLayoutSet=") }
            ?.removePrefix("KeyboardLayoutSet=")
            ?: subtypeLocale

        // Map the layout set name to a locale code
        val mappedLocale = when (locale) {
            "qwerty" -> "en"
            "azerty" -> "fr"
            "qwertz" -> "de"
            "russian" -> "ru"
            "arabic" -> "ar"
            "hebrew" -> "he"
            else -> locale
        }

        if (mappedLocale != _currentLocale.value) {
            _currentLocale.value = mappedLocale
            wordComposer.reset()
            composeSequence.cancel()
            deadAccentSequence.cancel()
            updateLayout()
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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
    @Suppress("UNUSED_PARAMETER")
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
     * Supported locales for cycling through with the globe key.
     * These should match the subtypes declared in res/xml/method.xml.
     */
    private val supportedLocales = listOf("en", "es", "fr", "de", "ru", "ar", "he")

    /**
     * Switches to the next available language subtype.
     * Cycles through the locales declared in method.xml.
     *
     * If the system supports it, this also calls [switchInputMethod]
     * to properly notify the system of the subtype change.
     */
    fun switchToNextLanguage() {
        if (supportedLocales.size <= 1) return

        val currentLocale = _currentLocale.value
        val currentIdx = supportedLocales.indexOf(currentLocale)
        val nextIdx = if (currentIdx < 0) 0 else (currentIdx + 1) % supportedLocales.size
        val nextLocale = supportedLocales[nextIdx]

        _currentLocale.value = nextLocale
        wordComposer.reset()
        composeSequence.cancel()
        deadAccentSequence.cancel()
        updateLayout()
    }

    /**
     * Switches to a specific locale by locale code.
     *
     * @param locale The locale code to switch to (e.g., "en", "fr", "de").
     */
    fun switchToLocale(locale: String) {
        if (locale == _currentLocale.value) return
        if (locale !in supportedLocales) return

        _currentLocale.value = locale
        wordComposer.reset()
        composeSequence.cancel()
        deadAccentSequence.cancel()
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
            _keyboardState.value = _keyboardState.value.copy(layoutId = layout.id, layout = layout)
        }
    }
    
    /**
     * Entry point for key events from the UI.
     */
    fun onKey(primaryCode: Int) {
        keyActionHandler.onKey(primaryCode)
    }
}
