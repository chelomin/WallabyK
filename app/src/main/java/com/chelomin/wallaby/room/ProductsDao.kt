package com.chelomin.wallaby.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

import io.reactivex.Single

/**
 * Created by huge on 12/10/17.
 */

@Dao
interface ProductsDao {
    @get:Query("SELECT * FROM products")
    val all: List<ProductEntity>

    @Query("SELECT * FROM products WHERE indx = (:index) LIMIT 1")
    fun loadByIndex(index: Int): Single<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<ProductEntity>)

    @Delete
    fun delete(pro: ProductEntity)
}
