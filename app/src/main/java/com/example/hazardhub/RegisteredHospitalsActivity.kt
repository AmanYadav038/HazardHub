package com.example.hazardhub

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.MyHospitalAdapter
import com.example.hazardhub.databinding.ActivityRegisteredHospitalsBinding
import com.example.hazardhub.dataclass.Hospitals
import com.google.firebase.firestore.FirebaseFirestore

class RegisteredHospitalsActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    lateinit var bindingRegistered : ActivityRegisteredHospitalsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingRegistered = ActivityRegisteredHospitalsBinding.inflate(layoutInflater)
        setContentView(bindingRegistered.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindingRegistered.toolbarhospital.setNavigationOnClickListener {
            finish()
        }
        fetchHospitals()
    }

    private fun fetchHospitals() {
        db.collection("hospitals").get()
            .addOnSuccessListener { documents ->
                val hospitalList = documents.map { it.toObject(Hospitals::class.java) }
                if (hospitalList.isEmpty()) {
                    Toast.makeText(this, "No hospitals found registered yet.", Toast.LENGTH_SHORT).show()
                } else {
                    bindingRegistered.registeredRV.apply {
                            layoutManager = LinearLayoutManager(this@RegisteredHospitalsActivity)
                            adapter = MyHospitalAdapter(this@RegisteredHospitalsActivity, hospitalList)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching hospitals: ${e.message}", Toast.LENGTH_SHORT).show()
                }
    }
}