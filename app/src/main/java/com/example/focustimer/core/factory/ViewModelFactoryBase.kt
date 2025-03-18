package com.example.focustimer.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Base abstracta para todos los ViewModelFactory
 * Implementa ViewModelProvider.Factory para la creación de ViewModel
 */
abstract class ViewModelFactoryBase : ViewModelProvider.Factory {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        throw NotImplementedError("Child class must implement create method")
    }


    protected fun <T : ViewModel> isViewModelOfType(modelClass: Class<T>, expectedClass: Class<out ViewModel>): Boolean {
        return expectedClass.isAssignableFrom(modelClass)
    }

    /**
     * Método de ayuda para lanzar excepción cuando se pide un ViewModel no soportado
     */
    protected fun throwUnsupportedViewModelException(modelClass: Class<*>): Nothing {
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}