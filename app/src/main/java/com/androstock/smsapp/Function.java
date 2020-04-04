package com.androstock.smsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by SHAJIB on 7/10/2017.
 */

public class Function {


    static final String _ID = "_id";
    static final String KEY_THREAD_ID = "thread_id";
    static final String KEY_NAME = "name";
    static final String KEY_PHONE = "phone";
    static final String KEY_MSG = "msg";
    static final String KEY_TYPE = "type";
    static final String KEY_TIMESTAMP = "timestamp";
    static final String KEY_TIME = "time";

    static SharedPreferences pref = null;
    //static final SharedPreferences.Editor editor;


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String converToTime(String timestamp) {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        return formatter.format(date);
    }


    public static HashMap<String, String> mappingInbox(String _id, String thread_id, String name, String phone, String msg, String type, String timestamp, String time) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(_ID, _id);
        map.put(KEY_THREAD_ID, thread_id);
        map.put(KEY_NAME, name);
        map.put(KEY_PHONE, phone);
        map.put(KEY_MSG, msg);
        map.put(KEY_TYPE, type);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        return map;
    }


    public static ArrayList<HashMap<String, String>> removeDuplicates(ArrayList<HashMap<String, String>> smsList) {
        ArrayList<HashMap<String, String>> gpList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < smsList.size(); i++) {
            boolean available = false;
            for (int j = 0; j < gpList.size(); j++) {
                if (Integer.parseInt(gpList.get(j).get(KEY_THREAD_ID)) == Integer.parseInt(smsList.get(i).get(KEY_THREAD_ID))) {
                    available = true;
                    break;
                }
            }

            if (!available) {
                gpList.add(mappingInbox(smsList.get(i).get(_ID), smsList.get(i).get(KEY_THREAD_ID),
                        smsList.get(i).get(KEY_NAME), smsList.get(i).get(KEY_PHONE),
                        smsList.get(i).get(KEY_MSG), smsList.get(i).get(KEY_TYPE),
                        smsList.get(i).get(KEY_TIMESTAMP), smsList.get(i).get(KEY_TIME)));
            }
        }
        return gpList;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static boolean sendSMS(Context c, String toPhoneNumber, String smsMessage) {
        try {
            /*int smsToSendFrom = simCardList.get(0); //assign your desired sim to send sms, or user selected choice
            SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom)
                    .sendTextMessage(phoneNumber, null, msg, sentPI, deliveredPI); //use your phone number, message and pending intent*/

            pref = c.getSharedPreferences("SMSApp", MODE_PRIVATE ); // 0 - for private mode
            String str = pref.getString("def_sim", null); // getting String
            if(str != null){
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.getSmsManagerForSubscriptionId(Integer.parseInt(str)+1).sendTextMessage(toPhoneNumber, null, smsMessage, null, null);
            }
            /*final SubscriptionManager subscriptionManager = SubscriptionManager.from(c);
            if (ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return true;
            }
            final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            int simCount = activeSubscriptionInfoList.size();
           // btnBack.setText(simCount+" Sim available");
            Log.e("123: ","simCount:" +simCount);
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                Log.e("123: ","iccId :"+ subscriptionInfo.getIccId()+" , name : "+ subscriptionInfo.getDisplayName()+" ~~ "+subscriptionInfo.getSubscriptionId());
            }*/

            return true;
        } catch (Exception e) {
            //Toast.makeText(getA)
            e.printStackTrace();
            return false;
        }
    }

    public static void sendWAMessage(Context c, String strMobileNo, String smsMessage) {
        try {
            if (strMobileNo != null && !strMobileNo.equalsIgnoreCase("")) {
                String strWhatsAppNo = "91" + strMobileNo; // E164 format without '+' sign
                System.out.println("strWhatsAppNo: "+strWhatsAppNo);
                Intent intent = new Intent(Intent.ACTION_SEND);
                //Bitmap picBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.promoting_image);
                //if (picBitmap != null) {
                   /* String url = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), picBitmap, "", "");
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));*/
                //} else {
                    intent.setType("text/plain");
               // }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_TEXT, smsMessage);
                intent.putExtra("jid", strWhatsAppNo + "@s.whatsapp.net"); //phone number without "+" prefix
                intent.setPackage("com.whatsapp");
                c.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static String getContactbyPhoneNumber(Context c, String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return phoneNumber;
        }else {
            String name = phoneNumber;
            try {

                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }

            return name;
        }
    }


    public static void createCachedFile (Context context, String key, ArrayList<HashMap<String, String>> dataList) throws IOException {
            FileOutputStream fos = context.openFileOutput (key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject (dataList);
            oos.close ();
            fos.close ();
    }

    public static Object readCachedFile (Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput (key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject ();
        return object;
    }
}
