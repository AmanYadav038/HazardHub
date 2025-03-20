package com.example.hazardhub

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.hazardhub.databinding.ActivityProjectBinding
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout.LabelVisibility

class ProjectActivity : AppCompatActivity() {
    lateinit var projectBinding : ActivityProjectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectBinding = ActivityProjectBinding.inflate(layoutInflater)
        setContentView(projectBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val accountType = sharedPreferences.getString("account_type","volunteers")

        setUpBottomNavigation(accountType)

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(projectBinding.container.id,CalamityViewFragment())
                .commit()
        }



        projectBinding.bottomNavigation.setOnItemSelectedListener { menuItem->
            when(menuItem.itemId){
                R.id.navigation_projects ->{
                    replaceFragment(CalamityViewFragment())
                    true
                }
                R.id.navigation_initiate_project -> {
                    replaceFragment(AddCalamityFragment())
                    true
                }
                R.id.navigation_log_out ->{
                    replaceFragment(LogOutFragment())
                    true
                }
                else ->false
            }
        }
    }

    private fun setUpBottomNavigation(accountType: String?) {
        val bottomNavigationMenu = projectBinding.bottomNavigation.menu
        // Clear existing menu items
        bottomNavigationMenu.clear()

        when (accountType) {
            "departments" -> {
                bottomNavigationMenu.add(0, R.id.navigation_projects, 0, "All Calamity")
                    .setIcon(R.drawable.ic_projects)
                bottomNavigationMenu.add(0, R.id.navigation_initiate_project, 1, "Add Project")
                    .setIcon(R.drawable.ic_initiate_project)
                bottomNavigationMenu.add(0, R.id.navigation_log_out, 2, "Log Out")
                    .setIcon(R.drawable.baseline_logout_24)
            }

            "volunteers" -> {
                bottomNavigationMenu.add(0, R.id.navigation_projects, 0, "My Calamity")
                    .setIcon(R.drawable.ic_projects)
                bottomNavigationMenu.add(0, R.id.navigation_log_out, 1, "Log Out")
                    .setIcon(R.drawable.baseline_logout_24)
            }

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(projectBinding.container.id, fragment)
        transaction.addToBackStack(null) // Optional: add to back stack for back navigation
        transaction.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}