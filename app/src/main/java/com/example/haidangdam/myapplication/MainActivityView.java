package com.example.haidangdam.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * Created by haidangdam on 6/7/17.
 */

public class MainActivityView extends Fragment implements MainActivityContract.View,
        View.OnClickListener{

  static MainActivityView mainActivityView;
  MainActivityContract.Presenter presenter;
  ProgressBar progressBar;
  Button cancelButton;
  Button pauseOrResumeButton;


  public MainActivityView() {}

  @Override
  public void setPresenter(MainActivityContract.Presenter presenter) {
    this.presenter = presenter;
  }

  public static MainActivityView newInstance() {
    if (mainActivityView == null) {
      mainActivityView = new MainActivityView();
    }
    return mainActivityView;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d("", "on create view");
    View view = inflater.inflate(R.layout.activity_view_layout, container, false);
    progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    cancelButton = (Button) view.findViewById(R.id.cancel_button);
    pauseOrResumeButton = (Button) view.findViewById(R.id.resume_button);
    cancelButton.setOnClickListener(this);
    pauseOrResumeButton.setOnClickListener(this);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.d("aaaa", "ActivityCreated");

    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        if (presenter != null) {
          presenter.startDownloading();
        }
      }
    }, 0);

  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.cancel_button: {
        presenter.cancelButtonSet();
      }
      case R.id.resume_button: {
        presenter.resumeOrPauseButtonSet();
      }
    }
  }

  @Override
  public String getResumeButtonText() {
    return pauseOrResumeButton.getText().toString();
  }

  @Override
  public void changeText() {
    if (pauseOrResumeButton.getText().toString().equals("Pause")) {
      pauseOrResumeButton.setText(R.string.resume_button);
    } else {
      pauseOrResumeButton.setText(R.string.pause_button);
    }
  }

  @Override
  public void updateProgressBar(int percentage) {
    progressBar.setProgress(percentage);
  }

  @Override
  public void resumeButtonPress() {}

  @Override
  public void pauseButtonPress() {}

  @Override
  public void cancelButtonPress() {}



}
