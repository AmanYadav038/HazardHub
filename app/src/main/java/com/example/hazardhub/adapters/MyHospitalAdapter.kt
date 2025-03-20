package com.example.hazardhub.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hazardhub.R
import com.example.hazardhub.dataclass.Hospitals
import android.widget.Button
import com.example.hazardhub.databinding.HospitalAvailableBinding

class MyHospitalAdapter(
    private val context: Context,
private val hospitalList: List<Hospitals>
) : RecyclerView.Adapter<MyHospitalAdapter.HospitalViewHolder>() {

    // ViewHolder for Hospital items
    class HospitalViewHolder(val binding: HospitalAvailableBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = HospitalAvailableBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HospitalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitalList[position]

        // Bind the hospital data to the views
        holder.binding.hospitalName.text = hospital.name
        val address = hospital.addressLine1 + hospital.addressLine2 +","+ hospital.pincode
        holder.binding.address.text = address
        // Set a click listener on the call button
        holder.binding.callBtn.setOnClickListener {
            initiateCall(hospital.contactNumber)
        }
        holder.binding.numberOfBedsIn.text = "Number of Beds : ${hospital.numberOfBeds}"
        val timing = hospital.startTime + "to" + hospital.endTime
        holder.binding.operatingHours.text = timing
        if(hospital.emergencyServices){
            holder.binding.emergencyServices.text = "Emergency Services Available"
        }else{
            holder.binding.emergencyServices.text = "Emergency Services Unavailable"
        }
    }

    private fun initiateCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        context.startActivity(intent)
    }
}