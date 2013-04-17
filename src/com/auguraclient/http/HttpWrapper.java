package com.auguraclient.http;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class HttpWrapper {

	private static int TIME_OUT = 15000;

	public String sendHttpGetRequest(String url) {
		Message msg = new Message();
		new GetWorker(msg, url).start();
		synchronized (msg) {
			try {
				msg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return msg.getResponse();
		}
	}

	public String sendUploadPhotoRequest(String url, String imagPath) {
		Message msg = new Message();
		new UploadWorker(msg, url, imagPath).start();
		synchronized (msg) {
			try {
				msg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return msg.getResponse();
		}

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

		private String url;

		private String imagePath;

		public UploadWorker(Message msg, String url, String imagePath) {
			this.msg = msg;
			this.url = url;
			this.imagePath = imagePath;
		}

		public void run() {
			if( imagePath  == null || url == null) {
				Log.e(Constants.TAG, "=============error photo path or api url is null "+imagePath+"    "+ url);
				return;
			}
			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			DataInputStream inStream = null;
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1024 * 1024;
			String responseFromServer = "";
			try {
				FileInputStream fileInputStream = new FileInputStream(new File(
						imagePath));
				// open a URL connection to the Servlet
				URL photoURL = new URL(this.url);
				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) photoURL.openConnection();
				// Allow Inputs
				conn.setDoInput(true);
				// Allow Outputs
				conn.setDoOutput(true);
				// Don't use a cached copy.
				conn.setUseCaches(false);
				// Use a post method.
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				dos = new DataOutputStream(conn.getOutputStream());
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; photoname=\""+imagePath+"\";photo=\""
						+ imagePath + "\"" + lineEnd);
				dos.writeBytes(lineEnd);
				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				// close streams
				Log.e(Constants.TAG, "File is written");
				fileInputStream.close();
				dos.flush();
				dos.close();
			} catch (MalformedURLException ex) {
				Log.e(Constants.TAG, "error: " + ex.getMessage(), ex);
			} catch (IOException ioe) {
				Log.e(Constants.TAG, "error: " + ioe.getMessage(), ioe);
			}
			try {
				inStream = new DataInputStream(conn.getInputStream());
				String str;

				while ((str = inStream.readLine()) != null) {
					Log.e(Constants.TAG, "Server Response " + str);
				}
				inStream.close();

			} catch (IOException ioex) {
				Log.e(Constants.TAG, "error: " + ioex.getMessage(), ioex);
			}
			
			synchronized(msg) {
				msg.flag = 1;
				msg.notify();
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
			Timer timer = new Timer();
			HttpParams httpparams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpparams, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpparams, TIME_OUT);
			HttpClient httpClient = new DefaultHttpClient(httpparams);
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					synchronized (msg) {
						msg.flag = -1;

						msg.notify();
					}
				}

			}, TIME_OUT);

			HttpGet httpget = null;
			try {
				Log.i(Constants.TAG, "sending url:  " + url);
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
			} catch (Exception e) {
				msg.flag = -2;
				Log.e(Constants.TAG, "", e);
			} finally {
				synchronized (msg) {
					msg.notify();
				}
				if (httpget != null) {
					httpClient.getConnectionManager().shutdown();
					httpget.abort();
					timer.cancel();
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
