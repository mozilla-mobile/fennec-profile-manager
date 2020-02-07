/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fpm.R
import org.mozilla.fpm.data.BackupRepositoryImpl.MIME_TYPE
import org.mozilla.fpm.models.Backup

class BackupsRVAdapter : RecyclerView.Adapter<BackupsRVAdapter.BackupViewHolder>() {

    private var dataSource: MutableList<Backup> = ArrayList()
    private lateinit var listener: MenuListener

    fun updateData(data: List<Backup>) {
        dataSource = data.toMutableList()
        this.notifyDataSetChanged()
    }

    fun add(backup: Backup) {
        dataSource.add(backup)
        this.notifyItemInserted(dataSource.size - 1)
    }

    fun delete(position: Int) {
        dataSource.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataSource.size)
    }

    fun update(backup: Backup, position: Int) {
        dataSource[position] = backup
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): BackupViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.backup_row, viewGroup, false)
        return BackupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull backupViewHolder: BackupViewHolder, i: Int) {
        backupViewHolder.bind(i)
    }

    inner class BackupViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val variant: TextView = itemView.findViewById(R.id.variant)
        private val size: TextView = itemView.findViewById(R.id.size)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val apply: ConstraintLayout = itemView.findViewById(R.id.apply_container)
        private val edit: ConstraintLayout = itemView.findViewById(R.id.edit_container)
        private val delete: ConstraintLayout = itemView.findViewById(R.id.delete_container)

        fun bind(position: Int) {
            val backup: Backup = dataSource[position]
            title.text = backup.name.replace(".$MIME_TYPE", "")
            variant.text = backup.variant
            date.text = backup.createdAt
            size.text = Formatter.formatShortFileSize(itemView.context, backup.size)
            apply.setOnClickListener { listener.onApplyClick(backup) }
            edit.setOnClickListener { listener.onEditClick(backup, position) }
            delete.setOnClickListener { listener.onDeleteClick(backup, position) }
        }
    }

    fun setListener(listener: MenuListener) {
        this.listener = listener
    }

    interface MenuListener {
        fun onApplyClick(item: Backup)
        fun onEditClick(item: Backup, position: Int)
        fun onDeleteClick(item: Backup, position: Int)
    }
}
