package com.example.birthday_notifyer.peopleedit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPersonEditBinding
import kotlinx.coroutines.*

class PeopleEditFragment: Fragment() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentPersonEditBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_person_edit, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = PeopleEditFragmentArgs.fromBundle(arguments!!)

        val dataSource = BirthdayDatabase.getInstance(application).birthdayDatabaseDao
        val viewModelFactory = PeopleEditViewModelFactory(arguments.personKey, dataSource)

        val peopleEditViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(PeopleEditViewModel::class.java)
        if (arguments.personKey != -1L){
               uiScope.launch {
                   var person: PersonBirthday = peopleEditViewModel.getPersonFromDataBase()
                   binding.nameEdit.setText(person.name)
                   binding.phoneEdit.setText(person.phoneNum)
                   binding.dateEdit.setText(person.birthdayDate.toString())
               }

        }

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.peopleEditViewModel = peopleEditViewModel

        binding.saveButton.setOnClickListener{
            peopleEditViewModel.onSave(binding.nameEdit.text.toString(),
                binding.phoneEdit.text.toString(), System.currentTimeMillis()
                )

            val inputMethodManager = this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.phoneEdit.windowToken, 0)
        }

        // Add an Observer to the state variable for Navigating when a Quality icon is tapped.
        peopleEditViewModel.navigateToPeopleShow.observe(this, Observer {
            if (it == true) {
                this.findNavController().navigate(
                    PeopleEditFragmentDirections.actionPeopleEditFragmentToPeopleListFragment())
                peopleEditViewModel.doneNavigating()
            }
        })

        return binding.root
    }
}