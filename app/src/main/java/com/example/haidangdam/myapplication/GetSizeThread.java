package com.example.haidangdam.myapplication;

import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by haidangdam on 6/9/17.
 */

public class GetSizeThread implements Runnable {

  MainActivityContract.ThreadCallback c;

  public GetSizeThread(MainActivityContract.ThreadCallback c) {
    this.c = c;
  }

  @Override
  public void run() {
    HttpURLConnection connection = null;
    try {
      Log.d("GetSizeThread", "Get size thread");
      URL url = new URL(MainActivityContract.linkUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.getInputStream();
      long fileSize = connection.getContentLength();
      c.callback(fileSize);
      Log.d("Get Size Thread", "file size: " + fileSize);
    } catch (IOException io) {
      Log.d("Main Presenter", "Get file size " + io.getLocalizedMessage());
    } finally {
      connection.disconnect();
    }
  }
}
