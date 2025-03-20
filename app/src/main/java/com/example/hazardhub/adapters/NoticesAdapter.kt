package com.example.hazardhub.adapters

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hazardhub.databinding.NoticesItemBinding
import com.example.hazardhub.dataclass.Notices
import java.text.SimpleDateFormat
import java.util.Locale

class NoticesAdapter(val context : Context, val noticesList : List<Notices>) : RecyclerView.Adapter<NoticesAdapter.VH>() {

    val sortedNotification = noticesList.sortedByDescending { it.timestamp }

    inner class VH(val binding : NoticesItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = NoticesItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return noticesList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val currNotice = sortedNotification[position]
        val selectNotice = holder.binding
        selectNotice.noticeMessage.text = currNotice.message
        val date = currNotice.timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        selectNotice.publishTime.text = dateFormat.format(date)
    }
}