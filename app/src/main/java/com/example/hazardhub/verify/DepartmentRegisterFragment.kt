package com.example.hazardhub.verify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hazardhub.R
import com.example.hazardhub.databinding.ActivityVerifyBinding
import com.example.hazardhub.databinding.FragmentDepartmentRegisterBinding
import com.example.hazardhub.databinding.FragmentRegisterBinding
import com.example.hazardhub.dataclass.Department
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class DepartmentRegisterFragment : Fragment() {
    lateinit var departFragmentBinding : FragmentDepartmentRegisterBinding
    lateinit var verifyBinding : ActivityVerifyBinding
    private var auth : FirebaseAuth = Firebase.auth
    private val departmentCollection = FirebaseFirestore.getInstance().collection("departments")
    private val userInputs = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        verifyBinding = ActivityVerifyBinding.inflate(layoutInflater, container, false)
        departFragmentBinding = FragmentDepartmentRegisterBinding.inflate(layoutInflater,container,false)
        chipViewSetup()
        departFragmentBinding.registerBtn.setOnClickListener {
            val name = departFragmentBinding.editName.text.toString()
            val email = departFragmentBinding.editTextEmail.text.toString()
            val password = departFragmentBinding.editRegisterPassword.text.toString()
            val states = userInputs

            if(validateInput(name, email, password, states)){
                saveDepartment(name,email,password,states)
            }
        }

        departFragmentBinding.backHome.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager.beginTransaction().replace(verifyBinding.verifyFrame.id,LogInFragment()).commit()
        }
        return departFragmentBinding.root
    }

    fun validateInput(name :String, email:String, password :String, states :ArrayList<String>) : Boolean{
        if (name.isEmpty()) {
            departFragmentBinding.editName.error = "Department name is required"
            return false
        }
        if (email.isEmpty()) {
            departFragmentBinding.editTextEmail.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            departFragmentBinding.editRegisterPassword.error = "Password is required"
            return false
        }
        if (states.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one state", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveDepartment(name: String, email: String, password: String, states: ArrayList<String>){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {task->
                if(task.isSuccessful){
                    val newDepartmentRef = departmentCollection.document()
                    val departmentId = newDepartmentRef.id

                    val newDepartment = Department(
                        name = name,
                        email = email,
                        stateOfOperation = states
                    )

                    departmentCollection.document(departmentId).set(newDepartment)
                        .addOnSuccessListener {
                            Toast.makeText(requireActivity(),"Department registered successfully",Toast.LENGTH_SHORT).show()
                            departFragmentBinding.registerBtn.visibility = View.GONE
                            departFragmentBinding.backHome.visibility =View.VISIBLE
                        }
                        .addOnFailureListener{e->
                            Toast.makeText(requireActivity(),"Error : ${e.message}",Toast.LENGTH_SHORT).show()

                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireActivity(), "Authentication Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun chipViewSetup(){
        val statesArray = resources.getStringArray(R.array.statesArray)

        for(state in statesArray){
            val chip = Chip(requireContext())
            chip.text = state
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    userInputs.add(state)
                } else {
                    userInputs.remove(state)
                }
            }
            departFragmentBinding.stateChipGroup.addView(chip)
        }
    }
}