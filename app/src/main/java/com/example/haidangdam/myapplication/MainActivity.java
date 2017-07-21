package com.example.haidangdam.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.mylibrary.MainActivityLibrary;

public class MainActivity extends AppCompatActivity {

  MainActivityView mainActivityView;
  MainActivityPresenter presenter;
  MainActivityLibrary 
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mainActivityView = MainActivityView.newInstance();
//    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);

    getFragmentManager().beginTransaction().add(R.id.frame_layout, mainActivityView).commit();
    presenter = new MainActivityPresenter(mainActivityView, this);

  }
}
