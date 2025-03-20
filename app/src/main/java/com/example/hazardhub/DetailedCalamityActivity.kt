package com.example.hazardhub

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hazardhub.adapters.MyHospitalAdapter
import com.example.hazardhub.adapters.MyVolunteerAdapter
import com.example.hazardhub.databinding.ActivityDetailedCalamityBinding
import com.example.hazardhub.dataclass.Hospitals
import com.example.hazardhub.dataclass.Notifications
import com.example.hazardhub.dataclass.Volunteers
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import android.widget.Button
import android.widget.ImageView
import com.example.hazardhub.dataclass.Concern
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.selects.select
import java.sql.Time
import java.util.UUID

class DetailedCalamityActivity : AppCompatActivity() {
    lateinit var detailedCalamityBinding : ActivityDetailedCalamityBinding
    private lateinit var db : FirebaseFirestore
    val volunteerEmails = mutableListOf<String>()
    var emailId : String = ""
    var initiate : String = ""
    var statusProject : String = ""
    lateinit var projectId: String


    private lateinit var dialogView: View
    private var selectedImageUri: Uri? = null

    private val getImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            showImageInDialog(dialogView, uri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val accountType = sharedPreferences.getString("account_type","departments")
        val loggedInVolunteer = sharedPreferences.getString("Email","")
        emailId = loggedInVolunteer ?: ""



        detailedCalamityBinding = ActivityDetailedCalamityBinding.inflate(layoutInflater)
        setContentView(detailedCalamityBinding.root)



        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        val toolbar = detailedCalamityBinding.backImage
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        if(accountType == "departments"){
            detailedCalamityBinding.volunteerBtn.visibility = View.GONE
        }else{
            detailedCalamityBinding.volunteerBtn.visibility = View.VISIBLE
        }

        // Get calamity data from Intent
        val calamityImageUrl = intent.getStringExtra("calamityImage")
        val calamityTitle = intent.getStringExtra("calamityTitle")
        val affectedPeople = intent.getStringExtra("calamityAffected")
        val location = intent.getStringExtra("location")
        val pincode = intent.getStringExtra("pincodeCalamity")
        val calamityDescription = intent.getStringExtra("calamityDescription")
        projectId = intent.getStringExtra("calamityId").toString()
        initiate = intent.getStringExtra("initiator").toString()
        statusProject = intent.getStringExtra("status").toString()

        toolbar.title = calamityTitle

        checkStatus(initiate,statusProject)

        // Set UI elements
        detailedCalamityBinding.calamityTitleText.text = calamityTitle
        Glide.with(this).load(calamityImageUrl).placeholder(R.drawable.loading).into(detailedCalamityBinding.calamityImage)
        detailedCalamityBinding.calamityAffected.text = affectedPeople.toString()
        detailedCalamityBinding.calamityLocation.setText("$location, $pincode")
        detailedCalamityBinding.calamityDesc.text = calamityDescription


        // Fetch Volunteers and Hospitals by Pincode
        pincode?.let {
            fetchVolunteers(projectId)
            fetchHospitalsByPincode(it)
        } ?: run {
            Toast.makeText(this, "Pincode not provided", Toast.LENGTH_SHORT).show()
        }

        detailedCalamityBinding.completeBtn.setOnClickListener {
            markTheProjectAsComplete(projectId)
        }

        detailedCalamityBinding.volunteerBtn.setOnClickListener {
            addVolunteer()
        }

        detailedCalamityBinding.extremeEmergency.setOnClickListener {
            sendEmail(
                "amansinghyadav876@gmail.com",
                "Subject of the email",
                "Body of the email"
            )
        }

        detailedCalamityBinding.seeAllQuery.setOnClickListener{
            val intent = Intent(this,ConcernActivity::class.java)
            intent.putExtra("projectId",projectId)
            startActivity(intent)
        }

        detailedCalamityBinding.raiseQuery.setOnClickListener{
            showAddQueryDialog()
        }

    }

    private fun showAddQueryDialog(){
        dialogView = layoutInflater.inflate(R.layout.add_concern,null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView).setTitle("Add A Concern")

        val dialog = dialogBuilder.create()

        val descriptionInput = dialogView.findViewById<EditText>(R.id.descInput)
        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadConcernImageButton)

        uploadImageButton.setOnClickListener {
            getImageLauncher.launch("image/*")
        }

        dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
            val desc = descriptionInput.text.toString().trim()

            if(desc.isNotEmpty() && selectedImageUri != null){
                uploadConcernImage(selectedImageUri!!){imageUrl->
                    saveConcernToDatabase(desc,imageUrl)
                    dialog.dismiss()
                }
            }else{
                Toast.makeText(this,"Please provide a image and concern description",Toast.LENGTH_SHORT)
            }
        }
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun uploadConcernImage(imageUri : Uri ,onComplete:(imageUrl : String)->Unit){
        val storageRef = FirebaseStorage.getInstance().reference.child("concerns/${projectId}/${UUID.randomUUID()}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this,"Failed to upload image",Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImageInDialog(dialogView : View, uri : Uri){
        val imagePreview = dialogView.findViewById<ImageView>(R.id.imageConcernPreview)
        imagePreview.visibility = View.VISIBLE
        dialogView.findViewById<Button>(R.id.uploadConcernImageButton).visibility = View.GONE
        imagePreview.setImageURI(uri)
    }

    private fun saveConcernToDatabase(desc: String, imageUrl: String) {

        // First, get the reference to the 'concerns' collection
        val concernRef = db.collection("concerns").document() // Automatically generates a new document reference

        val concernData = Concern(concernRef.id, desc, imageUrl, "In Progress", Timestamp.now())
        // Set the data for the document reference, including the concernId
        concernRef.set(concernData)
            .addOnSuccessListener {
                // Successfully added the concern document with the concernId set
                val concernId = concernRef.id

                // Add this concernId to the 'project-concerns' collection
                val projectId = projectId // Assuming you have a projectId variable set earlier

                db.collection("project-concerns").document(projectId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // If the project document exists, add the concernId to the 'concerns' array
                            db.collection("project-concerns").document(projectId)
                                .update("concerns", FieldValue.arrayUnion(concernId))
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Concern added and will be taken care of ASAP",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error updating project concerns: ${e.message}")
                                }
                        } else {
                            // If the project document doesn't exist, create a new one with concernId
                            val newConcern = hashMapOf(
                                "concerns" to arrayListOf(concernId),
                                "projectId" to projectId
                            )
                            db.collection("project-concerns").document(projectId).set(newConcern)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Concern added and will be taken care of ASAP",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching project concerns: ${e.message}")
                        Toast.makeText(this, "Failed to add concern.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving concern: ${e.message}")
                Toast.makeText(this, "Failed to save concern.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sendEmail(recipient: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Ensure only email apps handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            // Optional: Notify the user that no email app is installed
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkStatus(initiator : String , status : String){
        if(initiator == emailId && status=="Active"){
            detailedCalamityBinding.completeBtn.visibility = View.VISIBLE
        }
        if(status == "Completed"){
            detailedCalamityBinding.volunteerBtn.visibility = View.GONE
        }
    }
    private fun markTheProjectAsComplete(projectId : String){
        Toast.makeText(this,projectId,Toast.LENGTH_SHORT).show()
        db.collection("projects").document(projectId).get()
            .addOnSuccessListener {document->
                if (document.exists()){
                    db.collection("projects").document(projectId)
                        .update("status","completed")
                        .addOnSuccessListener {
                            checkStatus(initiate,statusProject)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error updating project status: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e->
                Toast.makeText(this,"Error fetching : ${e.message}",Toast.LENGTH_SHORT).show()

            }
    }

    private fun checkForVolunteer(){
        if(emailId in volunteerEmails){
            detailedCalamityBinding.volunteerBtn.visibility = View.GONE
        }
    }

    private fun addVolunteer() {
        val projectId = intent.getStringExtra("calamityId").toString()

        if (projectId.isNotEmpty()) {
            db.collection("projects-volunteers").whereEqualTo("projectId",projectId)
                .get()
                .addOnSuccessListener { documents->
                    if(!documents.isEmpty){
                        val document = documents.documents[0]
                        val projectRef = db.collection("projects-volunteers").document(document.id)

                        projectRef.update("volunteersIdArray",FieldValue.arrayUnion(emailId))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Volunteer added successfully", Toast.LENGTH_SHORT).show()
                                checkForVolunteer()  // Optionally update UI
                                fetchVolunteers(projectId)
                            }
                            .addOnFailureListener {e ->
                                Toast.makeText(this, "Error adding volunteer: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        val volunteerData = hashMapOf(
                            "projectId" to projectId,
                            "volunteersIdArray" to listOf(emailId)
                        )

                        db.collection("projects-volunteers").add(volunteerData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Volunteer added and new project entry created successfully", Toast.LENGTH_SHORT).show()
                                fetchVolunteers(projectId)  // Refresh volunteer list
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error creating project entry: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    addNotification(projectId)
                }
                .addOnFailureListener { e->
                    Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNotification(projectId : String){
        val db = FirebaseFirestore.getInstance().collection("notifications")
        val newNotificationReference = db.document()

        val notification = Notifications(
            newNotificationReference.id,"Volunteer Added",projectId,"A new Volunteer added.",
            Timestamp.now(),false)

        newNotificationReference.set(notification)
            .addOnSuccessListener {
                Log.d("Notification","Notification added")
            }
            .addOnFailureListener{e->
                Log.w("Notification","Error adding notification")

            }
    }

    private fun fetchVolunteers(projectId:String){
        volunteerEmails.clear()
        val volunteersList = mutableListOf<Volunteers>()

        db.collection("projects-volunteers")
            .whereEqualTo("projectId",projectId)
            .get()
            .addOnSuccessListener {documents ->
                for (document in documents){
                    val emailArray = document.get("volunteersIdArray") as? List<String>
                    if (emailArray != null){
                        volunteerEmails.addAll(emailArray)
                    }
                }
                checkForVolunteer()

                for (email in volunteerEmails) {
                    db.collection("volunteers").whereEqualTo("email",email)
                        .get()
                        .addOnSuccessListener { volunteerDocs->
                            for(volunteerDoc in volunteerDocs){
                                val volunteer = volunteerDoc.toObject(Volunteers::class.java)
                                volunteersList.add(volunteer)
                            }
                            if(volunteersList.size == volunteerEmails.size){
                                populateVolunteerRecycler(volunteersList)
                            }
                        }
                }
            }
            .addOnFailureListener{e->
                Log.w("FireStoreError","Error fetching volunteer emails",e)
            }

    }

    private fun populateVolunteerRecycler(volunteerList : List<Volunteers>){
        val adapter = MyVolunteerAdapter(this@DetailedCalamityActivity, volunteerList)
        detailedCalamityBinding.volunteerRecycler.adapter = adapter
        detailedCalamityBinding.volunteerRecycler.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
    }

    private fun fetchHospitalsByPincode(pincode: String) {
        if (pincode.isNotEmpty()) {
            db.collection("hospitals")
                .whereEqualTo("pincode", pincode)
                .get()
                .addOnSuccessListener { documents ->
                    val hospitalList = documents.map { it.toObject(Hospitals::class.java) }
                    if (hospitalList.isEmpty()) {
                        Toast.makeText(this, "No hospitals found for the selected pincode.", Toast.LENGTH_SHORT).show()
                    } else {
                        detailedCalamityBinding.hosiptalRecycler.apply {
                            layoutManager = LinearLayoutManager(this@DetailedCalamityActivity)
                            adapter = MyHospitalAdapter(this@DetailedCalamityActivity, hospitalList)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching hospitals: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please enter a valid pincode.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.calamity_detail_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.lost_found -> {
                val intent = Intent(this@DetailedCalamityActivity,LostActivity::class.java)
                intent.putExtra("projectId",projectId)
                startActivity(intent)
            }
            R.id.calamity_news->{
                val intent = Intent(this@DetailedCalamityActivity,NoticesActivity::class.java)
                intent.putExtra("projectID",projectId)
                intent.putExtra("initiator",initiate)
                startActivity(intent)
            }
        }
        return true
    }

}
