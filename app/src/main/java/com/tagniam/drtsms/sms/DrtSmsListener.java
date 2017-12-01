package com.tagniam.drtsms.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

/**
 * Created by jr on 30/11/17.
 */

public class DrtSmsListener extends BroadcastReceiver {
    private final String drtPhoneNumber = "8447460497";

    /**
     * Grabs contents of a text. If it's from DRT, call the schedule
     * updater.
     * @param context application context
     * @param intent contains the messages
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String from = smsMessage.getOriginatingAddress();

                // Only analyze if from DRT
                // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
                if (from.equals(drtPhoneNumber)) {
                    String msg = smsMessage.getMessageBody();
                    // TODO update schedule
                }
            }
        }
    }
}
