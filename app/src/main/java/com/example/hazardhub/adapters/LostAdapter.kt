package com.example.hazardhub.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hazardhub.R
import com.example.hazardhub.databinding.LostItemsBinding
import com.example.hazardhub.dataclass.LostPeople

class LostAdapter(val lostPeopleList : List<LostPeople>, val context : Context) : RecyclerView.Adapter<LostAdapter.VH>() {
    inner class VH(val binding : LostItemsBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LostItemsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return lostPeopleList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val currLost = lostPeopleList[position]
        holder.binding.lostPersonName.text = currLost.name
        holder.binding.lostPersonDescription.text = currLost.description
        holder.binding.lostPersonReward.text = currLost.reward
        Glide.with(context)
            .load(currLost.photo)
            .placeholder(R.drawable.loading)
            .into(holder.binding.lostPersonImage)

        holder.binding.lostPersonContact.text = currLost.contact
        holder.binding.lostPersonCallBtn.setOnClickListener {
            initiateCall(currLost.contact)
        }

    }
    private fun initiateCall(mobile: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$mobile")
        context.startActivity(intent)
    }
}