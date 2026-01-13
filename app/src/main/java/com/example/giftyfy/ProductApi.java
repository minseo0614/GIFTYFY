package com.example.giftyfy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductApi {

    // 네이버 쇼핑 검색 API
    @GET("v1/search/shop.json")
    Call<NaverShoppingResponse> searchShop(
            @Query("query") String query,
            @Query("display") int display,
            @Query("start") int start,
            @Query("sort") String sort
    );
}
