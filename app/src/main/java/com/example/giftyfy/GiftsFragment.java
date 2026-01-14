package com.example.giftyfy;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiftsFragment extends Fragment {

    private static final String ARG_FROM_FRIEND = "fromFriend";
    private static final String ARG_FRIEND_UID = "friendUid";
    private static final String ARG_FRIEND_NAME = "friendName";

    private boolean fromFriend = false;
    private String friendUid = "";
    private String friendName = "";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private TextView tvGiftHeader;
    private EditText etSearch;
    private ImageButton btnClearSearch; 

    private final List<Product> allProductsList = new ArrayList<>();
    private final List<String> friendInterests = new ArrayList<>();

    public GiftsFragment() {
        super(R.layout.fragment_gifts);
    }

    public static GiftsFragment newDefault() {
        GiftsFragment f = new GiftsFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_FROM_FRIEND, false);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGiftHeader = view.findViewById(R.id.tvGiftHeader);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch); 

        recyclerView = view.findViewById(R.id.rv_products);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            fromFriend = getArguments().getBoolean(ARG_FROM_FRIEND, false);
            friendUid = getArguments().getString(ARG_FRIEND_UID, "");
            friendName = getArguments().getString(ARG_FRIEND_NAME, "");
        }

        loadMyWishlist();

        if (!fromFriend) {
            tvGiftHeader.setVisibility(View.GONE);
            loadAllProducts();
        } else {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText(friendName + " 추천 선물");
            loadTop6ForFriend();
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString());
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText(""); 
                    hideKeyboard(etSearch);
                }
            });
        }
    }

    private void hideKeyboard(View view) {
        if (view != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void filterProducts(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProductsList) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.setItems(filtered);
    }

    private void loadMyWishlist() {
        FirebaseManager.getInstance().listenToMyProfile(data -> {
            if (data != null && isAdded()) {
                List<String> wishlist = (List<String>) data.get("wishlist");
                if (adapter != null) adapter.setWishlistIds(wishlist);
            }
        });
    }

    private void loadAllProducts() {
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                if (!isAdded()) return;
                allProductsList.clear();
                if (products != null) allProductsList.addAll(products);
                adapter.setItems(allProductsList);
            }

            @Override
            public void onError(Exception e) {
                Log.e("GiftsFragment", "loadAllProducts error", e);
                Toast.makeText(getContext(), "상품 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTop6ForFriend() {
        FirebaseManager.getInstance().getUserByUid(friendUid, new FirebaseManager.OnUserLoadedListener() {
            @Override
            public void onLoaded(Map<String, Object> userData) {
                friendInterests.clear();
                if (userData != null) {
                    Object raw = userData.get("interests");
                    if (raw instanceof List) {
                        friendInterests.addAll((List<String>) raw);
                    }
                }

                FirebaseManager.getInstance().getMyFriendRelation(friendUid, new FirebaseManager.OnRelationLoadedListener() {
                    @Override
                    public void onLoaded(String relation) {
                        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
                            @Override
                            public void onLoaded(List<Product> products) {
                                if (!isAdded()) return;
                                allProductsList.clear();
                                if (products != null) allProductsList.addAll(products);
                                
                                List<Product> top6 = Recommender.topN(allProductsList, relation, friendInterests, 6);
                                adapter.setItems(top6);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("GiftsFragment", "getAllProducts error", e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("GiftsFragment", "getMyFriendRelation error", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("GiftsFragment", "getUserByUid error", e);
            }
        });
    }
}
