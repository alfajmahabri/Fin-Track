package com.example.expensemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.expensemanager.Model.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class smsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String PREFS_NAME = "smsPrefs";
    private static final String PROCESSED_MESSAGES_KEY = "processedMessages";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;

        if (bundle != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            // Initialize Firebase
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser mUser = mAuth.getCurrentUser();
            DatabaseReference mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(mUser.getUid());
            DatabaseReference mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(mUser.getUid());

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String messageBody = messages[i].getMessageBody();
                String messageId = messages[i].getTimestampMillis() + "_" + messages[i].getOriginatingAddress();

                // Check if the message has already been processed
                if (sharedPreferences.contains(messageId)) {
                    Log.d(TAG, "Message already processed: " + messageId);
                    continue;
                }

                // Store the message ID to avoid processing it again
                editor.putBoolean(messageId, true);
                editor.apply();

                // Log the SMS for debugging
                Log.d(TAG, "SMS from " + messages[i].getOriginatingAddress());
                Log.d(TAG, "Message Body: " + messageBody);

                // Pattern for credited messages
                Pattern creditPattern = Pattern.compile("your A/c (\\w+)-(credited) by Rs.(\\d+) on (\\d{2}([a-zA-Z]{3}|[a-zA-Z]{4}))\\d{2} transfer from (.*) Ref No (\\d+)");
                Matcher creditMatcher = creditPattern.matcher(messageBody);

                // Updated Pattern for debited messages
                Pattern debitPattern = Pattern.compile("A/C (\\w+) debited by (\\d+\\.\\d+|\\d+) on date (\\d{2}[a-zA-Z]{3}\\d{2}) trf to (.*) Refno (\\d+)");
                Matcher debitMatcher = debitPattern.matcher(messageBody);

                if (creditMatcher.find()) {
                    // Extracting information for credited messages
                    String accountNumber = creditMatcher.group(1);
                    String transactionType = creditMatcher.group(2);
                    int amount = Integer.parseInt(creditMatcher.group(3));
                    String date = creditMatcher.group(4);
                    String transferFrom = creditMatcher.group(6);
                    String refNumber = creditMatcher.group(7);

                    // Store the data in Firebase
                    String id = mIncomeDatabase.push().getKey();
                    Data data = new Data(amount, transactionType, "Credited from " + transferFrom, id, date);

                    mIncomeDatabase.child(id).setValue(data)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Income data added successfully");
                                } else {
                                    Log.e(TAG, "Error adding income data: " + task.getException().getMessage());
                                }
                            });

                } else if (debitMatcher.find()) {
                    Log.d(TAG, "Checking for debit pattern...");

                    // Extracting information for debited messages
                    String accountNumber = debitMatcher.group(1);
                    String amountStr = debitMatcher.group(2); // Handle amount as string
                    String date = debitMatcher.group(3);
                    String transferTo = debitMatcher.group(4);
                    String refNumber = debitMatcher.group(5);

                    // Check if the amount contains any non-numeric characters
                    double amount = Double.parseDouble(amountStr);

                    // Store the data in Firebase
                    String id = mExpenseDatabase.push().getKey();
                    Data data = new Data((int) amount, "Debited", "Transferred to " + transferTo, id, date);

                    mExpenseDatabase.child(id).setValue(data)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Expense data added successfully");
                                } else {
                                    Log.e(TAG, "Error adding expense data: " + task.getException().getMessage());
                                }
                            });
                } else {
                    Log.d(TAG, "No match found for patterns.");
                }
            }
        }
    }
}
