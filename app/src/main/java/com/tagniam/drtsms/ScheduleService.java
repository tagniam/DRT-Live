package com.tagniam.drtsms;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class ScheduleService extends IntentService {
    public static final String SCHEDULE_RECEIVED = "schedule_received";
    private final String drtPhoneNumber = "8447460497";

    public ScheduleService() {
        super(ScheduleService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get stop id and send sms
        String stopId = intent.getStringExtra("stop_id");
        SmsManager smsSender = SmsManager.getDefault();
        smsSender.sendTextMessage(drtPhoneNumber, null, stopId, null, null);

        BroadcastReceiver msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        String from = smsMessage.getOriginatingAddress();

                        // Only analyze if from DRT
                        // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
                        if (from.equals(drtPhoneNumber)) {
                            String msg = smsMessage.getMessageBody();

                            Intent resultIntent = new Intent();
                            resultIntent.setAction(SCHEDULE_RECEIVED);
                            resultIntent.putExtra("result", msg);
                            sendBroadcast(resultIntent);
                        }
                    }
                }
            }
        };

        getApplicationContext().registerReceiver(msgReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }
}
