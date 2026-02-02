package com.example.viewmodelkotlinapp.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viewmodelkotlinapp.data.DataStoreSettingsRepository
import com.example.viewmodelkotlinapp.data.SettingsRepository
import com.example.viewmodelkotlinapp.viewmodel.SettingsViewModel

// Creates SettingsViewModel with dependencies
class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Create repository
            val repository: SettingsRepository = DataStoreSettingsRepository(context)

            // Create ViewModel
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}