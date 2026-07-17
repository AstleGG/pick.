package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Pick
import com.example.data.PickHistoryEntry
import com.example.data.PickRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    object Home : Screen
    object CreatePick : Screen
    data class PickDetail(val pickId: Long) : Screen
    object Settings : Screen
}

enum class PickMode {
    QUICK_PICK, SPIN_WHEEL
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PickViewModel(val repository: PickRepository, val application: android.app.Application) : ViewModel() {

    private val sharedPrefs = application.getSharedPreferences("pick_settings", android.content.Context.MODE_PRIVATE)

    var isHapticEnabled by mutableStateOf(sharedPrefs.getBoolean("haptic_enabled", true))
        private set

    var themeMode by mutableStateOf(sharedPrefs.getString("theme_mode", "system") ?: "system")
        private set

    fun updateHapticEnabled(enabled: Boolean) {
        isHapticEnabled = enabled
        sharedPrefs.edit().putBoolean("haptic_enabled", enabled).apply()
        SoundEffects.playPop()
    }

    fun updateThemeMode(mode: String) {
        themeMode = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        SoundEffects.playPop()
    }

    fun clearAllPicksAndHistory() {
        viewModelScope.launch {
            allPicks.value.forEach { pick ->
                repository.deletePick(pick.id)
            }
            SoundEffects.playRemove()
        }
    }

    // Current screen navigation state
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    // Current active decision mode
    var activeMode by mutableStateOf(PickMode.QUICK_PICK)
        private set

    fun setPickMode(mode: PickMode) {
        activeMode = mode
        resetPickResult(silent = true)
    }

    var createMode by mutableStateOf(PickMode.QUICK_PICK)
        private set

    fun updateCreateMode(mode: PickMode) {
        createMode = mode
    }

    // All available picks
    val allPicks: StateFlow<List<Pick>> = repository.allPicks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active pick being played/edited
    var activePickId by mutableStateOf<Long?>(null)
        private set

    val activePick: StateFlow<Pick?> = viewModelScope.run {
        // Flat map latest active pick from activePickId flow
        val activeIdFlow = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
        // Keep synced
        this@PickViewModel::activePickId.let {
            kotlinx.coroutines.flow.flow {
                while (true) {
                    emit(activePickId)
                    kotlinx.coroutines.delay(100)
                }
            }
        }.flatMapLatest { id ->
            if (id != null) {
                repository.getPickById(id)
            } else {
                emptyFlow()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    val activePickHistory: StateFlow<List<PickHistoryEntry>> = viewModelScope.run {
        kotlinx.coroutines.flow.flow {
            while (true) {
                emit(activePickId)
                kotlinx.coroutines.delay(100)
            }
        }.flatMapLatest { id ->
            if (id != null) {
                repository.getHistoryForPick(id)
            } else {
                emptyFlow()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- Create Pick Form State ---
    var createTitle by mutableStateOf("")
        private set
    var createOptions by mutableStateOf(listOf("", ""))
        private set

    fun updateCreateTitle(newTitle: String) {
        createTitle = newTitle
    }

    fun updateCreateOption(index: Int, newValue: String) {
        val newList = createOptions.toMutableList()
        if (index in newList.indices) {
            newList[index] = newValue
            createOptions = newList
        }
    }

    fun addCreateOptionField() {
        createOptions = createOptions + ""
        SoundEffects.playPop()
    }

    fun removeCreateOptionField(index: Int) {
        if (createOptions.size > 2 && index in createOptions.indices) {
            val newList = createOptions.toMutableList()
            newList.removeAt(index)
            createOptions = newList
            SoundEffects.playRemove()
        }
    }

    fun clearCreateForm() {
        createTitle = ""
        createOptions = listOf("", "")
        createMode = PickMode.QUICK_PICK
    }

    // Navigation triggers
    fun navigateTo(screen: Screen) {
        currentScreen = screen
        if (screen is Screen.PickDetail) {
            activePickId = screen.pickId
        } else {
            activePickId = null
        }
        SoundEffects.playClick()
    }

    // Actions
    fun createAndSavePick() {
        val validOptions = createOptions.map { it.trim() }.filter { it.isNotEmpty() }
        if (createTitle.trim().isEmpty() || validOptions.size < 2) return

        viewModelScope.launch {
            val newPick = Pick(
                title = createTitle.trim(),
                options = validOptions
            )
            val insertedId = repository.savePick(newPick)
            val selectedInitialMode = createMode
            clearCreateForm()
            activeMode = selectedInitialMode
            SoundEffects.playSave()
            navigateTo(Screen.PickDetail(insertedId))
        }
    }

    fun deletePick(pickId: Long) {
        viewModelScope.launch {
            repository.deletePick(pickId)
            SoundEffects.playRemove()
            if (activePickId == pickId) {
                navigateTo(Screen.Home)
            }
        }
    }

    fun toggleFavourite(pickId: Long, isFavourite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavourite(pickId, isFavourite)
            SoundEffects.playPop()
        }
    }

    var selectedPickResult by mutableStateOf<String?>(null)
        private set

    var isPickingAnimationRunning by mutableStateOf(false)
        private set

    fun selectOptionRandomly(pick: Pick) {
        if (pick.options.isEmpty()) return
        
        viewModelScope.launch {
            isPickingAnimationRunning = true
            SoundEffects.playStart()
            
            // A realistic physics deceleration delay list (dramatically slowing down)
            val delays = listOf(
                40L, 40L, 40L, 50L, 50L, 65L, 80L, 100L, 120L, 150L, 190L, 240L, 300L, 380L, 480L, 600L, 800L
            )
            
            var lastOption = selectedPickResult
            for (delayMs in delays) {
                // Ensure visual movement by forcing a different candidate if at least 2 options exist
                val nextOption = if (pick.options.size > 1) {
                    var candidate = pick.options.random()
                    while (candidate == lastOption) {
                        candidate = pick.options.random()
                    }
                    candidate
                } else {
                    pick.options.first()
                }
                
                selectedPickResult = nextOption
                lastOption = nextOption
                
                SoundEffects.playTick()
                kotlinx.coroutines.delay(delayMs)
            }
            
            // Absolute final unbiased random draw
            val finalChoice = pick.options.random()
            selectedPickResult = finalChoice
            
            isPickingAnimationRunning = false
            SoundEffects.playSuccess()
            
            repository.recordSelection(pick.id, finalChoice)
        }
    }

    fun startSpinning() {
        isPickingAnimationRunning = true
        SoundEffects.playStart()
    }

    fun recordSpinResult(pick: Pick, winner: String) {
        viewModelScope.launch {
            selectedPickResult = winner
            isPickingAnimationRunning = false
            SoundEffects.playSuccess()
            repository.recordSelection(pick.id, winner)
        }
    }

    fun resetPickResult(silent: Boolean = false) {
        selectedPickResult = null
        if (!silent) {
            SoundEffects.playClear()
        }
    }

    fun clearHistoryForPick(pickId: Long) {
        viewModelScope.launch {
            repository.clearHistory(pickId)
            SoundEffects.playClear()
        }
    }
}

class PickViewModelFactory(
    private val repository: PickRepository,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PickViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PickViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
