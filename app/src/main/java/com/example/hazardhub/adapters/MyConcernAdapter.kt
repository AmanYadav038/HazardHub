package com.example.hazardhub.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hazardhub.R
import com.example.hazardhub.databinding.ConcernItemBinding
import com.example.hazardhub.dataclass.Concern
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class MyConcernAdapter(val concernList : List<Concern> , val context : Context) : RecyclerView.Adapter<MyConcernAdapter.VH>() {
    val sortedNotification = concernList.sortedByDescending { it.timestamp }

    inner class VH(val binding: ConcernItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = ConcernItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return concernList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val currConcern = sortedNotification[position]
        holder.binding.concernDesc.text = currConcern.description
        Glide.with(context).load(currConcern.imageUrl).placeholder(R.drawable.loading)
            .into(holder.binding.concernImage)

        if (currConcern.status != "Completed") {
            holder.binding.concernStatus.text = currConcern.status
        } else {
            holder.binding.concernStatus.text = currConcern.status
            holder.binding.concernStatus.setTextColor(Color.RED)
        }

        val date = currConcern.timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        holder.binding.timeStampConcern.text = dateFormat.format(date)

        // Set click listener to show dialog and update status
        holder.binding.concernView.setOnClickListener {
            if (!currConcern.concernId.isNullOrEmpty()) {
                showConfirmDialogAndUpdateStatus(currConcern.concernId, position)
            } else {
                Toast.makeText(context, "Concern ID is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmDialogAndUpdateStatus(concernId: String?, position: Int) {
        if (concernId.isNullOrEmpty()) {
            Toast.makeText(context, "Invalid Concern ID", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to mark this concern as completed?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                updateConcernStatus(concernId, position)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun updateConcernStatus(id: String, position: Int) {
        if (id.isNotEmpty()) {
            val concernRef = FirebaseFirestore.getInstance().collection("concerns").document(id)

            concernRef.update("status", "Completed")
                .addOnSuccessListener {
                    // Update local data source with the new status
                    sortedNotification[position].status = "Completed"

                    // Notify adapter that the item has changed
                    notifyItemChanged(position)

                    Toast.makeText(context, "Concern marked as completed!", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        "Failed to update status: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Log.e("Firestore", "Invalid concern ID")
            Toast.makeText(context, "Invalid concern ID", Toast.LENGTH_SHORT).show()
        }
    }
}