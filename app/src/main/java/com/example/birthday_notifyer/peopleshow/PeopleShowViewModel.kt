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

    var people = database.getAllPeopleByNameAsc("%%")
    private val _navigateToPeopleEdit = MutableLiveData<String>()

    val navigateToPeopleEdit: LiveData<String>
        get() = _navigateToPeopleEdit
    private val _navigateToPersonCard = MutableLiveData<String>()

    val navigateToPersonCard: LiveData<String>
        get() = _navigateToPersonCard

    private suspend fun insertAll(people: List<PersonBirthday>) {
        withContext(Dispatchers.IO) {
            database.insertAll(people)
        }
    }

    fun onAdd(){
        uiScope.launch {
            _navigateToPeopleEdit.value = ""
        }
    }

    fun addPeople(people: List<PersonBirthday>){
        uiScope.launch {
            insertAll(people)
        }
    }

    fun onSortByNameAsc(name: String){
        people =  database.getAllPeopleByNameAsc("%"+name+"%")
    }

    fun onSortByNameDesc(name: String){
        people =  database.getAllPeopleByNameDesc("%"+name+"%")
    }

    fun onSortByDateAsc(name: String){
        people =  database.getAllPeopleByDateAsc("%"+name+"%")
    }

    fun onSortByDateDesc(name: String){
        people =  database.getAllPeopleByDateDesc("%"+name+"%")
    }

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
    fun onPersonCardClicked(id: String) {
        _navigateToPersonCard.value = id
    }

    fun onPersonCardNavigated() {
        _navigateToPersonCard.value = null
    }

    fun onPeopleEditNavigated() {
        _navigateToPeopleEdit.value = null
    }

}