package com.noobshubham.gostore.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.noobshubham.gostore.database.entity.GroceryEntities

@Dao
interface GroceryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryEntities)

    @Delete
    suspend fun delete(item: GroceryEntities)

    @Query("SELECT * FROM grocery_data")
    fun getAllGroceryItems(): LiveData<List<GroceryEntities>>
}







