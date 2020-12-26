package net.vasile2k.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;
import static net.vasile2k.smsforwarder.MainActivity.logText;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        logText("onReceive");
        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            logText("onReceive: received " + Telephony.Sms.Intents.getMessagesFromIntent(intent).length);
            for(SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();

                logText("message: " + messageBody);
                logText("address: " + address);

                int counter = context.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("counter", 1);
                for (int i = 0; i < counter; i++) {
                    String numberFrom = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("numberFrom" + i, "");
                    if (address.endsWith(numberFrom) && numberFrom.length() > 3) {
                        //message to forward
                        String message = "From " + address + ": " + messageBody;
                        String numberTo = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("numberTo" + i, "");
                        //verify if a phone numberTo is set
                        if (!numberTo.isEmpty()) {
                            logText("Gonna send " + message + " to " + numberTo);

                            Intent serviceIntent = new Intent(context.getApplicationContext(), SmsSenderService.class);
                            serviceIntent.putExtra("message", message);
                            serviceIntent.putExtra("numberTo", numberTo);
                            context.getApplicationContext().startService(serviceIntent);

                            Toast.makeText(context, "A message was redirected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }
}
