package com.example.birthday_notifyer.peopleshow

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.birthday_notifyer.database.PersonBirthday
import com.example.birthday_notifyer.databinding.ListItemPersonBinding
import com.facebook.drawee.view.SimpleDraweeView
import java.io.File
import java.util.*

class PeopleShowAdapter(val clickListener: PersonBirthdayListener) : RecyclerView.Adapter<PeopleShowAdapter.PersonViewHolder>() {
    private var tracker: SelectionTracker<String>? = null
    var items: MutableList<PersonBirthday> = mutableListOf()
    init {
        setHasStableIds(true)
    }
    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.itemView.isActivated = tracker!!.isSelected(items[position].personId)
        holder.bind(position, items[position], clickListener)

    }

    override fun getItemId(position: Int): Long {
        return items[position].personId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
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

    fun setTracker(tracker: SelectionTracker<String>) {
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
            if (binding.cardView.isActivated)
                binding.cardView.setBackgroundColor(Color.LTGRAY)
            else
                binding.cardView.setBackgroundColor(Color.WHITE)
            binding.imageButton.visibility = View.GONE
            binding.imageButton.setOnClickListener{
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + item.phoneNum)
                it.context.startActivity(intent)
            }
            if (item.birthdayDate != null) {
                val curDate: Calendar = Calendar.getInstance()
                val personBirthdayDate: Calendar = Calendar.getInstance()
                val day = curDate[Calendar.DAY_OF_MONTH]
                val month = curDate[Calendar.MONTH]
                personBirthdayDate.timeInMillis = item.birthdayDate!!
                if (day == personBirthdayDate.get(Calendar.DAY_OF_MONTH)
                    && month == personBirthdayDate.get(Calendar.MONTH)){
                    binding.imageButton.visibility = View.VISIBLE
                }
            }
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


class PersonBirthdayListener(val clickListener: (personId: String) -> Unit) {
    fun onClick(person: PersonBirthday) = clickListener(person.personId)
}