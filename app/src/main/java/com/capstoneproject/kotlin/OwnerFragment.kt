package com.capstoneproject.kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerFragment : Fragment() {

    private lateinit var restaurantAdapter: RestaurantAdapter
    private val restaurants = mutableListOf<Restaurant>()
    private val filteredRestaurants = mutableListOf<Restaurant>()
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_owner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add)
        val searchBar = view.findViewById<EditText>(R.id.et_search)

        restaurantAdapter = RestaurantAdapter(filteredRestaurants) { restaurant ->
            val intent = Intent(requireActivity(), RestaurantDetailActivity::class.java)
            intent.putExtra("restaurant", restaurant)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = restaurantAdapter

        fabAdd.setOnClickListener {
            val intent = Intent(requireActivity(), AddEditRestaurantActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_RESTAURANT)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRestaurants(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Retrieve user ID and load restaurants
        userId = firebaseAuth.currentUser?.uid
        loadRestaurants()
    }

    private fun loadRestaurants() {
        userId?.let { id ->
            db.collection("restaurants")
                .whereEqualTo("ownerId", id)
                .get()
                .addOnSuccessListener { result ->
                    restaurants.clear()
                    filteredRestaurants.clear()
                    for (document in result) {
                        val restaurant = document.toObject(Restaurant::class.java)
                        restaurants.add(restaurant)
                    }
                    filteredRestaurants.addAll(restaurants)
                    restaurantAdapter.updateData(filteredRestaurants)
                }
                .addOnFailureListener { exception ->
                    Log.e("OwnerFragment", "Error loading restaurants", exception)
                }
        }
    }

    private fun filterRestaurants(query: String) {
        val filteredList = restaurants.filter { restaurant ->
            restaurant.name.contains(query, ignoreCase = true) ||
                    restaurant.location.contains(query, ignoreCase = true)
        }
        filteredRestaurants.clear()
        filteredRestaurants.addAll(filteredList)
        restaurantAdapter.updateData(filteredRestaurants)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_RESTAURANT && resultCode == Activity.RESULT_OK) {
            loadRestaurants()
        }
    }

    companion object {
        private const val REQUEST_ADD_RESTAURANT = 1
    }
}
