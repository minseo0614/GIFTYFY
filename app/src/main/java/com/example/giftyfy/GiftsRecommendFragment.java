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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiftsRecommendFragment extends Fragment {

    private static final String ARG_FRIEND_UID = "friendUid";
    private static final String ARG_FRIEND_NAME = "friendName";

    private String friendUid;
    private String friendName;

    private GiftsMixAdapter adapter;
    private TextView tvGiftHeader;
    private ImageButton btnBack;
    private EditText etSearch;
    private ImageButton btnClearSearch;

    private final List<Product> originalRecommendedList = new ArrayList<>();
    private final List<Product> originalAllProductsList = new ArrayList<>();
    
    private ListenerRegistration friendListener;

    public GiftsRecommendFragment() {
        super(R.layout.fragment_gifts); 
    }

    public static GiftsRecommendFragment newInstance(String friendUid, String friendName) {
        GiftsRecommendFragment f = new GiftsRecommendFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FRIEND_UID, friendUid);
        b.putString(ARG_FRIEND_NAME, friendName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            friendUid = args.getString(ARG_FRIEND_UID, null);
            friendName = args.getString(ARG_FRIEND_NAME, "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGiftHeader = view.findViewById(R.id.tvGiftHeader);
        btnBack = view.findViewById(R.id.btnBack);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        if (btnBack != null) {
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).backToFriends();
                }
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterResults(s.toString());
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

        RecyclerView rv = view.findViewById(R.id.rv_products);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter != null && adapter.getItemViewType(position) == GiftsMixAdapter.TYPE_HEADER) {
                    return 2;
                }
                return 1;
            }
        });
        rv.setLayoutManager(layoutManager);

        adapter = new GiftsMixAdapter();
        adapter.setTargetFriendUid(friendUid);
        rv.setAdapter(adapter);

        if (friendName != null && !friendName.isEmpty()) {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText(friendName + " 추천 선물");
        } else {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText("추천 선물");
        }

        startListeningToFriendData();
    }

    private void startListeningToFriendData() {
        if (friendUid == null || friendUid.isEmpty()) return;

        friendListener = FirebaseFirestore.getInstance()
                .collection("users").document(friendUid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot != null && snapshot.exists() && isAdded()) {
                        Map<String, Object> userData = snapshot.getData();
                        if (userData == null) return;

                        // 1. 받은 선물 리스트 업데이트 (즉시 회색 처리 반영)
                        List<String> receivedIds = (List<String>) userData.get("receivedGifts");
                        if (adapter != null) adapter.setReceivedGiftIds(receivedIds);

                        // 2. 추천 리스트 갱신 (관계 및 관심사 로드)
                        ArrayList<String> interests = new ArrayList<>();
                        Object rawInts = userData.get("interests");
                        if (rawInts instanceof List) interests.addAll((List<String>) rawInts);

                        FirebaseManager.getInstance().getMyFriendRelation(friendUid, new FirebaseManager.OnRelationLoadedListener() {
                            @Override
                            public void onLoaded(String relation) {
                                FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
                                    @Override
                                    public void onLoaded(List<Product> allProducts) {
                                        if (!isAdded()) return;
                                        originalAllProductsList.clear();
                                        if (allProducts != null) originalAllProductsList.addAll(allProducts);

                                        List<Product> top6 = Recommender.topN(allProducts, relation, interests, 6);
                                        originalRecommendedList.clear();
                                        if (top6 != null) originalRecommendedList.addAll(top6);

                                        adapter.setModeFriend(originalRecommendedList, originalAllProductsList);
                                    }
                                });
                            }
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendListener != null) friendListener.remove(); // 리스너 해제
    }

    private void hideKeyboard(View view) {
        if (view != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void filterResults(String query) {
        if (query.isEmpty()) {
            adapter.setModeFriend(originalRecommendedList, originalAllProductsList);
            return;
        }

        List<Product> filteredReco = new ArrayList<>();
        for (Product p : originalRecommendedList) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredReco.add(p);
            }
        }

        List<Product> filteredAll = new ArrayList<>();
        for (Product p : originalAllProductsList) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredAll.add(p);
            }
        }

        adapter.setModeFriend(filteredReco, filteredAll);
    }
}
