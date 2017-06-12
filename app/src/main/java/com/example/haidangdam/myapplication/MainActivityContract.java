package com.example.haidangdam.myapplication;

import android.os.Environment;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by haidangdam on 6/6/17.
 */

public interface MainActivityContract {
  public static final File PATH = Environment.getExternalStorageDirectory().getAbsoluteFile();
  public static final int numberDivided = 10000;
  public static final String linkUrl = "http://speedtest.ftp.otenet.gr/files/test100Mb.db";


  interface View {
    void setPresenter(MainActivityContract.Presenter presenter);
    String getResumeButtonText();
    void changeText();
    void resumeButtonPress();
    void pauseButtonPress();
    void cancelButtonPress();
    void updateProgressBar(int percentage);

  }

  interface Presenter {
    void cancelButtonSet();
    void resumeOrPauseButtonSet();
    void startDownloading();
    void loadToFile(List<Future<ThreadFile>> list);
  }

  interface ThreadCallback {
    void callback(int size);
  }
}
