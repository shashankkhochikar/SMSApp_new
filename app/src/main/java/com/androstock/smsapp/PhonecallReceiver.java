package com.androstock.smsapp;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class PhonecallReceiver extends BroadcastReceiver {

    public static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;
    //public static MyCallsAppDatabase myCallsAppDatabase;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static int cuttStateFlag4WhatsappMsg = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
        {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            if (!intent.getExtras().containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                Log.i("Call receiver", "skipping intent=" + intent + ", extras=" + intent.getExtras() + " - no number was supplied");
                return;
            }
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
            //Log.e("123","Mobile Number: "+number);
            onCallStateChanged(context, state, number, intent);
        }
    }

    public void onCallStateChanged(Context context, int state, String number, Intent intent) {
        Log.e("123","Inside Mobile Number: "+number+" State: "+ state + " ~~ LastState: "+lastState );
        if (lastState == state) {
            //No change, debounce extras
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                //Log.e("123","onIncomingCallStarted Mobile Number: "+savedNumber + "  "+number);
                onIncomingCallStarted(context, savedNumber, callStartTime, intent);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                   // Log.e("123","onOutgoingCallStarted Mobile Number: "+savedNumber);
                    onOutgoingCallStarted(context, savedNumber, callStartTime, intent);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                   // Log.e("123","onMissedCall Mobile Number: "+savedNumber);
                    onMissedCall(context, savedNumber, callStartTime, intent);
                } else if (isIncoming) {
                    //Log.e("123","onIncomingCallEnded Mobile Number: "+savedNumber);
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date(), intent);
                } else {
                    //Log.e("123","onOutgoingCallEnded Mobile Number: "+savedNumber);
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date(), intent);
                }
                break;
        }
        lastState = state;
       /* Intent intent1 = new Intent("CallApp");
        context.sendBroadcast(intent1);*/
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start, Intent intent) {
        Toast.makeText(ctx, "calling from " + number, Toast.LENGTH_SHORT).show();
        //Function.sendSMS(number, "onIncomingCallStarted");
        pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
        String str = pref.getString("single_temp", null); // getting String
        if(str != null){
            String onIncomingCallStarted = pref.getString("onIncomingCallStarted", null); // getting String
            String sendWAMsg = pref.getString("sendWAmsg", null); // getting String for Whatsapp Msg
            if(onIncomingCallStarted != null) {
                if(onIncomingCallStarted.equals("1")){
                    //chk_onMissedCall.setChecked(true);
                    Function.sendSMS(ctx,number, str);
                    if(sendWAMsg.equals("1")){
                        cuttStateFlag4WhatsappMsg = 1;
                        Function.sendWAMessage(ctx,number.replace("+91", "") + "", str);
                    }
                }
            }
        }
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start, Intent intent) {
        Toast.makeText(ctx, "calling to " + number, Toast.LENGTH_SHORT).show();
        //Function.sendSMS(number, "onOutgoingCallStarted");
        pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
        String str = pref.getString("single_temp", null); // getting String
        if(str != null){
            String onOutgoingCallStarted = pref.getString("onOutgoingCallStarted", null); // getting String
            String sendWAMsg = pref.getString("sendWAmsg", null); // getting String for Whatsapp Msg
            if(onOutgoingCallStarted != null) {
                if(onOutgoingCallStarted.equals("1")){
                    //chk_onMissedCall.setChecked(true);
                    Function.sendSMS(ctx,number, str);
                    if(sendWAMsg.equals("1")){
                        cuttStateFlag4WhatsappMsg = 2;
                        Function.sendWAMessage(ctx,number.replace("+91", "") + "", str);
                    }

                }
            }
        }
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end, Intent intent) {
        Toast.makeText(ctx, "calling from " + number + " ended ", Toast.LENGTH_SHORT).show();
        //saveData(ctx, number, intent, "Incoming Call");
        //pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE); // 0 - for private mode
        //String str = pref.getString("single_temp", null); // getting String
       // if(str != null){
       //     Function.sendSMS(ctx,number, str);
       // }
        pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
        String str = pref.getString("single_temp", null); // getting String
        if(str != null){
            String onIncomingCallEnded = pref.getString("onIncomingCallEnded", null); // getting String
            String sendWAMsg = pref.getString("sendWAmsg", null); // getting String for Whatsapp Msg
            if(onIncomingCallEnded != null) {
                if(onIncomingCallEnded.equals("1")){
                    //chk_onMissedCall.setChecked(true);
                    Function.sendSMS(ctx,number, str);
                    if(sendWAMsg.equals("1")){
                        cuttStateFlag4WhatsappMsg = 3;
                        Function.sendWAMessage(ctx,number.replace("+91", "") + "", str);
                    }
                }
            }
        }
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end, Intent intent) {
        Toast.makeText(ctx, "calling to " + number + " ended ", Toast.LENGTH_SHORT).show();
        //saveData(ctx, number, intent, "Outgoing Call");
        //Function.sendSMS(number, "onOutgoingCallEnded");
        pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
        String str = pref.getString("single_temp", null); // getting String
        if(str != null){
            String onOutgoingCallEnded = pref.getString("onOutgoingCallEnded", null); // getting String
            String sendWAMsg = pref.getString("sendWAmsg", null); // getting String for Whatsapp Msg
            if(onOutgoingCallEnded != null) {
                if(onOutgoingCallEnded.equals("1")){
                    //chk_onMissedCall.setChecked(true);
                    Function.sendSMS(ctx,number, str);
                    if(sendWAMsg.equals("1")){
                        cuttStateFlag4WhatsappMsg = 4;
                        Function.sendWAMessage(ctx,number.replace("+91", "") + "", str);
                    }

                }
            }
        }
    }

    protected void onMissedCall(Context ctx, String number, Date start, Intent intent) {
        Toast.makeText(ctx, "missed call from " + number + " sim ", Toast.LENGTH_SHORT).show();
        //saveData(ctx, number, intent, "Missed Call");

        pref = ctx.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
        String str = pref.getString("single_temp", null); // getting String
        if(str != null){
            String onMissedCall = pref.getString("onMissedCall", null); // getting String
            String sendWAMsg = pref.getString("sendWAmsg", null); // getting String for Whatsapp Msg
            if(onMissedCall != null) {
                if(onMissedCall.equals("1")){
                    //chk_onMissedCall.setChecked(true);
                    Function.sendSMS(ctx,number, str);
                    if(sendWAMsg.equals("1")){
                        cuttStateFlag4WhatsappMsg = 5;
                        Function.sendWAMessage(ctx,number.replace("+91", "") + "", str);
                    }
                }
            }
        }
    }
/*
    //@SuppressLint("ServiceCast")
    protected void saveData(Context ctx, String number, Intent intent, String callType) {

        *//*myCallsAppDatabase = Room.databaseBuilder(ctx, MyCallsAppDatabase.class, "calldb")
                .allowMainThreadQueries()
                .build();

        number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        number = filterNumber(number);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aaa");
        String dateString = dateFormat.format(new Date(System.currentTimeMillis()));
        CallLog callLog = new CallLog();
        callLog.setMobile(number);
        callLog.setCallType(callType);
        callLog.setTime(dateString);
        myCallsAppDatabase.myCallDao().addCallDetails(callLog);*//*
    }*/
}
