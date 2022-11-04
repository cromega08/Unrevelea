package com.example.unrevelea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SecretAdapter extends RecyclerView.Adapter<SecretAdapter.SecretViewHolder> {

    private final ArrayList<QueryDocumentSnapshot> secrets;

    public SecretAdapter(ArrayList<QueryDocumentSnapshot> arraySecrets) {
        secrets = arraySecrets;
    }

    @NonNull
    @Override
    public SecretViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int secretModel = R.layout.secret;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachedParent = false;

        View view = inflater.inflate(secretModel, parent, attachedParent);
        return new SecretViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SecretViewHolder holder, int position) {
        holder.bind(secrets.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return secrets.size();
    }

    static class SecretViewHolder extends RecyclerView.ViewHolder {
        TextView user, date, title, content;
        String dUser, dDate, dTitle, dContent;
        Map<String, Object> rawDate;

        public SecretViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.secret_user);
            date = view.findViewById(R.id.secret_date);
            title = view.findViewById(R.id.secret_title);
            content = view.findViewById(R.id.secret_content);
        }

        @SuppressLint("DefaultLocale")
        void bind(Map<String, Object> secret) {
            rawDate = (Map<String, Object>) secret.get("date");
            dUser = String.format("%07d", Integer.valueOf(Objects.requireNonNull(secret.get("user")).toString()));
            dDate = String.format("%1$02d/%2$02d/%3$d", (long) rawDate.get("dayOfMonth"), (long) rawDate.get("monthValue"), (long) rawDate.get("year"));
            dTitle = Objects.requireNonNull(secret.get("title")).toString();
            dContent = Objects.requireNonNull(secret.get("content")).toString();

            user.setText(dUser);
            date.setText(dDate);
            title.setText(dTitle);
            content.setText(dContent);
        }
    }
}