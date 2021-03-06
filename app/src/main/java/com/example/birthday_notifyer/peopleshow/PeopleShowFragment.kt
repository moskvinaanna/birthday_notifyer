package com.example.birthday_notifyer.peopleshow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
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
import java.io.File
import java.util.*

class PeopleShowFragment: Fragment() {
    private var toolbar: Toolbar? = null
    private var viewModel: PeopleShowViewModel? = null
    private var tracker: SelectionTracker<String>? = null
    private var adapter: PeopleShowAdapter? = null
    private var actionMode: ActionMode? = null
    private var recyclerView: RecyclerView? = null
    private var isSortByName: Boolean = true
    private var isSortAsc: Boolean = true
    private var searchText: String = ""
    private var searchView: SearchView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val application = requireNotNull(this.activity).application
        val dataSource = BirthdayDatabase.getInstance(application).birthdayDatabaseDao
        val viewModelFactory = PeopleShowViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(
                this, viewModelFactory).get(PeopleShowViewModel::class.java)
        getSavedState(savedInstanceState)
        val binding: FragmentPeopleListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_people_list, container, false)
        binding.peopleShowViewModel = viewModel
        val manager = LinearLayoutManager(activity)
        binding.peopleList.layoutManager = manager
        setupMenu(binding)
        setupAdapter()
        setupTracker(binding)
        getDataFromViewModel()
        navigationObservers()
        return binding.root
    }

    private fun setupAdapter(){
        adapter = PeopleShowAdapter(PersonBirthdayListener { personId ->
            viewModel!!.onPersonCardClicked(personId)
        })
    }

    private fun setupMenu(binding: FragmentPeopleListBinding){
        toolbar = binding.toolbar
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        setHasOptionsMenu(true)
    }

    private fun setupTracker(binding: FragmentPeopleListBinding){
        recyclerView = binding.peopleList
        recyclerView!!.adapter = adapter
        tracker = SelectionTracker.Builder(
            "selected",
            recyclerView!!,
            PersonItemKeyProvider(adapter!!),
            PersonItemLookup(recyclerView!!),
            StorageStrategy.createStringStorage()
        ).build()
        recyclerView!!.setItemAnimator(null) //исправляет проблему с RecyclerView

        adapter!!.setTracker(tracker!!)
        tracker!!.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                toggleActionMode()
            }
        })
    }

    private fun getSavedState(savedInstanceState: Bundle?){
        if (savedInstanceState != null){
            isSortByName = savedInstanceState.getBoolean("isSortByName")
            isSortAsc = savedInstanceState.getBoolean("isSortAsc")
            searchText = savedInstanceState.getString("searchText")?:""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchText", searchText)
        outState.putBoolean("isSortByName", isSortByName)
        outState.putBoolean("isSortAsc", isSortAsc)
    }

    private fun navigationObservers(){
        viewModel!!.navigateToPeopleEdit.observe(viewLifecycleOwner,
            Observer {person ->
                person?.let{
                    this.findNavController().navigate(PeopleShowFragmentDirections.
                        actionPeopleListFragmentToPeopleEditFragment(person))
                    viewModel!!.onPeopleEditNavigated()
                }
            })
        viewModel!!.navigateToPersonCard.observe(viewLifecycleOwner,
            Observer {person ->
                person?.let{
                    this.findNavController().navigate(PeopleShowFragmentDirections.
                        actionPeopleListFragmentToPersonCardFragment(person))
                    viewModel!!.onPersonCardNavigated()
                }
            })
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
            var person = PersonBirthday(personId = UUID.randomUUID().toString(),
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

    private fun popUpClickAdd(menuItem: MenuItem): Boolean{
        when (menuItem.itemId){
            R.id.add_manually -> {
                viewModel!!.onAdd()
            }
            R.id.add_from_contacts -> {
                addFromContacts()
            }
        }
        return true
    }

    private fun popUpClick(menuItem: MenuItem): Boolean{
        when (menuItem.itemId){
            R.id.sort_by_date_asc -> {
                sort(true, true)
            }
            R.id.sort_by_date_desc -> {
                sort(true, false)
            }
            R.id.sort_by_name_asc -> {
                sort(false, true)
            }
            R.id.sort_by_name_desc -> {
                sort(false, false)
            }
        }
        return true
    }

    private fun sort(byName: Boolean, asc: Boolean){
        isSortAsc = asc
        isSortByName = byName
        getDataFromViewModel()
    }

    fun getDataFromViewModel(){
        if (isSortByName && isSortAsc)
            viewModel!!.onSortByNameAsc(searchText)
        if (isSortByName && !isSortAsc)
            viewModel!!.onSortByNameDesc(searchText)
        if (!isSortByName && isSortAsc)
            viewModel!!.onSortByDateAsc(searchText)
        if (!isSortByName && !isSortAsc)
            viewModel!!.onSortByDateDesc(searchText)
        recyclerView!!.smoothScrollToPosition(0)
        viewModel!!.people.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter!!.setItemsWithDiff(it)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.menu_search)
        searchView = item.actionView as SearchView
        if (searchText.isNotEmpty()) {
            item.expandActionView()
            searchView!!.setQuery(searchText, true)
        }
        searchViewAttachListener(menu)
        queryTextChangeListener()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun queryTextChangeListener(){
        searchView!!.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (view != null) {
                    searchText = newText
                    getDataFromViewModel()
                }
                return true
            }
        })
    }

    private fun searchViewAttachListener(menu: Menu){
        searchView!!.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                menu.findItem(R.id.menu_add).isVisible = true
                menu.findItem(R.id.menu_sort).isVisible = true
                getDataFromViewModel()
                val inputMethodManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchView!!.windowToken, 0)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menu = toolbar!!.menu
        when (item.itemId) {
            R.id.menu_add -> menuAdd(item, menu)
            R.id.menu_search -> {
                menu.findItem(R.id.menu_add).isVisible = false
                menu.findItem(R.id.menu_sort).isVisible = false
            }
            R.id.menu_sort -> menuSort(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun menuSort(item: MenuItem){
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

    private fun menuAdd(item: MenuItem, menu: Menu){
        if (activity is AppCompatActivity){
            val popupMenu = PopupMenu((activity as AppCompatActivity),
                (activity as AppCompatActivity).findViewById(item.getItemId()))
            popupMenu.inflate(R.menu.popup_menu_add)
            popupMenu.setOnMenuItemClickListener{ it
                popUpClickAdd(it)
            }
            popupMenu.show()
        }
    }

    private fun addFromContacts(){
        if(ContextCompat.checkSelfPermission(activity as AppCompatActivity,
                Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                1)
            return
        }
        val list: List<PersonBirthday> = getContactList()
        viewModel!!.addPeople(list)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val list: List<PersonBirthday> = getContactList()
                    viewModel!!.addPeople(list)
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
                startActionMode(selectionSize)
            }
        } else if (actionMode != null) {
            if (selectionSize > 0) {
                continueSelection(selectionSize)
            } else
                actionMode!!.finish()
        }
    }

    private fun continueSelection(selectionSize: Int){
        val edit = actionMode!!.menu.findItem(R.id.menu_edit)
        edit.setEnabled(selectionSize == 1)
        if (selectionSize == 1)
            edit.icon.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        else
            edit.icon.colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        actionMode!!.getMenu().findItem(R.id.menu_count).setTitle(
            String.format(
                Locale.US,
                "%d/%d",
                selectionSize,
                adapter!!.itemCount
            )
        )
    }

    private fun startActionMode(selectionSize: Int){
        val edit = actionMode!!.menu.findItem(R.id.menu_edit)
        edit.setEnabled(selectionSize == 1)
        if (selectionSize == 1)
            edit.icon.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        else
            edit.icon.colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
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
                R.id.menu_select_all -> selectAll(mode)
                R.id.menu_edit -> {
                    val id = tracker!!.getSelection().iterator().next()
                    viewModel!!.onPersonClicked(id)
                }
                R.id.menu_remove -> remove(mode)
            }
            return true
        }

        private fun remove(mode: ActionMode){
            val ids = ArrayList<String>()
            for (person in adapter!!.getAllPeople()) {
                if (tracker!!.selection.contains(person.personId)) {
                    ids.add(person.personId)
                    if (person.photo != "") {
                        val photo = File(person.photo)
                        photo.delete()
                    }
                }
            }

            mode.finish()
            viewModel!!.onRemove(ids)
        }

        private fun selectAll(mode: ActionMode){
            if (tracker!!.selection.size() != adapter!!.itemCount){
                val ids = ArrayList<String>()
                for (person in adapter!!.getAllPeople())
                    ids.add(person.personId)
                tracker!!.setItemsSelected(ids, true)
            }
            else  {
                mode.finish()
            }
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
        for (i in adapter.getAllPeople().indices) {
            if (adapter.getPerson(i).personId == key)
                return  i
        }
        return RecyclerView.NO_POSITION
    }

}

class PersonItemLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
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

class PersonItemDetail(private val position: Int, private val selectionKey: String) :
    ItemDetailsLookup.ItemDetails<String>() {

    override fun getPosition(): Int {
        return position
    }

    override fun getSelectionKey(): String? {
        return selectionKey
    }
}
