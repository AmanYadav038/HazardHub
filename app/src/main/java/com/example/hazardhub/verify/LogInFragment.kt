package com.example.hazardhub.verify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.hazardhub.ProjectActivity
import com.example.hazardhub.R
import com.example.hazardhub.databinding.ActivityVerifyBinding
import com.example.hazardhub.databinding.FragmentLogInBinding
import com.example.hazardhub.dataclass.Department
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject


class LogInFragment : Fragment() {
    lateinit var loginBinding: FragmentLogInBinding
    private lateinit var verifyBinding: ActivityVerifyBinding
    private val departmentCollection = FirebaseFirestore.getInstance().collection("departments")
    private val volunteerCollection = FirebaseFirestore.getInstance().collection("volunteers")
    var collectionUsed : CollectionReference = departmentCollection
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    var accountType : String = "departments"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginBinding = FragmentLogInBinding.inflate(inflater, container, false)
        verifyBinding = ActivityVerifyBinding.inflate(inflater) // Initialize binding properly

        loginBinding.registerForm.setOnClickListener {
            openFragment(RegisterFragment())
        }
        loginBinding.dBtn.setOnClickListener {
            initializeBtn()
            loginBinding.dBtn.setBackgroundColor(resources.getColor(R.color.dark_blue,requireActivity().theme))
            loginBinding.loginFrame.loginType.setText("departments")
            collectionUsed = departmentCollection
            accountType = "departments"
        }
        loginBinding.vBtn.setOnClickListener {
            initializeBtn()
            loginBinding.loginFrame.loginType.setText("volunteers")
            loginBinding.vBtn.setBackgroundColor(resources.getColor(R.color.dark_blue,requireActivity().theme))
            collectionUsed = volunteerCollection
            accountType = "volunteers"
        }

        loginBinding.loginFrame.signIn.setOnClickListener {
            val email = loginBinding.loginFrame.editTextEmail.text.toString()
            val password = loginBinding.loginFrame.editTextPassword.text.toString()

            authenticateDepartment(email, password)
        }

        return loginBinding.root
    }

    private fun initializeBtn() {
        loginBinding.dBtn.setBackgroundColor(resources.getColor(R.color.light_blue,requireActivity().theme))
        loginBinding.vBtn.setBackgroundColor(resources.getColor(R.color.light_blue,requireActivity().theme))
    }

    private fun authenticateDepartment(email: String, password: String) {
        collectionUsed.whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(
                        requireActivity(),
                        "No ${loginBinding.loginFrame.loginType.text} found with this email",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }else{
                    auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener{
                            if(it.isSuccessful){
                                Toast.makeText(requireActivity(), "Authentication Successful!", Toast.LENGTH_LONG).show()

                                saveUserSession(email,accountType,email)
                                val intent = Intent(requireActivity(),ProjectActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                            }else{
                                Toast.makeText(requireActivity(),"Wrong Password",Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }
    private fun openFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragTrans = fragmentManager.beginTransaction()
        fragTrans.replace(R.id.verifyFrame, fragment) // Use the actual ID of the container
            .addToBackStack(null) // Optional, if you want to navigate back
            .commit()
    }

    private fun saveUserSession(email: String, accountType: String, loggedIn : String){
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email",email)
        editor.putString("account_type",accountType)
        editor.putString("Email",loggedIn)

        editor.apply()
    }
}
