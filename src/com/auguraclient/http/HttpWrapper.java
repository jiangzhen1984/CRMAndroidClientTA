package com.auguraclient.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import android.util.Log;

import com.auguraclient.util.Constants;
import com.auguraclient.util.LogRecorder;

public class HttpWrapper {

	private static int TIME_OUT = 15000;

	public String sendHttpGetRequest(String url) {
		Message msg = new Message();
		Timer timer = new Timer();
		startTimer(timer, msg);
		new GetWorker(msg, url).start();
		synchronized (msg) {
			try {
				msg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timer.cancel();
			return msg.getResponse();
		}
	}

	public String sendUploadPhotoRequest(String url, String imagPath) {
		Message msg = new Message();
		Timer timer = new Timer();
		startTimer(timer, msg);
		new UploadWorker(msg, url, imagPath).start();
		synchronized (msg) {
			try {
				msg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timer.cancel();
			return msg.getResponse();
		}

	}

	private void startTimer(Timer timer, final Message msg) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				synchronized (msg) {
					msg.flag = -1;

					msg.notify();
				}
			}

		}, TIME_OUT);
	}

	class Message {
		private String response;

		private int flag = 0;

		public String getResponse() {
			return response;
		}

		public void setResponse(String response) {
			this.response = response;
		}

	}

	class UploadWorker extends Thread {
		private Message msg;

		private String urlAPI;

		private String imagePath;

		public UploadWorker(Message msg, String url, String imagePath) {
			this.msg = msg;
			this.urlAPI = url;
			this.imagePath = imagePath;
		}

		private final String CrLf = "\r\n";

		public void run() {
			URLConnection conn = null;
			OutputStream os = null;
			InputStream is = null;
			String photoName = getPhotoName(imagePath);
			if(photoName == null) {
				Log.e(Constants.TAG, "can't get photo name");
				synchronized (msg) {
					msg.flag = 1;
					msg.notify();
				}
				return;
			}

			try {
				URL url = new URL(urlAPI);
				Log.i(Constants.TAG, urlAPI);
				conn = url.openConnection();
				conn.setDoOutput(true);

				InputStream imgIs = new FileInputStream(new File(imagePath));
				byte[] imgData = new byte[imgIs.available()];
				imgIs.read(imgData);
				imgIs.close();

				String message1 = "";
				message1 += "Accept:"
						+ " text/html,application/xml,"
						+ "application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
						+ CrLf;
				message1 += "Connection:" + " Keep-Alive" + CrLf;
				message1 += "User-Agent:"
						+ " Mozilla/5.0 (X11; U; Linux "
						+ "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)"
						+ CrLf;

				
				message1 += "-----------------------------4664151417711" + CrLf;
				message1 += "Content-Disposition: form-data; name=\"photoname\";"
						+ CrLf+ CrLf;
				message1 += photoName + CrLf;
				message1 += CrLf + "-----------------------------4664151417711--"+ CrLf+ CrLf;
				
				message1 += "-----------------------------4664151417711" + CrLf;
				message1 += "Content-Disposition: form-data; name=\"photo\"; filename=\""+imagePath.trim()+"\""
						+ CrLf;
				message1 += "Content-Type: image/jpeg" + CrLf;
				message1 += CrLf;
				
	

				// the image is sent between the messages in the multipart
				// message.

				String message2 = "";
				message2 += CrLf
						+ "-----------------------------4664151417711--" + CrLf;

				conn.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=---------------------------4664151417711");
				// might not need to specify the content-length when sending
				// chunked
				// data.
				conn.setRequestProperty(
						"Content-Length",
						String.valueOf((message1.length() + message2.length() + imgData.length)));

				os = conn.getOutputStream();

				Log.i(Constants.TAG, message1);
				LogRecorder.i(message1);
				os.write(message1.getBytes());

				// FIXME
				int index = 0;
				int size = 1024;
				do {
					if ((index + size) > imgData.length) {
						size = imgData.length - index;
					}
					os.write(imgData, index, size);
					index += size;
				} while (index < imgData.length);

				os.write(message2.getBytes());
				os.flush();

				is = conn.getInputStream();
				StringBuilder sb = new StringBuilder();
				char buff = 512;
				int len;
				byte[] data = new byte[buff];
				do {
					len = is.read(data);

					if (len > 0) {
						sb.append(new String(data, 0, len));
					}
				} while (len > 0);

				Log.i(Constants.TAG, "=========================");
				Log.i(Constants.TAG, sb.toString());
				Log.i(Constants.TAG, "=========================");
				LogRecorder.i("=========================");
				LogRecorder.i(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
				LogRecorder.i(e.getMessage());
			} finally {
				try {
					os.close();
				} catch (Exception e) {
				}
				try {
					is.close();
				} catch (Exception e) {
				}

			}

			synchronized (msg) {
				msg.flag = 1;
				msg.notify();
			}

		}
		
		
		private String getPhotoName(String photoPath) {
			int index  = photoPath.lastIndexOf("/");
			if(index != -1) {
				return photoPath.substring(index);
			} else {
				return null;
			}
		}

	}

	class GetWorker extends Thread {

		private Message msg;

		private String url;

		public GetWorker(Message msg, String url) {
			this.msg = msg;
			this.url = url;
		}

		public void run() {
			HttpParams httpparams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpparams, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpparams, TIME_OUT);
			HttpClient httpClient = new DefaultHttpClient(httpparams);
			HttpGet httpget = null;
			try {
				Log.i(Constants.TAG, "sending url:  " + url);
				LogRecorder.i(url);
				httpget = new HttpGet(url);

				HttpResponse response = null;

				httpget.setHeader(
						"User-Agent",
						"Mozilla/5.0 (X11; U; Linux "
								+ "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");

				httpget.setHeader(
						"Accept",
						"text/html,application/xml,"
								+ "application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

				httpget.setHeader("Content-Type",
						"application/x-www-form-urlencoded");

				response = httpClient.execute(httpget, new BasicHttpContext());
				msg.response = request(response);
				Log.i(Constants.TAG, "get data " + msg.response);
				LogRecorder.i(msg.response);
			} catch (Exception e) {
				msg.flag = -2;
				Log.e(Constants.TAG, "", e);
				LogRecorder.i(e.getMessage());
			} finally {
				synchronized (msg) {
					msg.notify();
				}
				if (httpget != null) {
					httpClient.getConnectionManager().shutdown();
					httpget.abort();
				}
			}
		}

		private String request(HttpResponse response) throws Exception {

			InputStream in = null;
			BufferedReader reader = null;
			try {

				in = response.getEntity().getContent();
				reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder str = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					str.append(line + "\n");
					line = null;
				}
				return str.toString();
			} catch (Exception ex) {
				LogRecorder.i(ex.getMessage());
				throw ex;
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				in = null;
				if (reader != null)
					try {
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				reader = null;
			}

		}

	}

}
