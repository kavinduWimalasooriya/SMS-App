package com.e.smsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button sendBtn,refreshBtn;
    EditText phoneNum, messageTxt;
    private final static int REQUEST_CODE_PERMISSION_SEND_SMS = 123;
    ListView listV;
    private final static int REQUEST_CODE_PERMISSION_READ_SMS = 456;
    ArrayList<String> smsMsgList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    public static MainActivity instance;

    public static MainActivity Instance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        sendBtn = findViewById(R.id.sendBtn);
        phoneNum = findViewById(R.id.phoneNum);
        messageTxt = findViewById(R.id.messageTxt);
        listV = findViewById(R.id.listV);
        refreshBtn = findViewById(R.id.refreshBtn);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshInbox();
            }
        });

        sendBtn.setEnabled(false);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,smsMsgList);
        listV.setAdapter(arrayAdapter);

        if (checkPermission(Manifest.permission.READ_SMS)){
            refreshInbox();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    (Manifest.permission.READ_SMS)}, REQUEST_CODE_PERMISSION_READ_SMS);
        }


        if (checkPermission(Manifest.permission.SEND_SMS)){
            sendBtn.setEnabled(true);
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    (Manifest.permission.SEND_SMS)}, REQUEST_CODE_PERMISSION_SEND_SMS);
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = phoneNum.getText().toString();
                String msg = messageTxt.getText().toString();

                SmsManager smsMan = SmsManager.getDefault();
                smsMan.sendTextMessage(num, null, msg, null, null);

                phoneNum.setText("");
                messageTxt.setText("");

            }
        });

    }

    private boolean checkPermission(String permission){
        int checkPermission = ContextCompat.checkSelfPermission(this,permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_PERMISSION_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sendBtn.setEnabled(true);
                }
                break;
        }
    }

    public void refreshInbox(){
        ContentResolver cResolver = getContentResolver();
        Cursor smsInboxCursor = cResolver.query(Uri.parse("content://sms/inbox"),null,null,null,null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;

        arrayAdapter.clear();
        do {
            String str = "SMS from : "+smsInboxCursor.getString(indexAddress)+"\n";
            str += smsInboxCursor.getString(indexBody);
            arrayAdapter.add(str);

        }while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMsg){
        arrayAdapter.insert(smsMsg,0);
        arrayAdapter.notifyDataSetChanged();
    }


}
