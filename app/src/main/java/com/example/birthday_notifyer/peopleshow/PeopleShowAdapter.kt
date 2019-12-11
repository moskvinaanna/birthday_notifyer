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

class PeopleShowAdapter(val clickListener: PersonBirthdayListener) : RecyclerView.Adapter<PeopleShowAdapter.PersonViewHolder>() {
    private var tracker: SelectionTracker<Long>? = null
    var items: MutableList<PersonBirthday> = mutableListOf()
    init {
        setHasStableIds(true)
    }
    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.itemView.isActivated = tracker!!.isSelected(items[position].personId)
        holder.bind(position, items[position], clickListener)

    }

    override fun getItemId(position: Int): Long {
        return (position).toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        return PersonViewHolder.from(parent)
    }

    fun getPerson(position: Int): PersonBirthday{
        return items[position]
    }

    fun getAllPeople(): List<PersonBirthday> {
        return items
    }

    fun setTracker(tracker: SelectionTracker<Long>) {
        this.tracker = tracker
    }
    fun setItemsWithDiff(items: List<PersonBirthday>) {
        val diffCallback: DiffUtil.Callback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = this@PeopleShowAdapter.items.size

            override fun getNewListSize(): Int = items.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                this@PeopleShowAdapter.items[oldItemPosition].personId == items[newItemPosition].personId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                this@PeopleShowAdapter.items[oldItemPosition] == items[newItemPosition]
        }
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    class PersonViewHolder private constructor(val binding: ListItemPersonBinding) :
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
            fun from(parent: ViewGroup): PersonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemPersonBinding.inflate(layoutInflater, parent, false)
                return PersonViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }
}


//class PersonBirthdayDiffCallback : DiffUtil.ItemCallback<PersonBirthday>() {
//
//    override fun areItemsTheSame(oldItem: PersonBirthday, newItem: PersonBirthday): Boolean {
//        return oldItem.personId == newItem.personId
//    }
//
//    override fun areContentsTheSame(oldItem: PersonBirthday, newItem: PersonBirthday): Boolean {
//        return oldItem == newItem
//    }
//}


class PersonBirthdayListener(val clickListener: (personId: Long) -> Unit) {
    fun onClick(person: PersonBirthday) = clickListener(person.personId)
}