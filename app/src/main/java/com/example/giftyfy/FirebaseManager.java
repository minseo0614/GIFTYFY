package com.example.giftyfy;

import com.example.giftyfy.friend.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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

    // --- [내 프로필 관리] ---

    // 1. 내 취향 태그 및 정보 저장
    public void saveMyProfile(String name, String birthday, List<String> interests) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("birthday", birthday);
        profile.put("interests", interests);

        db.collection(COLLECTION_USERS).document(user.getUid())
                .set(profile);
    }

    // 2. 내 프로필 실시간 구독 (취향 태그 등)
    public void listenToMyProfile(OnProfileUpdateListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection(COLLECTION_USERS).document(user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && value.exists()) {
                        listener.onUpdate(value.getData());
                    }
                });
    }

    // --- [친구 목록 관리] ---

    // 3. 친구 추가 및 관계 태그 수정
    public void updateFriendRelation(String friendUid, String relation) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("friendUid", friendUid);
        data.put("relation", relation);

        db.collection(COLLECTION_USERS).document(user.getUid())
                .collection(SUB_COLLECTION_FRIENDS).document(friendUid)
                .set(data);
    }

    public interface OnProfileUpdateListener {
        void onUpdate(Map<String, Object> data);
    }
}
