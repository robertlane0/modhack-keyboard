package io.github.modhack.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
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

    lateinit var preferences: StateFlow<KeyboardPreferences>
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(this)
        layoutCache = LayoutCache(this)
        
        // Expose preferences as a state flow. Using a default until collect starts.
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
        
        // Listen to modifier state changes
        launch {
            ModifierState.activeModifiers.collect { modifiers ->
                _keyboardState.value = _keyboardState.value.copy(activeModifiers = modifiers)
            }
        }
    }

    override fun onCreateInputView(): View {
        // Return a basic view until Compose is integrated
        val view = android.widget.FrameLayout(this)
        view.setBackgroundColor(android.graphics.Color.DKGRAY)
        // In Phase 4, we will inflate a ComposeView here and set its content.
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        wordComposer.reset()
        updateLayout()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        wordComposer.reset()
        ModifierState.clearAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel CoroutineScope
    }

    /**
     * Called by KeyActionHandler to toggle between TEXT and SYMBOLS mode.
     */
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
        
        launch {
            val layout = layoutCache.getLayout(mode, "en", resources.configuration.orientation, fullModeStr)
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
