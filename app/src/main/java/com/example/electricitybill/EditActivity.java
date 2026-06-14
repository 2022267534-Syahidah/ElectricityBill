package com.example.electricitybill;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class EditActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editUnit;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue;
    private MaterialButton btnUpdate, btnCancel;
    private DatabaseHelper dbHelper;
    private int billId;
    private int selectedRebate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("✏️ Edit Record");
        }

        dbHelper = new DatabaseHelper(this);
        billId = getIntent().getIntExtra("id", -1);

        spinnerMonth  = findViewById(R.id.spinnerEditMonth);
        editUnit      = findViewById(R.id.editTextEditUnit);
        seekBarRebate = findViewById(R.id.seekBarEditRebate);
        tvRebateValue = findViewById(R.id.tvEditRebateValue);
        btnUpdate     = findViewById(R.id.btnUpdate);
        btnCancel     = findViewById(R.id.btnCancelEdit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRebate = progress;
                tvRebateValue.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        loadData();

        btnUpdate.setOnClickListener(v -> updateRecord());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadData() {
        if (billId == -1) { finish(); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_BILLS +
            " WHERE " + DatabaseHelper.COL_ID + " = " + billId, null);

        if (cursor.moveToFirst()) {
            String month = cursor.getString(1);
            double unit  = cursor.getDouble(2);
            int rebate   = (int) cursor.getDouble(3);

            String[] months = getResources().getStringArray(R.array.months);
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(month)) { spinnerMonth.setSelection(i); break; }
            }
            editUnit.setText(String.valueOf((int) unit));
            seekBarRebate.setProgress(rebate);
            selectedRebate = rebate;
            tvRebateValue.setText("Rebate: " + rebate + "%");
        }
        cursor.close();
    }

    private void updateRecord() {
        String unitStr = editUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            editUnit.setError("Please enter electricity units");
            editUnit.requestFocus();
            return;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            editUnit.setError("Please enter a valid number");
            editUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            editUnit.setError("Units must be between 1 and 1000 kWh");
            editUnit.requestFocus();
            Toast.makeText(this, "⚠️ Units must be between 1 – 1000 kWh!", Toast.LENGTH_SHORT).show();
            return;
        }

        double total;
        if (unit <= 200) {
            total = unit * 0.218;
        } else if (unit <= 300) {
            total = (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((unit - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((unit - 600) * 0.546);
        }

        double finalCost = total - (total * (selectedRebate / 100.0));
        String month = spinnerMonth.getSelectedItem().toString();

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_BILLS + " SET " +
                DatabaseHelper.COL_MONTH         + "='" + month     + "', " +
                DatabaseHelper.COL_UNIT          + "="  + unit      + ", " +
                DatabaseHelper.COL_REBATE        + "="  + selectedRebate + ", " +
                DatabaseHelper.COL_TOTAL_CHARGES + "="  + total     + ", " +
                DatabaseHelper.COL_FINAL_COST    + "="  + finalCost +
                " WHERE " + DatabaseHelper.COL_ID + "=" + billId);

            Toast.makeText(this, "✅ Record updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Error updating record: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
