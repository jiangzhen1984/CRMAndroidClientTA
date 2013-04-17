package com.auguraclient.http;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
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
	
	
	class UploadPhotoWorker extends Thread {
		private Message msg;

		private String url;

		private String imagePath;

		public UploadPhotoWorker(Message msg, String url, String imagePath) {
			this.msg = msg;
			this.url = url;
			this.imagePath = imagePath;
		}

		@Override
		public void run() {
			File f =new File(
					imagePath);
			FileInputStream fileInputStream;
			byte[] b =null ;
			try {
				fileInputStream = new FileInputStream(f);
				 b = new byte[(int)f.length()];
				fileInputStream.read(b);
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ByteArrayEntity arrayEntity=new ByteArrayEntity(b);
	        arrayEntity.setContentType("application/octet-stream");
	        HttpPost httpPost=new HttpPost(url);
	        httpPost.setEntity(arrayEntity);
	        DefaultHttpClient client=new DefaultHttpClient();
	        try {
	            int result=client.execute(httpPost).getStatusLine().getStatusCode();
	            Log.i("huilurry","]]]="+result);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
			
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

		
		private final String CrLf = "\r\n";
		public void run() {
			 URLConnection conn = null;
		        OutputStream os = null;
		        InputStream is = null;

		        try {
		            URL url = new URL("http://crm.augura.net/AuguraClasses/AsiactionMobile/loadphoto.php?photoname=test_id.jpg");
		            System.out.println("url:" + url);
		            conn = url.openConnection();
		            conn.setDoOutput(true);

		            String postData = "";

		            InputStream imgIs = new FileInputStream(new File(imagePath));
		            byte[] imgData = new byte[imgIs.available()];
		            imgIs.read(imgData);

		            String message1 = "";
		            message1 += "-----------------------------4664151417711" + CrLf;
		            message1 += "Content-Disposition: form-data; name=\"photo\"; filename=\"test_1234.jpg\""
		                    + CrLf;
		            message1 += "Content-Type: image/jpeg" + CrLf;
		            message1 += CrLf;

		            // the image is sent between the messages in the multipart message.

		            String message2 = "";
		            message2 += CrLf + "-----------------------------4664151417711--"
		                    + CrLf;

		            conn.setRequestProperty("Content-Type",
		                    "multipart/form-data; boundary=---------------------------4664151417711");
		            // might not need to specify the content-length when sending chunked
		            // data.
		            conn.setRequestProperty("Content-Length", String.valueOf((message1
		                    .length() + message2.length() + imgData.length)));

		            System.out.println("open os");
		            os = conn.getOutputStream();

		            System.out.println(message1);
		            os.write(message1.getBytes());

		            // SEND THE IMAGE
		            int index = 0;
		            int size = 1024;
		            do {
		                System.out.println("write:" + index);
		                if ((index + size) > imgData.length) {
		                    size = imgData.length - index;
		                }
		                os.write(imgData, index, size);
		                index += size;
		            } while (index < imgData.length);
		            System.out.println("written:" + index);

		            System.out.println(message2);
		            os.write(message2.getBytes());
		            os.flush();

		            System.out.println("open is");
		            is = conn.getInputStream();

		            char buff = 512;
		            int len;
		            byte[] data = new byte[buff];
		            do {
		                System.out.println("READ");
		                len = is.read(data);

		                if (len > 0) {
		                    System.out.println(new String(data, 0, len));
		                }
		            } while (len > 0);

		            System.out.println("DONE");
		        } catch (Exception e) {
		            e.printStackTrace();
		        } finally {
		            System.out.println("Close connection");
		            try {
		                os.close();
		            } catch (Exception e) {
		            }
		            try {
		                is.close();
		            } catch (Exception e) {
		            }
		            try {

		            } catch (Exception e) {
		            }
		        }
		}
		
		public void run1() {
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
				URL photoURL = new URL("http://crm.augura.net/AuguraClasses/AsiactionMobile/loadphoto.php?photoname=test_id.jpg");
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
				dos.writeBytes("Content-Type: image/jpeg"+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes("test_id.jpg");
				dos.writeBytes(lineEnd);
				
				
				
				dos.writeBytes("Content-Disposition: form-data; name=\"photo\"; " + lineEnd);
				dos.writeBytes("Content-Type: application/octet-stream; charset="
                                + "UTF-8" + lineEnd);
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
					Thread.currentThread().sleep(1000);
				}
				inStream.close();

			} catch (IOException ioex) {
				Log.e(Constants.TAG, "error: " + ioex.getMessage(), ioex);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
