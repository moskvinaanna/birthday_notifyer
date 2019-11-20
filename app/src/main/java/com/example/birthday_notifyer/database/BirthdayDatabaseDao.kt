package com.example.birthday_notifyer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BirthdayDatabaseDao {

    @Insert
    fun insert(person: PersonBirthday)
    @Update
    fun update(person: PersonBirthday)
    @Query("SELECT * from person_birthday_table WHERE personId = :key")
    fun get(key: String): PersonBirthday
    @Query("DELETE FROM person_birthday_table")
    fun clear()
    @Query("SELECT * FROM person_birthday_table ORDER BY personId DESC")
    fun getAllPeople(): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name")
    fun searchPeople(name: String): LiveData<List<PersonBirthday>>
}
