
package com.capstoneproject.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MenuItemAdapter(private val menuItems: List<MenuItem>) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item_layout, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.dishName.text = menuItem.name
        holder.price.text = menuItem.price

        // Use Glide to load the image
        Glide.with(holder.itemView.context)
            .load(menuItem.imageUrl)
            .placeholder(R.drawable.baseline_image_24)
            .into(holder.dishImage)
    }

    override fun getItemCount(): Int = menuItems.size

    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dishName: TextView = itemView.findViewById(R.id.tv_dish_name)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val dishImage: ImageView = itemView.findViewById(R.id.iv_dish_image)
    }
}
