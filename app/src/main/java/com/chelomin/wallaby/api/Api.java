package com.chelomin.wallaby.api;

import com.chelomin.wallaby.model.ProductListDto;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by huge on 12/8/17.
 */

public interface Api {
    // Note: another option could be to just hard code an API KEY right in the path here as
    // it's highly unlikely that this app will use multiple keys in future
    @GET("walmartproducts/{apiKey}/{pageNumber}/{pageSize}")
    Observable<ProductListDto> getProducts(
            @Path("apiKey") String apiKey,
            @Path("pageNumber") Integer pageNumber,
            @Path("pageSize") Integer pageSize);
}
