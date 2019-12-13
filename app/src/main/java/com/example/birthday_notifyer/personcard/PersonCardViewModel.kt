package com.example.birthday_notifyer.personcard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.birthday_notifyer.database.BirthdayDatabaseDao
import com.example.birthday_notifyer.database.PersonBirthday
import kotlinx.coroutines.*

class PersonCardViewModel(
    private val personKey: String = "",
    dataSource: BirthdayDatabaseDao
) : ViewModel() {
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


    fun onCancel(){
        _navigateToPeopleShow.value = true
    }


    suspend fun getPersonFromDataBase(): PersonBirthday {
        return withContext(Dispatchers.IO) {
            database.get(personKey)
        }
    }
}