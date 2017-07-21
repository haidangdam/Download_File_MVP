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
  long startByte;
  long size = 0;
  int position;
  int fileSize = 0;
  boolean pause = false;
  File fileGiven;
  int downloadedSize = 0;
  boolean changeRequest = false;
  long endByte = 0;
  boolean finish = false;
  static boolean stop = false;


  public DownloadThread(String url, long startByte, long size, int position) {
    this.url = url;
    this.startByte = startByte;
    this.size = size;
    this.position = position;
    Log.d("Downloaded thread",
        "url: " + url + ", startByte: " + startByte + " size: " + size + ", position: " + position
            + " ending point: " + (startByte + size - 1));
  }

  public DownloadThread(File fileGiven, String url, long startByte, long size, int position) {
    this.url = url;
    this.startByte = startByte;
    this.size = size;
    this.position = position;
    this.fileGiven = fileGiven;
    Log.d("Downloaded thread", "Restart Download thread. url: " + url + ", startByte: " +
        startByte + " size: " + size + ", position: " + position + " ending point: " + (startByte
        + size));
  }

  @Override
  public ThreadFile call() {
    File file;
    Long startTime = System.currentTimeMillis();
    if (fileGiven == null) {
      file = new File(
          Environment.getExternalStorageDirectory().toString() + File.separator + "a" + position);
    } else {
      file = fileGiven;
    }
    Log.d("Download thread", "Create new file: " + file.getAbsolutePath());
    HttpURLConnection connection = null;
    FileOutputStream fileOutput = null;
    int count = 0;
    try {
      URL link = new URL(this.url);
      connection = (HttpURLConnection) link.openConnection();
      if (size != 0) {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes=" + startByte + "-" + (startByte + size - 1));
      }
      fileOutput = new FileOutputStream(file);
      InputStream inputStream = connection.getInputStream();
//      int count;
      byte data[] = new byte[4096];
      while (!pause && (count = inputStream.read(data)) != -1) {
        if (!changeRequest) {
          fileOutput.write(data, 0, count);
          fileSize += count;
          downloadedSize += count;
        } else {
          connection.disconnect();
          connection = (HttpURLConnection) link.openConnection();
          connection.setRequestMethod("GET");
          connection.setRequestProperty("Range", "bytes=" + (startByte + getDownloadToDate())
              + "-" + (endByte));
          Log.d("Download thread", "Change request thread. New ending point: " + endByte
              + " Starting point: " + (startByte + getDownloadToDate()) + " , Position: "
              + position);
          changeRequest = false;
          inputStream = connection.getInputStream();
        }
      }
    } catch (IOException i) {
      Log.d("Thread", i.getLocalizedMessage());
    } finally {
      if (connection != null) {
        connection.disconnect();
      }

      Log.d("///////////////",
          "call: " + (size - fileSize) + " count " + count + " pause " + pause);
    }
    Log.d("Thread download", "Thread number:  " + position);
    try {
      fileOutput.close();
    } catch (IOException e) {
      Log.d("Thread download", e.getLocalizedMessage());
    }
    Long endTime = System.currentTimeMillis();
    if (pause == true) {
      connection.disconnect();
      Log.d("Download Thread", "Thread number pause: " + position);
      ThreadFile threadFile = new ThreadFile(position, file, (endTime - startTime));
      threadFile.setFinish(false);
      threadFile.setNextStartByte(fileSize + startByte);
      if (endByte != 0) {
        threadFile.setSupposeEndByte(endByte);
      } else {
        threadFile.setSupposeEndByte(startByte + size);
      }
      Log.d("Downloaded Thread " + position, "End  " + endTime + ", Start time"
          + startTime + " , ending point: " + (startByte + fileSize)
          + " Pause: " + pause + " Download to date: " + getDownloadToDate());
      MainActivityPresenter.addToDownloadedFile(getDownloadedSizeFromLastTime());

      return threadFile;
    }
    Log.d("Thread Download: " + position, "Finish");
    Log.d("Downloaded Thread " + position, "End  " + endTime + ", Start time"
        + startTime + " , ending point: " + (startByte + fileSize)
        + " Pause: " + pause + " Download to date: " + getDownloadToDate());
    if (stop) {
      file.delete();
      return null;
    }
    finish = true;
    MainActivityPresenter.addToDownloadedFile(getDownloadedSizeFromLastTime());
    return new ThreadFile(position, file, (endTime - startTime));
  }

  public void pause() {
    Log.d("Download Thread", "Thread number: " + position + " , Thread pause");
    pause = true;
  }

  public int getDownloadToDate() {
    return fileSize;
  }

  public int getDownloadedSizeFromLastTime() {
    int a = getDownloadedSize();
    Log.d("Download thread " + position, "File size: " + a);
    downloadedSize = 0;
    return a;
  }


  public int getDownloadedSize() {
    Log.d("Thread download", "Get downloaded size: " + downloadedSize + " from file: " + position);
    return downloadedSize;
  }

  public long getTargetDownload() {
    return startByte + size;
  }

  public void setTarget(long endByte) {
    this.endByte = endByte;
    size = endByte - startByte;
    changeRequest = true;
  }

  public long getStartByte() {
    return startByte;
  }

  public int getPosition() {
    return position;
  }

  public boolean getFinish() {
    return finish;
  }

  public void stopAllThread() {
    this.stop = true;
  }

  public long getEndByte() {
    return size + startByte;}

  public long getSize() {return size;}
}
