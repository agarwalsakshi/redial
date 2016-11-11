package com.sakshi.android.redial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.sakshi.android.redial.util.SharedPreference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "MainActiviy";
    private static final int CALL_CODE = 2;
    private EditText selectUser;
    private int remainingCall;
    private TelephonyManager manager;
    private StatePhoneReceiver statePhoneReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");
        statePhoneReceiver = new StatePhoneReceiver(this);
        manager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        manager.listen(statePhoneReceiver, PhoneStateListener.LISTEN_CALL_STATE);
        selectUser = (EditText) findViewById(R.id.select_user);
        ((ImageButton) findViewById(R.id.contact_icon)).setOnClickListener(this);
        ((Button) findViewById(R.id.call_user)).setOnClickListener(this);
        ArrayList<Integer> spinnerItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            spinnerItems.add(i + 1);
        }

        final Spinner sp = (Spinner) findViewById(R.id.calls_number_spinner);
        ArrayAdapter<Integer> adp = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, spinnerItems);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
        sp.setSelected(true);
        sp.setSelection(9);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int pos, long arg3) {
                remainingCall = Integer.valueOf(sp.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        if (!new SharedPreference().fetchContactNumber(MainActivity.this).isEmpty() &&
                new SharedPreference().fetchRemainingCallNumber(MainActivity.this) >= 0) {
            callNumber();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                selectUser.setText(contactNumber);

            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contact_icon:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
                break;

            case R.id.call_user:
                new SharedPreference().saveContactNumber(MainActivity.this, selectUser.getText().toString());
                new SharedPreference().saveRemainingCallNumber(MainActivity.this, remainingCall);
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    callNumber();
                } else {
                    callNumber();
                }
                break;
            case R.id.call_cut:
                new SharedPreference().removeContactNumber(MainActivity.this);
                new SharedPreference().removeRemainingCallNumber(MainActivity.this);
        }
    }

    private void callNumber() {
        Uri number = Uri.parse("tel:" + new SharedPreference().fetchContactNumber(this));
        Intent callIntent = new Intent(Intent.ACTION_CALL, number);
        remainingCall -= 1;
        new SharedPreference().saveRemainingCallNumber(this, remainingCall);
        Log.d(TAG, "callNumber: remaining: " + new SharedPreference().fetchRemainingCallNumber(MainActivity.this));
        Log.d(TAG, "callNumber: number: " + number);
        startActivity(callIntent);
        Log.d(TAG, "callNumber: last");
    }

    public class StatePhoneReceiver extends PhoneStateListener {
        Context context;

        public StatePhoneReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged: ringing");

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: //Call is established
                    Log.d(TAG, "onCallStateChanged: talking");
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Activate loudspeaker
                    AudioManager audioManager = (AudioManager)
                            getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setSpeakerphoneOn(true);
//                    if(audioManager.getMode(AudioManager))
//                    {
//                        new SharedPreference().removeContactNumber(MainActivity.this);
//                        new SharedPreference().removeRemainingCallNumber(MainActivity.this);
//                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "onCallStateChanged: idle");
                    break;
            }
        }

    }
}
