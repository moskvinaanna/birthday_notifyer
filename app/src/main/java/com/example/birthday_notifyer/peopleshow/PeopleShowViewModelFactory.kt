package com.example.birthday_notifyer.peopleshow

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.birthday_notifyer.database.BirthdayDatabaseDao

class PeopleShowViewModelFactory(private val dataSource: BirthdayDatabaseDao,
                                 private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleShowViewModel::class.java)) {
            return PeopleShowViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}