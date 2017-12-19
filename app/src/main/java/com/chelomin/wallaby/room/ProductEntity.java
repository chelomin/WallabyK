package com.chelomin.wallaby.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.chelomin.wallaby.model.ProductDto;

/**
 * Created by huge on 12/10/17.
 */

// Note: in real-life small projects I'd use the same class for the both DTO and Storage models
// But as it's usually considered as bad practice... here we have almost exact clone of ProductListDto
@Entity(tableName = "products")
public class ProductEntity {

    public ProductEntity() {

    }

    public ProductEntity(final int index, final ProductDto productDto) {
        this.indx = index;
        this.productId = productDto.productId;
        this.productName = productDto.productName;
        this.shortDescription = productDto.shortDescription;
        this.longDescription = productDto.longDescription;
        this.price = productDto.price;
        this.productImage = productDto.productImage;
        this.reviewCount = productDto.reviewCount;
        this.reviewRating = productDto.reviewRating;
        this.inStock = productDto.inStock;
    }

    // Note: could make sense to use Long here...
    @PrimaryKey
    private Integer indx;

    @ColumnInfo(name = "product_id")
    private String productId;

    @ColumnInfo(name = "product_name")
    private String productName;

    @ColumnInfo(name = "short_desc")
    private String shortDescription;

    @ColumnInfo(name = "long_desc")
    private String longDescription;

    @ColumnInfo(name = "price")
    private String price;

    @ColumnInfo(name = "img")
    private String productImage;

    @ColumnInfo(name = "review_rating")
    private Double reviewRating;

    @ColumnInfo(name = "review_count")
    private Integer reviewCount;

    @ColumnInfo(name = "in_stock")
    private Boolean inStock;

    public Integer getIndx() {
        return indx;
    }

    public void setIndx(Integer index) {
        this.indx = index;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Double getReviewRating() {
        return reviewRating;
    }

    public void setReviewRating(Double reviewRating) {
        this.reviewRating = reviewRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
}
