package com.example.electricitybill;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ViewBillActivity extends AppCompatActivity {

    TextView tvDetailMonth, tvDetailUnits, tvDetailTotal, tvDetailRebate, tvDetailFinal;
    TextView tvBreakdownTotal;
    LinearLayout layoutBlocks;
    Button buttonViewBack;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Details");
        }

        dbHelper       = new DatabaseHelper(this);
        tvDetailMonth  = findViewById(R.id.tvDetailMonth);
        tvDetailUnits  = findViewById(R.id.tvDetailUnits);
        tvDetailTotal  = findViewById(R.id.tvDetailTotal);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailFinal  = findViewById(R.id.tvDetailFinal);
        tvBreakdownTotal = findViewById(R.id.tvBreakdownTotal);
        layoutBlocks   = findViewById(R.id.layoutBlocks);
        buttonViewBack = findViewById(R.id.buttonViewBack);

        String month = getIntent().getStringExtra("month");
        if (month == null) { finish(); return; }

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null,
                DatabaseHelper.COL_MONTH + " = ?", new String[]{month},
                null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int units     = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNITS));
                double total  = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_CHARGES));
                double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE));
                double finalC = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FINAL_COST));
                double rebateAmt = total * rebate / 100.0;

                tvDetailMonth.setText(month);
                tvDetailUnits.setText(String.format(Locale.getDefault(), "%.1f kWh", (double) units));
                tvDetailTotal.setText(String.format(Locale.getDefault(), "RM %.2f", total));

                if ((int) rebate > 0) {
                    tvDetailRebate.setText(String.format(Locale.getDefault(),
                        "%d%%  (-RM %.2f)", (int) rebate, rebateAmt));
                    tvDetailRebate.setTextColor(getResources().getColor(R.color.aqua_accent, getTheme()));
                } else {
                    tvDetailRebate.setText("0%");
                    tvDetailRebate.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
                }

                tvDetailFinal.setText(String.format(Locale.getDefault(), "RM %.2f", finalC));
                buildBreakdown(units);
                tvBreakdownTotal.setText(String.format(Locale.getDefault(), "RM %.2f", total));
                cursor.close();
            } else {
                Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

        buttonViewBack.setOnClickListener(v -> finish());
    }

    private void buildBreakdown(int units) {
        layoutBlocks.removeAllViews();
        if (units <= 200) {
            addBlockRow("Block 1  (1 – 200 kWh)", units + " kWh × RM 0.218", units * 0.218);
        } else if (units <= 300) {
            addBlockRow("Block 1  (1 – 200 kWh)", "200 kWh × RM 0.218", 200 * 0.218);
            addBlockRow("Block 2  (201 – 300 kWh)", (units - 200) + " kWh × RM 0.334", (units - 200) * 0.334);
        } else if (units <= 600) {
            addBlockRow("Block 1  (1 – 200 kWh)", "200 kWh × RM 0.218", 200 * 0.218);
            addBlockRow("Block 2  (201 – 300 kWh)", "100 kWh × RM 0.334", 100 * 0.334);
            addBlockRow("Block 3  (301 – 600 kWh)", (units - 300) + " kWh × RM 0.516", (units - 300) * 0.516);
        } else {
            addBlockRow("Block 1  (1 – 200 kWh)", "200 kWh × RM 0.218", 200 * 0.218);
            addBlockRow("Block 2  (201 – 300 kWh)", "100 kWh × RM 0.334", 100 * 0.334);
            addBlockRow("Block 3  (301 – 600 kWh)", "300 kWh × RM 0.516", 300 * 0.516);
            addBlockRow("Block 4  (601 – 1000 kWh)", (units - 600) + " kWh × RM 0.546", (units - 600) * 0.546);
        }
    }

    private void addBlockRow(String label, String calc, double amount) {
        float dp = getResources().getDisplayMetrics().density;
        int pad = (int)(8 * dp);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(0xFFE0F2FE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int)(6 * dp);
        box.setLayoutParams(lp);
        box.setPadding(pad, pad, pad, pad);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(0xFF0284C7);
        tvLabel.setTextSize(11f);
        tvLabel.setPadding(0, 0, 0, 2);
        box.addView(tvLabel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvCalc = new TextView(this);
        tvCalc.setText(calc);
        tvCalc.setTextColor(0xFF0369A1);
        tvCalc.setTextSize(12f);
        LinearLayout.LayoutParams calcLp = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvCalc.setLayoutParams(calcLp);
        row.addView(tvCalc);

        TextView tvAmt = new TextView(this);
        tvAmt.setText(String.format(Locale.getDefault(), "RM %.2f", amount));
        tvAmt.setTextColor(0xFF0369A1);
        tvAmt.setTextSize(12f);
        tvAmt.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        row.addView(tvAmt);

        box.addView(row);
        layoutBlocks.addView(box);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
