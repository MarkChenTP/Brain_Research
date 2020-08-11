/**
 *  ProtocolQuestionsTestFragment.java
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
import android.widget.LinearLayout;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class ProtocolQuestionsTestFragment extends Fragment {

    //private ProtocolQuestionsTestViewModel protocolQuestionsTestViewModel;

    private LinearLayout SRLayout;
    private LinearLayout IMTLayout;

    private Spinner noticeBreathing;

    private EditText sIndex1, flow1, volume1;
    private EditText sIndex2, flow2, volume2;
    private EditText sIndex3, flow3, volume3;
    private EditText strengthNote;
    private EditText changeNote;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_protocolquestions_test, container, false);
        /*
        protocolQuestionsTestViewModel = new ViewModelProvider(this).get(ProtocolQuestionsTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_ProtocolQuestionsTest);
        protocolQuestionsTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        SRLayout = root.findViewById(R.id.linearLayout_SR);
        IMTLayout = root.findViewById(R.id.linearLayout_IMT);

        noticeBreathing = root.findViewById(R.id.spinner_noticeBreathing);

        sIndex1 = root.findViewById(R.id.editText_strength_sIndex1);
        flow1 = root.findViewById(R.id.editText_strength_flow1);
        volume1 = root.findViewById(R.id.editText_strength_volume1);
        sIndex2 = root.findViewById(R.id.editText_strength_sIndex2);
        flow2 = root.findViewById(R.id.editText_strength_flow2);
        volume2 = root.findViewById(R.id.editText_strength_volume2);
        sIndex3 = root.findViewById(R.id.editText_strength_sIndex3);
        flow3 = root.findViewById(R.id.editText_strength_flow3);
        volume3 = root.findViewById(R.id.editText_strength_volume3);

        strengthNote = root.findViewById(R.id.editText_strengthNote);

        changeNote = root.findViewById(R.id.editText_changeNote);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check UI
                checkProtocolQuestionsUI();

                // Save Study Data
                ((StudyActivity) activity).setProtocolQuestions(getProtocolQuestionsUI());

                Navigation.findNavController(v).navigate(R.id.action_PlanQueryTestToPlanQuery);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check UI
                checkProtocolQuestionsUI();

                // Save Study Data
                ((StudyActivity) activity).setProtocolQuestions(getProtocolQuestionsUI());

                Bundle arguments = new Bundle();
                int dayCount = Integer.parseInt(((StudyActivity) requireActivity()).getUserSetting("DayCount"));

                if (dayCount % 5 == 2) {
                    // every 5 days with offset 1 (day 2, 7, 12...)
                    Navigation.findNavController(v).navigate(R.id.action_PlanQueryTestToDayQuery);
                } else {
                    arguments.putSerializable("BPType", Config.BPType.AfterQuestions);
                    Navigation.findNavController(v).navigate(R.id.action_PlanQueryTestToBP1, arguments);
                }
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_protocolQuestionsTest));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_protocolQuestionsTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        // Load Study Data
        setProtocolQuestionsUI(((StudyActivity) activity).getProtocolQuestions());

        String protocol = ((StudyActivity) activity).getUserSetting("Protocol");
        switch (protocol) {
            case "SR":
                SRLayout.setVisibility(LinearLayout.VISIBLE);
                IMTLayout.setVisibility(LinearLayout.GONE);
                break;
            case "IMT":
                SRLayout.setVisibility(LinearLayout.GONE);
                IMTLayout.setVisibility(LinearLayout.VISIBLE);
                break;
            default:
                break;
        }

        sIndex1.clearFocus(); flow1.clearFocus(); volume1.clearFocus();
        sIndex2.clearFocus(); flow2.clearFocus(); volume2.clearFocus();
        sIndex3.clearFocus(); flow3.clearFocus(); volume3.clearFocus();
        strengthNote.clearFocus();
        changeNote.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public void checkProtocolQuestionsUI() {
        if (sIndex1.getText().toString().equals("")) sIndex1.setText("0.0");
        if (flow1.getText().toString().equals("")) flow1.setText("0.0");
        if (volume1.getText().toString().equals("")) volume1.setText("0.0");

        if (sIndex2.getText().toString().equals("")) sIndex2.setText("0.0");
        if (flow2.getText().toString().equals("")) flow2.setText("0.0");
        if (volume2.getText().toString().equals("")) volume2.setText("0.0");

        if (sIndex3.getText().toString().equals("")) sIndex3.setText("0.0");
        if (flow3.getText().toString().equals("")) flow3.setText("0.0");
        if (volume3.getText().toString().equals("")) volume3.setText("0.0");
    }

    public HashMap<String, String> getProtocolQuestionsUI() {
        HashMap<String, String> protocolQuestionsData = new LinkedHashMap<>();

        // Notice Breathing Question: 0 = 0 times, 1 = 1 to 2 times, 2 = 3 to 5 times, 4 = more than 5 times
        String noticeBreathingData = noticeBreathing.getSelectedItem().toString();

        // Multi-Strength Question:
        String[] breathExercise = new String[3];
        // Breathing Exercise 1: S-Index, flow, volume
        breathExercise[0] = sIndex1.getText().toString();
        breathExercise[1] = flow1.getText().toString();
        breathExercise[2] = volume1.getText().toString();
        String breathExercise1Data = Arrays.toString(breathExercise);
        // Breathing Exercise 2: S-Index, flow, volume
        breathExercise[0] = sIndex2.getText().toString();
        breathExercise[1] = flow2.getText().toString();
        breathExercise[2] = volume2.getText().toString();
        String breathExercise2Data = Arrays.toString(breathExercise);
        // Breathing Exercise 3: S-Index, flow, volume
        breathExercise[0] = sIndex3.getText().toString();
        breathExercise[1] = flow3.getText().toString();
        breathExercise[2] = volume3.getText().toString();
        String breathExercise3Data = Arrays.toString(breathExercise);

        // Multi-Strength Note Question: words as a String
        String strengthNoteData = strengthNote.getText().toString();

        // Change Note Question: words as a String
        String changeNoteData = changeNote.getText().toString();

        protocolQuestionsData.put("NoticeBreathingTime", noticeBreathingData);
        protocolQuestionsData.put("BreathingExercise1", breathExercise1Data);
        protocolQuestionsData.put("BreathingExercise2", breathExercise2Data);
        protocolQuestionsData.put("BreathingExercise3", breathExercise3Data);
        protocolQuestionsData.put("BreathingExerciseNote", strengthNoteData);
        protocolQuestionsData.put("ChangeNote", changeNoteData);

        return protocolQuestionsData;
    }

    public void setProtocolQuestionsUI(HashMap<String, String> protocolQuestionsData) {
        String noticeBreathingData = protocolQuestionsData.get("NoticeBreathingTime");
        switch (noticeBreathingData != null ? noticeBreathingData : "0 times") {
            case "0 times":
                noticeBreathing.setSelection(0);
                break;
            case "1 to 2 times":
                noticeBreathing.setSelection(1);
                break;
            case "3 to 5 times":
                noticeBreathing.setSelection(2);
                break;
            case "more than 5 times":
                noticeBreathing.setSelection(3);
                break;
            default:
                break;
        }

        String breathExerciseData;
        String[] breathExerciseStream;
        for (int i = 1; i <= 3; i++) {
            breathExerciseData = protocolQuestionsData.get("BreathingExercise" + i);
            breathExerciseStream = breathExerciseData != null ? breathExerciseData.substring(1,
                    breathExerciseData.length() - 1).split(", ") : new String[] {"0.0", "0.0", "0.0"};
            if (breathExerciseStream.length == 3) {
                switch (i) {
                    case 1:
                        sIndex1.setText(breathExerciseStream[0]);
                        flow1.setText(breathExerciseStream[1]);
                        volume1.setText(breathExerciseStream[2]);
                        break;
                    case 2:
                        sIndex2.setText(breathExerciseStream[0]);
                        flow2.setText(breathExerciseStream[1]);
                        volume2.setText(breathExerciseStream[2]);
                        break;
                    case 3:
                        sIndex3.setText(breathExerciseStream[0]);
                        flow3.setText(breathExerciseStream[1]);
                        volume3.setText(breathExerciseStream[2]);
                        break;
                    default:
                        break;
                }
            }
        }

        strengthNote.setText(protocolQuestionsData.get("BreathingExerciseNote"));

        changeNote.setText(protocolQuestionsData.get("ChangeNote"));
    }

}
