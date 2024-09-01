package com.capstoneproject.kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import android.content.pm.PackageManager
import java.io.InputStream

class AddEditRestaurantActivity : AppCompatActivity() {

    private val REQUEST_CODE_PICK_IMAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var ivImage: ImageView
    private lateinit var btnSave: Button

    private var imageUri: Uri? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_restaurant)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back arrow press
        }

        // Initialize UI components
        etName = findViewById(R.id.et_name)
        etLocation = findViewById(R.id.et_location)
        etDescription = findViewById(R.id.et_description)
        ivImage = findViewById(R.id.iv_image)
        btnSave = findViewById(R.id.btn_save)

        // Check the number of restaurants already added
        checkRestaurantLimit()

        // Set listeners
        ivImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE)
            }
        }

        btnSave.setOnClickListener {
            saveRestaurant()
        }
    }

    private fun checkRestaurantLimit() {
        currentUser?.let { user ->
            firestore.collection("restaurants")
                .whereEqualTo("user_id", user.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.size() >= 5) {
                        btnSave.isEnabled = false
                        btnSave.text = "Limit Reached"
                        Toast.makeText(this, "You can only add up to 5 restaurants", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to check restaurant limit", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                ivImage.setImageURI(imageUri)
            } ?: Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRestaurant() {
        val name = etName.text.toString()
        val location = etLocation.text.toString()
        val description = etDescription.text.toString()

        if (name.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageToFirebase(imageUri!!)
        } else {
            saveRestaurantToFirestore(null)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val fileRef = storage.child("restaurant_images/${System.currentTimeMillis()}.jpg")
        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveRestaurantToFirestore(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRestaurantToFirestore(imageUrl: String?) {
        val name = etName.text.toString()
        val location = etLocation.text.toString()
        val description = etDescription.text.toString()
        val userId = currentUser?.uid ?: ""

        val restaurant = hashMapOf(
            "name" to name,
            "location" to location,
            "description" to description,
            "image" to (imageUrl ?: ""),
            "user_id" to userId
        )

        firestore.collection("restaurants").add(restaurant)
            .addOnSuccessListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add restaurant", Toast.LENGTH_SHORT).show()
            }
    }
}
