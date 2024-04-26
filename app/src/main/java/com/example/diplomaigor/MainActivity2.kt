package com.example.diplomaigor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var profile: Button
    private lateinit var search: Button
    private lateinit var database: DatabaseReference
    private lateinit var donorAdapter: DonorAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addUser: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        database = FirebaseDatabase.getInstance().reference.child("Donors")

        profile = findViewById(R.id.button_profile)
        search = findViewById(R.id.button_main)
        addUser = findViewById(R.id.add_user_button)
        recyclerView = findViewById(R.id.recyclerViewDonors)

        addUser.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        search.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        donorAdapter = DonorAdapter(this, ArrayList())
        recyclerView.adapter = donorAdapter

        fetchDonors("name")

        findViewById<Button>(R.id.button_sort_name).setOnClickListener {
            fetchDonors("name")
        }

        findViewById<Button>(R.id.button_sort_blood_group).setOnClickListener {
            fetchDonors("bloodGroup")
        }
        findViewById<Button>(R.id.button_sort_surname).setOnClickListener {
            fetchDonors("surname")
        }
        findViewById<Button>(R.id.button_sort_last_vaccination).setOnClickListener {
            fetchDonors("lastVaccination")
        }

    }

    private fun fetchDonors(sortBy: String) {
        database.orderByChild(sortBy).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val donorsList = ArrayList<Donor>()
                for (donorSnapshot in snapshot.children) {
                    val donor = donorSnapshot.getValue(Donor::class.java)
                    donor?.let { donorsList.add(it) }
                }

                when (sortBy) {
                    "name" -> donorsList.sortBy { it.name }
                    "bloodGroup" -> donorsList.sortBy { it.bloodGroup }
                    "surname" -> donorsList.sortBy { it.surname }
                    "lastVaccination" -> donorsList.sortByDescending { it.lastBloodDonation}
                }

                donorAdapter.updateData(donorsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity2, "Помилка завантаження донорів", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
