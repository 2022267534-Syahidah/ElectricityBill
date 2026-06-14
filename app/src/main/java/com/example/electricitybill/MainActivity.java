package com.example.electricitybill;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Spinner      spinnerMonth;
    private EditText     editTextUnit;
    private SeekBar      seekBarRebate;
    private TextView     tvRebateValue, tvTotalCharges, tvFinalCost;
    private TextView     tvRebateLabel, tvRebateDeduction;
    private TextView     tvBlock1Calc, tvBlock1Amount;
    private TextView     tvBlock2Calc, tvBlock2Amount;
    private TextView     tvBlock3Calc, tvBlock3Amount;
    private TextView     tvBlock4Calc, tvBlock4Amount;
    private View         rowBlock1, rowBlock2, rowBlock3, rowBlock4, rowRebateDeduction;
    private CardView     cardResult;
    private MaterialButton btnCalculate, btnSave, btnViewList, btnAbout;

    private DatabaseHelper dbHelper;
    private double  calculatedTotal = 0;
    private double  calculatedFinal = 0;
    private int     selectedRebate  = 0;
    private boolean hasCalculated   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("⚡ Electricity Bill");
        }

        dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase(); // ensure table is created

        spinnerMonth        = findViewById(R.id.spinnerMonth);
        editTextUnit        = findViewById(R.id.editTextUnit);
        seekBarRebate       = findViewById(R.id.seekBarRebate);
        tvRebateValue       = findViewById(R.id.tvRebateValue);
        tvTotalCharges      = findViewById(R.id.tvTotalCharges);
        tvFinalCost         = findViewById(R.id.tvFinalCost);
        tvRebateLabel       = findViewById(R.id.tvRebateLabel);
        tvRebateDeduction   = findViewById(R.id.tvRebateDeduction);
        tvBlock1Calc        = findViewById(R.id.tvBlock1Calc);
        tvBlock1Amount      = findViewById(R.id.tvBlock1Amount);
        tvBlock2Calc        = findViewById(R.id.tvBlock2Calc);
        tvBlock2Amount      = findViewById(R.id.tvBlock2Amount);
        tvBlock3Calc        = findViewById(R.id.tvBlock3Calc);
        tvBlock3Amount      = findViewById(R.id.tvBlock3Amount);
        tvBlock4Calc        = findViewById(R.id.tvBlock4Calc);
        tvBlock4Amount      = findViewById(R.id.tvBlock4Amount);
        rowBlock1           = findViewById(R.id.rowBlock1);
        rowBlock2           = findViewById(R.id.rowBlock2);
        rowBlock3           = findViewById(R.id.rowBlock3);
        rowBlock4           = findViewById(R.id.rowBlock4);
        rowRebateDeduction  = findViewById(R.id.rowRebateDeduction);
        cardResult          = findViewById(R.id.cardResult);
        btnCalculate        = findViewById(R.id.btnCalculate);
        btnSave             = findViewById(R.id.btnSave);
        btnViewList         = findViewById(R.id.btnViewList);
        btnAbout            = findViewById(R.id.btnAbout);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
            this, R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRebate = progress;
                tvRebateValue.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCalculate.setOnClickListener(v -> calculateBill());
        btnSave.setOnClickListener(v -> saveBill());
        btnViewList.setOnClickListener(v -> startActivity(new Intent(this, ListActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
    }

    private void calculateBill() {
        String unitStr = editTextUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            editTextUnit.setError("Please enter electricity units (1 – 1000 kWh)");
            editTextUnit.requestFocus();
            return;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            editTextUnit.setError("Please enter a valid number");
            editTextUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            editTextUnit.setError("Units must be between 1 and 1000 kWh");
            editTextUnit.requestFocus();
            Toast.makeText(this, "⚠️ Units must be between 1 – 1000 kWh!", Toast.LENGTH_SHORT).show();
            return;
        }

        editTextUnit.setError(null);

        // ── Block calculation ──────────────────────────────────────
        double b1 = 0, b2 = 0, b3 = 0, b4 = 0;

        if (unit <= 200) {
            b1 = unit * 0.218;
        } else if (unit <= 300) {
            b1 = 200 * 0.218;
            b2 = (unit - 200) * 0.334;
        } else if (unit <= 600) {
            b1 = 200 * 0.218;
            b2 = 100 * 0.334;
            b3 = (unit - 300) * 0.516;
        } else {
            b1 = 200 * 0.218;
            b2 = 100 * 0.334;
            b3 = 300 * 0.516;
            b4 = (unit - 600) * 0.546;
        }

        double total     = b1 + b2 + b3 + b4;
        double rebateAmt = total * (selectedRebate / 100.0);
        double finalCost = total - rebateAmt;

        calculatedTotal = total;
        calculatedFinal = finalCost;
        hasCalculated   = true;

        // ── Show block rows ──────────────────────────────────────
        // Hide all first
        rowBlock1.setVisibility(View.GONE);
        rowBlock2.setVisibility(View.GONE);
        rowBlock3.setVisibility(View.GONE);
        rowBlock4.setVisibility(View.GONE);

        // Block 1 — always visible
        double b1Units = Math.min(unit, 200);
        tvBlock1Calc.setText(String.format("%.0f kWh × RM 0.218", b1Units));
        tvBlock1Amount.setText(String.format("RM %.2f", b1));
        rowBlock1.setVisibility(View.VISIBLE);

        // Block 2
        if (unit > 200) {
            double b2Units = Math.min(unit - 200, 100);
            tvBlock2Calc.setText(String.format("%.0f kWh × RM 0.334", b2Units));
            tvBlock2Amount.setText(String.format("RM %.2f", b2));
            rowBlock2.setVisibility(View.VISIBLE);
        }

        // Block 3
        if (unit > 300) {
            double b3Units = Math.min(unit - 300, 300);
            tvBlock3Calc.setText(String.format("%.0f kWh × RM 0.516", b3Units));
            tvBlock3Amount.setText(String.format("RM %.2f", b3));
            rowBlock3.setVisibility(View.VISIBLE);
        }

        // Block 4
        if (unit > 600) {
            double b4Units = unit - 600;
            tvBlock4Calc.setText(String.format("%.0f kWh × RM 0.546", b4Units));
            tvBlock4Amount.setText(String.format("RM %.2f", b4));
            rowBlock4.setVisibility(View.VISIBLE);
        }

        // ── Totals ──────────────────────────────────────────────
        tvTotalCharges.setText(String.format("RM %.2f", total));

        // Rebate row — only show if rebate > 0
        if (selectedRebate > 0) {
            tvRebateLabel.setText(String.format("Rebate (%d%%):", selectedRebate));
            tvRebateDeduction.setText(String.format("- RM %.2f", rebateAmt));
            rowRebateDeduction.setVisibility(View.VISIBLE);
        } else {
            rowRebateDeduction.setVisibility(View.GONE);
        }

        tvFinalCost.setText(String.format("RM %.2f", finalCost));

        cardResult.setVisibility(View.VISIBLE);
        Toast.makeText(this, "✅ Calculation complete!", Toast.LENGTH_SHORT).show();
    }

    private void saveBill() {
        if (!hasCalculated) {
            Toast.makeText(this, "⚠️ Please calculate the bill first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String unitStr = editTextUnit.getText().toString().trim();
        if (unitStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter units first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();
        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ Invalid unit value.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_MONTH,         month);
            values.put(DatabaseHelper.COL_UNIT,          unit);
            values.put(DatabaseHelper.COL_REBATE,        selectedRebate);
            values.put(DatabaseHelper.COL_TOTAL_CHARGES, calculatedTotal);
            values.put(DatabaseHelper.COL_FINAL_COST,    calculatedFinal);

            long rowId = db.insertOrThrow(DatabaseHelper.TABLE_BILLS, null, values);
            Log.d(TAG, "Saved rowId=" + rowId);

            Toast.makeText(this, "✅ Record saved successfully!", Toast.LENGTH_SHORT).show();

            // Reset
            editTextUnit.setText("");
            seekBarRebate.setProgress(0);
            tvRebateValue.setText("Rebate: 0%");
            cardResult.setVisibility(View.GONE);
            rowBlock1.setVisibility(View.GONE);
            rowBlock2.setVisibility(View.GONE);
            rowBlock3.setVisibility(View.GONE);
            rowBlock4.setVisibility(View.GONE);
            rowRebateDeduction.setVisibility(View.GONE);
            calculatedTotal = 0;
            calculatedFinal = 0;
            hasCalculated   = false;
            selectedRebate  = 0;

        } catch (Exception e) {
            Log.e(TAG, "Insert failed: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "📋 Records").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 2, 1, "ℹ️ About").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, ListActivity.class));
            return true;
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
