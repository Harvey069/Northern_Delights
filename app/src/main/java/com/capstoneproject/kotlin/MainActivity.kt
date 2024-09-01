package com.capstoneproject.kotlin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var signIn: Button
    private lateinit var signUp: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var loginLayout: LinearLayout
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        signIn = findViewById(R.id.sign_in)
        signUp = findViewById(R.id.sign_up)
        forgotPassword = findViewById(R.id.forgot_password)
        fragmentContainer = findViewById(R.id.fragment_container)
        loginLayout = findViewById(R.id.login_layout) // Ensure this ID is correctly defined in XML

        signUp.setOnClickListener {
            try {
                val intent = Intent(this, register::class.java) // Ensure RegisterActivity is the correct name
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening registration: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val emailBox: EditText = dialogView.findViewById(R.id.emailBox)

            builder.setView(dialogView)
            val dialog = builder.create()

            dialogView.findViewById<Button>(R.id.btnReset).setOnClickListener {
                val userEmail = emailBox.text.toString()

                if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Unable to send reset email", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
            dialog.show()
        }

        signIn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val role = document.getString("role")
                                        when (role) {
                                            "GastroOwner" -> {
                                                val intent = Intent(this, HomePage::class.java)
                                                intent.putExtra("USER_ROLE", "GastroOwner")
                                                startActivity(intent)
                                                finish()
                                            }
                                            "User" -> {
                                                val intent = Intent(this, HomePage::class.java)
                                                intent.putExtra("USER_ROLE", "User")
                                                startActivity(intent)
                                                finish()
                                            }
                                            else -> Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
