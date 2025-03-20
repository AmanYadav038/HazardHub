package com.example.hazardhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hazardhub.dataclass.Project
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private lateinit var mAuth:FirebaseAuth
    private lateinit var intent : Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        mAuth = FirebaseAuth.getInstance()
        val hd = Handler(Looper.getMainLooper())
        val currentUser = mAuth.currentUser?.uid
        if(currentUser!= null){
            intent = Intent(this,ProjectActivity::class.java)
        }else{
            intent = Intent(this, MainActivity::class.java)
        }


        hd.postDelayed({startActivity(intent)
        finish()},2000)
    }
}