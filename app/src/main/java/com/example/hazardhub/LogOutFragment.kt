package com.example.hazardhub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hazardhub.databinding.FragmentLogInBinding
import com.example.hazardhub.databinding.FragmentLogOutBinding
import com.google.firebase.auth.FirebaseAuth

class LogOutFragment : Fragment() {
    lateinit var logOutBinding: FragmentLogOutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        logOutBinding = FragmentLogOutBinding.inflate(layoutInflater, container, false)
        logOutBinding.logOutBtn.setOnClickListener {
            clearSharedPreferences()
            FirebaseAuth.getInstance().signOut()
        }
        return logOutBinding.root
    }

    private fun clearSharedPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        val intent = Intent(requireActivity(),MainActivity::class.java)
        startActivity(intent)
    }
}
