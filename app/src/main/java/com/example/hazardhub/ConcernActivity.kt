package com.example.hazardhub

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.MyConcernAdapter
import com.example.hazardhub.databinding.ActivityConcernBinding
import com.example.hazardhub.dataclass.Concern
import com.google.firebase.firestore.FirebaseFirestore

class ConcernActivity : AppCompatActivity() {
    lateinit var bindingConcerns : ActivityConcernBinding
    private var concernList = ArrayList<Concern>()
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingConcerns = ActivityConcernBinding.inflate(layoutInflater)
        setContentView(bindingConcerns.root)

        val toolbar = bindingConcerns.backImage
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val projectId = intent.getStringExtra("projectId").toString()
        fetchConcerns(projectId)
    }
    private fun fetchConcerns(projectId : String){
        db.collection("project-concerns").document(projectId).get()
            .addOnSuccessListener { document->
                if(document.exists()){
                    val concernsIds = document.get("concerns") as? MutableList<String> ?: mutableListOf()
                    if (concernsIds.isNotEmpty()){
                        fetchConcernsDetails(concernsIds)
                    }else{
                        Log.d("Firestore Firebase","Error fetching")
                    }
                }
            }
    }

    private fun fetchConcernsDetails(concernsIds : List<String>){
        concernList.clear()
        val totalConcern = concernsIds.size
        var processedCount = 0

        for(concernId in concernsIds){
            db.collection("concerns").document(concernId).get()
                .addOnSuccessListener { concernDocument->
                    if(concernDocument.exists()){
                        val concerns = concernDocument.toObject(Concern::class.java)
                        concerns?.let { concernList.add(it) }
                    }
                    processedCount++

                    if(processedCount == totalConcern){
                        populateConcernRecycler(this,concernList)
                    }
                }
                .addOnFailureListener { e->
                    Log.e("Firestore Firebase","Error fetchin ${e.message}")
                    processedCount++

                    if(processedCount == totalConcern){
                        populateConcernRecycler(this,concernList)
                    }
                }
        }
    }

    private fun populateConcernRecycler(context : Context, concernList : List<Concern>){
        bindingConcerns.rvConcern.adapter = MyConcernAdapter(concernList,this)
        bindingConcerns.rvConcern.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
    }
}