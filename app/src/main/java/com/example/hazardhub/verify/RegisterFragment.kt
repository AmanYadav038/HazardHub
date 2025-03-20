package com.example.hazardhub.verify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.hazardhub.R
import com.example.hazardhub.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    lateinit var registerFragmentBinding : FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerFragmentBinding = FragmentRegisterBinding.inflate(layoutInflater,container,false)
        // Inflate the layout for this fragment
        openFragment(DepartmentRegisterFragment())
        registerFragmentBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.dRadioButton -> openFragment(DepartmentRegisterFragment())
                R.id.vRadioButton -> openFragment(VolunteerFragment())
            }
        }

        registerFragmentBinding.acc.setOnClickListener{
            openFragment(LogInFragment())
        }
        return registerFragmentBinding.root
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragTrans = fragmentManager.beginTransaction()
        fragTrans.replace(registerFragmentBinding.registerFrame.id,fragment)
            .commit()
    }
}