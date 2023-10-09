package zile.gps;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class GPS_Runner extends Service {
	private static boolean isRunning = false;

	private Handler handler;
	private Runnable runnable;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isRunning) {
			Log.i("GPS_Runner", "onStartCommand");
			handler = new Handler();
			runnable = new Runnable() {
				@Override
				public void run() {
					Intent serviceIntent = new Intent(GPS_Runner.this, LocationService.class);
					ContextCompat.startForegroundService(GPS_Runner.this, serviceIntent);
					handler.postDelayed(this, 5*30 * 1000);
				}
			};
			handler.postDelayed(runnable, 5*30 * 1000);
			isRunning = true;
		} else {
			Toast.makeText(this, "GPS is already initialized.", Toast.LENGTH_SHORT).show();
		}
		return START_STICKY;
	}

	@Override
	public boolean stopService(Intent name) {
		handler.removeCallbacks(runnable);
		isRunning = false;
		return false;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}