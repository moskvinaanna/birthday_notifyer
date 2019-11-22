package com.example.birthday_notifyer.peopleshow

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.birthday_notifyer.database.BirthdayDatabaseDao
import com.example.birthday_notifyer.database.PersonBirthday
import kotlinx.coroutines.*

class PeopleShowViewModel (
    dataSource: BirthdayDatabaseDao,
    application: Application) : ViewModel(){
    val database = dataSource
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var people = database.getAllPeople()
    private val _navigateToPeopleEdit = MutableLiveData<String>()

    val navigateToPeopleEdit: LiveData<String>
        get() = _navigateToPeopleEdit

    private suspend fun insert(person: PersonBirthday) {
        withContext(Dispatchers.IO) {
            database.insert(person)
        }
    }

    private suspend fun update(person: PersonBirthday) {
        withContext(Dispatchers.IO) {
            database.update(person)
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    fun onAdd(){
        uiScope.launch {
            _navigateToPeopleEdit.value = ""
        }
    }

    fun onClear() {
        uiScope.launch {
            // Clear the database table.
            clear()
        }
    }

    fun onSortByNameAsc(){
        people =  database.getAllPeopleByNameAsc()
    }

    fun onSortByNameDesc(){
        people =  database.getAllPeopleByNameDesc()
    }

    fun onSortByDateAsc(){
        people =  database.getAllPeopleByDateAsc()
    }

    fun onSortByDateDesc(){
        people =  database.getAllPeopleByDateDesc()
    }

//        uiScope.launch {
//            withContext(Dispatchers.IO){
//                database.getAllPeopleByNameDesc()
//            }
//        }

    fun onRemove(idList: List<String>){
        uiScope.launch{
            withContext(Dispatchers.IO){
                database.removePeople(idList)
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onPersonClicked(id: String) {
        _navigateToPeopleEdit.value = id
    }

    fun onPeopleEditNavigated() {
        _navigateToPeopleEdit.value = null
    }

}