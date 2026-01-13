package com.example.giftyfy;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.giftyfy.friend.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

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
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // -----------------------------
    // ✅ Products & Naver Data Sync
    // -----------------------------
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
                        listener.onError(task.getException());
                    }
                });
    }

    public void getProductsByIds(List<String> ids, OnProductsLoadedListener listener) {
        if (ids == null || ids.isEmpty()) {
            listener.onLoaded(new ArrayList<>());
            return;
        }
        db.collection(COLLECTION_PRODUCTS)
                .whereIn(FieldPath.documentId(), ids)
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
                        listener.onError(task.getException());
                    }
                });
    }

    public void toggleWishlist(String productId, boolean isAdd) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        if (isAdd) {
            db.collection(COLLECTION_USERS).document(user.getUid())
                    .update("wishlist", FieldValue.arrayUnion(productId));
        } else {
            db.collection(COLLECTION_USERS).document(user.getUid())
                    .update("wishlist", FieldValue.arrayRemove(productId));
        }
    }

    public void addReceivedGiftToUser(String targetUid, String productId) {
        if (targetUid == null || targetUid.isEmpty()) return;
        db.collection(COLLECTION_USERS).document(targetUid)
                .update("receivedGifts", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(aVoid -> Log.d("FirebaseManager", "Gift added to user: " + targetUid))
                .addOnFailureListener(e -> Log.e("FirebaseManager", "Error adding gift", e));
    }

    public void getUserByUid(String uid, OnUserLoadedListener listener) {
        db.collection(COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        listener.onLoaded(doc.getData());
                    } else {
                        listener.onError(new Exception("user not found: " + uid));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void getMyFriendRelation(String friendUid, OnRelationLoadedListener listener) {
        FirebaseUser me = auth.getCurrentUser();
        if (me == null) { listener.onError(new Exception("not logged in")); return; }
        db.collection(COLLECTION_USERS).document(me.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .get()
                .addOnSuccessListener(doc -> {
                    String relation = (doc != null && doc.exists()) ? doc.getString("relation") : "미설정";
                    listener.onLoaded(relation != null ? relation : "미설정");
                })
                .addOnFailureListener(listener::onError);
    }

    // ✅ [중요] 신규 유저 생성 및 기존 유저 정보 업데이트 통합
    public void saveMyProfile(String userId, String name, String birthday, List<String> interests) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("name", name);
        profile.put("birthday", birthday);
        profile.put("interests", interests);

        // ✅ set(..., SetOptions.merge())를 사용하여 문서가 없으면 생성, 있으면 합침
        // 신규 가입 시 wishlist와 receivedGifts가 없으면 빈 리스트로 초기화되도록 보강할 수 있습니다.
        db.collection(COLLECTION_USERS).document(user.getUid())
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // 신규 가입 시에만 필요한 필드들을 안전하게 추가 (이미 있으면 유지됨)
                    initNewUserFields(user.getUid());
                })
                .addOnFailureListener(e -> Log.e("FirebaseManager", "Error saving profile", e));
    }

    private void initNewUserFields(String uid) {
        db.collection(COLLECTION_USERS).document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, Object> updates = new HashMap<>();
                if (!doc.contains("wishlist")) updates.put("wishlist", new ArrayList<String>());
                if (!doc.contains("receivedGifts")) updates.put("receivedGifts", new ArrayList<String>());
                
                if (!updates.isEmpty()) {
                    db.collection(COLLECTION_USERS).document(uid).update(updates);
                }
            }
        });
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

    public void fetchAllUsersAsFriends(OnFriendsLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) { listener.onError(new Exception("not logged in")); return; }
        db.collection(COLLECTION_USERS).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { listener.onError(task.getException()); return; }
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
                                    if (f.getId().equals(relDoc.getId())) {
                                        String r = relDoc.getString("relation");
                                        if (r != null) f.setRelation(r);
                                    }
                                }
                            }
                        }
                        listener.onLoaded(friends);
                    });
        });
    }

    public void updateFriendRelation(String friendUid, String relation) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("relation", relation);
        db.collection(COLLECTION_USERS).document(user.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .set(data);
    }

    public void getFriendContext(String friendUid, OnFriendContextLoadedListener listener) {
        if (auth.getCurrentUser() == null) { listener.onError(new Exception("로그인 정보 없음")); return; }
        String myUid = auth.getCurrentUser().getUid();
        db.collection("users").document(myUid).collection("myFriends").document(friendUid).get()
                .addOnSuccessListener(relSnap -> {
                    String r = (relSnap != null && relSnap.exists()) ? relSnap.getString("relation") : "미설정";
                    db.collection("users").document(friendUid).get().addOnSuccessListener(userSnap -> {
                        ArrayList<String> ints = new ArrayList<>();
                        if (userSnap != null && userSnap.exists()) {
                            List<String> list = (List<String>) userSnap.get("interests");
                            if (list != null) ints.addAll(list);
                        }
                        listener.onLoaded(r != null ? r : "미설정", ints);
                    }).addOnFailureListener(listener::onError);
                }).addOnFailureListener(listener::onError);
    }

    public interface OnProfileUpdateListener { void onUpdate(Map<String, Object> data); }
    public interface OnFriendsLoadedListener { void onLoaded(List<Friend> friends); default void onError(Exception e) { if (e != null) e.printStackTrace(); } }
    public interface OnProductsLoadedListener { void onLoaded(List<Product> products); default void onError(Exception e) { if (e != null) e.printStackTrace(); } }
    public interface OnUserLoadedListener { void onLoaded(Map<String, Object> userData); default void onError(Exception e) { if (e != null) e.printStackTrace(); } }
    public interface OnRelationLoadedListener { void onLoaded(String relation); default void onError(Exception e) { if (e != null) e.printStackTrace(); } }
    public interface OnFriendContextLoadedListener { void onLoaded(String relation, ArrayList<String> interests); default void onError(Exception e) { if (e != null) e.printStackTrace(); } }
}