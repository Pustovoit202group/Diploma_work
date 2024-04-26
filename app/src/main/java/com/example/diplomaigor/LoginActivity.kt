package com.example.diplomaigor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.diplomaigor.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSetIn.setOnClickListener{
            val email = binding.editTextName.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {task ->
                        if(task.isSuccessful){
                            Toast.makeText(this,"Авторизація успішна", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            Toast.makeText(this, "Невірні дані", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, "Введіть дані", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewCreateUser.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}