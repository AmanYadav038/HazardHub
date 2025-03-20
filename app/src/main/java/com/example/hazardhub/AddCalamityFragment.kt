package com.example.hazardhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hazardhub.databinding.FragmentAddCalamityBinding
import com.example.hazardhub.dataclass.Notifications
import com.example.hazardhub.dataclass.Project
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.logging.SimpleFormatter


class AddCalamityFragment : Fragment() {
    lateinit var binding : FragmentAddCalamityBinding
    lateinit var mAuth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    // Register the activity result launcher
    private val getImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        uri?.let {
            selectedImageUri = it
            binding.imagePreview.setImageURI(it) // Display selected image
            binding.imagePreview.visibility = View.VISIBLE // Show the image view
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mAuth = FirebaseAuth.getInstance()
        binding = FragmentAddCalamityBinding.inflate(inflater, container, false)

        binding.uploadImageButton.setOnClickListener {
            pickImage()
        }

        val statesAdapter = ArrayAdapter.createFromResource(requireActivity(),R.array.states,android.R.layout.simple_spinner_item)
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statesSpinner.adapter = statesAdapter

        binding.submitProjectButton.setOnClickListener {
            val calamityTitle = binding.calamityTitle.text.toString().trim()
            val calamityDescription = binding.calamityDescription.text.toString().trim()
            val affectedPeopleCount = binding.affectedPeopleCount.text.toString()
            val addressLine1 = binding.addressLine1.text.toString().trim()
            val addressLine2 = binding.addressLine2.text.toString().trim()
            val pincodeDetail = binding.pincodeDetail.text.toString().trim()
            val state = binding.statesSpinner.selectedItem.toString()
            submitProject(calamityTitle, calamityDescription, affectedPeopleCount, addressLine1, addressLine2, pincodeDetail,state)
        }

        return binding.root
    }

    private fun pickImage() {
        getImageLauncher.launch("image/*")
    }

    private fun submitProject(calamityTitle: String,calamityDescription: String,affectedPeopleCount: String,addressLine1: String,addressLine2: String,pincodeDetail: String,state : String ) {
        // Validate data
        if (calamityTitle.isEmpty() || calamityDescription.isEmpty() || affectedPeopleCount == null || addressLine1.isEmpty() || pincodeDetail.isEmpty() || binding.statesSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image if selected
        if (selectedImageUri != null) {
            val timestamp = System.currentTimeMillis().toString()
            val storageReference = FirebaseStorage.getInstance().reference.child("projects/$timestamp.jpg")

            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveProjectToDatabase(
                            calamityTitle,
                            calamityDescription,
                            affectedPeopleCount,
                            addressLine1,
                            addressLine2,
                            pincodeDetail,
                            state,
                            imageUrl
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If no image selected, just save the project data without an image URL
            saveProjectToDatabase(
                calamityTitle,
                calamityDescription,
                affectedPeopleCount,
                addressLine1,
                addressLine2,
                pincodeDetail,
                state,
                null
            )
        }
    }

    private fun saveProjectToDatabase(
        title: String,
        description: String,
        affectedCount: String,
        addrLine1: String,
        addrLine2: String,
        pincode: String,
        state : String,
        imageUrl: String?
    ) {
        // Create a new project object
        val newProject = Project(
            projectName = title,
            projectDescription = description,
            affectedPeople = affectedCount,
            projectAdd1 = addrLine1,
            projectAdd2 = addrLine2,
            projectState = state ,
            pincode = pincode,
            imageUrl = imageUrl,
            initiator = mAuth.currentUser!!.email!!
        )

        // Save project to Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("projects")
            .add(newProject)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Project submitted successfully", Toast.LENGTH_SHORT).show()
                addNotification(it.id)
                // Clear input fields if needed
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error saving project: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNotification(projectId : String){
        val db = FirebaseFirestore.getInstance().collection("notifications")
        val newNotificationReference = db.document()

        val notification = Notifications(
            newNotificationReference.id,"Calamity Added",projectId,"A new calamity added.",Timestamp.now(),false)

        newNotificationReference.set(notification)
            .addOnSuccessListener {
                Log.d("Notification","Notification added")
            }
            .addOnFailureListener{e->
                Log.w("Notification","Error adding notification")

            }
    }

    private fun clearFields() {
        // Clear all fields and reset image view
        binding.calamityTitle.setText("")
        binding.calamityDescription.setText("")
        binding.affectedPeopleCount.setText("")
        binding.addressLine1.setText("")
        binding.addressLine2.setText("")
        binding.pincodeDetail.setText("")
        binding.imagePreview.setImageURI(null)
        binding.imagePreview.visibility = View.GONE
        selectedImageUri = null
    }
}
