package com.example.giftyfy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import okhttp3.ResponseBody;

public interface ProductApi {

    @GET("products?limit=200")
    Call<ProductResponse> getProducts();

    @GET("products?limit=200")
    Call<ResponseBody> getProductsRaw();

    @GET("products/{id}")
    Call<Product> getProductDetail(@Path("id") int id);
}