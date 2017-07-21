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
  public static final int numberDivided = 100;
  public static final String linkUrl = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";


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

    void resumeOrPauseButtonSet(boolean isPause);

    void startDownloading1();

    void loadToFile(List<Future<ThreadFile>> list);

    void startDownloading2();

    void startDownloading3();
  }

  interface ThreadCallback {

    void callback(long size);
  }
}
