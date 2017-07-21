package com.example.mylibrary;

import java.util.ArrayList;

/**
 * Created by haidangdam on 7/5/17.
 */

public class SaveData {
  ArrayList<String> backupArray;
  static SaveData saveData;

  private SaveData() {
    backupArray = new ArrayList();
  }

  public static SaveData newInstance() {
    if (saveData == null) {
      saveData = new SaveData();
    }
    return saveData;
  }

  public String getStringAt(int position) {
    if (position >= backupArray.size()) {
      return null;
    }
    return backupArray.get(position);
  }

  public boolean addData(String data) {
    if (data == null && data.isEmpty()) {
      return false;
    }
    backupArray.add(data);
    return true;
  }

  public String removeData(int position) {
    if (position >= backupArray.size()) {
      return "";
    }
    return backupArray.remove(position);
  }

  public int getSize() {
    return backupArray.size();
  }

  public boolean isEmpty() {
    return backupArray.isEmpty();
  }

  public ArrayList<String> getList() {
    return backupArray;
  }

}
