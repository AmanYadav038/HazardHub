package com.example.hazardhub.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hazardhub.DetailedCalamityActivity
import com.example.hazardhub.databinding.NotificationItemBinding
import com.example.hazardhub.dataclass.Notifications
import com.example.hazardhub.dataclass.Project
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(val context : Context, val notificationList : List<Notifications>) : RecyclerView.Adapter<NotificationAdapter.VH>() {
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    val sortedNotification = notificationList.sortedByDescending { it.timestamp }
    inner class VH(val binding : NotificationItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = NotificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val currNotification = sortedNotification[position]

        getProjectName(currNotification.projectId){projectName->
            if (projectName!=null){
                holder.binding.calamityName.text = projectName
            }
        }
        holder.binding.messageNotification.text = currNotification.message
        holder.binding.calamityUpdateType.text = currNotification.type
        val date = currNotification.timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        holder.binding.publishTime.text = dateFormat.format(date)

        holder.binding.notificationDirect.setOnClickListener{
            getTheProjectDetails(currNotification.projectId){calamityInfo->
                calamityInfo?.let { project->
                    val intent = Intent(context,DetailedCalamityActivity::class.java)
                    intent.putExtra("calamityId",currNotification.projectId)
                    intent.putExtra("calamityImage",project.imageUrl)
                    intent.putExtra("calamityTitle",project.projectName)
                    intent.putExtra("calamityAffected",project.affectedPeople)
                    val address = project.projectAdd1+","+project.projectAdd2
                    intent.putExtra("location",address)
                    intent.putExtra("pincodeCalamity",project.pincode)
                    intent.putExtra("initiator",project.initiator)
                    intent.putExtra("status",project.status)
                    intent.putExtra("calamityDescription",project.projectDescription)
                    context.startActivity(intent)

                }?: Log.e("NotificationAdapter","Project Not Found")
            }
        }
    }

    private fun getTheProjectDetails(projectId: String, onResult: (Project?) -> Unit) {
        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val project = document.toObject(Project::class.java)
                    onResult(project)
                } else {
                    onResult(null) // Document not found
                }
            }
            .addOnFailureListener {
                onResult(null) // Handle any failure as null
            }
    }

    fun getProjectName(projectId : String, onResult: (String?)->Unit){
        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { document->
                if(document!=null && document.exists()){
                    val projectName = document.getString("projectName")
                    onResult(projectName)
                }else{
                    onResult(null)
                }
            }
            .addOnFailureListener { e->
                Log.e("FireStore","Error fetching ")
                onResult(null)
            }
    }
}