package com.arghya.expensetrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView totalBalance, totalIncome, totalExpense;
    RelativeLayout showAllIncome, showAllExpense;
    LinearLayout addIncome, addExpense;
    RecyclerView recentTransactionsRecyclerView;
    DatabaseHelper dbHelper;

    double prevTotalIncome = 0;
    double prevTotalExpense = 0;
    double prevTotalBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalBalance = findViewById(R.id.totalBalance);
        totalIncome = findViewById(R.id.totalIncome);
        totalExpense = findViewById(R.id.totalExpense);
        showAllIncome = findViewById(R.id.showAllIncome);
        showAllExpense = findViewById(R.id.showAllExpense);
        addIncome = findViewById(R.id.addIncome);
        addExpense = findViewById(R.id.addExpense);
        recentTransactionsRecyclerView = findViewById(R.id.recentTransactionsRecyclerView);
        dbHelper = new DatabaseHelper(this);

        TextView monthlyOverviewButton = findViewById(R.id.monthlyOverviewButton);
        monthlyOverviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MonthlyOverviewActivity.class));
            }
        });

        recentTransactionsRecyclerView = findViewById(R.id.recentTransactionsRecyclerView);
        int spanCount = 1; // Number of columns in the grid
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recentTransactionsRecyclerView.setLayoutManager(layoutManager);

        addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Add_Amount.isExpense = false;
                startActivity(new Intent(MainActivity.this, Add_Amount.class));
            }
        });

        showAllIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowAllData.Expense = false;
                startActivity(new Intent(MainActivity.this, ShowAllData.class));
            }
        });

        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Add_Amount.isExpense = true;
                startActivity(new Intent(MainActivity.this, Add_Amount.class));
            }
        });

        showAllExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowAllData.Expense = true;
                startActivity(new Intent(MainActivity.this, ShowAllData.class));
            }
        });

        updateTotals();
        updateRecentTransactions();
    }

    private void updateTotals() {
        TotalIncome();
        TotalExpense();
        TotalBalance();
    }


    public void TotalIncome() {
        double newTotalIncome = dbHelper.getTotalIncome();
        totalIncome.setText("₹" + Math.round(newTotalIncome));
        prevTotalIncome = newTotalIncome;
    }

    public void TotalExpense() {
        double newTotalExpense = dbHelper.getTotalExpense();
        totalExpense.setText("₹" + Math.round(newTotalExpense));
        prevTotalExpense = newTotalExpense;
    }

    public void TotalBalance() {
        double newTotalIncome = dbHelper.getTotalIncome();
        double newTotalExpense = dbHelper.getTotalExpense();
        double newTotalBalance = newTotalIncome - newTotalExpense;

        totalBalance.setText("₹" + Math.round(newTotalBalance));

        // Animate the balance change
        countAnim((int) prevTotalBalance, (int) newTotalBalance);

        // Update previous total balance
        prevTotalBalance = newTotalBalance;

        // Update last updated time
        long currentTime = System.currentTimeMillis();
        dbHelper.updateLastUpdatedTime(DatabaseHelper.LAST_BALANCE_UPDATE, currentTime);
    }

    private void countAnim(int fromValue, int toValue) {
        ValueAnimator animator = ValueAnimator.ofInt(fromValue, toValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                totalBalance.setText("₹" + animation.getAnimatedValue().toString());
            }
        });
        animator.setDuration(1000); // duration of the animation
        animator.start();
    }


//    private String getFormattedTimeDifference(long timeMillis) {
//        long currentTimeMillis = System.currentTimeMillis();
//        long diffMillis = currentTimeMillis - timeMillis;
//        long diffMinutes = diffMillis / (60 * 1000);
//        long diffHours = diffMillis / (60 * 60 * 1000);
//        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
//
//        if (diffMinutes < 1) {
//            return "just now";
//        } else if (diffMinutes < 60) {
//            return diffMinutes + " minute" + (diffMinutes != 1 ? "s" : "") + " ago";
//        } else if (diffHours < 24) {
//            return diffHours + " hour" + (diffHours != 1 ? "s" : "") + " ago";
//        } else if (diffDays == 1) {
//            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//            return "yesterday, " + sdf.format(new Date(timeMillis));
//        } else {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy, hh:mm a", Locale.getDefault());
//            return sdf.format(new Date(timeMillis));
//        }
//    }

    private void updateRecentTransactions() {
        List<HashMap<String, String>> recentTransactions = dbHelper.getRecentTransactions(5);
        RecentTransactionsAdapter adapter = new RecentTransactionsAdapter(recentTransactions);
        recentTransactionsRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotals();
        updateRecentTransactions();
    }

    private class RecentTransactionsAdapter extends RecyclerView.Adapter<RecentTransactionsAdapter.ViewHolder> {
        private List<HashMap<String, String>> transactions;

        public RecentTransactionsAdapter(List<HashMap<String, String>> transactions) {
            this.transactions = transactions;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HashMap<String, String> transaction = transactions.get(position);
            holder.reasonTextView.setText(transaction.get("reason"));

            String amountText = transaction.get("amount");
            String prefix = transaction.get("type").equals("income") ? "+" : "-";
            holder.amountTextView.setText(prefix + " ₹" + amountText);

//            if (transaction.get("type").equals("income")) {
//                holder.amountTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//            } else {
//                holder.amountTextView.setTextColor(getResources().getColor(android.R.color.black));
//            }

            try {
                long timeMillis = Double.valueOf(transaction.get("time")).longValue();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy, hh:mm a", Locale.getDefault());
                String formattedTime = sdf.format(new Date(timeMillis));
                holder.timeTextView.setText(formattedTime);
            } catch (NumberFormatException e) {
                holder.timeTextView.setText("Invalid Date");
            }
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView reasonTextView, amountTextView, timeTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                reasonTextView = itemView.findViewById(R.id.reasonTextView);
                amountTextView = itemView.findViewById(R.id.amountTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
            }
        }
    }


}