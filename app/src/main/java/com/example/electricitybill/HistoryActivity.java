package com.example.electricitybill;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView listView;
    ArrayList<String[]> records;
    BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
        }

        dbHelper = new DatabaseHelper(this);
        records = new ArrayList<>();
        listView = findViewById(R.id.listViewBills);

        adapter = new BillAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (records.isEmpty()) return;
            String[] rec = records.get(position);
            String month = rec[0];

            new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("Bill — " + month)
                    .setItems(new CharSequence[]{"View Details", "Update", "Delete"},
                            (dialog, which) -> {
                                if (which == 0) {
                                    Intent i = new Intent(HistoryActivity.this, ViewBillActivity.class);
                                    i.putExtra("month", month);
                                    startActivity(i);
                                } else if (which == 1) {
                                    Intent i = new Intent(HistoryActivity.this, UpdateBillActivity.class);
                                    i.putExtra("month", month);
                                    startActivity(i);
                                } else {
                                    new AlertDialog.Builder(HistoryActivity.this)
                                            .setTitle("Confirm Delete")
                                            .setMessage("Delete bill record for " + month + "?")
                                            .setPositiveButton("Delete", (d, w) -> {
                                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                // FIXED: TABLE_NAME → TABLE_BILLS
                                                db.delete(DatabaseHelper.TABLE_BILLS,
                                                        DatabaseHelper.COL_MONTH + " = ?",
                                                        new String[]{month});
                                                Toast.makeText(HistoryActivity.this,
                                                        "Record deleted", Toast.LENGTH_SHORT).show();
                                                loadRecords();
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                }
                            })
                    .show();
        });

        loadRecords();
    }

    private void loadRecords() {
        records.clear();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // FIXED: TABLE_NAME → TABLE_BILLS, COL_UNITS → COL_UNIT
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseHelper.TABLE_BILLS +
                            " ORDER BY CASE " + DatabaseHelper.COL_MONTH +
                            " WHEN 'January'   THEN 1  WHEN 'February'  THEN 2" +
                            " WHEN 'March'     THEN 3  WHEN 'April'     THEN 4" +
                            " WHEN 'May'       THEN 5  WHEN 'June'      THEN 6" +
                            " WHEN 'July'      THEN 7  WHEN 'August'    THEN 8" +
                            " WHEN 'September' THEN 9  WHEN 'October'   THEN 10" +
                            " WHEN 'November'  THEN 11 WHEN 'December'  THEN 12 END",
                    null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    records.add(new String[]{
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH)),
                            // FIXED: COL_UNITS → COL_UNIT
                            String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT))),
                            String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_CHARGES))),
                            String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE))),
                            String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FINAL_COST)))
                    });
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading records: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter.notifyDataSetChanged();
    }

    class BillAdapter extends ArrayAdapter<String[]> {

        BillAdapter() {
            super(HistoryActivity.this, 0, records);
        }

        @Override
        public int getCount() {
            return records.isEmpty() ? 1 : records.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (records.isEmpty()) {
                TextView tv = new TextView(HistoryActivity.this);
                tv.setText("No records yet. Save a bill from the main screen.");
                tv.setTextColor(0xFF0097A7);
                tv.setTextSize(14f);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(40, 100, 40, 100);
                return tv;
            }

            View row = convertView;
            if (row == null || row.getTag() == null) {
                row = LayoutInflater.from(HistoryActivity.this)
                        .inflate(R.layout.list_item_bill, parent, false);
                row.setTag(true);
            }

            String[] rec = records.get(position);
            TextView tvMonth = row.findViewById(R.id.tvItemMonth);
            TextView tvCost  = row.findViewById(R.id.tvItemFinalCost);

            tvMonth.setText(rec[0]);
            tvCost.setText(String.format(Locale.getDefault(), "RM %.2f", Double.parseDouble(rec[4])));

            return row;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}