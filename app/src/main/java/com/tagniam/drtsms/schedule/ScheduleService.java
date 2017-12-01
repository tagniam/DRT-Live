package com.tagniam.drtsms.schedule;

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

    private DrtSmsReceiver drtSmsReceiver;
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

        // Wait for sms to be received
        registerDrtSmsReceiver();
    }

    /**
     * Registers a BroadcastReceiver and waits for the DRT sms to be received.
     */
    private void registerDrtSmsReceiver() {
        drtSmsReceiver = new DrtSmsReceiver();
        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        getApplicationContext().registerReceiver(drtSmsReceiver, intentFilter);
    }

    /**
     * Used to execute code on receiving the SMS from DRT.
     */
    private class DrtSmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    String from = smsMessage.getOriginatingAddress();

                    // Only analyze if from DRT
                    // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
                    if (from.equals(drtPhoneNumber)) {
                        broadcastSchedule(smsMessage.getMessageBody());
                        break;
                    }
                }
            }
        }

        /**
         * Sends a broadcast containing the schedule information.
         * @param msg the text msg response from DRT
         */
        private void broadcastSchedule(String msg) {
            // TODO refactor to send a BusTime object
            Intent resultIntent = new Intent();
            resultIntent.setAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
            resultIntent.putExtra("result", msg);
            sendBroadcast(resultIntent);
        }
    }
}
