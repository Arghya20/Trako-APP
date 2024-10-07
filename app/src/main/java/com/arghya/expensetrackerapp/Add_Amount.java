package com.arghya.expensetrackerapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import kotlin.jvm.internal.markers.KMutableSet;

public class Add_Amount extends AppCompatActivity {


    TextView addTextTv;
    TextInputEditText amountEditText, reasonEditText;
    TextInputLayout amountInputLayout;
    AppCompatButton btnAdd;
    Button datePickerButton;
    DatabaseHelper dbHelper;

    public static boolean isExpense = false;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.add_amount);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        addTextTv = findViewById(R.id.addTextTV);
        amountEditText = findViewById(R.id.amountEditText);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        reasonEditText = findViewById(R.id.reasonEditText);
        btnAdd = findViewById(R.id.btnAdd);
        datePickerButton = findViewById(R.id.datePickerButton);
        dbHelper = new DatabaseHelper(this);

        selectedDate = Calendar.getInstance();
        updateDateButtonText();

        if (isExpense) {
            addTextTv.setText("Add Your Expense");
            btnAdd.setText("Add Expense");
        } else {
            addTextTv.setText("Add Your Income");
            btnAdd.setText("Add Income");

        }

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values of amount and reason directly
                String amount = amountEditText.getText().toString();
                String reason = reasonEditText.getText().toString();


                if (!amount.isEmpty()) {
                    try {
                        double doubleAmount = Double.parseDouble(amount);
                        amountInputLayout.setError(null);

                        long timestamp = selectedDate.getTimeInMillis();

                        if (isExpense) {
                            dbHelper.addExpense(doubleAmount, reason, timestamp);
                        } else {
                            dbHelper.addIncome(doubleAmount, reason, timestamp);
                        }

                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(20);
                        }

                        Toast.makeText(Add_Amount.this, amount+" Added ", Toast.LENGTH_SHORT)
                                .show();

                        amountEditText.setText("");
                        reasonEditText.setText("");
                        selectedDate = Calendar.getInstance(); // Reset to current date
                        updateDateButtonText();
                    } catch (NumberFormatException e) {
                        Toast.makeText(Add_Amount.this, "Invalid amount format", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    amountInputLayout.setError("Enter Amount");
                }
            }
        });
    }
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateButtonText();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String dateString = dateFormat.format(selectedDate.getTime());
        datePickerButton.setText(dateString);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}