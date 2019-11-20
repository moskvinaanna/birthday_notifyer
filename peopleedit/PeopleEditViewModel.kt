package com.example.birthday_notifyer.peopleedit

import android.app.Person
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.birthday_notifyer.database.BirthdayDatabaseDao
import com.example.birthday_notifyer.database.PersonBirthday
import kotlinx.coroutines.*
import java.util.*

class PeopleEditViewModel(
    private val personKey: Long = 0L,
    dataSource: BirthdayDatabaseDao) : ViewModel() {
    val database = dataSource
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _navigateToPeopleShow = MutableLiveData<Boolean?>()
    val navigateToPeopleShow: LiveData<Boolean?>
        get() = _navigateToPeopleShow
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun doneNavigating() {
        _navigateToPeopleShow.value = null
    }

    fun onSave(name: String, phoneNum: String, date: Long) {
        uiScope.launch {
            if (personKey == -1L) {
                val person = PersonBirthday(name = name, phoneNum = phoneNum, birthdayDate = date)
                insert(person)
            }
            else {
                update(name, phoneNum, date)
            }
        }
        _navigateToPeopleShow.value = true
    }

    fun onCancel(){
        _navigateToPeopleShow.value = true
    }

    private suspend fun insert(person: PersonBirthday) {
        withContext(Dispatchers.IO) {

            database.insert(person)
        }
    }
    private suspend fun update(name: String, phoneNum: String, date: Long) {
        withContext(Dispatchers.IO) {
            val person = database.get(personKey)
            person.name = name
            person.phoneNum = phoneNum
            person.birthdayDate = date
            database.update(person)
        }
    }

    suspend fun getPersonFromDataBase(): PersonBirthday {
        return withContext(Dispatchers.IO) {
            database.get(personKey)
        }
    }
}
