package com.example.electricitybill;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ℹ️ About");
        }

        MaterialButton btnBack = findViewById(R.id.btnBackAbout);
        TextView tvUrl = findViewById(R.id.tvGithubUrl);

        // Make URL clickable
        tvUrl.setOnClickListener(v -> {
            String url = getString(R.string.about_github_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
