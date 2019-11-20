package com.example.birthday_notifyer.peopleshow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
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
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPeopleListBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class PeopleShowFragment: Fragment() {
    private var toolbar: Toolbar? = null

    private var viewModel: PeopleShowViewModel? = null
    private var tracker: SelectionTracker<String>? = null
    private var adapter: PeopleShowAdapter? = null
    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.i("PeopleShowFragment", "you''re here")
        tracker
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
        adapter = PeopleShowAdapter(PersonBirthdayListener { personId ->
            peopleShowViewModel.onPersonClicked(personId)
        })
        //this.adapter = adapter
        val recyclerView = binding.peopleList
        recyclerView.adapter = adapter
        tracker = SelectionTracker.Builder(
            "selected",
            recyclerView,
            PersonItemKeyProvider(adapter!!),
            PersonItemLookup(recyclerView),
            StorageStrategy.createStringStorage()
        ).build()

        adapter!!.setTracker(tracker!!)
        tracker!!.addObserver(object : SelectionTracker.SelectionObserver<Any>() {
            override fun onSelectionChanged() {
                toggleActionMode()
            }
        })
        binding.setLifecycleOwner(this)

        peopleShowViewModel.people.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter!!.submitList(it)
                adapter!!.notifyDataSetChanged()
            }
        })


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

    private fun toggleActionMode() {
        val selectionSize = tracker!!.selection.size()
        if (selectionSize > 0 && actionMode == null) {
            actionMode = (activity as AppCompatActivity).startSupportActionMode(callback)
            if (actionMode != null) {
                val edit = actionMode!!.menu.findItem(R.id.menu_edit)
                edit.setEnabled(selectionSize == 1)
            }
        } else if (actionMode != null) {
            if (selectionSize > 0) {
                val edit = actionMode!!.menu.findItem(R.id.menu_edit)
                edit.setEnabled(selectionSize == 1)
                actionMode!!.getMenu().findItem(R.id.menu_count).setTitle(
                    String.format(
                        Locale.US,
                        "%d/%d",
                        selectionSize,
                        adapter!!.itemCount
                    )
                )
            } else
                actionMode!!.finish()
        }
    }

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_context, menu)
            menu.findItem(R.id.menu_count).title =
                String.format(
                    Locale.US,
                    "%d/%d",
                    tracker?.getSelection()?.size(),
                    adapter?.getItemCount()
                )
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_select_all -> {
                }
                R.id.menu_edit -> {
                }
                R.id.menu_remove -> {
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            tracker?.clearSelection()
        }

    }
}

class PersonItemKeyProvider(private val adapter: PeopleShowAdapter) :
    ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String? {
        if (position < adapter.getAllPeople().size)
            return adapter.getPerson(position).personId
        return null
    }

    override fun getPosition(key: String): Int {
        var pos = RecyclerView.NO_POSITION
        for (i in 0 until adapter.getAllPeople().size) {
            if (adapter.getPerson(i).personId == key)
                pos = i
                break
        }
        return pos
    }
}

class PersonItemLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is PeopleShowAdapter.ViewHolder) {
                val name = viewHolder.getPersonDetails().selectionKey
                return viewHolder.getPersonDetails()
            }
        }
        return null
    }
}

class PersonItemDetail(private val position: Int, private val selectionKey: String) :
    ItemDetailsLookup.ItemDetails<String>() {

    override fun getPosition(): Int {
        return position
    }

    override fun getSelectionKey(): String? {
        return selectionKey
    }
}
