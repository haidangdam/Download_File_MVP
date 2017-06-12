package com.example.haidangdam.myapplication;

import android.util.Log;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by haidangdam on 6/9/17.
 */

public class ComparatorThreadFile implements Comparator<Future<ThreadFile>> {
  @Override
  public int compare(Future<ThreadFile> t1, Future<ThreadFile> t2) {
    Log.d("Comparator Thread", "Sorting thread");
    int a = 0;
    try {
      a = t1.get().getPosition() - t2.get().getPosition();
    } catch (InterruptedException | ExecutionException e) {
      Log.d("Comparator", e.getLocalizedMessage());
    }
    return a;
  }
}
