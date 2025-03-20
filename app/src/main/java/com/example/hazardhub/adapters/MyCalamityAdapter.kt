package com.example.hazardhub.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hazardhub.DetailedCalamityActivity
import com.example.hazardhub.R
import com.example.hazardhub.dataclass.Project
import com.example.hazardhub.dataclass.ProjectFetch

class MyCalamityAdapter(val context: Context, dataList : MutableList<ProjectFetch>) : RecyclerView.Adapter<MyCalamityAdapter.VH>() {

    private val sortedNotification: MutableList<ProjectFetch> = dataList.sortedByDescending { it.initiateTimestamp }.toMutableList()
    private var filteredList: MutableList<ProjectFetch> = sortedNotification


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.calamityImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.calamityTitleTextView)
        val pincodeTextView: TextView = itemView.findViewById(R.id.calamityPincodeTextView)
        val affectedPeopleTextView: TextView = itemView.findViewById(R.id.affectedPeopleTextView)
        val calamityView : CardView = itemView.findViewById(R.id.calamity_card)
        val calamityStatus = itemView.findViewById<TextView>(R.id.calamityStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calamity_item,parent,false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterSearch(query : String){
        filteredList = if(query.isEmpty()){
            sortedNotification
        }else{
           sortedNotification.filter {
               it.projectName.contains(query, ignoreCase = true)
           }
        }.toMutableList()
        notifyDataSetChanged()  //refresh the recyclerView
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = filteredList[position]

        // Bind project data to the views

        holder.titleTextView.text = project.projectName
        holder.pincodeTextView.text = "Pincode : ${ project.pincode }"
        holder.affectedPeopleTextView.text = "${project.affectedPeople} people affected"

        holder.calamityStatus.text = project.status
        val color = if (project.status == "Active") {
            ContextCompat.getColor(holder.itemView.context, R.color.green)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.red)
        }
        holder.calamityStatus.setTextColor(color)

        // Load image if imageUrl is not null
        if (!project.imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(project.imageUrl)
                .placeholder(R.drawable.loading) // Replace with your placeholder image
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.loading)
        }

        val address = "${project.projectAdd1}, ${project.projectAdd2}, ${project.projectState}"

        // Set click listener on the card view (if needed)
        if(project.status == "Active"){
            holder.calamityView.setOnClickListener {
                val intent = Intent(context, DetailedCalamityActivity::class.java)
                intent.putExtra("calamityId",project.projectId)
                intent.putExtra("calamityImage",project.imageUrl)
                intent.putExtra("calamityTitle",project.projectName)
                intent.putExtra("calamityAffected",project.affectedPeople)
                intent.putExtra("location",address)
                intent.putExtra("pincodeCalamity",project.pincode)
                intent.putExtra("initiator",project.initiator)
                intent.putExtra("status",project.status)
                intent.putExtra("calamityDescription",project.projectDescription)
                context.startActivity(intent)
            }
        }else{
            holder.calamityView.setOnClickListener{
                Toast.makeText(context,"This Project is Completed Already.",Toast.LENGTH_SHORT).show()
            }
        }
    }
}