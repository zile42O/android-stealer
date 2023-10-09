package zile.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GPS_Receiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.ACTION_LOCKED_BOOT_COMPLETED")) {
			Log.i("GPS_Receiver", "BroadcastReceiver");
			Intent serviceIntent = new Intent(context, GPS_Runner.class);
			context.startService(serviceIntent);
		}
	}
}