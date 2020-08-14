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
 *  StroopTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.stroop;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class StroopTestFragment extends Fragment {

    //private StroopTestViewModel stroopTestViewModel;

    private ArrayList<String> colorNames;
    private ArrayList<String> colorColors;
    private ArrayList<String> colorResponses;
    private ArrayList<String> colorResults;

    private final String[] mColorNames
            = { "Green", "Orange", "Blue", "Red", "Yellow", "Purple"};            // Same indices as mColorRGBs
    private final String[] mColorRGBs
            = { "#008000", "#FFA500", "#0000FF", "#FF0000", "#FFFF00", "#800080"}; // Same indices as mColorNames
    private int stroopRounds;
    private Handler mHandler;
    private Runnable myStroopTask;
    private boolean mStroopTaskInit = false;
    private int stroopTaskCount;
    private String mColorName = "";
    private String mColorColor = "";

    private LinearLayout stroopTestArea;
    private TextView stroopColor;
    private TextView stroopResult;

    private LinearLayout stroopTestTextArea;
    private TextView stroopText;

    private ConstraintLayout stroopTestButtonsArea;
    private Button greenButton;
    private Button orangeButton;
    private Button blueButton;
    private Button redButton;
    private Button yellowButton;
    private Button purpleButton;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stroop_test, container, false);
        /*
        stroopTestViewModel = new ViewModelProvider(this).get(StroopTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_StroopTest);
        stroopTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        colorNames = new ArrayList<>();
        colorColors = new ArrayList<>();
        colorResponses = new ArrayList<>();
        colorResults = new ArrayList<>();

        stroopTestArea = root.findViewById(R.id.linearLayout_StroopTestArea);
        stroopColor = root.findViewById(R.id.textView_stroopQuestion);
        stroopResult = root.findViewById(R.id.textView_stroopAnswer);

        stroopTestTextArea = root.findViewById(R.id.linearLayout_StroopTestText);
        stroopText = root.findViewById(R.id.textView_stroopTestText);

        stroopTestButtonsArea = root.findViewById(R.id.constraintLayout_StroopTestButtons);
        greenButton = root.findViewById(R.id.button_Green);
        greenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Green");
            }
        });
        orangeButton = root.findViewById(R.id.button_Orange);
        orangeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Orange");
            }
        });
        blueButton =  root.findViewById(R.id.button_Blue);
        blueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Blue");
            }
        });
        redButton =  root.findViewById(R.id.button_Red);
        redButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Red");
            }
        });
        yellowButton = root.findViewById(R.id.button_Yellow);
        yellowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Yellow");
            }
        });
        purpleButton = root.findViewById(R.id.button_Purple);
        purpleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStroopAnswer("Purple");
            }
        });

        stroopRounds = ((StudyActivity) activity).getStroopRounds();

        if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());

        if (myStroopTask == null) {
            myStroopTask = new Runnable() {
                public void run() {
                    boolean reschedule = checkReschedule();
                    if(!reschedule) {
                        ((StudyActivity) activity).setStroopDone(true);

                        // Save Study Data
                        ((StudyActivity) activity).setStroopNames(colorNames);
                        ((StudyActivity) activity).setStroopColors(colorColors);
                        ((StudyActivity) activity).setStroopResponses(colorResponses);
                        ((StudyActivity) activity).setStroopResults(colorResults);


                        View view = getView();
                        assert view != null;

                        Bundle arguments = new Bundle();
                        arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                        Navigation.findNavController(view).navigate(R.id.action_StroopTestToBP4, arguments);

                    } else {

                        makeStroopQuestion();
                        stroopTaskCount++;
                    }
                }

                private boolean checkReschedule() {
                    boolean reschedule = true;
                    if (stroopTaskCount >= stroopRounds) {
                        reschedule = false;
                    }
                    return reschedule;
                }
            };
        }

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_StroopTestToStroop);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                Navigation.findNavController(v).navigate(R.id.action_StroopTestToBP4, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_stroopTest));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Resume UI and Data
        ActionBar actionBar = activity.getSupportActionBar();
        Window window = activity.getWindow();

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_stroopTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimaryAlternate)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarkAlternate));


        boolean stroopDone = ((StudyActivity) activity).getStroopDone();
        if (!stroopDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);

            stroopTestArea.setVisibility(View.VISIBLE);
            stroopTestTextArea.setVisibility(View.GONE);
            stroopTestButtonsArea.setVisibility(View.VISIBLE);

            if (!mStroopTaskInit) {
                colorNames.clear(); colorColors.clear();
                colorResponses.clear(); colorResults.clear();
                stroopTaskCount = 0;

                mStroopTaskInit = true;

                mHandler.postDelayed(myStroopTask, 1000);

            } else {

                backButton.setEnabled(true);

                String stroopMessage = "Stroop interrupted!\n Re-enter this page to restart";
                stroopText.setText(stroopMessage);

                stroopTestTextArea.setVisibility(View.VISIBLE);
            }

        } else {
            backButton.setEnabled(true);
            nextButton.setEnabled(true);

            String stroopMessage = "Task Completed!";
            stroopText.setText(stroopMessage);

            stroopTestArea.setVisibility(View.GONE);
            stroopTestTextArea.setVisibility(View.VISIBLE);
            stroopTestButtonsArea.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void makeStroopQuestion() {
        Random rand = new Random();
        int choice = rand.nextInt(Integer.MAX_VALUE) % mColorNames.length;
        mColorName = mColorNames[choice];
        colorNames.add(mColorName);
        stroopColor.setText(mColorNames[choice]);

        choice = rand.nextInt(Integer.MAX_VALUE) % mColorNames.length;
        mColorColor = mColorNames[choice];
        colorColors.add(mColorColor);
        stroopColor.setTextColor(Color.parseColor(mColorRGBs[choice]));


        stroopResult.setText("");
        toggleStroopButtons(true);
    }

    private void checkStroopAnswer(String answer) {
        colorResponses.add(answer);

        if (mColorName.equals(answer)) {
            stroopResult.setText("O");
            stroopResult.setTextColor(Color.parseColor("#008000"));
            colorResults.add("O");
        } else {
            stroopResult.setText("X");
            stroopResult.setTextColor(Color.parseColor("#FF0000"));
            colorResults.add("X");
        }

        toggleStroopButtons(false);

        mHandler.postDelayed(myStroopTask, 1500);
    }

    private void toggleStroopButtons(boolean isEnable) {
        greenButton.setEnabled(isEnable);
        orangeButton.setEnabled(isEnable);
        blueButton.setEnabled(isEnable);
        redButton.setEnabled(isEnable);
        yellowButton.setEnabled(isEnable);
        purpleButton.setEnabled(isEnable);
    }



}
