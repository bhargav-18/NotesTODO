package com.example.notestodo.notes.ui.note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notestodo.R
import com.example.notestodo.databinding.ItemNotesBinding
import com.example.notestodo.notes.data.Notes

class NotesAdapter(
    private val listener: OnItemClickListener
) :
    ListAdapter<Notes, NotesAdapter.NotesViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = ItemNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class NotesViewHolder(private val binding: ItemNotesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = getItem(position)
                        listener.onItemClick(note)
                    }
                }
                menuIcon.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = getItem(position)
                        PopupMenu(it.context, it).apply {
                            setOnMenuItemClickListener { menuItem ->
                                if (menuItem.itemId == R.id.editNotePopup) {
                                    listener.onEditPopupClick(note)
                                    return@setOnMenuItemClickListener false
                                } else {
                                    listener.onDeletePopupClick(note)
                                    return@setOnMenuItemClickListener false
                                }
                            }
                            inflate(R.menu.popup_menu)
                            show()
                        }
                    }
                }
            }
        }

        fun bind(notes: Notes) {
            binding.apply {
                textViewTitle.text = notes.title
                textViewContent.text = notes.description
                textViewTime.text = "Created: ${notes.createdDateFormatted}"
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(notes: Notes)
        fun onEditPopupClick(notes: Notes)
        fun onDeletePopupClick(notes: Notes)
    }

    class DiffCallback : DiffUtil.ItemCallback<Notes>() {
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notes, newItem: Notes) =
            oldItem == newItem
    }
}