package com.arghya.expensetrackerapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ShowAllData extends AppCompatActivity {

    TextView showAllDataTitle, lastUpdatedTime;
    ListView listView;
    DatabaseHelper dbHelper;

    ArrayList<HashMap<String, String>> arrayList;
    HashMap<String, String> hashMap;

    public static boolean Expense = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_all_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showAllDataTitle = findViewById(R.id.showAllDataTitle);
        lastUpdatedTime = findViewById(R.id.lastUpdatedTime);
        listView = findViewById(R.id.listView);
        dbHelper = new DatabaseHelper(this);

        // Get current month name for the title
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());
        
        if (Expense) {
            showAllDataTitle.setText(currentMonth + " Expense Overview");
        } else {
            showAllDataTitle.setText(currentMonth + " Income Overview");
        }
        updateLastUpdatedTimeDisplay();
        loadData();
    }

    private void updateLastUpdatedTimeDisplay() {
        String key = Expense ? DatabaseHelper.LAST_EXPENSE_UPDATE : DatabaseHelper.LAST_INCOME_UPDATE;
        long lastUpdatedTimeMillis = dbHelper.getLastUpdatedTime(key);

        if (lastUpdatedTimeMillis == 0) {
            lastUpdatedTime.setText("Last updated: Never");
            return;
        }

        String formattedTime = getFormattedTimeDifference(lastUpdatedTimeMillis);
        lastUpdatedTime.setText("Last updated: " + formattedTime);
    }

    private String getFormattedTimeDifference(long timeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long diffMillis = currentTimeMillis - timeMillis;
        long diffMinutes = diffMillis / (60 * 1000);
        long diffHours = diffMillis / (60 * 60 * 1000);
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);

        if (diffMinutes < 1) {
            return "just now";
        } else if (diffMinutes < 60) {
            return diffMinutes + " minute" + (diffMinutes != 1 ? "s" : "") + " ago";
        } else if (diffHours < 24) {
            return diffHours + " hour" + (diffHours != 1 ? "s" : "") + " ago";
        } else if (diffDays == 1) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return "yesterday, " + sdf.format(new Date(timeMillis));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }

    public void loadData() {
        Cursor cursor = null;
        
        // Get current year and month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based

        if (Expense) {
            cursor = dbHelper.getMonthlyExpenseData(currentYear, currentMonth);
        } else {
            cursor = dbHelper.getMonthlyIncomeData(currentYear, currentMonth);
        }

        if (cursor != null && cursor.getCount() > 0) {
            arrayList = new ArrayList<>();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                double amount = cursor.getDouble(1);
                String reason = cursor.getString(2);
                long time = cursor.getLong(3);

                hashMap = new HashMap<>();
                hashMap.put("id", "" + id);
                hashMap.put("amount", "" + Math.round(amount));
                hashMap.put("reason", "" + reason);
                hashMap.put("time", "" + time);
                arrayList.add(hashMap);
            }

            MyAdapter myAdapter = new MyAdapter();
            listView.setAdapter(myAdapter);

        } else {
            showAllDataTitle.setText("No Data Found");
        }


    }


    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();

            View view;

            if (convertView == null) {
                if (Expense) {
                    view = inflater.inflate(R.layout.expense_card_ui, null);
                } else {
                    view = inflater.inflate(R.layout.income_card_ui, null);
                }
            } else {
                view = convertView;
            }


            TextView spendingTitle, spendingAmount, timeAndDate;
            LinearLayout btnDelete;

            spendingTitle = view.findViewById(R.id.spendingTitle);
            spendingAmount = view.findViewById(R.id.spendingAmount);
            timeAndDate = view.findViewById(R.id.timeAndDate);
            btnDelete = view.findViewById(R.id.btnDelete);

            hashMap = arrayList.get(position);
            String id = hashMap.get("id");
            String amount = hashMap.get("amount");
            String reason = hashMap.get("reason");
            double time = Double.parseDouble(hashMap.get("time"));


            spendingTitle.setText(reason);
            if (Expense) {
                spendingAmount.setText("- ₹" + amount);

            } else {
                spendingAmount.setText("+ ₹" + amount);
            }

            // Convert timestamp to human-readable date and time
            Date date = new Date((long) time);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy , hh:mm a", Locale.getDefault());
            String formattedDate = sdf.format(date);

            // Display formatted date and time
            timeAndDate.setText(formattedDate);

            // Delete Item

            // Delete Item with confirmation dialog
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an AlertDialog to confirm deletion
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllData.this);
                    builder.setTitle("Delete Confirmation")
                            .setMessage("Are you sure you want to delete this record?")
                            .setNegativeButton("Cancel", null);

                    // Set the positive button with red text
                    builder.setPositiveButton("Delete", null);
                    AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            // Get the delete button and set its text color to red
                            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            deleteButton.setTextColor(Color.RED);
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Perform deletion
                                    if (Expense) {
                                        dbHelper.deleteExpense(id);
                                    } else {
                                        dbHelper.deleteIncome(id);
                                    }
                                    // Refresh data
                                    loadData();
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                }
            });

            return view;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLastUpdatedTimeDisplay();
        loadData();
    }

}

//app v 1.1 completed