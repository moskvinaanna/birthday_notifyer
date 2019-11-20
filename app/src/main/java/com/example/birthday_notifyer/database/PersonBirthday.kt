package com.example.birthday_notifyer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_birthday_table"

)
data class PersonBirthday(
    @PrimaryKey(autoGenerate = true)
    var personId: Long? = null,

    @ColumnInfo(name = "birthday_date")
    var birthdayDate: Long,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "phone_num")
    var phoneNum: String
)