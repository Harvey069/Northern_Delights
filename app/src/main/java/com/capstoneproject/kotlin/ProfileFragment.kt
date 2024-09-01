package com.capstoneproject.kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var uploadIcon: ImageView
    private lateinit var fullNameText: TextView
    private lateinit var phoneText: TextView
    private lateinit var emailText: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var storageReference: StorageReference

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        uploadIcon = view.findViewById(R.id.upload_icon)
        fullNameText = view.findViewById(R.id.full_name_text)
        phoneText = view.findViewById(R.id.phone_text)
        emailText = view.findViewById(R.id.email_text)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        currentUser = firebaseAuth.currentUser!!

        loadUserProfile()

        uploadIcon.setOnClickListener {
            openFileChooser()
        }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                profileImage.setImageBitmap(bitmap)
                uploadImageToFirebase(imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val fileRef: StorageReference = storageReference.child("users/${currentUser.uid}/profile.jpg")
        fileRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the download URL to Firestore
                    saveImageUrlToFirestore(uri.toString())
                    Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userRef = db.collection("users").document(currentUser.uid)
        val data = hashMapOf("profileImageUrl" to imageUrl)

        userRef.update(data as Map<String, Any>)
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to save image URL", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        currentUser.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fullName = documentSnapshot.getString("fullName")
                        val phone = documentSnapshot.getString("phone")
                        val email = user.email

                        fullNameText.text = fullName
                        phoneText.text = phone
                        emailText.text = email

                        // Load profile image using Picasso
                        val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                        if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                            Picasso.get().load(profileImageUrl).placeholder(R.drawable.nav_home).into(profileImage)
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle error
                    Toast.makeText(activity, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
