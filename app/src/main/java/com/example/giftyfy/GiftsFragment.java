package com.example.giftyfy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GiftsFragment extends Fragment {

    private final List<Product> products = new ArrayList<>();
    private ProductAdapter adapter;

    public GiftsFragment() {
        super(R.layout.fragment_gifts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_products);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // ✅ 지금 ProductAdapter가 (List<Product>) 생성자만 있는 상태라고 가정
        adapter = new ProductAdapter(products);
        rv.setAdapter(adapter);

        fetchProducts();
    }

    private void fetchProducts() {
        ProductApi api = ApiClient.get().create(ProductApi.class);

        api.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call,
                                   @NonNull Response<ProductResponse> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().products == null) {
                    Toast.makeText(requireContext(), "응답 이상: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                products.clear();
                products.addAll(response.body().products);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "API 실패: " + t.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}