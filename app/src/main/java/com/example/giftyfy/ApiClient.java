package com.example.giftyfy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    private static final String NAVER_CLIENT_ID = "j_F0nqLzUnSdhG6ckX7d";
    private static final String NAVER_CLIENT_SECRET = "74YUluIU1H";

    public static Retrofit get() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("X-Naver-Client-Id", NAVER_CLIENT_ID.trim())
                                .header("X-Naver-Client-Secret", NAVER_CLIENT_SECRET.trim())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://openapi.naver.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
