package com.tagniam.drtsms.schedule.fetcher;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import java.io.Serializable;

public class SmsScheduleService extends IntentService {
  private final String DRT_PHONE_NO = "8447460497";
  private DrtSmsReceiver drtSmsReceiver;
  private BroadcastReceiver drtSmsSender;

  public SmsScheduleService() {
    super(SmsScheduleService.class.getSimpleName());
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Get stop id and send sms
    String stopId = intent.getStringExtra(ScheduleFetcher.SCHEDULE_FETCH_STOP_ID);
    sendSmsToDrt(stopId);

    // Wait for sms to be received
    registerDrtSmsReceiver();
  }

  /**
   * Attempt to send the SMS containing the bus stop id to DRT. If the attempt fails, then intent
   * with action SCHEDULE_FETCH_FAIL_ACTION will be broadcast.
   *
   * @param stopId bus stop id for DRT bus
   */
  private void sendSmsToDrt(String stopId) {
    SmsManager smsSender = SmsManager.getDefault();

    // Set up pending intents to track success/fail sent status
    PendingIntent sentPI =
        PendingIntent.getBroadcast(
            getApplicationContext(), 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT), 0);
    PendingIntent deliveredPI =
        PendingIntent.getBroadcast(
            getApplicationContext(),
            0,
            new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED),
            0);
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT);
    intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED);

    // Set up broadcast receiver to track sms sent status
    drtSmsSender =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
              case Activity.RESULT_OK:
                // No need for sender anymore
                getApplicationContext().unregisterReceiver(drtSmsSender);
                break;
              default:
                // Sms failed, broadcast the fail and stop service
                sendBroadcast(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION));
                getApplicationContext().unregisterReceiver(drtSmsSender);
                stopSelf();
                break;
            }
          }
        };
    getApplicationContext().registerReceiver(drtSmsSender, intentFilter);

    // Send sms message
    smsSender.sendTextMessage(DRT_PHONE_NO, null, stopId, sentPI, deliveredPI);
  }

  /** Registers a BroadcastReceiver and waits for the DRT sms to be received. */
  private void registerDrtSmsReceiver() {
    drtSmsReceiver = new DrtSmsReceiver();
    IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
    getApplicationContext().registerReceiver(drtSmsReceiver, intentFilter);
  }

  /** Used to execute code on receiving the SMS from DRT. */
  private class DrtSmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
          String from = smsMessage.getOriginatingAddress();

          // Only analyze if from DRT
          // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
          if (from.equals(DRT_PHONE_NO)) {
            broadcastSchedule(smsMessage.getMessageBody());
            break;
          }
        }
      }
    }

    /**
     * Sends a broadcast containing the schedule information.
     *
     * @param msg the text msg response from DRT
     */
    private void broadcastSchedule(String msg) {
      // Create schedule from msg
      try {
        Schedule schedule = new SmsSchedule(msg);

        Intent resultIntent = new Intent();
        resultIntent.setAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
        resultIntent.putExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT, (Serializable) schedule);
        sendBroadcast(resultIntent);

      } catch (StopNotFoundException | StopTimesNotAvailableException e) {
        // Stop number not found or not available
        sendBroadcast(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION));
      }

      getApplicationContext().unregisterReceiver(drtSmsReceiver);
    }
  }
}
