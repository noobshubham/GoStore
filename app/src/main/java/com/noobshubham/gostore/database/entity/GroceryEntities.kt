package com.noobshubham.gostore.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_data")
data class GroceryEntities(
    @ColumnInfo(name = "Name")
    var itemName: String,

    @ColumnInfo(name = "Quantity")
    var itemQuantity: Int,

    @ColumnInfo(name = "Price")
    var itemPrice: Int,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}