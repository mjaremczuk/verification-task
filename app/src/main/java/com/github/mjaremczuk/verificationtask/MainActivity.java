package com.github.mjaremczuk.verificationtask;

import android.Manifest.permission;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.github.mjaremczuk.verificationtask.PermissionManager.PermissionRequestCallback;
import com.github.mjaremczuk.verificationtask.PermissionManager.RationaleResponse;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionRequestCallback {

	public final List<String> L = new ArrayList<>();
	T1 mThread1 = new T1(L);
	T2 mThread2 = new T2(L);
	T3 mThread3 = new T3(L, "http://google.com");
	LocationManager locationManager;
	private boolean started = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.start).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				askForLocationPermissionAndStartIfGranted();
			}
		});
		findViewById(R.id.stop).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopThreads();
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionManager.onPermissionRequestResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopThreads();
	}

	private void askForLocationPermissionAndStartIfGranted() {
		PermissionManager.askPermission(this, permission.ACCESS_FINE_LOCATION, this);
	}

	private void startThreads() {
		if (!started) {
			mThread1.setUpLocationUpdates(this, locationManager);
			mThread2.setUpBatteryBroadcast(this);
			mThread1.start();
			mThread2.start();
			mThread3.start();
			started = true;
		}
	}

	private void stopThreads() {
		started = false;
		stopThread(mThread1);
		stopThread(mThread2);
		stopThread(mThread3);
		L.clear();
	}

	private void stopThread(Thread thread) {
		if (thread != null) {
			if (!thread.isInterrupted()) {
				thread.interrupt();
			}
		}
	}

	@Override
	public void onPermissionGranted(String permission) {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		checkIfGpsIsEnabledAndRunThreads();
	}

	@Override
	public void onPermissionDenied(String permission) {
		// NO-OP
	}

	@Override
	public void onShouldShowRationale(RationaleResponse rationaleResponse) {
		// NO-OP
	}

	private void checkIfGpsIsEnabledAndRunThreads() {
		boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (isEnabled) {
			startThreads();
		}
		else {
			Toast.makeText(this, "GPS is disabled, turn it on", Toast.LENGTH_SHORT).show();
		}
	}
}