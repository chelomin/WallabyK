package com.chelomin.wallaby.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by huge on 12/10/17.
 */

@Dao
public interface ProductsDao {
    @Query("SELECT * FROM products")
    List<ProductEntity> getAll();

    @Query("SELECT * FROM products WHERE indx = (:index) LIMIT 1")
    Single<ProductEntity> loadByIndex(int index);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> products);

    @Delete
    void delete(ProductEntity pro);
}
