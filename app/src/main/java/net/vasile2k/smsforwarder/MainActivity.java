package net.vasile2k.smsforwarder;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_SMS_SEND_RECEIVE = 10;
    private int counter;
    private Boolean state;
    private LinearLayout linerLayoutRule;
    private final Vector<EditText> editTextFrom = new Vector<>();
    private final Vector<EditText> editTextTo = new Vector<>();
    private final Vector<String> numberFrom = new Vector<>();
    private final Vector<String> numberTo = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!checkForSmsPermission()) {
            requestPermission();
        }

        logText("onCreate()");

        setContentView(R.layout.activity_main);

        linerLayoutRule = findViewById(R.id.rules);

        loadRules();

        SwitchCompat run = findViewById(R.id.run);
        run.setChecked(state);
        if(state) {
            enableBroadcastReceiver();
        } else {
            disableBroadcastReceiver();
        }
    }

    /**
     * This method is a method
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_SMS_SEND_RECEIVE) {
            logText("Got SEND'n'RECEIVE permission!");
            if(isMyServiceRunning(SmsSenderService.class)){
                logText("Service already runnin'!");
            }else{
                Intent intent = new Intent(this, SmsSenderService.class);
                startService(intent);
                logText("Service started!");
            }
        }
    }

    /**
     * Checks if a service is running
     * @param serviceClass the service to be checked
     * @return wether the service is running
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method updates the phone numbers and settings.
     * @param v view
     */
    public void setPhoneNumber(View v) {
        SwitchCompat run = findViewById(R.id.run);
        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        String tempFrom, tempTo;
        int newCounter = 0;
        for(int i = 0; i < counter; i++) {
            tempFrom = editTextFrom.elementAt(i).getText().toString();
            tempTo = editTextTo.elementAt(i).getText().toString();
            if(!tempFrom.isEmpty() && !tempTo.isEmpty()) {
                editor.putString("numberFrom" + newCounter, tempFrom);
                editor.putString("numberTo" + newCounter, tempTo);
                newCounter++;
            }
        }
        if(newCounter == 0) {
            editor.putString("numberFrom" + newCounter, "");
            editor.putString("numberTo" + newCounter, "");
            newCounter = 1;
        }
        editor.putInt("counter", newCounter);
        editor.putBoolean("state", run.isChecked());
        editor.apply();

        Toast.makeText(getApplicationContext(), "Settings are saved!",
                Toast.LENGTH_SHORT).show();

        if(run.isChecked()) {
            enableBroadcastReceiver();
        } else {
            disableBroadcastReceiver();
        }
    }

    /**
     * This method checks for permission for SMS
     * @return true/false
     */
    private boolean checkForSmsPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     *This method requests permission for SMS
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, PERMISSIONS_REQUEST_SMS_SEND_RECEIVE);
    }

    /**
     * This method enables the Broadcast receiver registered in the AndroidManifest file.
     */
    private void enableBroadcastReceiver(){
        logText("enableBroadcastReceiver");
        ComponentName receiver = new ComponentName(this, SmsListener.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //Toast.makeText(this, "Enabled broadcast receiver", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method disables the Broadcast receiver registered in the AndroidManifest file.
     */
    private void disableBroadcastReceiver(){
        logText("disableBroadcastReceiver");
        ComponentName receiver = new ComponentName(this, SmsListener.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        //Toast.makeText(this, "Disabled broadcast receive", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method add a new forwarding rule
     * @param view isn't use
     */
    public void addRule(View view) {
        if(counter < 3) {
            editTextFrom.add(new EditText(this));
            editTextFrom.elementAt(counter).setGravity(Gravity.CENTER);
            editTextFrom.elementAt(counter).setHint(R.string.hint_msg_from);
            editTextFrom.elementAt(counter).setInputType(InputType.TYPE_CLASS_PHONE);
            editTextFrom.elementAt(counter).setTextColor(getResources().getColor(R.color.White));
            editTextFrom.elementAt(counter).setHintTextColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(editTextFrom.elementAt(counter));

            editTextTo.add(new EditText(this));
            editTextTo.elementAt(counter).setGravity(Gravity.CENTER);
            editTextTo.elementAt(counter).setHint(R.string.hint_msg_to);
            editTextTo.elementAt(counter).setInputType(InputType.TYPE_CLASS_PHONE);
            editTextTo.elementAt(counter).setTextColor(getResources().getColor(R.color.White));
            editTextTo.elementAt(counter).setHintTextColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(editTextTo.elementAt(counter));

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(300,3));
            divider.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(divider);

            counter++;
        } else {
            Toast.makeText(this, "Limited up to 3 forwarding rules!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This method loads saved forwarding rules.
     */
    private void loadRules() {
        counter = getSharedPreferences("data", Context.MODE_PRIVATE).getInt("counter", 1);
        state = getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("state", true);
        for(int i = 0; i < counter; i++) {
            numberTo.add(getSharedPreferences("data", Context.MODE_PRIVATE).getString("numberTo"+i, ""));
            numberFrom.add(getSharedPreferences("data", Context.MODE_PRIVATE).getString("numberFrom"+i, ""));
            logText("numberTo: " + numberTo.elementAt(i));
            logText("numberFrom: " + numberFrom.elementAt(i));

            editTextFrom.add(new EditText(this));
            editTextFrom.elementAt(i).setGravity(Gravity.CENTER);
            editTextFrom.elementAt(i).setInputType(InputType.TYPE_CLASS_PHONE);
            if(!numberFrom.elementAt(i).isEmpty()) {
                editTextFrom.elementAt(i).setText(numberFrom.elementAt(i), TextView.BufferType.EDITABLE);
            } else {
                editTextFrom.elementAt(i).setHint(R.string.hint_msg_from);
            }
            editTextFrom.elementAt(i).setTextColor(getResources().getColor(R.color.White));
            editTextFrom.elementAt(i).setHintTextColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(editTextFrom.elementAt(i));

            editTextTo.add(new EditText(this));
            editTextTo.elementAt(i).setGravity(Gravity.CENTER);
            editTextTo.elementAt(i).setInputType(InputType.TYPE_CLASS_PHONE);
            if(!numberTo.elementAt(i).isEmpty()) {
                editTextTo.elementAt(i).setText(numberTo.elementAt(i), TextView.BufferType.EDITABLE);
            } else {
                editTextTo.elementAt(i).setHint(R.string.hint_msg_to);
            }
            editTextTo.elementAt(i).setTextColor(getResources().getColor(R.color.White));
            editTextTo.elementAt(i).setHintTextColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(editTextTo.elementAt(i));

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(300,3));
            divider.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            linerLayoutRule.addView(divider);
        }
    }


    /**
     * Log a text with this app's tag
     * @param text text to be logged
     */
    public static void logText(String text){
        Log.d("2ksms", text);
    }
}
