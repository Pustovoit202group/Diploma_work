package com.example.diplomaigor

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddUserActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var lastBloodDonationEditText: EditText
    private lateinit var calendar: Calendar
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        checkPermission()

        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextSurname = findViewById<EditText>(R.id.editTextSurname)
        val editTextBloodGroup = findViewById<EditText>(R.id.editTextBloodGroup)
        lastBloodDonationEditText = findViewById(R.id.editTextLastBloodDonation)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        database = FirebaseDatabase.getInstance().reference.child("Donors")
        calendar = Calendar.getInstance()

        lastBloodDonationEditText.setOnClickListener {
            showDatePickerDialog()
        }

        buttonSave.setOnClickListener {
            val phoneNumber = editTextPhone.text.toString().trim()
            val name = editTextName.text.toString().trim()
            val surname = editTextSurname.text.toString().trim()
            val bloodGroup = editTextBloodGroup.text.toString().trim()
            val lastBloodDonation = lastBloodDonationEditText.text.toString().trim()

            if (phoneNumber.isNotEmpty() && name.isNotEmpty() && surname.isNotEmpty() && bloodGroup.isNotEmpty() && lastBloodDonation.isNotEmpty() && isValidBloodGroup(bloodGroup)) {
                saveData(phoneNumber, name, surname, bloodGroup, lastBloodDonation)
            }
        }
    }

    private fun isValidBloodGroup(bloodGroup: String): Boolean {
        val regex = "^(A|B|AB|O)[-+]$".toRegex()
        return regex.matches(bloodGroup)
    }

    private fun saveData(phoneNumber: String, name: String, surname: String, bloodGroup: String, lastBloodDonation: String) {
        val donorId = database.push().key
        if (donorId != null) {
            val donor = Donor(phoneNumber, name, surname, bloodGroup, lastBloodDonation)
            database.child(donorId).setValue(donor)
            scheduleSMS(donor)
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
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
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        lastBloodDonationEditText.setText(sdf.format(calendar.time))
    }

    private fun scheduleSMS(donor: Donor) {
        handler.postDelayed({
            sendSMS(donor.phoneNumber, getString(R.string.sms_message))
        }, TimeUnit.DAYS.toMillis(14))
    }

    private fun checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(applicationContext, "SMS відправлено", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Помилка відправки SMS", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}

data class Donor(
    val phoneNumber: String = "",
    val name: String = "",
    val surname: String = "",
    val bloodGroup: String = "",
    val lastBloodDonation: String = ""
)
