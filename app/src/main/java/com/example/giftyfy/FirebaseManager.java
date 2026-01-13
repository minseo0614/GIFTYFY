package com.example.giftyfy;

import android.util.Log;

import androidx.annotation.NonNull;

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
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // -----------------------------
    // ✅ Products
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

    // -----------------------------
    // ✅ Users/Profile (기존 메서드들 복구)
    // -----------------------------
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
        if (me == null) {
            listener.onError(new Exception("not logged in"));
            return;
        }

        db.collection(COLLECTION_USERS).document(me.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .get()
                .addOnSuccessListener(doc -> {
                    String relation = "미설정";
                    if (doc != null && doc.exists()) {
                        String r = doc.getString("relation");
                        if (r != null && !r.isEmpty()) relation = r;
                    }
                    listener.onLoaded(relation);
                })
                .addOnFailureListener(listener::onError);
    }

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

    public void fetchAllUsersAsFriends(OnFriendsLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("not logged in"));
            return;
        }

        db.collection(COLLECTION_USERS).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                listener.onError(task.getException());
                return;
            }

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

    // ✅ FriendAdapter에서 사용하는 메서드 (복구 완료)
    public void updateFriendRelation(String friendUid, String relation) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("relation", relation);

        db.collection(COLLECTION_USERS).document(user.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .set(data);
    }

    // ✅ 팀원의 추천 기능용 Context 로드
    public void getFriendContext(String friendUid, OnFriendContextLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            listener.onError(new Exception("로그인 정보 없음"));
            return;
        }
        String myUid = auth.getCurrentUser().getUid();

        db.collection(COLLECTION_USERS).document(myUid)
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .get()
                .addOnSuccessListener(relSnap -> {
                    String relation = (relSnap != null && relSnap.exists()) ? relSnap.getString("relation") : "미설정";
                    if (relation == null) relation = "미설정";
                    
                    final String finalRelation = relation;
                    db.collection(COLLECTION_USERS).document(friendUid).get().addOnSuccessListener(userSnap -> {
                        ArrayList<String> interests = new ArrayList<>();
                        if (userSnap != null && userSnap.exists()) {
                            List<String> ints = (List<String>) userSnap.get("interests");
                            if (ints != null) interests.addAll(ints);
                        }
                        listener.onLoaded(finalRelation, interests);
                    }).addOnFailureListener(listener::onError);
                }).addOnFailureListener(listener::onError);
    }

    // -----------------------------
    // Interfaces (default 메서드로 기존 람다 호환성 유지)
    // -----------------------------
    public interface OnProfileUpdateListener { void onUpdate(Map<String, Object> data); }

    public interface OnFriendsLoadedListener {
        void onLoaded(List<Friend> friends);
        default void onError(Exception e) { if (e != null) e.printStackTrace(); }
    }

    public interface OnProductsLoadedListener {
        void onLoaded(List<Product> products);
        default void onError(Exception e) { if (e != null) e.printStackTrace(); }
    }

    public interface OnUserLoadedListener {
        void onLoaded(Map<String, Object> userData);
        default void onError(Exception e) { if (e != null) e.printStackTrace(); }
    }

    public interface OnRelationLoadedListener {
        void onLoaded(String relation);
        default void onError(Exception e) { if (e != null) e.printStackTrace(); }
    }

    public interface OnFriendContextLoadedListener {
        void onLoaded(String relation, ArrayList<String> interests);
        default void onError(Exception e) { if (e != null) e.printStackTrace(); }
    }
}