package com.github.mjaremczuk.verificationtask;

import android.util.Log;
import java.io.IOException;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class T3 extends Thread {

	private final List<String> list;
	private String serverUrl;
	private OkHttpClient client;
	private boolean interrupt = false;

	public T3(List<String> list, String serverUrl) {
		super();
		this.serverUrl = serverUrl;
		this.list = list;
		client = new OkHttpClient();
	}

	@Override
	public void run() {
		synchronized (list) {
			try {
				list.wait();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!interrupt) {
				Log.d("thread3Handler", "List items: " + list);
				makeServerCall();
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

	private void makeServerCall() {
		try {
			Response response = client.newCall(createRequest()).execute();
			Log.d("Thread 3", "makeServerCall: response is successful: " + response.isSuccessful());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Request createRequest() {
		return new Request.Builder()
				.url(serverUrl)
				.post(createRequestBody())
				.build();
	}

	private RequestBody createRequestBody() {
		return new FormBody.Builder()
				.add("an_key", buildStringFromList()).build();
	}

	private String buildStringFromList() {
		StringBuilder builder = new StringBuilder();
		for (String item : list) {
			builder.append(item);
		}
		return builder.toString();
	}
}
