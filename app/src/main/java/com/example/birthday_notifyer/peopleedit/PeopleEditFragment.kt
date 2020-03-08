package com.example.birthday_notifyer.peopleedit

import android.app.Activity
import com.google.android.material.datepicker.MaterialDatePicker
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPersonEditBinding
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class PeopleEditFragment: Fragment() {
    private var toolbar: Toolbar? = null
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var photoUri: Uri? = null
    private var photo: SimpleDraweeView? = null
    private var dateTextView: TextView? = null
    private var cal = Calendar.getInstance()
    private var peopleEditViewModel: PeopleEditViewModel? = null
    private var binding: FragmentPersonEditBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_person_edit, container, false)
        dateTextView = binding!!.dateEdit
        val application = requireNotNull(this.activity).application
        val arguments = PeopleEditFragmentArgs.fromBundle(arguments!!)
        val dataSource = BirthdayDatabase.getInstance(application).birthdayDatabaseDao
        val viewModelFactory = PeopleEditViewModelFactory(arguments.personKey, dataSource)
        peopleEditViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(PeopleEditViewModel::class.java)
        var person: PersonBirthday? = null
        toolbar = binding!!.toolbar2
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        photo = binding!!.photoView
        if (arguments.personKey != ""){
               uiScope.launch {
                   person = peopleEditViewModel!!.getPersonFromDataBase()
                   if (savedInstanceState == null)
                       setFields(person!!, binding!!)
               }
        }

        if (person != null && person!!.photo != ""){
            photoUri = Uri.parse(person!!.photo)
            photo!!.setImageURI(photoUri, null)
        }

        setClickListeners(binding!!, person)

        binding!!.peopleEditViewModel = peopleEditViewModel

        peopleEditViewModel!!.navigateToPeopleShow.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(
                    PeopleEditFragmentDirections.actionPeopleEditFragmentToPeopleListFragment())
                peopleEditViewModel!!.doneNavigating()
            }
        })
        if (savedInstanceState != null) {
            getSavedState(savedInstanceState)
        }
        setHasOptionsMenu(true)
        return binding!!.root
    }

    private fun getSavedState(savedInstanceState: Bundle) {
        binding!!.nameEdit.setText(savedInstanceState.getString("name"))
        binding!!.phoneEdit.setText(savedInstanceState.getString("phone"))
        cal.timeInMillis = savedInstanceState.getLong("cal")
        val format = SimpleDateFormat("dd.MM.yyyy")
        binding!!.dateEdit.setText(format.format(cal.timeInMillis))
        val path = savedInstanceState.getString("photo", null)
        if (path != null) {
            photoUri = Uri.parse(path)
            photo!!.setImageURI(photoUri, null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("name", binding!!.nameEdit!!.text.toString())
        outState.putString("phone", binding!!.phoneEdit!!.text.toString())
        outState.putLong("cal", cal.timeInMillis)
        if (photoUri != null) {
            outState.putString("photo", photoUri.toString())
        }
        super.onSaveInstanceState(outState)
    }

    private fun setClickListeners(binding: FragmentPersonEditBinding, person: PersonBirthday?){
        binding.photoView.setOnClickListener{ v: View? ->
            setPhoto()
        }

        binding.saveButton.setOnClickListener{
            onSave(binding, person)
        }

        binding.dateEdit.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(cal.timeInMillis)
                .build()
            datePicker.addOnPositiveButtonClickListener {
                cal.timeInMillis = it
                updateDateInView()
            }
            datePicker.show(activity!!.supportFragmentManager, "datePicker")
        }
    }

    private fun onSave(binding: FragmentPersonEditBinding, person: PersonBirthday?){
        if (binding.nameEdit.text!!.isEmpty() || binding.phoneEdit.text!!.isEmpty() || binding.dateEdit.text!!.isEmpty() || Date(cal.timeInMillis).after(Date())) {
            setErrorMessages(binding)
        }
        else {
            var filePath = setNewPhoto(person)
            if (dateTextView!!.text.toString() == "") {
                peopleEditViewModel!!.onSave(
                    UUID.randomUUID().toString(), binding.nameEdit.text.toString(),
                    binding.phoneEdit.text.toString(), null, filePath

                )
            } else {
                peopleEditViewModel!!.onSave(
                    UUID.randomUUID().toString(), binding.nameEdit.text.toString(),
                    binding.phoneEdit.text.toString(), cal.timeInMillis, filePath

                )

            }
            val inputMethodManager =
                this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.phoneEdit.windowToken, 0)
        }
    }

    private fun setErrorMessages(binding: FragmentPersonEditBinding){
        if (binding.nameEdit.text!!.isEmpty())
            binding.nameEdit.error = getString(R.string.name_error)
        if (binding.phoneEdit.text!!.isEmpty())
            binding.phoneEdit.error = getString(R.string.phone_error)
        if (binding.dateEdit.text!!.isEmpty())
            binding.dateEdit.error = getString(R.string.date_error)
        else
            if (Date(cal.timeInMillis).after(Date())) {
                binding.dateEdit.error = getString(R.string.future_date_error)
                Snackbar.make(activity!!.findViewById(android.R.id.content), R.string.future_date_error, BaseTransientBottomBar.LENGTH_LONG).show()
            }
    }

    private fun setNewPhoto(person: PersonBirthday?): String{
        var filePath = ""
        var personId: String = UUID.randomUUID().toString()
        if (person != null)
            personId = person.personId
        if (photoUri != null) {
            filePath = getExternalFilesDirs(
                (activity as AppCompatActivity),
                null
            ).get(0).getAbsolutePath() + "/" + personId
            try {
                if (photoUri!!.lastPathSegment != personId.toString()) {
                    val outFile = File(filePath)
                    val inputStream: InputStream? =
                        (activity as AppCompatActivity).getContentResolver()
                            .openInputStream(photoUri!!)
                    val os: OutputStream = FileOutputStream(outFile)
                    val buffer = ByteArray(4096)
                    while (inputStream!!.read(buffer) != -1) os.write(buffer)
                    inputStream!!.close()
                    os.close()
                    val imagePipeline = Fresco.getImagePipeline()
                    imagePipeline.evictFromCache(Uri.fromFile(outFile))
                }
            } catch (e: Exception) {
            }
        } else if (person != null) {
            val p: String =
                getExternalFilesDirs(
                    (activity as AppCompatActivity),
                    null
                ).get(0).getAbsolutePath() + "/" + person!!.personId.toString()
            val file = File(p)
            if (file.exists()) file.delete()
        }
        return filePath
    }

    private fun setPhoto(){
        if (photoUri != null) {
            AlertDialog.Builder(activity as AppCompatActivity)
                .setTitle(R.string.select_an_action)
                .setPositiveButton(
                    R.string.change
                ) { _: DialogInterface?, _: Int -> selectFile() }
                .setNegativeButton(
                    R.string.remove
                ) { _: DialogInterface?, _: Int ->
                    val imagePipeline = Fresco.getImagePipeline()
                    imagePipeline.evictFromCache(photoUri)
                    photo!!.setImageURI(Uri.EMPTY, null)
                    photoUri = null
                }
                .show()
        } else selectFile()
    }

    private fun setFields(person: PersonBirthday, binding: FragmentPersonEditBinding){
        binding.nameEdit.setText(person.name)
        binding.phoneEdit.setText(person.phoneNum)
        if (person.birthdayDate != null) {
            val date = Date(person.birthdayDate!!)
            val format = SimpleDateFormat("dd.MM.yyyy")
            binding.dateEdit.setText(format.format(date))
        }
        if (person.photo != ""){
            photoUri = Uri.fromFile(File(person.photo))
            photo!!.setImageURI(photoUri, null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                peopleEditViewModel!!.onCancel()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dateTextView!!.text = sdf.format(cal.timeInMillis)
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

    private fun selectFile(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_a_picture)),
            3
        )
    }
}