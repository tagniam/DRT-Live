package com.tagniam.drtsms.schedule.fetcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import java.io.Serializable;
import org.greenrobot.eventbus.EventBus;

public class SmsListener extends BroadcastReceiver {

  private final String DRT_PHONE_NO = "8447460497";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
      for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

        // Only analyze if from DRT
        // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
        if (smsMessage.getOriginatingAddress().equals(DRT_PHONE_NO)) {
          try {
            Schedule schedule = new SmsSchedule(smsMessage.getMessageBody());
            Intent result = new Intent(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
            result.putExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT, (Serializable) schedule);
            EventBus.getDefault().postSticky(result);
          } catch (StopNotFoundException | StopTimesNotAvailableException e) {
            EventBus.getDefault()
                .postSticky(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION));
          }
          break;
        }
      }
    }
  }
}
