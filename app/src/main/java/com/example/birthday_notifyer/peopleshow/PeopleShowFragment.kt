package com.example.birthday_notifyer.peopleshow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birthday_notifyer.R
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.FragmentPeopleListBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class PeopleShowFragment: Fragment() {
    private var toolbar: Toolbar? = null
    private var viewModel: PeopleShowViewModel? = null
    private var tracker: SelectionTracker<Long>? = null
    private var adapter: PeopleShowAdapter? = null
    private var actionMode: ActionMode? = null
    private var popupMenu: PopupMenu? = null


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
            StorageStrategy.createLongStorage()
        ).build()

        adapter!!.setTracker(tracker!!)
        tracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                toggleActionMode()
            }
        })
        //binding.setLifecycleOwner(this)

        getDataFromViewModel()

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

    private fun getContactList(): List<PersonBirthday>{
        val cur = context!!.contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
        var peopleList: MutableList<PersonBirthday> = mutableListOf()
        while (cur != null && cur.moveToNext()) {
            var person = PersonBirthday(
                name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                phoneNum = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                birthdayDate = null)
            peopleList.add(person)
        }
        cur!!.close()
        return peopleList
    }

    override fun onPause() {
        if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
        super.onPause()
    }

    private fun popUpClick(menuItem: MenuItem): Boolean{
        when (menuItem.itemId){
            R.id.sort_by_date_asc -> {
                viewModel!!.onSortByDateAsc()
                getDataFromViewModel()
            }
            R.id.sort_by_date_desc -> {
                viewModel!!.onSortByDateDesc()
                getDataFromViewModel()
            }
            R.id.sort_by_name_asc -> {
                viewModel!!.onSortByNameAsc()
                getDataFromViewModel()
            }
            R.id.sort_by_name_desc -> {
                viewModel!!.onSortByNameDesc()
                getDataFromViewModel()
            }
        }
        return true
    }

    fun getDataFromViewModel(){
        viewModel!!.people.observe(this, Observer {
            it?.let {
                //adapter!!.submitList(it)
                adapter!!.setItemsWithDiff(it)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.menu_search)
        val sv = item.actionView as SearchView
        sv.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                menu.findItem(R.id.menu_add).isVisible = true
                menu.findItem(R.id.menu_sort).isVisible = true
                viewModel!!.onSortByNameAsc()
                getDataFromViewModel()
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(sv.windowToken, 0)
            }
        })
        sv.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (view != null) {
                    viewModel!!.onSearchPeople(newText)
                    getDataFromViewModel()
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menu = toolbar!!.menu
        when (item.itemId) {
            R.id.menu_add -> {
                if(ContextCompat.checkSelfPermission(activity as AppCompatActivity,
                        Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        1)
                    //Snackbar.make(, "Нет доступа", Snackbar.LENGTH_SHORT).show()
                    return true
                }
                val list: List<PersonBirthday> = getContactList()
                viewModel!!.addPeople(list)
            }
               // viewModel?.onAdd() }
            R.id.menu_search -> {
                menu.findItem(R.id.menu_add).isVisible = false
                menu.findItem(R.id.menu_sort).isVisible = false
            }
            R.id.menu_sort ->{
                if (activity is AppCompatActivity){
                    val popupMenu = PopupMenu((activity as AppCompatActivity),
                        (activity as AppCompatActivity).findViewById(item.getItemId()))
                    popupMenu.inflate(R.menu.popup_menu)
                    popupMenu.setOnMenuItemClickListener{ it
                        popUpClick(it)
                    }
                    popupMenu.show()
                }


            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val list: List<PersonBirthday> = getContactList()
                    viewModel!!.addPeople(list)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
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
                    if (tracker!!.selection.size() != adapter!!.itemCount){
                        val ids = ArrayList<Long>()
                        for (person in adapter!!.getAllPeople())
                            ids.add(person.personId)
                        tracker!!.setItemsSelected(ids, true)
                    }
                    else  {
                        actionMode!!.finish()
                        actionMode = null
                    }
                }
                R.id.menu_edit -> {
                    val id = tracker!!.getSelection().iterator().next()
                    viewModel!!.onPersonClicked(id)
                }
                R.id.menu_remove -> {
                    val ids = ArrayList<Long>()
                    for (person in adapter!!.getAllPeople()) {
                        if (tracker!!.selection.contains(person.personId)) {
                            ids.add(person.personId)
                            if (person.photo != "") {
                                val photo = File(person.photo)
                                photo.delete()
                            }
                        }
                    }
                    viewModel!!.onRemove(ids)
                    actionMode!!.finish()
                    actionMode = null
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
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        if (position < adapter.getAllPeople().size)
            return adapter.getPerson(position).personId
        return null
    }

    override fun getPosition(key: Long): Int {
        for (i in adapter.getAllPeople().indices) {
            if (adapter.getPerson(i).personId == key)
                return  i
        }
        return RecyclerView.NO_POSITION
    }

}

class PersonItemLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is PeopleShowAdapter.PersonViewHolder) {
                return viewHolder.getPersonDetails()
            }
        }
        return null
    }
}

class PersonItemDetail(private val position: Int, private val selectionKey: Long) :
    ItemDetailsLookup.ItemDetails<Long>() {

    override fun getPosition(): Int {
        return position
    }

    override fun getSelectionKey(): Long? {
        return selectionKey
    }
}
