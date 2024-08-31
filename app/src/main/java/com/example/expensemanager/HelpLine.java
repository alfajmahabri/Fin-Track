package com.example.expensemanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

public class HelpLine extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_line);

        // Button for Bank 1
        Button callButton1 = findViewById(R.id.callButton1);
        callButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:1234567890");
            }
        });

        // Button for Bank 2
        Button callButton2 = findViewById(R.id.callButton2);
        callButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:0987654321");
            }
        });

        // Button for Bank 3
        Button callButton3 = findViewById(R.id.callButton3);
        callButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:1122334455");
            }
        });

        // Button for Payment App 1
        Button callButtonApp1 = findViewById(R.id.callButtonApp1);
        callButtonApp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:6677889900");
            }
        });

        // Button for Payment App 2
        Button callButtonApp2 = findViewById(R.id.callButtonApp2);
        callButtonApp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:5566778899");
            }
        });
        Button callExec1 = findViewById(R.id.callExec1);
        callExec1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("tel:7387272230");
            }
        });
    }

    private void makePhoneCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse(phoneNumber));
        startActivity(callIntent);
    }
}