/*
 * Copyright (c) 2016. Richard P. Parkins, M. A.
 */

package uk.co.yahoo.p1rpp.powertest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/*
String EXTRA_DOCK_STATE

Used as an int extra field in ACTION_DOCK_EVENT intents to request the dock state. Possible values are EXTRA_DOCK_STATE_UNDOCKED, EXTRA_DOCK_STATE_DESK, or EXTRA_DOCK_STATE_CAR, or EXTRA_DOCK_STATE_LE_DESK, or EXTRA_DOCK_STATE_HE_DESK.
// Are we charging / charged?
int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL;

// How are we charging?
int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
boolean wirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;


 */

public class MainActivity extends Activity {
    private static TextView timeMessage;
    private static TextView chargingMessage;
    private static TextView dockMessage;
    private static TextView theText;

    // 0 USB not connected or connected to dumb charger
    // 1 device is USB host to some peripheral
    // 2 device is USB slave and receiving power from a USB host
    private static int usbState;

    private static void log(String s) {
        String ss = DateFormat.getDateTimeInstance().format(new Date())
                    + ": " + s + "\n";
        theText.setText(theText.getText().toString().concat(ss));
    }

    private static void updateCharging(Intent intent) {
        switch(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1))
        {
            case BatteryManager.BATTERY_PLUGGED_AC:
                //chargingMessage.setText(R.string.charging_AC);
                log("BATTERY_PLUGGED_AC");
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                //if (usbState == 2)
                //{
                //    chargingMessage.setText
                //(R.string.charging_slave);
                //}
                //else
                //{
                //    chargingMessage.setText
                //(R.string.charging_USB);
                //}
                log("BATTERY_PLUGGED_USB");
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                //chargingMessage.setText(R.string.charging_wireless);
                log("BATTERY_PLUGGED_WIRELESS");
                break;
            case -1:
                log("no EXTRA_PLUGGED");
            default:
                //chargingMessage.setText(R.string.charging_none);
                log("EXTRA_PLUGGED=".concat(String.valueOf(
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1))));
                break;
        }
    }

    private static void updateDocking(Intent intent) {
        if (intent == null)
        {
            log("registerReceiver(null, new IntentFilter(Intent"
                + ".ACTION_DOCK_EVENT)) returned null");
        }
        else
        {
            switch (intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1))
            {
                case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                    log("EXTRA_DOCK_STATE_UNDOCKED");
                    break;
                case Intent.EXTRA_DOCK_STATE_CAR:
                    //dockMessage.setText(R.string.docked_car);
                    log("EXTRA_DOCK_STATE_CAR");
                    break;
                case Intent.EXTRA_DOCK_STATE_DESK:
                    //dockMessage.setText(R.string.docked_desk);
                    log("EXTRA_DOCK_STATE_DESK");
                    break;
                case Intent.EXTRA_DOCK_STATE_LE_DESK:
                    //dockMessage.setText(R.string.docked_le_desk);
                    log("EXTRA_DOCK_STATE_LE_DESK");
                    break;
                case Intent.EXTRA_DOCK_STATE_HE_DESK:
                    //dockMessage.setText(R.string.docked_he_desk);
                    log("EXTRA_DOCK_STATE_HE_DESK");
                    break;
                default:
                    //dockMessage.setText(R.string.not_docked);
                    log("EXTRA_DOCK_STATE=".concat(String.valueOf(
                        intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1))));
                    break;
            }
        }
    }

    private static void getUsbState(Context c) {
        UsbManager manager
            = (UsbManager) c.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = manager.getDeviceList();
        if (map.isEmpty())
        {
            log("getDeviceList() returns empty map");
            if (manager.getAccessoryList() == null)
            {
                log("getAccessoryList() returns null");
                usbState = 0;
            }
            else
            {
                log("getAccessoryList() returns non-null");
                usbState = 1;
            }
        }
        else
        {
            log("getDeviceList() returns nonempty map");
            usbState = 2;
        }
    }

    public static class PowerChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //timeMessage.setText(
            //    DateFormat.getDateTimeInstance().format(new Date()));
            if (action == Intent.ACTION_BATTERY_CHANGED)
            {
                log("ACTION_BATTERY_CHANGED");
                updateCharging(intent);
                getUsbState(context);
            }
            else if (action == Intent.ACTION_DOCK_EVENT)
            {
                log("ACTION_DOCK_EVENT");
                MainActivity.updateDocking(intent);
            }
            else if (action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            {
                usbState = 1;
                //chargingMessage.setText(R.string.USB_host);
                log("ACTION_USB_ACCESSORY_ATTACHED");
            }
            else if (action == UsbManager.ACTION_USB_ACCESSORY_DETACHED)
            {
                usbState = 0;
                //chargingMessage.setText(R.string.charging_none);
                log("ACTION_USB_ACCESSORY_DETACHED");
            }
            else if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED)
            {
                usbState = 2;
                //chargingMessage.setText
                //(R.string.charging_slave);
                log("ACTION_USB_DEVICE_ATTACHED");
            }
            else if (action == UsbManager.ACTION_USB_DEVICE_DETACHED)
            {
                usbState = 0;
                //chargingMessage.setText(R.string.charging_none);
                log("ACTION_USB_DEVICE_DETACHED");
            }
            else
            {
                log("Unexpected action ".concat(action));
            }
        }
    }
    private PowerChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //timeMessage = new TextView(this);
        //chargingMessage = new TextView(this);
        //dockMessage = new TextView(this);
        receiver = new PowerChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        LinearLayout rl = (LinearLayout)findViewById(R.id.activity_main);
        rl.removeAllViews();
        timeMessage.setText(
            DateFormat.getDateTimeInstance().format(new Date()));
        rl.addView(timeMessage);
        */
        theText = (TextView)findViewById(R.id.thetext);
        theText.setText("");
        log("onResume");
        getUsbState(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        updateCharging(registerReceiver(null, filter));
        //rl.addView(chargingMessage);
        filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        updateDocking(registerReceiver(null, filter));
        //rl.addView(dockMessage);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
