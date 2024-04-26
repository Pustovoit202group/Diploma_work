package com.example.diplomaigor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.*

class EditDonorActivity : AppCompatActivity() {

    private lateinit var surnameEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var lastBloodDonationEditText: EditText
    private lateinit var bloodGroupEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var updateButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var donorId: String
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_donor)

        surnameEditText = findViewById(R.id.surnameEditText)
        nameEditText = findViewById(R.id.nameEditText)
        lastBloodDonationEditText = findViewById(R.id.lastBloodDonationEditText)
        bloodGroupEditText = findViewById(R.id.bloodGroupEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        updateButton = findViewById(R.id.updateButton)

        database = FirebaseDatabase.getInstance().reference.child("Donors")

        donorId = intent.getStringExtra("DONOR_PHONE").toString()

        calendar = Calendar.getInstance()

        lastBloodDonationEditText.setOnClickListener {
            showDatePickerDialog()
        }

        updateButton.setOnClickListener {
            val surname = surnameEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val lastBloodDonation = lastBloodDonationEditText.text.toString().trim()
            val bloodGroup = bloodGroupEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()

            if (surname.isEmpty() || name.isEmpty() || lastBloodDonation.isEmpty() || bloodGroup.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
            } else {
                val donor = Donor(
                    phoneNumber = phoneNumber,
                    name = name,
                    surname = surname,
                    bloodGroup = bloodGroup,
                    lastBloodDonation = lastBloodDonation
                )
                updateDonor(donor)
            }
        }

        database.orderByChild("phoneNumber").equalTo(donorId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val donor = snapshot.getValue(Donor::class.java)
                    if (donor != null) {
                        surnameEditText.setText(donor.surname)
                        nameEditText.setText(donor.name)
                        lastBloodDonationEditText.setText(donor.lastBloodDonation)
                        bloodGroupEditText.setText(donor.bloodGroup)
                        phoneNumberEditText.setText(donor.phoneNumber)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@EditDonorActivity, "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy" // Формат дати
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        lastBloodDonationEditText.setText(sdf.format(calendar.time))
    }

    private fun updateDonor(donor: Donor) {
        database.orderByChild("phoneNumber").equalTo(donorId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    snapshot.ref.setValue(donor)
                        .addOnSuccessListener {
                            Toast.makeText(this@EditDonorActivity, "Донор оновлено", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@EditDonorActivity, "Помилка під час оновлення донора", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@EditDonorActivity, "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
