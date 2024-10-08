package com.arghya.expensetrackerapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MonthlyOverviewActivity extends AppCompatActivity {
    private Spinner monthSpinner;
    private Spinner filterSpinner;
    private TextView monthlyIncomeTextView, monthlyExpenseTextView, monthlyBalanceTextView;
    private ListView monthlyTransactionListView;
    private DatabaseHelper dbHelper;
    private int selectedYear;
    private int selectedMonth;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_overview);

        monthSpinner = findViewById(R.id.monthSpinner);
        filterSpinner = findViewById(R.id.filterSpinner);
        monthlyIncomeTextView = findViewById(R.id.monthlyIncomeTextView);
        monthlyExpenseTextView = findViewById(R.id.monthlyExpenseTextView);
        monthlyBalanceTextView = findViewById(R.id.monthlyBalanceTextView);
        monthlyTransactionListView = findViewById(R.id.monthlyTransactionListView);

        dbHelper = new DatabaseHelper(this);

        // Set up month spinner
        setupMonthSpinner();

        // Set up filter spinner
        setupFilterSpinner();

        // Set initial data
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        updateMonthlyData();

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] dateParts = parent.getItemAtPosition(position).toString().split(" ");
                selectedMonth = getMonthNumber(dateParts[0]);
                selectedYear = Integer.parseInt(dateParts[1]);
                updateMonthlyData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                updateTransactionList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void setupMonthSpinner() {
        List<String> months = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (int i = 0; i < 12; i++) {
            months.add(monthFormat.format(calendar.getTime()));
            calendar.add(Calendar.MONTH, -1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        List<String> filters = new ArrayList<>();
        filters.add("All");
        filters.add("Income");
        filters.add("Expense");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
    }

    private void updateMonthlyData() {
        double monthlyIncome = dbHelper.getMonthlyTotalIncome(selectedYear, selectedMonth);
        double monthlyExpense = dbHelper.getMonthlyTotalExpense(selectedYear, selectedMonth);
        double monthlyBalance = monthlyIncome - monthlyExpense;

        monthlyIncomeTextView.setText(String.format("Income: ₹%.2f", monthlyIncome));
        monthlyExpenseTextView.setText(String.format("Expense: ₹%.2f", monthlyExpense));
        monthlyBalanceTextView.setText(String.format("Balance: ₹%.2f", monthlyBalance));

        updateTransactionList();
    }

    private void updateTransactionList() {
        ArrayList<HashMap<String, String>> transactionList = new ArrayList<>();
        Cursor incomeCursor = dbHelper.getMonthlyIncomeData(selectedYear, selectedMonth);
        Cursor expenseCursor = dbHelper.getMonthlyExpenseData(selectedYear, selectedMonth);

        if (currentFilter.equals("All") || currentFilter.equals("Income")) {
            while (incomeCursor.moveToNext()) {
                HashMap<String, String> transaction = new HashMap<>();
                transaction.put("type", "Income");
                transaction.put("amount", "+" + incomeCursor.getDouble(1));
                transaction.put("reason", incomeCursor.getString(2));
                transaction.put("time", formatDate(incomeCursor.getLong(3)));
                transactionList.add(transaction);
            }
        }

        if (currentFilter.equals("All") || currentFilter.equals("Expense")) {
            while (expenseCursor.moveToNext()) {
                HashMap<String, String> transaction = new HashMap<>();
                transaction.put("type", "Expense");
                transaction.put("amount", "-" + expenseCursor.getDouble(1));
                transaction.put("reason", expenseCursor.getString(2));
                transaction.put("time", formatDate(expenseCursor.getLong(3)));
                transactionList.add(transaction);
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                transactionList,
                R.layout.transaction_list_item,
                new String[]{"type", "amount", "reason", "time"},
                new int[]{R.id.transactionTypeTextView, R.id.transactionAmountTextView,
                        R.id.transactionReasonTextView, R.id.transactionTimeTextView}
        );

        monthlyTransactionListView.setAdapter(adapter);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private int getMonthNumber(String monthName) {
        try {
            Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
