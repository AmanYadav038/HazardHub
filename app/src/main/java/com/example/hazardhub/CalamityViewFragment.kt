package com.example.hazardhub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazardhub.adapters.MyCalamityAdapter
import com.example.hazardhub.databinding.FragmentCalamityViewBinding
import com.example.hazardhub.dataclass.Department
import com.example.hazardhub.dataclass.Project
import com.example.hazardhub.dataclass.ProjectFetch
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class CalamityViewFragment : Fragment() {
    lateinit var calamityViewBinding: FragmentCalamityViewBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var calamityAdapter: MyCalamityAdapter
    private var projectList: MutableList<ProjectFetch> = mutableListOf() // Mutable list to hold projects

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        calamityViewBinding = FragmentCalamityViewBinding.inflate(layoutInflater, container, false)


        // Set up RecyclerView with an empty adapter initially
        calamityAdapter = MyCalamityAdapter(requireContext(), mutableListOf())
        calamityViewBinding.calamityRecycler.layoutManager = LinearLayoutManager(requireContext())
        calamityViewBinding.calamityRecycler.adapter = calamityAdapter

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        fetchCalamities()

        calamityViewBinding.notifyIcon.setOnClickListener{
            val intent = Intent(requireActivity(),NotificationActivity::class.java)
            startActivity(intent)
        }

        calamityViewBinding.searchAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calamityAdapter.filterSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return calamityViewBinding.root
    }

    private fun populateCalamityRecyclerView(calamityList: MutableList<ProjectFetch>) {
        calamityViewBinding.calamityRecycler.layoutManager = LinearLayoutManager(requireContext())
        calamityAdapter = MyCalamityAdapter(requireContext(), calamityList)
        calamityViewBinding.calamityRecycler.adapter = calamityAdapter
    }

    private fun fetchCalamities(){
        val calamityList = mutableListOf<ProjectFetch>()
        db.collection("projects")
            .get()
            .addOnSuccessListener { calamityDocuments ->
                if (calamityDocuments.isEmpty) {
                    Toast.makeText(requireActivity(), "No calamities found for the department.", Toast.LENGTH_SHORT).show()
                } else {
                    for (calamityDoc in calamityDocuments) {
                        val calamity = calamityDoc.toObject(Project::class.java)
                        val projectWithId = ProjectFetch(
                            projectId = calamityDoc.id, // Set projectId from document ID
                            projectName = calamity.projectName,
                            projectDescription = calamity.projectDescription,
                            affectedPeople = calamity.affectedPeople,
                            projectAdd1 = calamity.projectAdd1,
                            projectAdd2 = calamity.projectAdd2,
                            projectState = calamity.projectState,
                            pincode = calamity.pincode,
                            initiateTimestamp = Timestamp.now(),
                            initiator = calamity.initiator,
                            status = calamity.status,
                            emergency = calamity.emergency,
                            imageUrl = calamity.imageUrl
                        )
                        calamityList.add(projectWithId)
                    }
                    populateCalamityRecyclerView(calamityList)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireActivity(), "Error fetching calamities: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
