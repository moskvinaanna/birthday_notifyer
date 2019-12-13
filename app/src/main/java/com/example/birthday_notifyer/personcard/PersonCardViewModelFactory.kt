package com.example.birthday_notifyer.personcard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.birthday_notifyer.database.BirthdayDatabaseDao
import com.example.birthday_notifyer.peopleedit.PeopleEditViewModel
import com.example.birthday_notifyer.peopleshow.PeopleShowViewModel

class PeopleEditViewModelFactory(
    private val personKey: String,
    private val dataSource: BirthdayDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonCardViewModel::class.java)) {
            return PersonCardViewModel(personKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}