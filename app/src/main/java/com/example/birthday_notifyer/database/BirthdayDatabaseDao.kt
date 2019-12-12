package com.example.birthday_notifyer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BirthdayDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(person: PersonBirthday)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(people: List<PersonBirthday>)
    @Update
    fun update(person: PersonBirthday)
    @Query("SELECT * from person_birthday_table WHERE personId = :key")
    fun get(key: String): PersonBirthday
    @Query("DELETE FROM person_birthday_table WHERE personId IN (:idList)")
    fun removePeople(idList: List<String>)
    @Query("DELETE FROM person_birthday_table")
    fun clear()
    @Query("SELECT * FROM person_birthday_table ORDER BY personId DESC")
    fun getAllPeople(): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name ORDER BY name ASC")
    fun getAllPeopleByNameAsc(name: String): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name ORDER BY name DESC")
    fun getAllPeopleByNameDesc(name: String): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name ORDER BY birthday_date ASC")
    fun getAllPeopleByDateAsc(name: String): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name ORDER BY birthday_date DESC")
    fun getAllPeopleByDateDesc(name: String): LiveData<List<PersonBirthday>>
    @Query("SELECT * FROM person_birthday_table WHERE name LIKE :name")
    fun searchPeople(name: String): LiveData<List<PersonBirthday>>
}
