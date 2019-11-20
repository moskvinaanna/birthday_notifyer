package com.example.birthday_notifyer.peopleshow

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.ListItemPersonBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

class PeopleShowAdapter(val clickListener: PersonBirthdayListener) :
    ListAdapter<PersonBirthday, PeopleShowAdapter.ViewHolder>(PersonBirthdayDiffCallback()) {
    private var tracker: SelectionTracker<String>? = null
    init {
        setHasStableIds(true)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, getItem(position)!!, clickListener)
        holder.itemView.isActivated = tracker!!.isSelected(getItem(position).personId.toString())
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    fun getPerson(position: Int): PersonBirthday{
        return getItem(position)
    }

    fun getAllPeople(): List<PersonBirthday> {
        var allPeople: MutableList<PersonBirthday> = mutableListOf()
        for (i in 0 until this.itemCount) {
            allPeople.add(getItem(i))
        }
        return allPeople
    }

    fun setTracker(tracker: SelectionTracker<String>) {
        this.tracker = tracker
    }

    class ViewHolder private constructor(val binding: ListItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var personItemDetail: PersonItemDetail? = null

        fun bind(pos: Int, item: PersonBirthday, clickListener: PersonBirthdayListener) {
            binding.person = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
            personItemDetail = PersonItemDetail(pos, item.personId)
        }

        fun getPersonDetails(): PersonItemDetail{
            return personItemDetail!!
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemPersonBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class PersonBirthdayDiffCallback : DiffUtil.ItemCallback<PersonBirthday>() {

    override fun areItemsTheSame(oldItem: PersonBirthday, newItem: PersonBirthday): Boolean {
        return oldItem.personId == newItem.personId
    }

    override fun areContentsTheSame(oldItem: PersonBirthday, newItem: PersonBirthday): Boolean {
        return oldItem == newItem
    }
}


class PersonBirthdayListener(val clickListener: (personId: String) -> Unit) {
    fun onClick(person: PersonBirthday) = clickListener(person.personId!!)
}