


package com.capstoneproject.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantDetailActivity : AppCompatActivity() {

    private val REQUEST_CODE_ADD_MENU_ITEM = 1
    private lateinit var btnAddMenuItem: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapter
    private val menuItemList = mutableListOf<MenuItem>()

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var restaurantId: String? = null  // Initialize with the restaurant ID passed from the previous activity
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        // Retrieve restaurant ID from the Intent
        restaurantId = intent.getStringExtra("RESTAURANT_ID")
        userId = firebaseAuth.currentUser?.uid

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed() // Handle back arrow press
        }

        btnAddMenuItem = findViewById(R.id.btn_add_menu_item)
        recyclerView = findViewById(R.id.rv_menu_items)
        menuItemAdapter = MenuItemAdapter(menuItemList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = menuItemAdapter

        // Make the Add Menu Item button visible for all users
        btnAddMenuItem.visibility = View.VISIBLE

        btnAddMenuItem.setOnClickListener {
            val intent = Intent(this, AddMenuItemActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_MENU_ITEM)
        }

        // Load menu items from Firebase
        loadMenuItems()
    }

    private fun loadMenuItems() {
        if (restaurantId != null) {
            firestore.collection("restaurants")
                .document(restaurantId!!)
                .collection("menu_items")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    menuItemList.clear()
                    for (snapshot in snapshots!!) {
                        val menuItem = snapshot.toObject(MenuItem::class.java)
                        menuItemList.add(menuItem)
                    }
                    menuItemAdapter.notifyDataSetChanged()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_MENU_ITEM && resultCode == RESULT_OK) {
            loadMenuItems()  // Reload menu items to show the new one
        }
    }
}
