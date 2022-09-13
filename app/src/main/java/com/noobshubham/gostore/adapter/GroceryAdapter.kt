package com.noobshubham.gostore.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noobshubham.gostore.R
import com.noobshubham.gostore.database.entity.GroceryEntities

class GroceryAdapter(val context: Context,
    var list: List<GroceryEntities>,
    val groceryItemClickInterface: GroceryItemClickInterface
) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    inner class GroceryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val quantity: TextView = view.findViewById(R.id.quantity)
        val rate: TextView = view.findViewById(R.id.rate)
        val totalAmount: TextView = view.findViewById(R.id.total_amouont)
        val delete: ImageView = view.findViewById(R.id.delete)
    }

    interface GroceryItemClickInterface {
        fun onItemClick(groceryItems: GroceryEntities)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_item_view, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        holder.name.text = list[position].itemName
        holder.quantity.text = context.getString(R.string.quantity, list[position].itemQuantity)
        holder.rate.text = context.getString(R.string.rate, list[position].itemPrice)
        val itemTotal: Int = list[position].itemPrice * list[position].itemQuantity
        holder.totalAmount.text = context.getString(R.string.total_amount, itemTotal)
        holder.delete.setOnClickListener {
            groceryItemClickInterface.onItemClick(list[position])
        }
    }

    override fun getItemCount() = list.size
}



























