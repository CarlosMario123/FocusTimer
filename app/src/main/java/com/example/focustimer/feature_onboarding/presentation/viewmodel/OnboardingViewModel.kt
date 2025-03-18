package com.example.focustimer.feature_onboarding.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val saveOnboardingCompleted: (Boolean) -> Unit
) : ViewModel() {

    data class OnboardingPage(
        val title: String,
        val description: String,
        val icon: ImageVector
    )

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pages = MutableStateFlow<List<OnboardingPage>>(emptyList())
    val pages: StateFlow<List<OnboardingPage>> = _pages.asStateFlow()

    init {//Cargamos la pagina de onboarding

        _pages.value = listOf(
            OnboardingPage(
                title = "Mantén tu enfoque",
                description = "Organiza tu tiempo de forma eficiente con intervalos de estudio y descanso",
                icon = Icons.Filled.EmojiObjects
            ),
            OnboardingPage(
                title = "Configura tus intervalos",
                description = "Elige entre diferentes duraciones de tiempo para adaptarse a tu estilo de trabajo",
                icon = Icons.Filled.Timer
            ),
            OnboardingPage(
                title = "Mantén la motivación",
                description = "Recibe mensajes motivacionales para mantenerte concentrado durante tus sesiones",
                icon = Icons.Filled.Lightbulb
            )
        )
    }

    fun nextPage(): Boolean {
        if (_currentPage.value < _pages.value.size - 1) {
            _currentPage.value += 1
            return true
        }
        return false
    }


    fun previousPage(): Boolean {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            return true
        }
        return false
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            saveOnboardingCompleted(true)
        }
    }
}