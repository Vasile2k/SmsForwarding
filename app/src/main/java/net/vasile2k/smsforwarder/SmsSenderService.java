package net.vasile2k.smsforwarder;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import static net.vasile2k.smsforwarder.MainActivity.logText;

public class SmsSenderService extends IntentService {

	public SmsSenderService(){
		super("SmsSenderService");
		logText("SMS sender service constructed!");
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		logText("In service handler!");
		Context context = this;

		if(ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
			logText("Service permitted to send!");

			String message = workIntent.getStringExtra("message");
			String numberTo = workIntent.getStringExtra("numberTo");

			if(message != null && numberTo != null){
				logText("Service SMS: " + message + " to " + numberTo);
				sendSms(message, numberTo);
			}else{
				logText("Message or phone number are not present. Maybe first time starting service?");
			}

		}else{
			logText("Service not permitted to send!");
		}
	}

	private void sendSms(String message, String numberTo){
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);

		if(parts.size() > 1){
			sms.sendMultipartTextMessage(numberTo, null, parts, null, null);
			logText("Sent " + parts.size() + "-part sms!");
		}else{
			sms.sendTextMessage(numberTo, null, message, null, null);
			logText("Sent short sms!");
		}

	}

}
