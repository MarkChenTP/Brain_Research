/**
 *  SubjectQuestionsTestFragment.java
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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class SubjectQuestionsTestFragment extends Fragment {

    //private SubjectQuestionsTestViewModel subjectQuestionsTestViewModel;

    private LinearLayout menstruateLayout;
    private LinearLayout CPAPLayout;

    private Spinner startMenstruate;

    private int[] CPAPTime;
    private NumberPicker CPAPHour, CPAPMinute;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_subjectquestions_test, container, false);
        /*
        subjectQuestionsTestViewModel = new ViewModelProvider(this).get(SubjectQuestionsTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_SubjectQuestionsTest);
        subjectQuestionsTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        menstruateLayout = root.findViewById(R.id.linearLayout_Menstruate);
        CPAPLayout = root.findViewById(R.id.linearLayout_CPAP);

        startMenstruate = root.findViewById(R.id.spinner_startMenstruate);

        CPAPTime = new int[2];
        CPAPHour = root.findViewById(R.id.numberPicker_CPAPTime_hour);
        CPAPHour.setMinValue(0);
        CPAPHour.setMaxValue(24);
        CPAPHour.setWrapSelectorWheel(true);
        CPAPHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                CPAPTime[0] = newVal;
            }
        });
        CPAPMinute = root.findViewById(R.id.numberPicker_CPAPTime_minute);
        CPAPMinute.setMinValue(0);
        CPAPMinute.setMaxValue(59);
        CPAPMinute.setWrapSelectorWheel(true);
        CPAPMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                CPAPTime[1] = newVal;
            }
        });

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setSubjectQuestions(getSubjectQuestionsUI());

                Navigation.findNavController(v).navigate(R.id.action_UserQueryTestToUserQuery);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setSubjectQuestions(getSubjectQuestionsUI());

                Bundle arguments = new Bundle();
                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                String protocol = ((StudyActivity) activity).getUserSetting("Protocol");
                Calendar today = Calendar.getInstance();
                boolean isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

                if ((dayCount >= 15 && protocol.equals("SR")) ||
                    (isFriday && protocol.equals("IMT"))) {
                    // (SR only) start from day 15 or (IMT only) on Friday
                    Navigation.findNavController(v).navigate(R.id.action_UserQueryTestToPlanQuery);
                } else if (dayCount % 5 == 2) {
                    // every 5 days with offset 1 (day 2, 7, 12...)
                    Navigation.findNavController(v).navigate(R.id.action_UserQueryTestToDayQuery);
                } else {
                    arguments.putSerializable("BPType", Config.BPType.AfterQuestions);
                    Navigation.findNavController(v).navigate(R.id.action_UserQueryTestToBP1, arguments);
                }
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_subjectQuestionsTest));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_subjectQuestionsTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        // Load Study Data
        setSubjectQuestionsUI(((StudyActivity) activity).getSubjectQuestions());

        String menstruate = ((StudyActivity) activity).getUserSetting("Menstruating");
        String cpap = ((StudyActivity) activity).getUserSetting("CPAP");
        switch (menstruate) {
            case "Yes":
                menstruateLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case "No":
                menstruateLayout.setVisibility(LinearLayout.GONE);
                break;
            default:
                break;
        }
        switch (cpap) {
            case "Yes":
                CPAPLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            case "No":
                CPAPLayout.setVisibility(LinearLayout.GONE);
                break;
            default:
                break;
        }

        CPAPHour.clearFocus(); CPAPMinute.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public HashMap<String, String> getSubjectQuestionsUI() {
        HashMap<String, String> subjectQuestionsData = new LinkedHashMap<>();

        // Start Menstruating Question: 0 = No, 1 = Less than 1 day, 2 = More than 1 day
        String MenstruateStartedData = startMenstruate.getSelectedItem().toString();

        // CPAP Time Question: hours and minutes (HH:MM)
        String CPAPTimeData = CPAPTime[0] + ":"  + CPAPTime[1];

        subjectQuestionsData.put("MenstruateStarted", MenstruateStartedData);
        subjectQuestionsData.put("CPAPTime", CPAPTimeData);

        return subjectQuestionsData;
    }

    public void setSubjectQuestionsUI(HashMap<String, String> subjectQuestionsData) {
        String MenstruateStartedData = subjectQuestionsData.get("MenstruateStarted");
        switch (MenstruateStartedData != null ? MenstruateStartedData : "No") {
            case "No":
                startMenstruate.setSelection(0);
                break;
            case "Less than 1 day":
                startMenstruate.setSelection(1);
                break;
            case "More than 1 day":
                startMenstruate.setSelection(2);
                break;
            default:
                break;
        }

        String CPAPTimeData = subjectQuestionsData.get("CPAPTime");
        String[] CPAPTimeStream =
                CPAPTimeData != null ? CPAPTimeData.split(":") : new String[] {"0", "0"};
        if (CPAPTimeStream.length == 2) {
            CPAPTime[0] = Integer.parseInt(CPAPTimeStream[0]);
            CPAPHour.setValue(Integer.parseInt(CPAPTimeStream[0]));
            CPAPTime[1] = Integer.parseInt(CPAPTimeStream[1]);
            CPAPMinute.setValue(Integer.parseInt(CPAPTimeStream[1]));
        }
    }

}
