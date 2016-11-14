package com.sakshi.android.redial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.sakshi.android.redial.util.AppUtils;
import com.sakshi.android.redial.util.SharedPreference;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "MainActiviy";
    private EditText selectUser;
    private int remainingCall;
    private Button callCut;
    private static final int MY_PERMISSIONS_REQUEST_CALL_NUMBER = 1;
    private int gapBetweenCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
            .listen(new StatePhoneReceiver(), PhoneStateListener.LISTEN_CALL_STATE);
        selectUser = (EditText) findViewById(R.id.select_user);

        (findViewById(R.id.contact_icon)).setOnClickListener(this);
        (findViewById(R.id.call_user)).setOnClickListener(this);
        callCut = (Button) findViewById(R.id.call_cut);
        callCut.setOnClickListener(this);
        setUpCallSpinner();
        setUpGapSpinner();
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
                selectUser.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
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
                checkForPermission();
                break;
        }
    }

    private boolean lastCallTime() {
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        managedCursor.moveToLast();
        String callDuration = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DURATION));
        managedCursor.close();
        int CALL_RINGING_TIME = 75;
        return Integer.parseInt(callDuration) < CALL_RINGING_TIME;
    }

    private void setUpCallSpinner() {
        ArrayList<Integer> spinnerItems = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            spinnerItems.add(i);
        }
        for (int i = 15; i <30 ; i+=5) {
            spinnerItems.add(i);
        }
        final Spinner sp = (Spinner) findViewById(R.id.calls_number_spinner);
        ArrayAdapter<Integer> adp = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, spinnerItems);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
        sp.setSelected(true);
        sp.setSelection(0);

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

    private void setUpGapSpinner() {
        ArrayList<Integer> spinnerItems = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            spinnerItems.add(i);
        }
        for (int i = 10; i <= 20; i+=5) {
            spinnerItems.add(i);
        }
        final Spinner sp = (Spinner) findViewById(R.id.gap_number_spinner);
        ArrayAdapter<Integer> adp = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, spinnerItems);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
        sp.setSelected(true);
        sp.setSelection(0);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int pos, long arg3) {
                gapBetweenCalls = Integer.valueOf(sp.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void callNumber() {
        Uri number = Uri.parse("tel:" + new SharedPreference().fetchContactNumber(this));
        Intent callIntent = new Intent(Intent.ACTION_CALL, number);
        remainingCall -= 1;
        new SharedPreference().saveRemainingCallNumber(this, remainingCall);
        startActivity(callIntent);
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                AppUtils.getInstance().showAlertDialogBox(this, "explaination");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_NUMBER);
            }
        } else {
            callNumber();
        }
    }

    private void clearDataOnCallCut() {
        new SharedPreference().removeContactNumber(MainActivity.this);
        new SharedPreference().saveRemainingCallNumber(MainActivity.this, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_NUMBER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callNumber();
                } else {
                    AppUtils.getInstance().showAlertDialogBox(this, "Permission rejected.");
                }
            }
        }
    }

    class StatePhoneReceiver extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    putCallOnLoudspeaker();
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    checkForCall();
                    callCut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clearDataOnCallCut();
                        }
                    });
            }
        }

        private void checkForCall() {
            Timer time = new Timer();
            time.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (new SharedPreference().fetchRemainingCallNumber(MainActivity.this) > 0 && lastCallTime()) {
                                callNumber();
                            } else {
                                clearDataOnCallCut();
                            }
                        }
                    });
                }
            }, gapBetweenCalls*1000);

        }

        private void putCallOnLoudspeaker() {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        }

    }
}