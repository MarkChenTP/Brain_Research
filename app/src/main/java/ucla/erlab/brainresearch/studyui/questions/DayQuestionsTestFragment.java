/**
 *  DayQuestionsTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.questions;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class DayQuestionsTestFragment extends Fragment {

    //private DayQuestionsTestViewModel dayQuestionsTestViewModel;

    private int[] sleepTime;
    private NumberPicker sleepHour, sleepMinute;
    private int sleepLevel;
    private SeekBar sleepRank;
    private TextView sleepRankText;

    private RadioGroup newExperience;
    private EditText experienceNote;

    private Spinner badLuckEmotion;
    private Spinner hardLifeEmotion;
    private Spinner boredEmotion;
    private Spinner depressEmotion;
    private Spinner nervousEmotion;
    private Spinner worryEmotion;

    private Spinner exerciseLevel;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dayquestions_test, container, false);
        /*
        dayQuestionsTestViewModel = new ViewModelProvider(this).get(DayQuestionsTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_DayQuestionsTest);
        dayQuestionsTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        sleepTime = new int[2];
        sleepHour = root.findViewById(R.id.numberPicker_sleepTime_hour);
        sleepHour.setMinValue(0);
        sleepHour.setMaxValue(24);
        sleepHour.setWrapSelectorWheel(true);
        sleepHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                sleepTime[0] = newVal;
            }
        });
        sleepMinute = root.findViewById(R.id.numberPicker_sleepTime_minute);
        sleepMinute.setMinValue(0);
        sleepMinute.setMaxValue(59);
        sleepMinute.setWrapSelectorWheel(true);
        sleepMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                sleepTime[1] = newVal;
            }
        });

        sleepRank = root.findViewById(R.id.seekBar_sleepRank);
        sleepRank.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sleepLevel = progress;
                String text = progress + " out of 10";
                sleepRankText.setText(text);
            }
        });
        sleepRankText = root.findViewById(R.id.textView_sleepRankText);

        newExperience = root.findViewById(R.id.radioGroup_newExperience);
        newExperience.check(R.id.radioButton_newExperience_no);

        experienceNote = root.findViewById(R.id.editText_newExperienceNote);

        badLuckEmotion = root.findViewById(R.id.spinner_badLuckEmotion);
        hardLifeEmotion = root.findViewById(R.id.spinner_hardLifeEmotion);
        boredEmotion = root.findViewById(R.id.spinner_boredEmotion);
        depressEmotion = root.findViewById(R.id.spinner_depressEmotion);
        nervousEmotion = root.findViewById(R.id.spinner_nervousEmotion);
        worryEmotion = root.findViewById(R.id.spinner_worryEmotion);

        exerciseLevel = root.findViewById(R.id.spinner_exerciseLevel);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setDayQuestions(getDayQuestionsUI());

                Navigation.findNavController(v).navigate(R.id.action_DayQueryTestToDayQuery);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setDayQuestions(getDayQuestionsUI());

                Bundle arguments = new Bundle();
                arguments.putSerializable("BPType", Config.BPType.AfterQuestions);
                Navigation.findNavController(v).navigate(R.id.action_DayQueryTestToBP1, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_dayQuestionsTest));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Window window = activity.getWindow();

        // Close Soft Keyboard
        InputMethodManager inputManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((window.getCurrentFocus() == null) ?
                null : window.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Resume UI and Data
        ActionBar actionBar = activity.getSupportActionBar();
        Window window = activity.getWindow();

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_dayQuestionsTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        setDayQuestionsUI(((StudyActivity) requireActivity()).getDayQuestions());

        sleepHour.clearFocus(); sleepMinute.clearFocus();
        experienceNote.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public HashMap<String, String> getDayQuestionsUI() {
        HashMap<String, String> dayQuestionsData = new LinkedHashMap<>();

        // Sleep Time Question: hours and minutes (HH:MM)
        String sleepTimeData = sleepTime[0] + ":"  + sleepTime[1];

        // Sleep Rank Question: 0 to 10
        String sleepRankData = Integer.toString(sleepLevel);

        // New Experience Question: 0 = Yes, 1 = No
        String newExperienceData;
        int newExperience_index = newExperience.indexOfChild(
                newExperience.findViewById(newExperience.getCheckedRadioButtonId()));
        switch (newExperience_index) {
            case 0:
                newExperienceData = "Yes";
                break;
            case 1:
                newExperienceData = "No";
                break;
            default:
                newExperienceData = "";
                break;
        }

        // Experience Note Question: words as a String
        String newExperienceNoteData = experienceNote.getText().toString();

        // Bad Luck Emotion Question: 0 = Never, 1 = Almost Never, 2 = Sometimes, 3 = Fairly Often, 4 = Very Often
        String badLuckEmotionData = badLuckEmotion.getSelectedItem().toString();

        // Hard Life Emotion Question: 0 = Never, 1 = Almost Never, 2 = Sometimes, 3 = Fairly Often, 4 = Very Often
        String hardLifeEmotionData = hardLifeEmotion.getSelectedItem().toString();

        // Bored Emotion Question: 0 = Not at all, 1 = Several times, 2 = Fairly Often, 3 = The whole day
        String boredEmotionData = boredEmotion.getSelectedItem().toString();

        // Depress Emotion Question: 0 = Not at all, 1 = Several times, 2 = Fairly Often, 3 = The whole day
        String depressEmotionData = depressEmotion.getSelectedItem().toString();

        // Nervous Emotion Question: 0 = Not at all, 1 = Several times, 2 = Fairly Often, 3 = The whole day
        String nervousEmotionData = nervousEmotion.getSelectedItem().toString();

        // Worry Emotion Question: 0 = Not at all, 1 = Several times, 2 = Fairly Often, 3 = The whole day
        String worryEmotionData = worryEmotion.getSelectedItem().toString();

        // Exercise Level Question: 0 = Minimal, 1 = Low, 2 = Medium, 3 = High
        String exerciseLevelData = exerciseLevel.getSelectedItem().toString();

        dayQuestionsData.put("SleepTime", sleepTimeData);
        dayQuestionsData.put("SleepRank", sleepRankData);
        dayQuestionsData.put("NewExperience", newExperienceData);
        dayQuestionsData.put("NewExperienceNote", newExperienceNoteData);
        dayQuestionsData.put("BadLuckEmotion", badLuckEmotionData);
        dayQuestionsData.put("HardLifeEmotion", hardLifeEmotionData);
        dayQuestionsData.put("BoredEmotion", boredEmotionData);
        dayQuestionsData.put("DepressEmotion", depressEmotionData);
        dayQuestionsData.put("NervousEmotion", nervousEmotionData);
        dayQuestionsData.put("WorryEmotion", worryEmotionData);
        dayQuestionsData.put("ExerciseLevel", exerciseLevelData);

        return dayQuestionsData;
    }

    public void setDayQuestionsUI(HashMap<String, String> dayQuestionsData) {
        String SleepTimeData = dayQuestionsData.get("SleepTime");
        String[] SleepTimeStream =
                SleepTimeData != null ? SleepTimeData.split(":") : new String[] {"0", "0"};
        if (SleepTimeStream.length == 2) {
            sleepTime[0] = Integer.parseInt(SleepTimeStream[0]);
            sleepHour.setValue(Integer.parseInt(SleepTimeStream[0]));
            sleepTime[1] = Integer.parseInt(SleepTimeStream[1]);
            sleepMinute.setValue(Integer.parseInt(SleepTimeStream[1]));
        }

        String SleepRankData = dayQuestionsData.get("SleepRank");
        sleepLevel = Integer.parseInt(SleepRankData != null ? SleepRankData : "5");
        sleepRank.setProgress(sleepLevel);

        String newExperienceData = dayQuestionsData.get("NewExperience");
        switch (newExperienceData != null ? newExperienceData : "No") {
            case "Yes":
                newExperience.check(R.id.radioButton_newExperience_yes);
                break;
            case "No":
                newExperience.check(R.id.radioButton_newExperience_no);
                break;
            default:
                break;
        }

        experienceNote.setText(dayQuestionsData.get("NewExperienceNote"));

        String badLuckEmotionData = dayQuestionsData.get("BadLuckEmotion");
        switch (badLuckEmotionData != null ? badLuckEmotionData : "Never") {
            case "Never":
                badLuckEmotion.setSelection(0);
                break;
            case "Almost Never":
                badLuckEmotion.setSelection(1);
                break;
            case "Sometimes":
                badLuckEmotion.setSelection(2);
                break;
            case "Fairly Often":
                badLuckEmotion.setSelection(3);
                break;
            case "Very Often":
                badLuckEmotion.setSelection(4);
                break;
            default:
                break;
        }

        String hardLifeEmotionData = dayQuestionsData.get("HardLifeEmotion");
        switch (hardLifeEmotionData != null ? hardLifeEmotionData : "Never") {
            case "Never":
                hardLifeEmotion.setSelection(0);
                break;
            case "Almost Never":
                hardLifeEmotion.setSelection(1);
                break;
            case "Sometimes":
                hardLifeEmotion.setSelection(2);
                break;
            case "Fairly Often":
                hardLifeEmotion.setSelection(3);
                break;
            case "Very Often":
                hardLifeEmotion.setSelection(4);
                break;
            default:
                break;
        }

        String boredEmotionData = dayQuestionsData.get("BoredEmotion");
        switch (boredEmotionData != null ? boredEmotionData : "Not at all") {
            case "Not at all":
                boredEmotion.setSelection(0);
                break;
            case "Several times":
                boredEmotion.setSelection(1);
                break;
            case "Fairly Often":
                boredEmotion.setSelection(2);
                break;
            case "The whole day":
                boredEmotion.setSelection(3);
                break;
            default:
                break;
        }

        String depressEmotionData = dayQuestionsData.get("DepressEmotion");
        switch (depressEmotionData != null ? depressEmotionData : "Not at all") {
            case "Not at all":
                depressEmotion.setSelection(0);
                break;
            case "Several times":
                depressEmotion.setSelection(1);
                break;
            case "Fairly Often":
                depressEmotion.setSelection(2);
                break;
            case "The whole day":
                depressEmotion.setSelection(3);
                break;
            default:
                break;
        }

        String nervousEmotionData = dayQuestionsData.get("NervousEmotion");
        switch (nervousEmotionData != null ? nervousEmotionData : "Not at all") {
            case "Not at all":
                nervousEmotion.setSelection(0);
                break;
            case "Several times":
                nervousEmotion.setSelection(1);
                break;
            case "Fairly Often":
                nervousEmotion.setSelection(2);
                break;
            case "The whole day":
                nervousEmotion.setSelection(3);
                break;
            default:
                break;
        }

        String worryEmotionData = dayQuestionsData.get("WorryEmotion");
        switch (worryEmotionData != null ? worryEmotionData : "Not at all") {
            case "Not at all":
                worryEmotion.setSelection(0);
                break;
            case "Several times":
                worryEmotion.setSelection(1);
                break;
            case "Fairly Often":
                worryEmotion.setSelection(2);
                break;
            case "The whole day":
                worryEmotion.setSelection(3);
                break;
            default:
                break;
        }

        String exerciseLevelData = dayQuestionsData.get("ExerciseLevel");
        switch (exerciseLevelData != null ? exerciseLevelData : "Minimal") {
            case "Minimal":
                worryEmotion.setSelection(0);
                break;
            case "Low":
                worryEmotion.setSelection(1);
                break;
            case "Medium":
                worryEmotion.setSelection(2);
                break;
            case "High":
                worryEmotion.setSelection(3);
                break;
            default:
                break;
        }
    }

}
