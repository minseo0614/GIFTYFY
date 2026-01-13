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
                        Log.e("FirebaseManager", "Error getting products", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    // -----------------------------
    // ✅ Users/Profile
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

    /**
     * ✅ 현재 로그인한 사용자가 friendUid를 어떤 관계로 설정했는지
     * users/{myUid}/myFriends/{friendUid} 문서의 relation 필드 읽기
     */
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

    // -----------------------------
    // (기존) 내 프로필 저장/리스닝
    // -----------------------------
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

    // (기존) 친구 목록 불러오기 + 관계 합치기
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

    public void updateFriendRelation(String friendUid, String relation) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("relation", relation);

        db.collection(COLLECTION_USERS).document(user.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .set(data);
    }

    // -----------------------------
    // Interfaces
    // -----------------------------
    public interface OnProfileUpdateListener { void onUpdate(Map<String, Object> data); }

    public interface OnFriendsLoadedListener {
        void onLoaded(List<Friend> friends);
        void onError(Exception e);
    }

    public interface OnProductsLoadedListener {
        void onLoaded(List<Product> products);
        void onError(Exception e);
    }

    public interface OnUserLoadedListener {
        void onLoaded(Map<String, Object> userData);
        void onError(Exception e);
    }

    public interface OnRelationLoadedListener {
        void onLoaded(String relation);
        void onError(Exception e);
    }
    // FirebaseManager.java 안에 추가

    public interface OnFriendContextLoaded {
        void onLoaded(String relation, ArrayList<String> interests);
    }

    public void getFriendContext(String friendUid, OnFriendContextLoaded listener) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) {
            listener.onLoaded("미설정", new ArrayList<>());
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(myUid)
                .collection("myFriends")
                .document(friendUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String relation = snapshot.getString("relation");
                    if (relation == null) relation = "미설정";

                    ArrayList<String> interests = new ArrayList<>();
                    Object obj = snapshot.get("interests");
                    if (obj instanceof List) {
                        for (Object o : (List<?>) obj) {
                            if (o != null) interests.add(String.valueOf(o));
                        }
                    }

                    listener.onLoaded(relation, interests);
                })
                .addOnFailureListener(e -> {
                    listener.onLoaded("미설정", new ArrayList<>());
                });
    }
    // ✅ 친구 추천용: relation + interests 가져오기
    public interface OnFriendContextLoadedListener {
        void onLoaded(String relation, ArrayList<String> interests);
        void onError(Exception e);
    }

    /**
     * friendUid(=users 컬렉션의 문서ID)를 기준으로
     * 1) 내 users/{myUid}/myFriends/{friendUid} 의 relation
     * 2) users/{friendUid} 의 interests
     * 를 합쳐서 반환
     */
    public void getFriendContext(String friendUid, OnFriendContextLoadedListener listener) {
        if (listener == null) return;

        if (friendUid == null || friendUid.isEmpty()) {
            listener.onError(new IllegalArgumentException("friendUid is null/empty"));
            return;
        }

        if (auth.getCurrentUser() == null) {
            listener.onError(new IllegalStateException("로그인 정보 없음"));
            return;
        }

        String myUid = auth.getCurrentUser().getUid();

        // 기본값
        final String[] relationBox = {"미설정"};
        final ArrayList<String> interestsBox = new ArrayList<>();

        // 1) relation: users/{myUid}/myFriends/{friendUid}
        db.collection("users")
                .document(myUid)
                .collection("myFriends")
                .document(friendUid)
                .get()
                .addOnSuccessListener(relSnap -> {
                    if (relSnap != null && relSnap.exists()) {
                        String r = relSnap.getString("relation");
                        if (r != null && !r.isEmpty()) relationBox[0] = r;
                    }

                    // 2) interests: users/{friendUid}
                    db.collection("users")
                            .document(friendUid)
                            .get()
                            .addOnSuccessListener(userSnap -> {
                                if (userSnap != null && userSnap.exists()) {
                                    List<String> interests = (List<String>) userSnap.get("interests");
                                    interestsBox.clear();
                                    if (interests != null) interestsBox.addAll(interests);
                                }

                                listener.onLoaded(relationBox[0], interestsBox);
                            })
                            .addOnFailureListener(e -> listener.onError(e));

                })
                .addOnFailureListener(e -> listener.onError(e));
    }


}