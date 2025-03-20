package com.example.hazardhub

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.NoticesAdapter
import com.example.hazardhub.databinding.ActivityNoticesBinding
import com.example.hazardhub.dataclass.Notices
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NoticesActivity : AppCompatActivity() {
    lateinit var noticesBinding : ActivityNoticesBinding
    val db = FirebaseFirestore.getInstance()
    var emailId : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noticesBinding = ActivityNoticesBinding.inflate(layoutInflater)
        setContentView(noticesBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val accountType = sharedPreferences.getString("account_type","departments")
        val loggedInVolunteer = sharedPreferences.getString("Email","")
        emailId = loggedInVolunteer ?: ""

        val toolbar = noticesBinding.toolBarLost
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        checkInitiator()
        val projectIds = intent.getStringExtra("projectID").toString()
        CoroutineScope(Dispatchers.Main).launch {
            val noticesList = getNoticesWithTime(projectIds)
            noticesBinding.rvNotices.adapter = NoticesAdapter(this@NoticesActivity,noticesList)
            noticesBinding.rvNotices.layoutManager = LinearLayoutManager(this@NoticesActivity)
        }

    }

    fun checkInitiator(){
        val initiator = intent.getStringExtra("initiator")
        if(initiator==emailId){
            noticesBinding.addNotice.visibility = View.VISIBLE
        }

    }

    suspend fun getNoticesWithTime(projectId : String):List<Notices>{
        val noticesReference = db.collection("notices")

        return try{
            val querySnapshot = noticesReference.whereEqualTo("projectId",projectId).get().await()
            val noticesList = querySnapshot.documents[0].get("notices") as? List<Map<String,Any>> ?: emptyList()

            noticesList.map { noticeMap ->
                Notices(
                    message = noticeMap["message"] as? String ?: "",
                    timestamp = noticeMap["timestamp"] as Timestamp,
                    projectId = projectId
                )
            }.sortedByDescending { it.timestamp.toDate().time ?: 0 }

        }catch (e:Exception){
            println(e.message)
            emptyList()
        }

    }
}