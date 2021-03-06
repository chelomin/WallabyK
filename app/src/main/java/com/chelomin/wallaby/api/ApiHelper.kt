package com.chelomin.wallaby.api

import com.chelomin.wallaby.config.C
import com.chelomin.wallaby.model.ProductListDto

import io.reactivex.Observable

/**
 * Helper wrapper around the Api interface.
 * Note: this class is not strictly necessary as the app is small. But if it grows, it may quickly
 * become ugly to explicitly state API KEY or page size in every call to Api
 *
 * Created by huge on 12/8/17.
 */

class ApiHelper(private val api: Api) {

    /**
     * returns a product list page using default API KEY and page size
     * @param pageNumber
     * @return
     */
    fun getProducts(pageNumber: Int?): Observable<ProductListDto> {
        return api.getProducts(C.API_KEY, pageNumber, C.PRODUCTS_PER_PAGE)
    }

    /**
     * returns product page for the given index of product
     * @param productIndex 0 - based index of the product
     * @return
     */
    fun getProductsPageForProduct(productIndex: Int?): Observable<ProductListDto> {
        return getProducts(productIndex!! / C.PRODUCTS_PER_PAGE + 1)
    }

}
