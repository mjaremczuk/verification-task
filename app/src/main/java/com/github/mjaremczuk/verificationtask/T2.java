package com.github.mjaremczuk.verificationtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import java.util.List;

import static com.github.mjaremczuk.verificationtask.T1.ITEM_COUNT_TO_NOTIFY_THREAD;

public class T2 extends Thread {

	private static final int SLEEP_TIME = 2000;
	private final List<String> list;
	private IntentFilter batteryIntent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	private String batteryLevel;
	private boolean interrupt = false;

	public T2(List<String> list) {
		super();
		this.list = list;
	}

	public void setUpBatteryBroadcast(Context context) {
		Intent batteryStatus = context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				readBatteryLevel(intent);
			}
		}, batteryIntent);
		readBatteryLevel(batteryStatus);
	}

	private void readBatteryLevel(Intent intent) {
		batteryLevel = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (interrupt) {
					break;
				}
				sleepAndAddItem();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void start() {
		interrupt = false;
		super.start();
	}

	@Override
	public void interrupt() {
		interrupt = true;
		super.interrupt();
	}

	private void sleepAndAddItem() throws InterruptedException {
		sleep(SLEEP_TIME);
		synchronized (list) {
			list.add(batteryLevel);
			if (list.size() >= ITEM_COUNT_TO_NOTIFY_THREAD) {
				list.notifyAll();
			}
		}
	}
}
