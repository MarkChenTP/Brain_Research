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
 *  ValsalvaTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.valsalva;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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


public class ValsalvaTestFragment extends Fragment {

    //private ValsalvaTestViewModel valsalvaTestViewModel;

    private ProgressBar mProgressBar;
    private int valsalvaProgress;
    private TextView valsalvaText;
    private long valsalvaDuration;
    private long valsalvaTimeRemain;
    private CountDownTimer mCountDownTimer;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_valsalva_test, container, false);
        /*
        valsalvaTestViewModel = new ViewModelProvider(this).get(ValsalvaTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_ValsalvaTest);
        valsalvaTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        mProgressBar = root.findViewById(R.id.progressBar_valsalvaTimer);
        valsalvaText = root.findViewById(R.id.textView_valsalvaTimer);

        valsalvaDuration = ((StudyActivity) activity).getValsalvaDuration();

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("RestType", Config.RestType.BeforeValsalva);
                Navigation.findNavController(v).navigate(R.id.action_ValsalvaTestToRest2, arguments);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("RestType", Config.RestType.AfterValsalva);
                Navigation.findNavController(v).navigate(R.id.action_ValsalvaTestToRest3, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_valsalvaTest));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_valsalvaTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimaryAlternate)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarkAlternate));


        boolean valsalvaDone = ((StudyActivity) activity).getValsalvaDone();
        if (!valsalvaDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);

            valsalvaTimeRemain = valsalvaDuration;
            valsalvaProgress = 0;
            mProgressBar.setProgress(valsalvaProgress);
            String valsalvaMessage = "Blow the tube!";
            valsalvaText.setText(valsalvaMessage);

            runCountDownTimer();

        } else {
            backButton.setEnabled(true);
            nextButton.setEnabled(true);

            valsalvaTimeRemain = 0;
            valsalvaProgress = 100;
            mProgressBar.setProgress(valsalvaProgress);
            String valsalvaMessage = "Task Completed!";
            valsalvaText.setText(valsalvaMessage);
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
            mCountDownTimer = new CountDownTimer(valsalvaTimeRemain, 20) {
                @Override
                public void onTick(long millisUntilFinished) {
                    valsalvaTimeRemain = millisUntilFinished;
                    valsalvaProgress = (int)((double)valsalvaTimeRemain/(double)valsalvaDuration * 100);
                    mProgressBar.setProgress(mProgressBar.getMax()- valsalvaProgress);
                }
                @Override
                public void onFinish() {
                    valsalvaTimeRemain = 0;
                    valsalvaProgress = (int)((double)valsalvaTimeRemain/(double)valsalvaDuration * 100);
                    mProgressBar.setProgress(mProgressBar.getMax()- valsalvaProgress);

                    ((StudyActivity) activity).setValsalvaDone(true);
                    backButton.setEnabled(true);
                    nextButton.setEnabled(true);

                    Bundle arguments = new Bundle();
                    arguments.putSerializable("RestType", Config.RestType.AfterValsalva);
                    Navigation.findNavController(view).navigate(R.id.action_ValsalvaTestToRest3, arguments);
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
