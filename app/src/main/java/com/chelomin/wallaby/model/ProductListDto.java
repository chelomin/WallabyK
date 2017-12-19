package com.chelomin.wallaby.model;

import java.util.List;

/**
 * Created by huge on 12/8/17.
 */

public class ProductListDto {
    public List<ProductDto> products;
    public Integer totalProducts;
    public Integer pageNumber;
    public Integer pageSize;
    public Integer status;
    public String kind;
    public String etag;
}
