package com.example.birthday_notifyer.peopleedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.birthday_notifyer.database.BirthdayDatabaseDao
import com.example.birthday_notifyer.database.PersonBirthday
import kotlinx.coroutines.*

class PeopleEditViewModel(
    private val personKey: String = "",
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

    fun onSave(id: String, name: String, phoneNum: String, date: Long?, photo: String) {
        uiScope.launch {
            if (personKey == "") {
                val person = PersonBirthday(personId = id, name = name, phoneNum = phoneNum, birthdayDate = date, photo = photo)
                insert(person)
            }
            else {
                update(name, phoneNum, date, photo)
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
    private suspend fun update(name: String, phoneNum: String, date: Long?, photo: String) {
        withContext(Dispatchers.IO) {
            val person = database.get(personKey)
            person.name = name
            person.phoneNum = phoneNum
            person.birthdayDate = date
            person.photo = photo
            database.update(person)
        }
    }

    suspend fun getPersonFromDataBase(): PersonBirthday {
        return withContext(Dispatchers.IO) {
            database.get(personKey)
        }
    }
}
