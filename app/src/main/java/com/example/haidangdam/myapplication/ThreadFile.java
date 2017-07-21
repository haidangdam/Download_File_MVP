package com.example.haidangdam.myapplication;

import java.io.File;

/**
 * Created by haidangdam on 6/9/17.
 */

public class ThreadFile {
  int position;
  File file;
  long time = 1;
  boolean finished = true;
  long nextStartByte = 0;
  long supposeEndByte = 0;

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

  public void setFinish(boolean status) {
    this.finished = status;
  }

  public boolean getFinish() {
    return finished;
  }

  public void setNextStartByte(long startByte) {
    this.nextStartByte = startByte;
  }

  public long getNextStartByte() {
    return nextStartByte;
  }

  public void setSupposeEndByte(long endByte) {
    this.supposeEndByte = endByte;
  }

  public long getSupposeEndByte() {
    return this.supposeEndByte;
  }
}
