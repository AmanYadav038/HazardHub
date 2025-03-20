package com.example.hazardhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.NotificationAdapter
import com.example.hazardhub.databinding.ActivityNotificationBinding
import com.example.hazardhub.dataclass.Notifications
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class NotificationActivity : AppCompatActivity() {
    lateinit var notificationBinding : ActivityNotificationBinding
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationBinding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(notificationBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        notificationBinding.tools.setNavigationOnClickListener {
            val intent = Intent(this,ProjectActivity::class.java)
            startActivity(intent)
            finish()
        }

        val NotificationList = ArrayList<Notifications>()

        db.collection("notifications").get()
            .addOnSuccessListener {documents->
                for(document in documents){
                    val notificationCurr = document.toObject(Notifications::class.java)
                    NotificationList.add(notificationCurr)
                }
                notificationBinding.notifyRv.adapter = NotificationAdapter(this,NotificationList)
                notificationBinding.notifyRv.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            }
            .addOnFailureListener { e->
                Log.e("FireStore","error fetching notifications")
            }




    }
}