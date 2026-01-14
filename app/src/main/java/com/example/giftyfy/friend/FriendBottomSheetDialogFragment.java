package com.example.giftyfy.friend;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.Product;
import com.example.giftyfy.R;
import com.example.giftyfy.Recommender;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FriendBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnGoGiftListener {
        void onGoGift(String friendUid, String friendName);
    }

    public interface OnRelationChangedListener {
        void onRelationChanged();
    }

    private OnGoGiftListener goGiftListener;
    private OnRelationChangedListener relationChangedListener;

    public void setOnGoGiftListener(OnGoGiftListener listener) { this.goGiftListener = listener; }
    public void setOnRelationChangedListener(OnRelationChangedListener listener) { this.relationChangedListener = listener; }

    private static final String ARG_FRIEND_UID = "friend_uid";

    public static FriendBottomSheetDialogFragment newInstance(String friendUid) {
        FriendBottomSheetDialogFragment f = new FriendBottomSheetDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FRIEND_UID, friendUid);
        f.setArguments(b);
        return f;
    }

    private TextView tvName, tvBirthday;
    private ImageView ivProfile;
    private ChipGroup groupRelation;
    private ChipGroup layoutInterestTags;
    private LinearLayout layoutWishlist, layoutRecommend;
    
    private String friendUid = "";
    private String friendName = "";
    private String friendRelation = "미설정";
    private List<String> friendInterests = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_friend, container, false);
        bindViews(v);
        
        if (getArguments() != null) friendUid = getArguments().getString(ARG_FRIEND_UID, "");
        
        loadFriendData();
        setupListeners(v);
        
        return v;
    }

    private void bindViews(View v) {
        ivProfile = v.findViewById(R.id.ivProfile);
        tvName = v.findViewById(R.id.tvName);
        tvBirthday = v.findViewById(R.id.tvBirthday);
        groupRelation = v.findViewById(R.id.groupRelation);
        layoutInterestTags = v.findViewById(R.id.layoutInterestTags);
        layoutWishlist = v.findViewById(R.id.layoutWishlist);
        layoutRecommend = v.findViewById(R.id.layoutRecommend);
    }

    private void setupListeners(View v) {
        View btnClose = v.findViewById(R.id.btnClose);
        if (btnClose != null) btnClose.setOnClickListener(view -> dismiss());
        
        MaterialButton btnGoGift = v.findViewById(R.id.btnGoGift);
        if (btnGoGift != null) {
            btnGoGift.setOnClickListener(view -> {
                if (goGiftListener != null) {
                    goGiftListener.onGoGift(friendUid, friendName);
                }
                dismiss();
            });
        }

        if (groupRelation != null) {
            groupRelation.setOnCheckedStateChangeListener((group, checkedIds) -> {
                String newRel = "미설정";
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chipFamily) newRel = "가족";
                    else if (checkedId == R.id.chipFriend) newRel = "친구";
                    else if (checkedId == R.id.chipLove) newRel = "연인";
                    else if (checkedId == R.id.chipWork) newRel = "동료";
                    else if (checkedId == R.id.chipAwkward) newRel = "어색";
                }
                
                friendRelation = newRel;
                updateRecommendations(); // ✅ 관계 변경 시 추천 목록 실시간 업데이트

                String myUid = FirebaseAuth.getInstance().getUid();
                if (myUid != null && !friendUid.isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("relation", newRel);
                    FirebaseFirestore.getInstance().collection("users").document(myUid)
                            .collection("myFriends").document(friendUid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                if (relationChangedListener != null) {
                                    relationChangedListener.onRelationChanged();
                                }
                            });
                }
            });
        }
    }

    private void loadFriendData() {
        if (friendUid.isEmpty()) return;
        String myUid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("users").document(friendUid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) return;
                    friendName = doc.getString("name");
                    String bday = doc.getString("birthday");
                    String pUrl = doc.getString("profileUrl");

                    if (tvName != null) tvName.setText(friendName != null ? friendName : "");
                    if (tvBirthday != null) tvBirthday.setText(bday != null ? bday : "");
                    if (ivProfile != null && pUrl != null && !pUrl.isEmpty()) {
                        Glide.with(this).load(pUrl).circleCrop().into(ivProfile);
                    }
                    
                    friendInterests = getListFromDoc(doc, "interests");
                    renderTags(friendInterests);
                    renderWishlist(getListFromDoc(doc, "wishlist"));
                    
                    // 전역 users doc 로드 후 관계 정보 가져오기
                    if (myUid != null) {
                        FirebaseFirestore.getInstance().collection("users").document(myUid)
                                .collection("myFriends").document(friendUid).get()
                                .addOnSuccessListener(relDoc -> {
                                    if (!isAdded()) return;
                                    friendRelation = relDoc.exists() ? relDoc.getString("relation") : "미설정";
                                    setRelationToggle(friendRelation);
                                    updateRecommendations(); // ✅ 모든 정보 로드 후 추천 실행
                                });
                    }
                });
    }

    private void updateRecommendations() {
        if (layoutRecommend == null || !isAdded()) return;
        
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> allProducts) {
                if (!isAdded()) return;
                // ✅ Recommender를 통해 이 친구에게 맞는 Top 6 계산
                List<Product> top6 = Recommender.topN(allProducts, friendRelation, friendInterests, 6);
                renderRecommendations(top6);
            }
            @Override
            public void onError(Exception e) {
                Log.e("FriendSheet", "Reco error", e);
            }
        });
    }

    private void renderRecommendations(List<Product> products) {
        if (layoutRecommend == null || getContext() == null) return;
        layoutRecommend.removeAllViews();
        if (products == null || products.isEmpty()) return;

        int w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getResources().getDisplayMetrics());
        int m = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        LayoutInflater inf = LayoutInflater.from(getContext());
        for (Product p : products) {
            View item = inf.inflate(R.layout.item_recommend_mini, layoutRecommend, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(m);
            item.setLayoutParams(lp);

            TextView name = item.findViewById(R.id.tvGiftName);
            TextView price = item.findViewById(R.id.tvGiftPrice);
            ImageView img = item.findViewById(R.id.ivGiftImage);

            if (name != null) name.setText(p.getTitle());
            if (price != null) price.setText(String.format(Locale.KOREA, "%,.0f원", p.getPrice()));
            if (img != null) Glide.with(this).load(p.getThumbnail()).into(img);

            layoutRecommend.addView(item);
        }
    }

    private void setRelationToggle(String relation) {
        if (groupRelation == null || relation == null) return;
        int id = -1;
        switch (relation) {
            case "가족": id = R.id.chipFamily; break;
            case "친구": id = R.id.chipFriend; break;
            case "연인": id = R.id.chipLove; break;
            case "동료": id = R.id.chipWork; break;
            case "어색": id = R.id.chipAwkward; break;
        }
        if (id != -1) groupRelation.check(id);
        else groupRelation.clearCheck();
    }

    private void renderTags(List<String> tags) {
        if (layoutInterestTags == null) return;
        layoutInterestTags.removeAllViews();
        if (tags == null) return;
        LayoutInflater inf = LayoutInflater.from(getContext());
        for (String t : tags) {
            if (t == null || t.isEmpty()) continue;
            View chipView = inf.inflate(R.layout.item_chip_tag, layoutInterestTags, false);
            TextView tv = chipView.findViewById(R.id.tvTag);
            if (tv != null) tv.setText(String.format("#%s", t));
            layoutInterestTags.addView(chipView);
        }
    }

    private void renderWishlist(List<String> ids) {
        if (layoutWishlist == null || getContext() == null || ids == null) return;
        layoutWishlist.removeAllViews();
        int w = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
        int m = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        for (String id : ids) {
            if (id == null || id.isEmpty()) continue;
            View item = LayoutInflater.from(getContext()).inflate(R.layout.item_wishlist_mini, layoutWishlist, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(m);
            item.setLayoutParams(lp);

            FirebaseFirestore.getInstance().collection("products").document(id).get().addOnSuccessListener(pdoc -> {
                if (!isAdded() || !pdoc.exists()) return;
                TextView n = item.findViewById(R.id.tvGiftName);
                TextView pr = item.findViewById(R.id.tvGiftPrice);
                ImageView i = item.findViewById(R.id.ivGiftImage);
                Double pVal = pdoc.getDouble("price");
                if (n != null) n.setText(pdoc.getString("title"));
                if (pr != null && pVal != null) pr.setText(String.format(Locale.KOREA, "%,.0f원", pVal));
                if (i != null) Glide.with(this).load(pdoc.getString("thumbnail")).into(i);
            });
            layoutWishlist.addView(item);
        }
    }

    private List<String> getListFromDoc(DocumentSnapshot doc, String field) {
        Object obj = doc.get(field);
        if (obj instanceof List<?>) {
            List<String> res = new ArrayList<>();
            for (Object i : (List<?>) obj) if (i instanceof String) res.add((String) i);
            return res;
        }
        return new ArrayList<>();
    }
}
