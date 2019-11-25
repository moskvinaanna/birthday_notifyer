package com.example.birthday_notifyer.peopleshow

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("nameString")
fun TextView.setPersonName(item: PersonBirthday?) {
    item?.let {
        text = item.name
    }
}


@BindingAdapter("dateString")
fun TextView.setBirthdayDate(item: PersonBirthday?) {
    item?.let {
        if (item.birthdayDate != null) {
            val date = Date(item.birthdayDate!!)
            val format = SimpleDateFormat("dd.MM.yyyy")
            text = format.format(date)
        }
        else
            text = ""
    }
}


@BindingAdapter("phoneString")
fun TextView.setPhoneNumber(item: PersonBirthday?) {
    item?.let {
        text = item.phoneNum
    }
}