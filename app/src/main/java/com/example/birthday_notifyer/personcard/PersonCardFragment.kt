package com.example.birthday_notifyer.personcard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPersonCardBinding
import com.example.birthday_notifyer.peopleedit.PeopleEditFragmentArgs
import com.example.birthday_notifyer.peopleedit.PeopleEditViewModel
import com.example.birthday_notifyer.peopleedit.PeopleEditViewModelFactory
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PersonCardFragment: Fragment() {
    private var toolbar: Toolbar? = null
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var photoUri: Uri? = null
    private var photo: SimpleDraweeView? = null
    private var dateTextView: TextView? = null
    private var cal = Calendar.getInstance()
    private var personCardViewModel: PeopleEditViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentPersonCardBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_person_card, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = PeopleEditFragmentArgs.fromBundle(arguments!!)

        val dataSource = BirthdayDatabase.getInstance(application).birthdayDatabaseDao
        val viewModelFactory = PeopleEditViewModelFactory(arguments.personKey, dataSource)

        personCardViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(PeopleEditViewModel::class.java)
        var person: PersonBirthday? = null
        toolbar = binding.toolbar2
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        photo = binding.photo
        uiScope.launch {
            person = personCardViewModel!!.getPersonFromDataBase()
            binding.nameLabel.setText(person!!.name)
            binding.phoneLabel.setText(person!!.phoneNum)
            if (person!!.birthdayDate != null) {
                val date = Date(person!!.birthdayDate!!)
                val format = SimpleDateFormat("dd.MM.yyyy")
                binding.dateLabel.setText(format.format(date))
            }
            else
                binding.dateLabel.setText(R.string.not_set)
            if (person!!.photo != ""){
                photoUri = Uri.fromFile(File(person!!.photo))
                photo!!.setImageURI(photoUri, null)
            }
        }

        if (person != null && person!!.photo != ""){
            photoUri = Uri.parse(person!!.photo)
            photo!!.setImageURI(photoUri, null)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (activity as AppCompatActivity).onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK && requestCode == 3) {
            if (data != null) {
                photoUri = data.data
                photo!!.setImageURI(photoUri, null)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}