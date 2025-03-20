package com.example.hazardhub

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.LostAdapter
import com.example.hazardhub.databinding.ActivityLostBinding
import com.example.hazardhub.dataclass.LostPeople
import com.example.hazardhub.dataclass.Notifications
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class LostActivity : AppCompatActivity() {
        private val db = FirebaseFirestore.getInstance()
        private var peopleLost = ArrayList<LostPeople>()
        private lateinit var lostPeopleIdsArray: MutableList<String>
        private lateinit var lostActivityBinding: ActivityLostBinding
        private var selectedImageUri: Uri? = null
        private lateinit var dialogView: View
        private val getImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                showImageInDialog(dialogView, it)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            lostActivityBinding = ActivityLostBinding.inflate(layoutInflater)
            setContentView(lostActivityBinding.root)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            lostActivityBinding.toolBarLost.setNavigationOnClickListener {
                finish()
            }

            lostActivityBinding.addLostPeople.setOnClickListener {
                showAddLostPersonDialog()
            }

            val projectId = intent.getStringExtra("projectId").toString()
            db.collection("project-losts").document(projectId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val lostPeopleIds = document.get("losts") as? MutableList<String> ?: mutableListOf()
                        if (lostPeopleIds.isNotEmpty()) {
                            lostPeopleIdsArray = lostPeopleIds
                            fetchLostPeopleDetails(lostPeopleIds)
                        } else {
                            Log.d("Firestore", "No lost people found for this project.")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching project-losts document: ${e.message}")
                }
        }

        private fun showAddLostPersonDialog() {
            dialogView = layoutInflater.inflate(R.layout.layout_dialog_add_lost, null)
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView).setTitle("Add Lost Person")
            val dialog = dialogBuilder.create()

            val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
            val contactInput = dialogView.findViewById<EditText>(R.id.contactInput)
            val descriptionInput = dialogView.findViewById<EditText>(R.id.descriptionInput)
            val rewardInput = dialogView.findViewById<EditText>(R.id.rewardInput)
            val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)

            uploadImageButton.setOnClickListener {
                getImageLauncher.launch("image/*")
            }

            dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
                val name = nameInput.text.toString().trim()
                val contact = contactInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val reward = rewardInput.text.toString().trim()

                if (name.isNotEmpty() && selectedImageUri != null) {
                    uploadLostPersonImage(selectedImageUri!!) { imageUrl ->
                        saveLostPersonToDatabase(name, contact, description, reward, imageUrl)
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(this, "Please provide a name and select an image", Toast.LENGTH_SHORT).show()
                }
            }

            dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        private fun uploadLostPersonImage(imageUri: Uri, onComplete: (imageUrl: String) -> Unit) {
            val storageRef = FirebaseStorage.getInstance().reference.child("lost/${UUID.randomUUID()}")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }

        private fun showImageInDialog(dialogView: View, uri: Uri) {
            val imagePreview = dialogView.findViewById<ImageView>(R.id.imagePreview)
            imagePreview.visibility = View.VISIBLE
            dialogView.findViewById<Button>(R.id.uploadImageButton).visibility = View.GONE
            imagePreview.setImageURI(uri)
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun saveLostPersonToDatabase(
        name: String, contact: String, description: String, reward: String, imageUrl: String
    ) {
        val lostPersonData = LostPeople(contact, description, name, imageUrl, reward)

        // Add the lost person to the 'losts' collection
        db.collection("losts").add(lostPersonData)
            .addOnSuccessListener { documentReference ->
                val lostPersonId = documentReference.id
                val projectId = intent.getStringExtra("projectId").toString()

                // Check if the 'projectId' exists in the 'project-losts' collection
                db.collection("project-losts").document(projectId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // If the project-losts document exists, update it with the new lost person
                            db.collection("project-losts").document(projectId)
                                .update("losts", FieldValue.arrayUnion(lostPersonId))
                                .addOnSuccessListener {
                                    lostPeopleIdsArray.add(lostPersonId)
                                    Toast.makeText(this, "Lost person added and project updated successfully", Toast.LENGTH_SHORT).show()
                                    fetchLostPeopleDetails(lostPeopleIdsArray)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error updating project-losts: ${e.message}")
                                    Toast.makeText(this, "Failed to update project-losts.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // If the project-losts document does not exist, create a new one
                            val newLostDocument = hashMapOf(
                                "losts" to arrayListOf(lostPersonId),
                                "projectId" to projectId
                            )
                            db.collection("project-losts").document(projectId).set(newLostDocument)
                                .addOnSuccessListener {
                                    // After creating the new document, we can proceed without fetching again
                                    val intent = Intent(this,LostActivity::class.java)
                                    intent.putExtra("projectId",projectId)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error creating project-losts document: ${e.message}")
                                    Toast.makeText(this, "Failed to create project-losts document.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        addNotification(projectId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching project-losts document: ${e.message}")
                        Toast.makeText(this, "Failed to check project-losts document.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding lost person: ${e.message}")
                Toast.makeText(this, "Failed to add lost person.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNotification(projectId : String){
        val db = FirebaseFirestore.getInstance().collection("notifications")
        val newNotificationReference = db.document()

        val notification = Notifications(
            newNotificationReference.id,"Missing Person",projectId,"A new person reported missing.",
            Timestamp.now(),false)

        newNotificationReference.set(notification)
            .addOnSuccessListener {
                Log.d("Notification","Notification added")
            }
            .addOnFailureListener{e->
                Log.w("Notification","Error adding notification")

            }
    }


    private fun fetchLostPeopleDetails(lostPeopleIds: List<String>) {
            peopleLost.clear()
            val totalLostPeople = lostPeopleIds.size
            var processedCount = 0

            for (lostId in lostPeopleIds) {
                db.collection("losts").document(lostId).get()
                    .addOnSuccessListener { lostDocument ->
                        if (lostDocument.exists()) {
                            val lostPerson = lostDocument.toObject(LostPeople::class.java)
                            lostPerson?.let { peopleLost.add(it) }
                        }
                        processedCount++

                        if (processedCount == totalLostPeople) {
                            populateLostRecycler(this, peopleLost)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching lost person details: ${e.message}")
                        processedCount++

                        if (processedCount == totalLostPeople) {
                            populateLostRecycler(this, peopleLost)
                        }
                    }
            }
        }

        private fun populateLostRecycler(context: Context, peopleList: List<LostPeople>) {
            lostActivityBinding.rvLost.adapter = LostAdapter(peopleList, context)
            lostActivityBinding.rvLost.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }
