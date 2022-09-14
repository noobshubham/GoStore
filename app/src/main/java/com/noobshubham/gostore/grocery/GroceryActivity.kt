package com.noobshubham.gostore.grocery

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.noobshubham.gostore.R
import com.noobshubham.gostore.adapter.GroceryAdapter
import com.noobshubham.gostore.database.GroceryDatabase
import com.noobshubham.gostore.database.GroceryRepository
import com.noobshubham.gostore.database.entity.GroceryEntities
import com.noobshubham.gostore.databinding.ActivityGroceryBinding

class GroceryActivity : AppCompatActivity(), GroceryAdapter.GroceryItemClickInterface {

    private lateinit var binding: ActivityGroceryBinding
    private lateinit var list: List<GroceryEntities>
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var groceryViewModal: GroceryViewModal

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroceryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = listOf()
        groceryAdapter = GroceryAdapter(this, list, this)
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groceryAdapter
        }
        val groceryRepository = GroceryRepository(GroceryDatabase(this))
        val factory = GroceryViewModalFactory(groceryRepository)
        groceryViewModal = ViewModelProvider(this, factory)[GroceryViewModal::class.java]
        groceryViewModal.getAllGroceryItems().observe(this, Observer {
            groceryAdapter.list = it
            groceryAdapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                binding.tempImageView.visibility = View.GONE
                binding.tempTextView.visibility = View.GONE
            } else {
                binding.tempImageView.visibility = View.VISIBLE
                binding.tempTextView.visibility = View.VISIBLE
            }
        })
        binding.fab.setOnClickListener { openDialog() }
    }

    private fun openDialog() {
        val dialog = Dialog(this, R.style.NewDialog)
        dialog.setContentView(R.layout.grocery_add_dialog)

        val cancel: Button = dialog.findViewById(R.id.idBtnCancel)
        val add: Button = dialog.findViewById(R.id.idBtnAdd)
        val item: EditText = dialog.findViewById(R.id.idEdtItemName)
        val price: EditText = dialog.findViewById(R.id.idEdtItemPrice)
        val quantity: EditText = dialog.findViewById(R.id.idEdtItemQuantity)

        cancel.setOnClickListener { dialog.dismiss() }

        add.setOnClickListener {
            if (validateInput(item, quantity, price)) {
                addItemToDB(item.text.toString(), quantity.text.toString(), price.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addItemToDB(itemName: String, quantity: String, price: String) {
        val items = GroceryEntities(itemName, quantity.toInt(), price.toInt())
        groceryViewModal.insert(items)
        Toast.makeText(this, "Item Added!", Toast.LENGTH_SHORT).show()
        groceryAdapter.notifyDataSetChanged()
    }

    private fun validateInput(item: TextView, quantity: TextView, price: TextView): Boolean {
        if (item.text.isEmpty()) {
            item.error = "Name is empty."
            return false
        }
        if (quantity.text.isEmpty()) {
            quantity.error = "Quantity is empty."
            return false
        }
        if (price.text.isEmpty()) {
            price.error = "Price is empty."
            return false
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(groceryItems: GroceryEntities) {
        groceryViewModal.delete(groceryItems)
        groceryAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
    }
}