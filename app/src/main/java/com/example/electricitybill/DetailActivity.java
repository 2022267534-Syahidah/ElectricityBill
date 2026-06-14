package com.example.electricitybill;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DetailActivity extends AppCompatActivity {

    private TextView tvDetailMonth, tvDetailUnit, tvDetailRebate, tvDetailTotal, tvDetailFinal;
    private MaterialButton btnEdit, btnDelete, btnBackDetail;
    private DatabaseHelper dbHelper;
    private int billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bill Detail");
        }

        dbHelper = new DatabaseHelper(this);
        billId = getIntent().getIntExtra("id", -1);

        tvDetailMonth  = findViewById(R.id.tvDetailMonth);
        tvDetailUnit   = findViewById(R.id.tvDetailUnit);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailTotal  = findViewById(R.id.tvDetailTotal);
        tvDetailFinal  = findViewById(R.id.tvDetailFinal);
        btnEdit        = findViewById(R.id.btnEdit);
        btnDelete      = findViewById(R.id.btnDelete);
        btnBackDetail  = findViewById(R.id.btnBackDetail);

        loadData();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, EditActivity.class);
            intent.putExtra("id", billId);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (d, w) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_BILLS +
                        " WHERE " + DatabaseHelper.COL_ID + " = " + billId);
                    Toast.makeText(this, "✅ Record deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show()
        );

        btnBackDetail.setOnClickListener(v -> finish());
    }

    private void loadData() {
        if (billId == -1) { finish(); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_BILLS +
            " WHERE " + DatabaseHelper.COL_ID + " = " + billId, null);

        if (cursor.moveToFirst()) {
            tvDetailMonth.setText(cursor.getString(1));
            tvDetailUnit.setText(String.format("%.0f kWh", cursor.getDouble(2)));
            tvDetailRebate.setText(String.format("%.0f%%", cursor.getDouble(3)));
            tvDetailTotal.setText(String.format("RM %.2f", cursor.getDouble(4)));
            tvDetailFinal.setText(String.format("RM %.2f", cursor.getDouble(5)));
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
