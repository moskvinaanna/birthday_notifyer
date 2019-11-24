package com.example.birthday_notifyer.peopleedit

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class PeopleEditFragment: Fragment() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var photoUri: Uri? = null
    private var photo: SimpleDraweeView? = null

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
        var person: PersonBirthday? = null
        photo = binding.photoView
        if (arguments.personKey != ""){
               uiScope.launch {
                   person = peopleEditViewModel.getPersonFromDataBase()
                   binding.nameEdit.setText(person!!.name)
                   binding.phoneEdit.setText(person!!.phoneNum)
                   binding.dateEdit.setText(person!!.birthdayDate.toString())
                   if (person!!.photo != ""){
                       photoUri = Uri.fromFile(File(person!!.photo))
                       photo!!.setImageURI(photoUri, null)
                   }
               }

        }

        if (person != null && person!!.photo != ""){
            photoUri = Uri.parse(person!!.photo)
            photo!!.setImageURI(photoUri, null)
        }

        binding.photoView.setOnClickListener{ v: View? ->
            if (photoUri != null) {
                AlertDialog.Builder(activity as AppCompatActivity)
                    .setTitle("Выберите действие")
                    .setPositiveButton(
                        "Изменить"
                    ) { dialog: DialogInterface?, which: Int -> selectFile() }
                    .setNegativeButton(
                        "Удалить"
                    ) { dialog: DialogInterface?, which: Int ->
                        val imagePipeline = Fresco.getImagePipeline()
                        imagePipeline.evictFromCache(photoUri)
                        photo!!.setImageURI(Uri.EMPTY, null)
                        photoUri = null
                    }
                    .show()
            } else selectFile()
        }



        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.peopleEditViewModel = peopleEditViewModel

        binding.saveButton.setOnClickListener{
            var filePath: String = ""
            var personId: String = UUID.randomUUID().toString()
            if (person != null)
                personId = person!!.personId
            if (photoUri != null) {
                filePath = getExternalFilesDirs((activity as AppCompatActivity), null).get(0).getAbsolutePath() + "/" + personId
                try {
                    if (photoUri!!.lastPathSegment != personId) {
                        val outFile = File(filePath)
                        val inputStream: InputStream? =  (activity as AppCompatActivity).getContentResolver().openInputStream(photoUri!!)
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
                    getExternalFilesDirs((activity as AppCompatActivity), null).get(0).getAbsolutePath() + "/" + person!!.personId
                val file = File(p)
                if (file.exists()) file.delete()
            }
            peopleEditViewModel.onSave(personId, binding.nameEdit.text.toString(),
                binding.phoneEdit.text.toString(), System.currentTimeMillis(), filePath

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
            Intent.createChooser(intent, "Выберите изображение"),
            3
        )
    }
}