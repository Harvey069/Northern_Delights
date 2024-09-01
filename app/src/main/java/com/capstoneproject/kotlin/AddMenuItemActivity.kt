package com.capstoneproject.kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import android.content.pm.PackageManager
import java.io.InputStream

class AddMenuItemActivity : AppCompatActivity() {

    private val REQUEST_CODE_PICK_IMAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private lateinit var etDishName: EditText
    private lateinit var etPrice: EditText
    private lateinit var ivDishImage: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnAddDish: Button

    private var imageBitmap: Bitmap? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu_item)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back arrow press
        }

        // Initialize UI components
        etDishName = findViewById(R.id.et_dish_name)
        etPrice = findViewById(R.id.et_price)
        ivDishImage = findViewById(R.id.iv_dish_image)
        btnPickImage = findViewById(R.id.btn_pick_image)
        btnAddDish = findViewById(R.id.btn_add_dish)

        // Set listeners
        btnPickImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE)
            }
        }

        btnAddDish.setOnClickListener {
            addDish()
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
            val imageUri = data?.data
            if (imageUri != null) {
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                    imageBitmap = BitmapFactory.decodeStream(inputStream)
                    ivDishImage.setImageBitmap(imageBitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addDish() {
        val dishName = etDishName.text.toString().trim()
        val price = etPrice.text.toString().trim()

        if (dishName.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dishData = hashMapOf(
            "name" to dishName,
            "price" to price,
            "image" to ""  // Default empty string if no image is provided
        )

        val imageRef = if (imageBitmap != null) {
            val imageRef = storage.child("dish_images/${System.currentTimeMillis()}.jpg")

            // Convert bitmap to byte array
            val baos = ByteArrayOutputStream()
            imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        dishData["image"] = uri.toString()
                        saveDishToFirestore(dishData)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveDishToFirestore(dishData)
        }
    }

    private fun saveDishToFirestore(dishData: Map<String, Any>) {
        firestore.collection("menu_items").add(dishData)
            .addOnSuccessListener {
                Toast.makeText(this, "Dish added successfully", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add dish", Toast.LENGTH_SHORT).show()
            }
    }
}