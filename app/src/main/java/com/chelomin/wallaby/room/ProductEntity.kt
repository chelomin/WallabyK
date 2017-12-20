package com.chelomin.wallaby.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.chelomin.wallaby.model.ProductDto

/**
 * Created by huge on 12/10/17.
 */

// Note: in real-life small projects I'd use the same class for the both DTO and Storage models
// But as it's usually considered as bad practice... here we have almost exact clone of ProductListDto
@Entity(tableName = "products")
class ProductEntity {

    // Note: could make sense to use Long here...
    @PrimaryKey
    var indx: Int? = null

    @ColumnInfo(name = "product_id")
    var productId: String? = null

    @ColumnInfo(name = "product_name")
    var productName: String? = null

    @ColumnInfo(name = "short_desc")
    var shortDescription: String? = null

    @ColumnInfo(name = "long_desc")
    var longDescription: String? = null

    @ColumnInfo(name = "price")
    var price: String? = null

    @ColumnInfo(name = "img")
    var productImage: String? = null

    @ColumnInfo(name = "review_rating")
    var reviewRating: Double? = null

    @ColumnInfo(name = "review_count")
    var reviewCount: Int? = null

    @ColumnInfo(name = "in_stock")
    var inStock: Boolean? = null

    constructor()

    constructor(index: Int, productDto: ProductDto) {
        this.indx = index
        this.productId = productDto.productId
        this.productName = productDto.productName
        this.shortDescription = productDto.shortDescription
        this.longDescription = productDto.longDescription
        this.price = productDto.price
        this.productImage = productDto.productImage
        this.reviewCount = productDto.reviewCount
        this.reviewRating = productDto.reviewRating
        this.inStock = productDto.inStock
    }
}
