package com.example.birthday_notifyer.peopleshow

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonBirthday, clickListener: PersonBirthdayListener) {
            binding.person = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
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
