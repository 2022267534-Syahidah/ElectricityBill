package com.example.electricitybill;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ViewDetailActivity extends AppCompatActivity {

    private TextView tvDetailMonth, tvDetailUnit, tvDetailTotal, tvDetailRebate, tvFinalCost;
    private com.google.android.material.button.MaterialButton btnEdit, btnDelete, btnBack;
    private DatabaseHelper dbHelper;
    private int recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Details");
        }

        dbHelper = new DatabaseHelper(this);
        recordId = getIntent().getIntExtra("id", -1);

        tvDetailMonth  = findViewById(R.id.tvDetailMonth);
        tvDetailUnit   = findViewById(R.id.tvDetailUnit);
        tvDetailTotal  = findViewById(R.id.tvDetailTotal);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvFinalCost    = findViewById(R.id.tvFinalCost);
        btnEdit        = findViewById(R.id.btnEdit);
        btnDelete      = findViewById(R.id.btnDelete);
        btnBack        = findViewById(R.id.btnBack);

        loadDetail();

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, UpdateActivity.class);
            i.putExtra("id", recordId);
            startActivity(i);
        });

        btnDelete.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (d, w) -> deleteRecord())
                .setNegativeButton("Cancel", null)
                .show()
        );

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetail();
    }

    private void loadDetail() {
        if (recordId == -1) { finish(); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_BILLS +
            " WHERE " + DatabaseHelper.COL_ID + "=?",
            new String[]{String.valueOf(recordId)});

        if (c.moveToFirst()) {
            String month     = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH));
            double units     = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));
            double total     = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_CHARGES));
            int rebatePct    = (int) c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE));
            double finalCost = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_FINAL_COST));

            tvDetailMonth.setText(month);
            tvDetailUnit.setText(String.format(Locale.getDefault(), "%.0f kWh", units));
            tvDetailTotal.setText(String.format(Locale.getDefault(), "RM %.2f", total));
            tvDetailRebate.setText(String.format(Locale.getDefault(), "-%d%% (RM %.2f)",
                rebatePct, total - finalCost));
            tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
        }
        c.close();
    }

    private void deleteRecord() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_BILLS,
            DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(recordId)});
        if (rows > 0) {
            Toast.makeText(this, "✅ Record deleted successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "❌ Error deleting record.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
