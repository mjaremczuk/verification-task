package com.github.mjaremczuk.verificationtask;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import java.util.List;
import java.util.Locale;

public class T1 extends Thread implements LocationListener {

	private static final int SLEEP_TIME = 1000;
	private static final int LOCATION_UPDATES_INTERVAL = 500;
	private static final int LOCATION_UPDATES_DISTANCE = 1;
	static final int ITEM_COUNT_TO_NOTIFY_THREAD = 4;
	private final List<String> list;
	private Location lastLocation;
	private LocationManager locationManager;
	private boolean interrupt = false;

	public T1(List<String> list) {
		super();
		this.list = list;
	}

	public void setUpLocationUpdates(Context context, LocationManager locationManager) {
		this.locationManager = locationManager;
		lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(context, permission.ACCESS_COARSE_LOCATION) !=
						PackageManager.PERMISSION_GRANTED) {
			return;
		}
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				LOCATION_UPDATES_INTERVAL,
				LOCATION_UPDATES_DISTANCE,
				this);
	}

	@Override
	public void run() {
		while (true) {
			if (interrupt) {
				break;
			}
			try {
				sleepAndAddItem();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sleepAndAddItem() throws InterruptedException {
		sleep(SLEEP_TIME);
		synchronized (list) {
			if (lastLocation != null) {
				list.add(createLocationString());
				if (list.size() >= ITEM_COUNT_TO_NOTIFY_THREAD) {
					list.notifyAll();
				}
			}
		}
	}

	private String createLocationString() {
		return String.format(Locale.ENGLISH, "Lat:%fLon:%f", lastLocation.getLatitude(), lastLocation.getLongitude());
	}

	@Override
	public synchronized void start() {
		interrupt = false;
		super.start();
	}

	@Override
	public void interrupt() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
		interrupt = true;
		super.interrupt();
	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// NO-OP
	}

	@Override
	public void onProviderEnabled(String provider) {
		// NO-OP
	}

	@Override
	public void onProviderDisabled(String provider) {
		// NO-OP
	}
}