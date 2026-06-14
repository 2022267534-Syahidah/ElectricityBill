package com.example.electricitybill;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateBillActivity extends AppCompatActivity {

    TextView textViewUpdateMonth, textViewUpdateRebateVal, tvUpdateError;
    EditText editTextUpdateUnits;
    SeekBar seekBarUpdateRebate;
    Button buttonUpdate, buttonUpdateBack;
    DatabaseHelper dbHelper;
    String currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bill);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Update Bill");
        }

        dbHelper                = new DatabaseHelper(this);
        textViewUpdateMonth     = findViewById(R.id.textViewUpdateMonth);
        editTextUpdateUnits     = findViewById(R.id.editTextUpdateUnits);
        seekBarUpdateRebate     = findViewById(R.id.seekBarUpdateRebate);
        textViewUpdateRebateVal = findViewById(R.id.textViewUpdateRebateVal);
        tvUpdateError           = findViewById(R.id.tvUpdateError);
        buttonUpdate            = findViewById(R.id.buttonUpdate);
        buttonUpdateBack        = findViewById(R.id.buttonUpdateBack);

        seekBarUpdateRebate.setMax(5);
        tvUpdateError.setVisibility(View.GONE);

        currentMonth = getIntent().getStringExtra("month");
        if (currentMonth == null) { finish(); return; }

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null,
                DatabaseHelper.COL_MONTH + " = ?", new String[]{currentMonth},
                null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int units  = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNITS));
                int rebate = (int) cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE));
                textViewUpdateMonth.setText(currentMonth);
                editTextUpdateUnits.setText(String.valueOf(units));
                seekBarUpdateRebate.setProgress(rebate);
                textViewUpdateRebateVal.setText(rebate + "%");
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading record.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        seekBarUpdateRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean f) {
                textViewUpdateRebateVal.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        buttonUpdate.setOnClickListener(v -> updateBill());
        buttonUpdateBack.setOnClickListener(v -> finish());
    }

    private void updateBill() {
        tvUpdateError.setVisibility(View.GONE);
        String unitsStr = editTextUpdateUnits.getText().toString().trim();

        if (unitsStr.isEmpty()) {
            tvUpdateError.setText("⚠  Please enter units used.");
            tvUpdateError.setVisibility(View.VISIBLE);
            editTextUpdateUnits.requestFocus();
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsStr);
        } catch (NumberFormatException e) {
            tvUpdateError.setText("⚠  Please enter a valid number.");
            tvUpdateError.setVisibility(View.VISIBLE);
            return;
        }

        if (units < 1 || units > 1000) {
            tvUpdateError.setText("⚠  Units must be between 1 and 1000 kWh.");
            tvUpdateError.setVisibility(View.VISIBLE);
            return;
        }

        double total;
        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }

        int rebate = seekBarUpdateRebate.getProgress();
        double finalCost = total - (total * rebate / 100.0);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_NAME + " SET " +
                DatabaseHelper.COL_UNITS + " = " + units + ", " +
                DatabaseHelper.COL_TOTAL_CHARGES + " = " + total + ", " +
                DatabaseHelper.COL_REBATE + " = " + rebate + ", " +
                DatabaseHelper.COL_FINAL_COST + " = " + finalCost +
                " WHERE " + DatabaseHelper.COL_MONTH + " = '" + currentMonth + "'");
            Toast.makeText(this, "Record updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error updating record.", Toast.LENGTH_LONG).show();
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
