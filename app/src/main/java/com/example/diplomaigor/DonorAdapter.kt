package com.example.diplomaigor

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class DonorAdapter(private val context: Context, private val donors: MutableList<Donor>) : RecyclerView.Adapter<DonorAdapter.ViewHolder>() {

    private lateinit var database: DatabaseReference

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val surnameTextView: TextView = itemView.findViewById(R.id.SurnameTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.NameTextView)
        private val vectorImageView: ImageView = itemView.findViewById(R.id.vectorImageView)
        private val lastBloodDonationTextView: TextView = itemView.findViewById(R.id.LastBloodDonation)
        private val bloodGroupTextView: TextView = itemView.findViewById(R.id.BloodGroupTextView)
        private lateinit var phoneNumber: String

        init {
            vectorImageView.setOnClickListener(this)
        }

        fun bind(donor: Donor) {
            surnameTextView.text = donor.surname
            nameTextView.text = donor.name
            lastBloodDonationTextView.text = donor.lastBloodDonation
            bloodGroupTextView.text = donor.bloodGroup
            phoneNumber = donor.phoneNumber
        }

        override fun onClick(v: View?) {
            if (v == vectorImageView) {
                sendSMS(phoneNumber, context.getString(R.string.sms_message))
            } else if (v == itemView) {
                val donor = donors[adapterPosition]
                val intent = Intent(context, EditDonorActivity::class.java)
                intent.putExtra("DONOR_PHONE", donor.phoneNumber)
                context.startActivity(intent)
            }
        }

        private fun sendSMS(phoneNumber: String, message: String) {
            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(context, "SMS відправлено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка відправки SMS", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.donor_list_item, parent, false)
        database = FirebaseDatabase.getInstance().reference.child("Donors")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donor = donors[position]
        holder.bind(donor)
        holder.itemView.findViewById<ImageView>(R.id.vectorImageView2).setOnClickListener {
            removeItem(holder.adapterPosition)
        }
        holder.itemView.setOnClickListener(holder)
    }

    override fun getItemCount(): Int {
        return donors.size
    }

    fun updateData(newDonors: List<Donor>) {
        donors.clear()
        donors.addAll(newDonors)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val donorPhoneNumber = donors[position].phoneNumber
            database.orderByChild("phoneNumber").equalTo(donorPhoneNumber).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Донор видалено", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Помилка під час видалення донора", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
                }
            })
            donors.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
