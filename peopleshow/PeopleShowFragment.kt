package com.example.birthday_notifyer.peopleshow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPeopleListBinding
import com.google.android.material.snackbar.Snackbar

class PeopleShowFragment: Fragment() {
    private var toolbar: Toolbar? = null

    private var viewModel: PeopleShowViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val application = requireNotNull(this.activity).application

        // Create an instance of the ViewModel Factory.
        val dataSource = BirthdayDatabase.getInstance(application).birthdayDatabaseDao
        val viewModelFactory = PeopleShowViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val peopleShowViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(PeopleShowViewModel::class.java)
        viewModel = peopleShowViewModel
        val binding: FragmentPeopleListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_people_list, container, false)
        binding.peopleShowViewModel = peopleShowViewModel
        val manager = LinearLayoutManager(activity)
        binding.peopleList.layoutManager = manager
        toolbar = binding.toolbar
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        setHasOptionsMenu(true)
        val adapter = PeopleShowAdapter(PersonBirthdayListener { personId ->
            peopleShowViewModel.onPersonClicked(personId)
        })
        binding.peopleList.adapter = adapter

        peopleShowViewModel.people.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })
        binding.setLifecycleOwner(this)

        peopleShowViewModel.navigateToPeopleEdit.observe(this,
            Observer {person ->
                person?.let{
                    this.findNavController().navigate(PeopleShowFragmentDirections.
                        actionPeopleListFragmentToPeopleEditFragment(person))
                    peopleShowViewModel.onPeopleEditNavigated()
                }
            })
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
//        val item = menu.findItem(R.id.menu_search)
//        val sv = item.actionView as SearchView
//        sv.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(v: View) {
//
//            }
//            override fun onViewDetachedFromWindow(v: View) {
//                menu.findItem(R.id.menu_add).isVisible = true
//                menu.findItem(R.id.menu_sort).isVisible = true
//            }
//        }
//        )
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menu = toolbar!!.menu
        when (item.itemId) {
            R.id.menu_add -> viewModel?.onAdd()
            R.id.menu_search -> {
            }
            R.id.menu_sort -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }
}