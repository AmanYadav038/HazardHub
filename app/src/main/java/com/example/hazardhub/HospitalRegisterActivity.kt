package com.example.hazardhub

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hazardhub.databinding.ActivityHospitalRegisterBinding
import com.example.hazardhub.databinding.ActivityLostBinding
import com.example.hazardhub.dataclass.Hospitals
import com.google.firebase.firestore.FirebaseFirestore

class HospitalRegisterActivity : AppCompatActivity() {
    lateinit var hospitalRegisterBinding: ActivityHospitalRegisterBinding
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hospitalRegisterBinding = ActivityHospitalRegisterBinding.inflate(layoutInflater)
        setContentView(hospitalRegisterBinding.root)

        hospitalRegisterBinding.toolbarhospital.setNavigationOnClickListener {
            finish()
        }

        val statesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.states,
            android.R.layout.simple_spinner_item
        )

        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hospitalRegisterBinding.statesSpinner.adapter = statesAdapter
        hospitalRegisterBinding.statesSpinner.setSelection(0)

        hospitalRegisterBinding.registerBtn.setOnClickListener {
            valid()
        }

        hospitalRegisterBinding.showAllHospital.setOnClickListener {
            val intent = Intent(this@HospitalRegisterActivity,RegisteredHospitalsActivity::class.java)
            startActivity(intent)
        }

    }


    private fun valid(){
        val hospitalName = hospitalRegisterBinding.editTextName.text.toString()
        val hospitalEmail = hospitalRegisterBinding.editTextEmail.text.toString()
        val hospitalPhone = hospitalRegisterBinding.editTextNum.text.toString()
        val numberOfBed = hospitalRegisterBinding.bedCount.text.toString()
        val timeStart = hospitalRegisterBinding.startTime.text.toString()
        val timeEnd = hospitalRegisterBinding.endTime.text.toString()
        val address1 = hospitalRegisterBinding.line1.text.toString()
        val address2 = hospitalRegisterBinding.line2.text.toString()
        val state = hospitalRegisterBinding.statesSpinner.selectedItem.toString()
        val pincode = hospitalRegisterBinding.pincodeDetail.text.toString()
        val emergencyServices = hospitalRegisterBinding.emergencyServices.isChecked
        if(hospitalName!="" || hospitalEmail!="" || hospitalPhone!="" || numberOfBed != "" || timeStart == ""|| address1 == ""|| state == ""|| pincode == "" ){
            saveToDatabase(hospitalName,hospitalEmail,hospitalPhone,emergencyServices,numberOfBed,timeStart,timeEnd,address1,address2,state,pincode)
        }else{
            Toast.makeText(this@HospitalRegisterActivity,"Fill Out Every Entries For All Information",Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToDatabase(hospitalName : String,hospitalEmail: String,hospitalPhone: String,emergencyService : Boolean,numberOfBed: String,timeStart: String,timeEnd:String,address1: String,address2: String,state: String, pincode: String ){
        val hospitalInfo = Hospitals(address1,address2,hospitalPhone,emergencyService, timeEnd,hospitalName,numberOfBed,pincode,timeStart,state)
        db.collection("hospitals").add(hospitalInfo).addOnSuccessListener {
            Toast.makeText(this,"Record Updated",Toast.LENGTH_SHORT).show()
            clearAllViews()
        }
    }

    private fun clearAllViews(){
        hospitalRegisterBinding.editTextName.setText("")
        hospitalRegisterBinding.editTextEmail.setText("")
        hospitalRegisterBinding.editTextNum.setText("")
        hospitalRegisterBinding.bedCount.setText("")
        hospitalRegisterBinding.startTime.setText("")
        hospitalRegisterBinding.endTime.setText("")
        hospitalRegisterBinding.line1.setText("")
        hospitalRegisterBinding.line2.setText("")
         hospitalRegisterBinding.pincodeDetail.setText("")
        hospitalRegisterBinding.emergencyServices.isChecked = false
    }
}