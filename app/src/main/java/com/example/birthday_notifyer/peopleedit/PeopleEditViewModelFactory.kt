package com.example.birthday_notifyer.peopleedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.birthday_notifyer.database.BirthdayDatabaseDao

class PeopleEditViewModelFactory(
    private val personKey: String,
    private val dataSource: BirthdayDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleEditViewModel::class.java)) {
            return PeopleEditViewModel(personKey, dataSource) as T
        }
        throw IllegalArgumentException()
    }
}