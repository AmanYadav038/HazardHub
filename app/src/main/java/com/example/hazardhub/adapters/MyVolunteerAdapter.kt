package com.example.hazardhub.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hazardhub.dataclass.Volunteers
import android.widget.Button
import com.example.hazardhub.R

class MyVolunteerAdapter(
    private val context: Context,
    private val volunteerList: List<Volunteers>
) : RecyclerView.Adapter<MyVolunteerAdapter.VolunteerViewHolder>() {

    // ViewHolder for Volunteer items
    class VolunteerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name_volunteer)
        val ageTextView: TextView = itemView.findViewById(R.id.volunteer_bloodGroup)
        val locationTextView: TextView = itemView.findViewById(R.id.email)
        val callButton: Button = itemView.findViewById(R.id.call_volunteer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolunteerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.volunteer_items, parent, false)
        return VolunteerViewHolder(view)
    }
    override fun getItemCount(): Int {
        return volunteerList.size
    }

    override fun onBindViewHolder(holder: VolunteerViewHolder, position: Int) {
        val volunteer = volunteerList[position]

        // Bind the volunteer data to the views
        holder.nameTextView.text = volunteer.name
        holder.ageTextView.text = volunteer.bloodGroup // Add a function to calculate age
        holder.locationTextView.text = volunteer.email

        // Set a click listener on the call button
        holder.callButton.setOnClickListener {
            initiateCall(volunteer.mobile)
        }
    }

    private fun initiateCall(mobile: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$mobile")
        context.startActivity(intent)
    }


}
