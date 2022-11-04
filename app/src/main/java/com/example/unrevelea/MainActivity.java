package com.example.unrevelea;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBInstance db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                this::onSignInResult
        );

        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();

        signInLauncher.launch(signInIntent);

        Resources resources = getResources();
        Context context = this.getApplicationContext();
        NotificationHandler notificationHandler = new NotificationHandler(resources, context);

        ArrayList<QueryDocumentSnapshot> arraySecrets = new ArrayList<>();
        RecyclerView secrets = findViewById(R.id.secrets);
        SecretAdapter secretAdapter = new SecretAdapter(arraySecrets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.secrets_swipe);
        db = new DBInstance(secretAdapter, arraySecrets, notificationHandler);

        secrets.setLayoutManager(layoutManager);
        secrets.setHasFixedSize(true);
        secrets.setAdapter(secretAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            db.updateSecrets();
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Log.i("onSignInResult Successful", Objects.requireNonNull(result.getIdpResponse()).toString());
        } else {Log.i("onSignInResult Failed", result.getResultCode().toString());}
    }

    public void displayCreate(View view) {
        RelativeLayout parent = (RelativeLayout) view.getParent();
        Context parentContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parentContext);

        View createSecretPanel = inflater.inflate(R.layout.create_secret, null, true);
        parent.addView(createSecretPanel);
    }

    public void postSecret(View view) {db.postSecret(view);}
}