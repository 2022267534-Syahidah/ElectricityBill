package com.example.electricitybill;

import android.content.DialogInterface;
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
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView listViewBills;
    private DatabaseHelper dbHelper;
    private List<BillModel> billList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📋 Bill Records");
        }

        dbHelper = new DatabaseHelper(this);
        listViewBills = findViewById(R.id.listViewBills);
        MaterialButton btnBack = findViewById(R.id.btnBackFromList);

        refreshList();
        btnBack.setOnClickListener(v -> finish());
    }

    public void refreshList() {
        billList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_BILLS + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                billList.add(new BillModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getDouble(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (billList.isEmpty()) {
            Toast.makeText(this, "No records saved yet.", Toast.LENGTH_SHORT).show();
        }

        BillListAdapter adapter = new BillListAdapter();
        listViewBills.setAdapter(adapter);

        listViewBills.setOnItemClickListener((parent, view, position, id) -> {
            final BillModel selected = billList.get(position);
            final CharSequence[] options = {"👁️ View Detail", "✏️ Edit", "🗑️ Delete"};

            new AlertDialog.Builder(this)
                .setTitle(selected.getMonth() + " – RM " + String.format("%.2f", selected.getFinalCost()))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent viewIntent = new Intent(ListActivity.this, DetailActivity.class);
                            viewIntent.putExtra("id", selected.getId());
                            startActivity(viewIntent);
                            break;
                        case 1:
                            Intent editIntent = new Intent(ListActivity.this, EditActivity.class);
                            editIntent.putExtra("id", selected.getId());
                            startActivity(editIntent);
                            break;
                        case 2:
                            new AlertDialog.Builder(this)
                                .setTitle("Delete Record")
                                .setMessage("Are you sure you want to delete the record for " + selected.getMonth() + "?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    SQLiteDatabase dbW = dbHelper.getWritableDatabase();
                                    dbW.execSQL("DELETE FROM " + DatabaseHelper.TABLE_BILLS +
                                        " WHERE " + DatabaseHelper.COL_ID + " = " + selected.getId());
                                    Toast.makeText(this, "✅ Record deleted.", Toast.LENGTH_SHORT).show();
                                    refreshList();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                            break;
                    }
                })
                .show();
        });
    }

    private class BillListAdapter extends ArrayAdapter<BillModel> {
        BillListAdapter() {
            super(ListActivity.this, R.layout.list_item_bill, billList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ListActivity.this)
                    .inflate(R.layout.list_item_bill, parent, false);
            }
            BillModel bill = billList.get(position);
            TextView tvMonth = convertView.findViewById(R.id.tvItemMonth);
            TextView tvFinal = convertView.findViewById(R.id.tvItemFinalCost);
            tvMonth.setText(bill.getMonth());
            tvFinal.setText(String.format("RM %.2f", bill.getFinalCost()));
            return convertView;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
