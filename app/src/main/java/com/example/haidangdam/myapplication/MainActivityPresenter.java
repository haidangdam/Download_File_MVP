package com.example.haidangdam.myapplication;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by haidangdam on 6/7/17.
 */

public class MainActivityPresenter implements MainActivityContract.Presenter{
  public static long speed;
  public static int fileSize;
  MainActivityContract.View mainActivityView;
  public static int numThread = 1;
  public static int numThreadHaveRun;
  FileOutputStream outputStream;
  MainActivity mainActivity;


  public MainActivityPresenter(MainActivityContract.View mainActivityView, MainActivity mainActivity) {
    this.mainActivityView = mainActivityView;
    this.mainActivityView.setPresenter(this);
    numThreadHaveRun = 0;
    this.mainActivity = mainActivity;
  }

  @Override
  public void resumeOrPauseButtonSet() {
    switch (mainActivityView.getResumeButtonText()) {
      case "Pause" : {
        mainActivityView.pauseButtonPress();
      }
      case "Resume" : {
        mainActivityView.resumeButtonPress();
      }
    }
  }

  @Override
  public void cancelButtonSet() {

  }

  @Override
  public void startDownloading() {
    Log.d("aaaa", "startDownloading");
    getFileSize();
    File file =  new File(MainActivityContract.PATH, "fileDownloaded");
    try {
      file.createNewFile();
    } catch (IOException io) {
      Log.d("Main presenter", "Start downloading: " + io.getLocalizedMessage());
    }
    try {
      outputStream = new FileOutputStream(file);
    } catch (FileNotFoundException err) {
      Log.d("Main Presenter", err.getLocalizedMessage());
    }
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<ThreadFile>> listResult = new ArrayList();
        ArrayList<Callable<ThreadFile>> listThread = new ArrayList();
        while (numThreadHaveRun + numThread <= MainActivityContract.numberDivided) {
          Log.d("Main Presenter", "Number of thread to work: " + numThread);
          for (int i = 0; i < numThread; i++) {
            Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
                (i + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
                (fileSize / MainActivityContract.numberDivided), numThreadHaveRun + i);
            listThread.add(worker);
          }
          try {
            listResult = executor.invokeAll(listThread);
          } catch (InterruptedException e) {
            Log.d("Main presenter", "Thread file interrupted " + e.getLocalizedMessage());
          }
          Collections.sort(listResult, new ComparatorThreadFile());
          loadToFile(listResult);
          if (numThreadHaveRun == 0) {
            try {
              if (listResult.get(0).get().getTime() != 0) {
                speed =  ((long) fileSize / MainActivityContract.numberDivided) / (listResult.get(0).get().getTime());
              }
              numThread++;
              Log.d("Main presenter", "Old velocity: " + speed);
            } catch (InterruptedException | ExecutionException e) {
              Log.d("Main Presenter", e.getLocalizedMessage());
            }
          } else {
            long totalTime = 0;
            long newSpeed = 0;
            for (Future<ThreadFile> fut : listResult) {
              try {
                totalTime += fut.get().getTime();
              } catch (InterruptedException | ExecutionException e) {
                Log.d("Main Presenter", e.getLocalizedMessage());
              }
            }
            if (totalTime != 0) {
              newSpeed = ((fileSize / MainActivityContract.numberDivided) * numThread) / totalTime;
            }
            if (newSpeed >= speed) {
              numThread++;
              speed = newSpeed;
            } else {
              if (numThread > 1) {
                speed = newSpeed;
                numThread--;
              }
            }
            Log.d("Main Presenter", "Old velocity: " + speed);
            Log.d("Main Presenter", "New velocity: " + newSpeed);
          }
          numThreadHaveRun += numThread;
          mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mainActivityView.updateProgressBar((numThreadHaveRun * 100) / MainActivityContract.numberDivided);
            }
          });
          listThread.clear();
          listResult.clear();
        }
        if (numThreadHaveRun < MainActivityContract.numberDivided) {
          numThread = MainActivityContract.numberDivided - numThreadHaveRun;
          ((ThreadPoolExecutor) executor).setCorePoolSize(numThread);
          for (int i = 0; i < numThread - 1; i++) {
            Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
                (i + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
                (fileSize / MainActivityContract.numberDivided), i + numThreadHaveRun);
            listThread.add(worker);
          }
          Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
              (numThread + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
              fileSize - (MainActivityContract.numberDivided - 1) *
                  (fileSize / MainActivityContract.numberDivided), MainActivityContract.numberDivided);
          listThread.add(worker);
          try {
            listResult = executor.invokeAll(listThread);
          } catch (InterruptedException e) {
            Log.d("Main presenter", "Thread file interrupted " + e.getLocalizedMessage());
          }
          Collections.sort(listResult, new ComparatorThreadFile());
          loadToFile(listResult);
        }
        mainActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mainActivityView.updateProgressBar((MainActivityContract.numberDivided * 100)/ MainActivityContract.numberDivided);
          }
        });
        executor.shutdown();
        Log.d("Main presenter", "Finish loading file");
        try {
          outputStream.close();
        } catch (IOException e) {
          Log.d("Main presenter", e.getLocalizedMessage());
        }
      }
    });
    thread.start();
  }

  private void getFileSize() {
    Thread thread = new Thread(new GetSizeThread(new MainActivityContract.ThreadCallback() {
      @Override
      public void callback(int size) {
        fileSize = size;
      }
    }));
    thread.start();
  }

  @Override
  public void loadToFile(List<Future<ThreadFile>> list) {
    try {
      Log.d("Main presenter", "Load to file number: " + list.size());
      for (Future<ThreadFile> fut : list) {
        File file = fut.get().getFile();
        long length = file.length();
        byte[] bytes = new byte[(int) length];
        RandomAccessFile access = new RandomAccessFile(file, "r");
        access.readFully(bytes);
        outputStream.write(bytes);
        access.close();
        Log.d("Main presenter", "finish load to file");
      }
    } catch (InterruptedException | ExecutionException | IOException e) {
        Log.d("Main presenter", "Load to file " + e.getLocalizedMessage());
    }
  }


}
