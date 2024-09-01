package com.capstoneproject.kotlin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class register : AppCompatActivity() {

    private lateinit var editTextFullName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextPhone: TextInputEditText
    private lateinit var signUp: Button
    private lateinit var signIn: TextView
    private lateinit var roleGroup: RadioGroup

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextFullName = findViewById(R.id.full_name)
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextPhone = findViewById(R.id.phone)
        signIn = findViewById(R.id.sign_in)
        signUp = findViewById(R.id.sign_up)
        roleGroup = findViewById(R.id.role_group)

        signIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUp.setOnClickListener {
            val fullName = editTextFullName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val phone = editTextPhone.text.toString()

            if (TextUtils.isEmpty(fullName)) {
                Toast.makeText(this, "Enter Full Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!TextUtils.isDigitsOnly(phone)) {
                Toast.makeText(this, "Phone Number should contain only numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoleId = roleGroup.checkedRadioButtonId
            val role = when (selectedRoleId) {
                R.id.radio_gastro_owner -> "GastroOwner"
                R.id.radio_user -> "User"
                else -> {
                    Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid

                        val user = hashMapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "phone" to phone,
                            "role" to role
                        )

                        userId?.let {
                            db.collection("users")
                                .document(it)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("userRole", role)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
