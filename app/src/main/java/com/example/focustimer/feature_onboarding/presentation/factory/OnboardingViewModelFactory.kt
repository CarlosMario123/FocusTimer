package com.example.focustimer.feature_onboarding.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.focustimer.core.factory.ViewModelFactoryBase
import com.example.focustimer.feature_onboarding.presentation.viewmodel.OnboardingViewModel


class OnboardingViewModelFactory(
    private val context: Context
) : ViewModelFactoryBase() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (isViewModelOfType(modelClass, OnboardingViewModel::class.java)) {

            return OnboardingViewModel(
                saveOnboardingCompleted = { completed ->
                    val sharedPrefs = context.getSharedPreferences("focus_timer_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("onboarding_completed", completed).apply()
                }
            ) as T
        }

        return throwUnsupportedViewModelException(modelClass)
    }
}