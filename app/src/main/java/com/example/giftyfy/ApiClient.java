package com.example.giftyfy;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit get() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://dummyjson.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}