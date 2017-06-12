package com.example.haidangdam.myapplication;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by haidangdam on 6/6/17.
 */

public class DownloadThread implements Callable<ThreadFile> {

  String url;
  int startByte;
  int size;
  int position;

  public DownloadThread(String url, int startByte, int size, int position) {
    this.url = url;
    this.startByte = startByte;
    this.size = size;
    this.position = position;
    Log.d("Downloaded thread", "url: " + url + ", startByte: " + startByte + " size: " + size + ", position: " + position);
  }


  @Override
  public ThreadFile call() {
    Long startTime = System.currentTimeMillis();
    File file =  new File(Environment.getExternalStorageDirectory().toString() + File.separator + "a" + position);
    Log.d("Download thread", "Create new file: " + file.getAbsolutePath());
    HttpURLConnection connection = null;
    try {
      URL link = new URL(this.url);
      connection = (HttpURLConnection) link.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Range", "bytes=" + startByte + "-" + (startByte + size));
      FileOutputStream fileOutput = new FileOutputStream(file);
      InputStream inputStream = connection.getInputStream();
      int count;
      byte data[] = new byte[4096];
      while ((count = inputStream.read(data)) != -1) {
        fileOutput.write(data, 0, count);
      }
    } catch (IOException i) {
      Log.d("Thread", i.getLocalizedMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    Log.d("Thread download", "Thread number:  " + position);
    Long endTime = System.currentTimeMillis();
    Log.d("Downloaded Thread " + position, "End  " + endTime + ", Start time" + startTime);
    return new ThreadFile(position, file, (endTime - startTime));
  }


}
