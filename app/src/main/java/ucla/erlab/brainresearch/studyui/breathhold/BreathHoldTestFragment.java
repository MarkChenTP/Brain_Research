/*
 * Copyright (c) 2020 Mark Chen (@MarkChenTP, https://github.com/MarkChenTP)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 *  BreathHoldTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.breathhold;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class BreathHoldTestFragment extends Fragment {

    //private BreathHoldTestViewModel breathHoldTestViewModel;

    private ProgressBar mProgressBar;
    private int breathHoldProgress;
    private TextView breathHoldText;
    private long breathHoldDuration;
    private long breathHoldTimeRemain;
    private CountDownTimer mCountDownTimer;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_breathhold_test, container, false);
        /*
        breathHoldTestViewModel = new ViewModelProvider(this).get(BreathHoldTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_BreathHoldTest);
        breathHoldTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        mProgressBar = root.findViewById(R.id.progressBar_breathHoldTimer);
        breathHoldText = root.findViewById(R.id.textView_breathHoldTimer);

        breathHoldDuration = ((StudyActivity) activity).getBreathHoldDuration();

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("RestType", Config.RestType.BeforeBreathHold);
                Navigation.findNavController(v).navigate(R.id.action_BreathHoldTestToRest4, arguments);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("RestType", Config.RestType.AfterBreathHold);
                Navigation.findNavController(v).navigate(R.id.action_BreathHoldTestToRest5, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_breathHoldTest));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Pause UI and Data
        pauseCountDownTimer();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Resume UI and Data
        ActionBar actionBar = activity.getSupportActionBar();
        Window window = activity.getWindow();

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_breathHoldTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimaryAlternate)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarkAlternate));

        boolean breathHoldDone = ((StudyActivity) activity).getBreathHoldDone();
        if (!breathHoldDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);

            breathHoldTimeRemain = breathHoldDuration;
            breathHoldProgress = 0;
            mProgressBar.setProgress(breathHoldProgress);
            String breathHoldMessage = "Hold your breath!";
            breathHoldText.setText(breathHoldMessage);

            runCountDownTimer();

        } else {
            backButton.setEnabled(true);
            nextButton.setEnabled(true);

            breathHoldTimeRemain = 0;
            breathHoldProgress = 100;
            mProgressBar.setProgress(breathHoldProgress);
            String breathHoldMessage = "Task Completed!";
            breathHoldText.setText(breathHoldMessage);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void runCountDownTimer() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        View view = requireView();

        if (mCountDownTimer == null) {
            mCountDownTimer = new CountDownTimer(breathHoldTimeRemain, 50) {
                @Override
                public void onTick(long millisUntilFinished) {
                    breathHoldTimeRemain = millisUntilFinished;
                    breathHoldProgress = (int)((double)breathHoldTimeRemain/(double)breathHoldDuration * 100);
                    mProgressBar.setProgress(mProgressBar.getMax()- breathHoldProgress);
                }
                @Override
                public void onFinish() {
                    breathHoldTimeRemain = 0;
                    breathHoldProgress = (int)((double)breathHoldTimeRemain/(double)breathHoldDuration * 100);
                    mProgressBar.setProgress(mProgressBar.getMax()- breathHoldProgress);

                    ((StudyActivity) activity).setBreathHoldDone(true);
                    backButton.setEnabled(true);
                    nextButton.setEnabled(true);

                    Bundle arguments = new Bundle();
                    arguments.putSerializable("RestType", Config.RestType.AfterBreathHold);
                    Navigation.findNavController(view).navigate(R.id.action_BreathHoldTestToRest5, arguments);
                }
            };
            mCountDownTimer.start();
        }
    }

    private void pauseCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }
}
