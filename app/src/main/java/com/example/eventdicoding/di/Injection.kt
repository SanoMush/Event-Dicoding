package com.example.eventdicoding.di

import android.content.Context
import com.example.eventdicoding.data.local.FavoriteEventRepository

object Injection {
    fun provideRepository(application: Context): FavoriteEventRepository {
        return FavoriteEventRepository.getInstance(application)
    }
}
