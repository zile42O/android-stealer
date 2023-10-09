package zile.gps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_CODE = 14201;

	private final String[] permissionList = {
			android.Manifest.permission.ACCESS_FINE_LOCATION,
			android.Manifest.permission.ACCESS_COARSE_LOCATION,
			android.Manifest.permission.ACCESS_NETWORK_STATE,
			android.Manifest.permission.INTERNET,
			android.Manifest.permission.READ_PHONE_NUMBERS,
			android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
			android.Manifest.permission.READ_PHONE_STATE,
			android.Manifest.permission.ACCESS_WIFI_STATE
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkPermissions();
	}
	private void checkPermissions() {
		List<String> ungrantedPermissions = new ArrayList<>();

		for (String permission : permissionList) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				ungrantedPermissions.add(permission);
			}
		}

		if (!ungrantedPermissions.isEmpty()) {
			ActivityCompat.requestPermissions(this, ungrantedPermissions.toArray(new String[0]), REQUEST_CODE);
		}
		if (isNetworkAvailable()) {
			Toast.makeText(this, "Starting Zile GPS service..", Toast.LENGTH_SHORT).show();
			scheduleLocationJob();
		} else {
			Toast.makeText(this, "This app requires valid internet connection.", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE) {
			for (int grantResult : grantResults) {
				if (grantResult != PackageManager.PERMISSION_GRANTED) {
					// If the user didn't grant the permission, show a message
					Toast.makeText(this, "Please allow all permision for this app.", Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
			}
			if (isNetworkAvailable()) {
				Toast.makeText(this, "Starting Zile GPS service..", Toast.LENGTH_SHORT).show();
				scheduleLocationJob();
			} else {
				Toast.makeText(this, "This app requires valid internet connection.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	private void scheduleLocationJob() {
		Intent intent = new Intent(this, GPS_Runner.class);
		startService(intent);
		finish();
		moveTaskToBack(true);
	}
}
