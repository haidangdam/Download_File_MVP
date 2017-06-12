package com.example.haidangdam.myapplication;

import java.io.File;

/**
 * Created by haidangdam on 6/9/17.
 */

public class ThreadFile {
  int position;
  File file;
  long time = 1;

  public ThreadFile(int position, File file, long time) {
    this.position = position;
    this.file = file;
    this.time = time;
  }

  public int getPosition() {
    return position;
  }

  public File getFile() {
    return file;
  }

  public long getTime() {
    return time;
  }
}
