package com.example.haidangdam.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by haidangdam on 6/7/17.
 */

public class MainActivityPresenter implements MainActivityContract.Presenter {

  public static long speed;
  public static long fileSize = 1;
  MainActivityContract.View mainActivityView;
  public static int numThread = 1;
  public static int numThreadHaveRun;
  FileOutputStream outputStream;
  MainActivity mainActivity;
  File file;
  List<Future<ThreadFile>> finishList;
  ArrayList<Callable<ThreadFile>> pauseList;
  ArrayList<Callable<ThreadFile>> listThreadRunning;
  ExecutorService executor;
  static long fileDownloaded = 0;
  boolean addFirst = false;
  Timer t;
  Thread thread1;
  TimerTask task;
  Thread thread;
  boolean resume = false;
  private Object lock;
  private Queue<Object> queue;
  boolean pause = false;
  int point1 = 0;
  int point2 = 0;
  boolean stopped = false;
  static int start = 0;



  public MainActivityPresenter(MainActivityContract.View mainActivityView,
      MainActivity mainActivity) {
    this.mainActivityView = mainActivityView;
    this.mainActivityView.setPresenter(this);
    numThreadHaveRun = 0;
    this.mainActivity = mainActivity;
    finishList = new ArrayList();
    listThreadRunning = new ArrayList();
    pauseList = new ArrayList();
    executor = Executors.newCachedThreadPool();
    lock = new Object();
    queue = new PriorityQueue();
  }

  @Override
  public void resumeOrPauseButtonSet(boolean isPause) {
    if (isPause) {
      mainActivityView.pauseButtonPress();
      pause = true;
    } else {
      synchronized (lock) {
        Log.d("Main presenter", "Press resume button");
        t = new Timer();
        resume = true;
        pause = false;
        lock.notifyAll();
        point1 = 0;
      }
    }
  }

  @Override
  public void cancelButtonSet() {
    if (!listThreadRunning.isEmpty()) {
      thread = null;
      DownloadThread a = (DownloadThread) listThreadRunning.get(0);
      a.stopAllThread();
      Log.d("Main presenter", "Cancel button press");
      listThreadRunning.clear();
      try {
        outputStream.close();
      } catch (IOException e) {
        Log.d("Main presenter", e.getLocalizedMessage());
      }
      stopped = true;
      file.delete();
      t.cancel();
      mainActivityView.updateProgressBar(0);
    }
  }

  @Override
  public void startDownloading1() {
    getFileSize();
    File file = new File(MainActivityContract.PATH, "fileDownloaded");
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
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        Thread t = Thread.currentThread();
        executor = Executors.newCachedThreadPool();
        List<Future<ThreadFile>> listResult = new ArrayList();
        while (numThreadHaveRun + numThread <= MainActivityContract.numberDivided) {
          Log.d("Main Presenter", "Number of thread to work: " + numThread);
          for (int i = 0; i < numThread; i++) {
            Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
                (i + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
                (fileSize / MainActivityContract.numberDivided), numThreadHaveRun + i);
            Log.d("aaa", "" + (i + numThreadHaveRun));
            listResult.add(executor.submit(worker));
            Log.d("Current highest:", "" + numThreadHaveRun + numThread);
          }
          Collections.sort(listResult, new ComparatorThreadFile());
          loadToFile(listResult);
          if (numThreadHaveRun == 0) {
            try {
              if (listResult.get(0).get().getTime() != 0) {
                speed =
                    ((long) fileSize / MainActivityContract.numberDivided) / (listResult.get(0)
                        .get().getTime());
              }
              numThreadHaveRun += numThread;
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
              newSpeed =
                  ((fileSize / MainActivityContract.numberDivided) * numThread) / totalTime;
            }
            if (newSpeed >= speed) {
              numThreadHaveRun += numThread;
              numThread++;
              speed = newSpeed;
            } else {
              if (numThread > 1) {
                numThreadHaveRun += numThread;
                speed = newSpeed;
                numThread--;
              }
            }
            Log.d("Main Presenter", "Old velocity: " + speed);
            Log.d("Main Presenter", "New velocity: " + newSpeed);
          }
          Log.d("numThreadHaveRun", "" + numThreadHaveRun);
          mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mainActivityView
                  .updateProgressBar(
                      (numThreadHaveRun * 100) / MainActivityContract.numberDivided);
            }
          });
          listResult.clear();
        }
        if (numThreadHaveRun < MainActivityContract.numberDivided) {
          numThread = MainActivityContract.numberDivided - numThreadHaveRun;
          ((ThreadPoolExecutor) executor).setCorePoolSize(numThread);
          for (int i = 0; i < numThread; i++) {
            Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
                (i + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
                (fileSize / MainActivityContract.numberDivided), i + numThreadHaveRun);
            listResult.add(executor.submit(worker));
          }
          Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
              (numThread + numThreadHaveRun) * (fileSize / MainActivityContract.numberDivided),
              fileSize - (MainActivityContract.numberDivided - 1) *
                  (fileSize / MainActivityContract.numberDivided),
              MainActivityContract.numberDivided);
          listResult.add(executor.submit(worker));
          Collections.sort(listResult, new ComparatorThreadFile());
          loadToFile(listResult);
        }
        mainActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mainActivityView.updateProgressBar(
                (MainActivityContract.numberDivided * 100) / MainActivityContract.numberDivided);
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
      public void callback(long size) {
        fileSize = size;
        addFirst = true;
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
        Log.d("Main presenter", "finish load to file: " + fut.get().getPosition());
      }
      try {
        outputStream.close();
      } catch (IOException e) {
        Log.d("Main presenter", e.getLocalizedMessage());
      }
    } catch (InterruptedException | ExecutionException | IOException e) {
      Log.d("Main presenter", "Load to file " + e.getLocalizedMessage());
    }
    mainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (fileSize != 0) {
          mainActivityView.updateProgressBar(100);
        }
      }
    });

  }

  @Override
  public void startDownloading2() {
    getFileSize();
    file = new File(MainActivityContract.PATH, "fileDownloaded");
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
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d("Main presenter", "Start command thread");
        while (!stopped) {
          Log.d("Main presenter", "Pause while loop: " + (!pause));
          Log.d("Main presenter", "point1 if loop: " + (point1 == 0));
          while (!pause) {
           int a = 0;
            if (point1 == 0) {
              Log.d("Main presenter", "In point loop");
              point1++;
              if (t == null) {
                t = new Timer();
              }
              while (fileDownloaded < (fileSize)) {
                if (addFirst) {
                  if (a == 0) {
                    if (start == 0) {
                      Log.d("abc", "123");
                      Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
                          0,
                          fileSize, numThread);
                      listThreadRunning.add(worker);
                      finishList.add(executor.submit(worker));
                      start++;
                    }
                    task = new TimerTask() {
                      @Override
                      public void run() {
                        Log.d("Timer task", "Start");
                        long size = 0;
                        if (!listThreadRunning.isEmpty()) {
                          for (Callable<ThreadFile> callable : listThreadRunning) {
                            DownloadThread a = (DownloadThread) callable;
                            if (!a.getFinish()) {
                              size += a.getDownloadedSizeFromLastTime();
                            }
                            Log.d("Main presenter", "Size of file has been downloaded: " + size);
                          }
                          fileDownloaded += size;
                          Log.d("Main presenter",
                              "Downloaded CUR: " + fileDownloaded);
                        }
                        if (speed <= (size / 10) && pauseList.isEmpty()) {
                          speed = size / 10;
                          numThread++;
                          Log.d("Main presenter",
                              "Speed is less than current speed and pause list is empty");
                          addThreadToPool();
                        } else if (speed <= (size / 10) && !pauseList.isEmpty()) {
                          int a = 0;
                          if (!finishList.isEmpty()) {
                            Log.d("Main presenter",
                                "Previous speed is less than and pause list and finish list is not empty");
                            int b = 0;
                            for (Future<ThreadFile> fut : finishList) {
                              Log.d("Main presenter", "Iterating finish list");
                              try {
                                b++;
                                if (fut.isDone()) {
                                  Log.d("aaa1", "" + fut.get().getPosition());
                                  Log.d("aaa", "" + ((DownloadThread) pauseList.get(0)).position);
                                  Log.d("Main presenter",
                                      "Have finished: " + fut.get().getFinish());
                                  if (a == 0 && !fut.get().getFinish() && fut.get().getPosition()
                                      == ((DownloadThread) pauseList.get(0)).position) {
                                    Log.d("Main presenter",
                                        "Check the item number " + b + " of the finish list");
                                    Callable<ThreadFile> worker = new DownloadThread(
                                        fut.get().getFile(),
                                        MainActivityContract.linkUrl, fut.get().getNextStartByte(),
                                        fut.get().getSupposeEndByte() - fut.get()
                                            .getNextStartByte() + 1,
                                        fut.get().getPosition());
                                    listThreadRunning.add(worker);
                                    finishList.remove(b - 1);
                                    finishList.add(executor.submit(worker));
                                    pauseList.remove(0);
                                    a++;
                                    break;
                                  }
                                }
                              } catch (InterruptedException | ExecutionException e) {
                                Log.d("Main presenter1", e.getLocalizedMessage());
                              }
                            }
                            speed = size / 10;
                            if (a == 0) {
                              addThreadToPool();
                            }
                          }
                        } else {
                          speed = size / 10;
                          removeThreadFromPool();
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                            Log.d("Main presenter",
                                "File downloaded: " + ((fileDownloaded * 100) / fileSize));
                            mainActivityView
                                .updateProgressBar((int) ((fileDownloaded * 100) / fileSize));
                          }
                        });
                      }
                    };
                    t.scheduleAtFixedRate(task, 8000, 10000);
                    a++;
                  }
                  if (resume) {
                    Log.d("aaa", "enter pause");
                    int b = 0;
                    for (Future<ThreadFile> fut : finishList) {
                      Log.d("Main presenter", "Iterating finish list");
                      try {
                        b++;
                        if (fut.isDone()) {
                          if (!fut.get().getFinish()) {
                            Log.d("Main presenter",
                                "Check the item number " + b + " of the finish list");
                            Callable<ThreadFile> worker = new DownloadThread(fut.get().getFile(),
                                MainActivityContract.linkUrl, fut.get().getNextStartByte(),
                                fut.get().getSupposeEndByte() - fut.get().getNextStartByte(),
                                fut.get().getPosition());
                            listThreadRunning.add(worker);
                            a++;
                          }
                        }
                      } catch (InterruptedException | ExecutionException e) {
                        Log.d("Main presenter1", e.getLocalizedMessage());
                      }
                    }
                    resume = false;
                    finishList.clear();
                    try {
                      finishList = executor.invokeAll(listThreadRunning);
                    } catch (InterruptedException e) {
                      Log.d("Main presenter", e.getLocalizedMessage());
                    }
                  }
                }
                if (pause) {
                  Log.d("Main presenter", "Thread pause");
                  for (Callable<ThreadFile> callable : listThreadRunning) {
                    DownloadThread b = (DownloadThread) callable;
                    b.pause();
                  }
                  wrapUpPause();
                  listThreadRunning.clear();
                  t.cancel();
                  break;
                }
              }
              if (fileDownloaded >= fileSize) {
                Log.d("Main presenter", "Enter ending download file");
                t.cancel();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                  public void run() {
                    Log.d("Main presenter",
                        "List thread running size: " + listThreadRunning.size());
                    for (Callable<ThreadFile> callable : listThreadRunning) {
                      DownloadThread b = (DownloadThread) callable;
                      Log.d("aaa", "123");
                      Log.d("Main presenter",
                          "File downloaded: " + ((fileDownloaded)));
                      Log.d("Main presenter",
                          "File downloaded: " + ((fileDownloaded * 100) / fileSize));
                      Log.d("Main presenter",
                          "Thread remaining, Position: " + b.position + ", Remaining size: " +
                              (b.size - b.fileSize) + ", Intended ending: " + b.endByte
                              + " , Current byte:  " +
                              (b.getStartByte() + b.fileSize) + ", Start byte: " + b
                              .getStartByte());
                    }
                    Collections.sort(finishList, new ComparatorThreadFile());
                    loadToFile(finishList);
                    executor.shutdown();
                    Log.d("Main presenter", "Finish loading file");
                    stopped = true;
                  }

                }, 1000);
              }
            }
          }
          synchronized (lock) {
            try {
              lock.wait();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }
      }
    });
    thread.start();
  }


  private void addThreadToPool() {
    int position = 0;
    boolean findPosition = false;
    for (int i = listThreadRunning.size() - 1; i >= 0; i--) {
      Log.d("Remove thread", "Thread current: " + i);
      if (!((DownloadThread) listThreadRunning.get(i)).getFinish()) {
        position = i;
        findPosition = true;
        Log.d("Remove thread", "Last thread active: "  + ((DownloadThread) listThreadRunning.get(i)).getPosition());
        break;
      }
    }
    if (findPosition) {
      DownloadThread a = ((DownloadThread) listThreadRunning.get(position));
      Log.d("Main presenter", "Last thread position: " + a.position);
      Callable<ThreadFile> worker = new DownloadThread(MainActivityContract.linkUrl,
          ((a.getEndByte()
              + a.getStartByte() + a.getDownloadToDate()) / 2),
          a.getEndByte() - (((a.getEndByte()
              + a.getStartByte() + a.getDownloadToDate()) / 2)), numThread);
      a.setTarget((a.getEndByte() + a.getStartByte() + a.getDownloadToDate()) / 2  - 1);
      listThreadRunning.add(worker);
      finishList.add(executor.submit(worker));
    }
  }

  private void wrapUpPause() {
    Log.d("Main presenter", "Wrap up pause");
    pause = true;
  }


  private void removeThreadFromPool() {
    int position = 0;
    boolean findPosition = false;
    for (int i = listThreadRunning.size() - 1; i >= 0; i--) {
      Log.d("Remove thread", "Thread current: " + i);
      if (!((DownloadThread) listThreadRunning.get(i)).getFinish()) {
        position = i;
        findPosition = true;
        Log.d("Remove thread",
            "Last thread active: " + ((DownloadThread) listThreadRunning.get(i)).getPosition());
        break;
      }
    }
    if (findPosition) {
      Log.d("Main presenter",
          "Remove thread from pool. Thread number: " + (position));
      DownloadThread a = ((DownloadThread) listThreadRunning.get(position));
      a.pause();
      Log.d("Main presenter", "Thread pause: " + a.getPosition());
      listThreadRunning.remove(position);
      pauseList.add(0, a);
    }
  }

  public static void addToDownloadedFile(int haveDownloaded) {
    Log.d("Main presenter", "Have received: " + haveDownloaded);
    fileDownloaded += haveDownloaded;
  }

  public void startDownloading3() {
    getFileSize();
    file = new File(MainActivityContract.PATH, "fileDownloaded");
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
    executor = Executors.newCachedThreadPool();
    thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (!addFirst) {
            ;
          }
          Callable<ThreadFile> callable1 = new DownloadThread(MainActivityContract.linkUrl, 0,
              fileSize / 2, 0);
          Callable<ThreadFile> callable2 = new DownloadThread(MainActivityContract.linkUrl,
              (fileSize / 2) + 1,
              fileSize - ((fileSize / 2) + 1), 1);
          ArrayList<Callable<ThreadFile>> array = new ArrayList();
          array.add(callable1);
          array.add(callable2);
          finishList = executor.invokeAll(array);
        } catch (InterruptedException e) {
          Log.d("Main Presenter", e.getLocalizedMessage());
        }
      }
    });
    thread1.start();
  }


}
