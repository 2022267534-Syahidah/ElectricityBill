package com.example.electricitybill;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue, tvError;
    private Button btnUpdate, btnCancel;
    private DatabaseHelper dbHelper;
    private int recordId;
    private int selectedRebate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Record");
        }

        dbHelper = new DatabaseHelper(this);
        recordId = getIntent().getIntExtra("id", -1);

        spinnerMonth  = findViewById(R.id.spinnerMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        tvRebateValue = findViewById(R.id.tvRebateValue);
        tvError       = findViewById(R.id.tvError);
        btnUpdate     = findViewById(R.id.btnUpdate);
        btnCancel     = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                selectedRebate = p;
                tvRebateValue.setText("Rebate: " + p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        loadExistingData();
        btnUpdate.setOnClickListener(v -> updateRecord());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadExistingData() {
        if (recordId == -1) { finish(); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_BILLS +
            " WHERE " + DatabaseHelper.COL_ID + "=?",
            new String[]{String.valueOf(recordId)});

        if (c.moveToFirst()) {
            String month  = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH));
            double units  = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));
            int rebatePct = (int) c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE));

            String[] months = getResources().getStringArray(R.array.months);
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(month)) { spinnerMonth.setSelection(i); break; }
            }
            editTextUnits.setText(String.valueOf((int) units));
            seekBarRebate.setProgress(rebatePct);
            selectedRebate = rebatePct;
            tvRebateValue.setText("Rebate: " + rebatePct + "%");
        }
        c.close();
    }

    private void updateRecord() {
        tvError.setVisibility(View.GONE);
        String unitsStr = editTextUnits.getText().toString().trim();

        if (unitsStr.isEmpty()) { showError("Please enter electricity units."); return; }

        double units;
        try { units = Double.parseDouble(unitsStr); }
        catch (NumberFormatException e) { showError("Please enter a valid number."); return; }

        if (units < 1 || units > 1000) { showError("Units must be between 1 and 1000 kWh."); return; }

        String month     = spinnerMonth.getSelectedItem().toString();
        double total     = BillCalculator.calculateTotal(units);
        double finalCost = BillCalculator.calculateFinal(total, selectedRebate);

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_MONTH,         month);
        cv.put(DatabaseHelper.COL_UNIT,          units);
        cv.put(DatabaseHelper.COL_TOTAL_CHARGES, total);
        cv.put(DatabaseHelper.COL_REBATE,        selectedRebate);
        cv.put(DatabaseHelper.COL_FINAL_COST,    finalCost);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(DatabaseHelper.TABLE_BILLS, cv,
            DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(recordId)});

        if (rows > 0) {
            Toast.makeText(this, "✅ Record updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "❌ Error updating record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String msg) {
        tvError.setText("⚠ " + msg);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
