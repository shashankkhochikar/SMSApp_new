package com.androstock.smsapp;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.telecom.Call;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class WhatsappAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent (AccessibilityEvent event) {
        try {
            if (getRootInActiveWindow () == null) {
                return;
            }

            AccessibilityNodeInfoCompat rootInActiveWindow = AccessibilityNodeInfoCompat.wrap (getRootInActiveWindow ());

            // Whatsapp send button id
            List<AccessibilityNodeInfoCompat> sendMessageNodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
            if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty()) {
                return;
            }

            AccessibilityNodeInfoCompat sendMessageButton = sendMessageNodeInfoList.get(0);
            if (!sendMessageButton.isVisibleToUser()) {
                return;
            }

            if (PhonecallReceiver.cuttStateFlag4WhatsappMsg != 0){
                    if(PhonecallReceiver.cuttStateFlag4WhatsappMsg == 1 ||
                        PhonecallReceiver.cuttStateFlag4WhatsappMsg == 2 ||
                        PhonecallReceiver.cuttStateFlag4WhatsappMsg == 3 ||
                        PhonecallReceiver.cuttStateFlag4WhatsappMsg == 4 ||
                        PhonecallReceiver.cuttStateFlag4WhatsappMsg == 5 )
                    {

                        // Now fire a click on the send button
                        sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                        Thread.sleep(500); // hack for certain devices in which the immediate back click is too fast to handle
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        Thread.sleep(500);  // same hack as above

                        performGlobalAction(GLOBAL_ACTION_BACK);

                        // Now go back to your app by clicking on the Android back button twice:
                        // First one to leave the conversation screen
                        // Second one to leave whatsapp
                        PhonecallReceiver.cuttStateFlag4WhatsappMsg = 0;
                    }else{
                        Toast.makeText(getApplicationContext(),"SMSez App Error",Toast.LENGTH_LONG).show();
                    }
            }else{
                //Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInterrupt() {
        System.out.println("Whatsapp Accessibility Service onInterrupt");
    }
}