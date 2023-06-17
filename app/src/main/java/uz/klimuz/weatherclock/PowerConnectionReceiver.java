package uz.klimuz.weatherclock;

import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class PowerConnectionReceiver extends BroadcastReceiver {
//    boolean acCharge = false;
//    boolean usbCharge = false;
//    public static boolean powerConnected = false;
//    Float batteryPct = 0.0f;
    @Override
    public void onReceive(Context context, Intent intent) {

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BATTERY_PLUGGED_AC;
        boolean powerConnected = usbCharge || acCharge;

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;

        MainActivity.batteryPercentageTextView.setText(batteryPct + "%");
        if (!powerConnected){
            MainActivity.powerImageView.setImageResource(R.drawable.baseline_power_off_50);
        }else {
            MainActivity.powerImageView.setImageResource(R.drawable.baseline_power_50);
        }
    }

}
