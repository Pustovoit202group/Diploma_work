package com.example.diplomaigor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var profile: Button
    private lateinit var main: Button
    private lateinit var donorAdapter: DonorAdapter
    private var userList: ArrayList<Donor> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        recyclerView = findViewById(R.id.recyclerViewDonors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchEditText = findViewById(R.id.editTextSearch)
        searchButton = findViewById(R.id.buttonSearch)
        profile = findViewById(R.id.button_profile)
        main = findViewById(R.id.button_all)

        database = FirebaseDatabase.getInstance().reference.child("Donors")

        donorAdapter = DonorAdapter(this, userList)
        recyclerView.adapter = donorAdapter

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchUsers(searchText)
            }
        }



        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        main.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun searchUsers(searchText: String) {
        database.orderByChild("surname").startAt(searchText).endAt("$searchText\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(Donor::class.java)
                        user?.let {
                            userList.add(it)
                        }
                    }
                    donorAdapter.notifyDataSetChanged()
                    recyclerView.visibility = if (userList.isEmpty()) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        }
    }
}
