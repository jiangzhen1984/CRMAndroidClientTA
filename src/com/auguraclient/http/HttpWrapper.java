
package com.auguraclient.http;

import com.auguraclient.util.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class HttpWrapper {

    private static int TIME_OUT = 15000;

    public String sendHttpGetRequest(String url) {
        Message msg = new Message();
        new Worker(msg, url).start();
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

    class Worker extends Thread {

        private Message msg;

        private String url;


        public Worker(Message msg, String url) {
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
                    synchronized(msg) {
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

                httpget.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux "
                        + "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");

                httpget.setHeader(
                        "Accept",
                        "text/html,application/xml,"
                                + "application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

                httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");

                response = httpClient.execute(httpget, new BasicHttpContext());
                msg.response = request(response);
                Log.i(Constants.TAG, "get data " + msg.response);
            } catch (Exception e) {
                msg.flag = -2;
                Log.e(Constants.TAG, "", e);
            } finally {
                synchronized(msg) {
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
