package com.chelomin.wallaby.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by huge on 12/10/17.
 */

@Database(entities = {ProductEntity.class}, version = 1)
public abstract class Db extends RoomDatabase {
    public abstract ProductsDao productsDao();
}