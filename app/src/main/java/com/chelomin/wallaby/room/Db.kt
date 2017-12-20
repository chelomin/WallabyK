package com.chelomin.wallaby.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by huge on 12/10/17.
 */

@Database(entities = [(ProductEntity::class)], version = 1)
abstract class Db : RoomDatabase() {
    abstract fun productsDao(): ProductsDao
}