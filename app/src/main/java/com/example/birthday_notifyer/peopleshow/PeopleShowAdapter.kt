package com.example.birthday_notifyer.peopleshow

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.ListItemPersonBinding
import com.facebook.drawee.view.SimpleDraweeView
import java.io.File

class PeopleShowAdapter(val clickListener: PersonBirthdayListener) :
    ListAdapter<PersonBirthday, PeopleShowAdapter.ViewHolder>(PersonBirthdayDiffCallback()) {
    private var tracker: SelectionTracker<Long>? = null
    init {
        //setHasStableIds(true)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, getItem(position)!!, clickListener)
        holder.itemView.isActivated = tracker!!.isSelected(getItem(position).personId)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).personId
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

    fun setTracker(tracker: SelectionTracker<Long>) {
        this.tracker = tracker
    }

    class ViewHolder private constructor(val binding: ListItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var personItemDetail: PersonItemDetail? = null

        fun bind(pos: Int, item: PersonBirthday, clickListener: PersonBirthdayListener) {
            val photoView: SimpleDraweeView = binding.photo
            binding.person = item
            if (item.photo != "")
                photoView.setImageURI(Uri.fromFile(File(item.photo)), null)
            else
                photoView.setImageURI(Uri.EMPTY, null)
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


class PersonBirthdayListener(val clickListener: (personId: Long) -> Unit) {
    fun onClick(person: PersonBirthday) = clickListener(person.personId)
}