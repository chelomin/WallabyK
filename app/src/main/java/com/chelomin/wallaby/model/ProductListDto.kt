package com.chelomin.wallaby.model

/**
 * Created by huge on 12/8/17.
 */

class ProductListDto {
    var products: List<ProductDto>? = null
    var totalProducts: Int? = null
    var pageNumber: Int? = null
    var pageSize: Int? = null
    var status: Int? = null
    var kind: String? = null
    var etag: String? = null
}
