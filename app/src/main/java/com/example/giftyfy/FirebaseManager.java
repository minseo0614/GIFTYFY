package com.example.giftyfy;

import android.util.Log;
import com.example.giftyfy.friend.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    
    private final String COLLECTION_USERS = "users";
    private final String COLLECTION_PRODUCTS = "products";
    private final String SUB_COLLECTION_FRIENDS = "myFriends";

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void getAllProducts(OnProductsLoadedListener listener) {
        db.collection(COLLECTION_PRODUCTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Product p = doc.toObject(Product.class);
                            p.setId(doc.getId());
                            products.add(p);
                        }
                        listener.onLoaded(products);
                    } else {
                        Log.e("FirebaseManager", "Error getting products", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    // ✅ 성공/실패 로그 추가
    public void addSampleProduct(Product product) {
        db.collection(COLLECTION_PRODUCTS)
                .add(product)
                .addOnSuccessListener(doc -> Log.d("FirebaseManager", "Product added: " + product.getTitle()))
                .addOnFailureListener(e -> Log.e("FirebaseManager", "Error adding product", e));
    }

    // --- (기존 로직 유지) ---
    public void saveMyProfile(String userId, String name, String birthday, List<String> interests) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("name", name);
        profile.put("birthday", birthday);
        profile.put("interests", interests);
        db.collection(COLLECTION_USERS).document(user.getUid()).set(profile);
    }

    public void listenToMyProfile(OnProfileUpdateListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection(COLLECTION_USERS).document(user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && value.exists()) listener.onUpdate(value.getData());
                });
    }

    public void updateFriendRelation(String friendUid, String relation) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("relation", relation);
        db.collection(COLLECTION_USERS).document(user.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid).set(data);
    }

    public void fetchAllUsersAsFriends(OnFriendsLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        db.collection(COLLECTION_USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Friend> friends = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    if (doc.getId().equals(currentUser.getUid())) continue;
                    Friend f = doc.toObject(Friend.class);
                    f.setId(doc.getId());
                    friends.add(f);
                }
                db.collection(COLLECTION_USERS).document(currentUser.getUid())
                        .collection(SUB_COLLECTION_FRIENDS).get().addOnCompleteListener(relTask -> {
                            if (relTask.isSuccessful()) {
                                for (DocumentSnapshot relDoc : relTask.getResult()) {
                                    for (Friend f : friends) {
                                        if (f.getId().equals(relDoc.getId())) f.setRelation(relDoc.getString("relation"));
                                    }
                                }
                            }
                            listener.onLoaded(friends);
                        });
            }
        });
    }

    public interface OnProfileUpdateListener { void onUpdate(Map<String, Object> data); }
    public interface OnFriendsLoadedListener { void onLoaded(List<Friend> friends); }
    public interface OnProductsLoadedListener { void onLoaded(List<Product> products); void onError(Exception e); }
}
