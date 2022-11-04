package com.example.unrevelea;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DBInstance {

    private final FirebaseFirestore db;
    private final ArrayList<QueryDocumentSnapshot> arraySecrets;
    private final ArrayList<String> idSecrets;
    private final SecretAdapter secretAdapter;
    private final NotificationHandler notifications;
    private final String userRawId;
    private final long USER_ID;

    DBInstance(SecretAdapter adapter, ArrayList<QueryDocumentSnapshot> secrets, NotificationHandler notificationHandler) {
        db = FirebaseFirestore.getInstance();
        userRawId = FirebaseAuth.getInstance().getUid();
        USER_ID = handleUserId();
        idSecrets = new ArrayList<>();
        arraySecrets = secrets;
        secretAdapter = adapter;
        notifications = notificationHandler;

        updateSecrets();
    }

    private long handleUserId() {
        boolean userExist = checkUserInDB(userRawId);
        long newId;

        Log.i("handleUserId", String.valueOf(userExist));

        if (userExist) {
            newId = getUserId(userRawId);
        }
        else {
            long lastId = getLastId(); newId = ++lastId;

            setLastId(newId);
            createNewUser(userRawId, newId);
        }

        return newId;
    }

    public void updateSecrets() {
        db.waitForPendingWrites();
        db.collection("secrets")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (idSecrets.contains(document.getId())) {continue;}
                            arraySecrets.add(0, document);
                            idSecrets.add(document.getId());
                        }
                        secretAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(task -> Log.e("getSecrets Failed", task.getMessage()));
    }

    public void postSecret(View view) {
        RelativeLayout parent = (RelativeLayout) view.getParent(),
                grandParent = (RelativeLayout) parent.getParent();
        EditText title = parent.findViewById(R.id.create_title),
                content = parent.findViewById(R.id.create_content);
        Map<String, Object> newData = new HashMap<>();

        parent.setEnabled(false);

        newData.put("user", USER_ID);
        newData.put("date", LocalDateTime.now());
        newData.put("title", title.getText().toString());
        newData.put("content", content.getText().toString());

        db.waitForPendingWrites();
        db.collection("secrets")
                .add(newData)
                .addOnSuccessListener(documentReference -> notifications.createPostNotification(Objects.requireNonNull(newData.get("title")).toString()))
                .addOnFailureListener(e -> Log.w("createSecret Failed", "Error adding Document", e));

        grandParent.removeView(parent);

        updateSecrets();
    }

    private boolean checkUserInDB(String userId) {
        final boolean[] result = new boolean[1];
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    result[0] = task.getResult().exists();
                    Log.i("checkUserInDB 1", String.valueOf(result[0]));
                })
                .addOnFailureListener(e -> {
                    Log.e("checkUserInDB Failed", "Error checking user", e);
                    result[0] = false;
                });
        Log.i("checkUserInDB 2", String.valueOf(result[0]));
        return result[0];
    }

    private long getUserId(String user) {
        final long[] userId = new long[1];

        db.collection("users")
                .document(user)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        userId[0] = (int) Objects.requireNonNull(data).get("id");
                    }
                })
                .addOnFailureListener(e -> Log.e("getUserId Failed", "Error getting the user Id existed in DB", e));

        return userId[0];
    }

    @SuppressLint("DefaultLocale")
    private void createNewUser(String userRawId, long newId) {
        Map<String, Object> newUserData = new HashMap<>();
        newUserData.put("id", newId);

        db.waitForPendingWrites();
        db.collection("users")
                .document(userRawId)
                .set(newUserData)
                .addOnSuccessListener(unused -> notifications.createNewUserNotification(newId))
                .addOnFailureListener(e -> {
                    notifications.createNUErrorNotification();
                    Log.e("createNewUser Failed", "Error while creating new user", e);
                });
    }

    private long getLastId() {
        final long[] test = {0};

        db.waitForPendingWrites();
        db.collection("users")
                .document("last_id")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        test[0] = (long) Objects.requireNonNull(data).get("id");
                    }
                })
                .addOnFailureListener(e -> Log.e("getLastId Failed", "Error getting last_id document", e));

        return test[0];
    }

    private void setLastId(long newLastId) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("id", newLastId);

        db.collection("users")
                .document("last_id")
                .update(updatedData)
                .addOnSuccessListener(unused -> Log.i("setLastId", "New last_id was set"))
                .addOnFailureListener(e -> Log.e("setLastId Failed", "Couldn't connect or update de DB"));
    }
}
