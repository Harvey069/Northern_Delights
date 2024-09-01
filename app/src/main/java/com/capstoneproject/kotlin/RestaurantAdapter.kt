package com.capstoneproject.kotlin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantAdapter(
    private var restaurants: List<Restaurant>,
    private val onClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.name.text = restaurant.name
        holder.location.text = restaurant.location

        // Use Glide to load the image
        Glide.with(holder.itemView.context)
            .load(restaurant.imageUrl)
            .placeholder(R.drawable.baseline_image_24) // optional placeholder
            .into(holder.image)

        // Set up RatingBar and button click listener
        holder.ratingBar.rating = restaurant.averageRating ?: 0.0f
        holder.userCount.text = "Users: ${restaurant.userCount ?: 0}"
        holder.avgRating.text = "Avg Rating: ${restaurant.averageRating ?: 0.0f}"

        holder.submitButton.setOnClickListener {
            val rating = holder.ratingBar.rating
            submitRating(restaurant.id, rating, holder.userCount, holder.avgRating, holder.itemView)
        }

        holder.itemView.setOnClickListener { onClick(restaurant) }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateData(newRestaurants: List<Restaurant>) {
        this.restaurants = newRestaurants
        notifyDataSetChanged()
    }

    private fun submitRating(
        restaurantId: String,
        rating: Float,
        userCountTextView: TextView,
        avgRatingTextView: TextView,
        itemView: View
    ) {
        val db = FirebaseFirestore.getInstance()
        val restaurantRef = db.collection("restaurants").document(restaurantId)

        db.runTransaction { transaction ->
            val restaurantSnapshot = transaction.get(restaurantRef)
            if (restaurantSnapshot.exists()) {
                val currentUserCount = restaurantSnapshot.getLong("userCount") ?: 0
                val currentTotalRating = restaurantSnapshot.getDouble("totalRating") ?: 0.0

                val newUserCount = currentUserCount + 1
                val newTotalRating = currentTotalRating + rating
                val newAvgRating = newTotalRating / newUserCount

                transaction.update(restaurantRef, "userCount", newUserCount)
                transaction.update(restaurantRef, "totalRating", newTotalRating)
                transaction.update(restaurantRef, "averageRating", newAvgRating)

                userCountTextView.text = "Users: $newUserCount"
                avgRatingTextView.text = "Avg Rating: $newAvgRating"
            }
        }.addOnSuccessListener {
            Toast.makeText(itemView.context, "Rating submitted successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.e("RestaurantAdapter", "Failed to submit rating", e)
            Toast.makeText(itemView.context, "Failed to submit rating", Toast.LENGTH_SHORT).show()
        }
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val location: TextView = itemView.findViewById(R.id.tv_location)
        val image: ImageView = itemView.findViewById(R.id.iv_restaurant)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val userCount: TextView = itemView.findViewById(R.id.tv_user_count)
        val avgRating: TextView = itemView.findViewById(R.id.tv_avg_rating)
        val submitButton: Button = itemView.findViewById(R.id.btn_submit_rating)
    }
}
