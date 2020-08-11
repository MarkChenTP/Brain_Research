/**
 *  BloodPressureFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.bloodpressure;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class BloodPressureFragment extends Fragment {

    //private BloodPressureViewModel bloodPressureViewModel;
    private Point screenSize;
    private Config.BPType mBPScreenType;

    private EditText systolicBP, diastolicBP, pulseBP;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bloodpressure, container, false);
        /*
        bloodPressureViewModel = new ViewModelProvider(this).get(BloodPressureViewModel.class);

        final TextView textView = root.findViewById(R.id.text_BloodPressure);
        bloodPressureViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Get Display Size
        Display display = activity.getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        // Initialize UI and Data
        Bundle arguments = getArguments();
        assert arguments != null;
        mBPScreenType = (Config.BPType) arguments.getSerializable("BPType");
        assert mBPScreenType != null;

        systolicBP = root.findViewById(R.id.editText_systolic);
        diastolicBP = root.findViewById(R.id.editText_diastolic);
        pulseBP = root.findViewById(R.id.editText_pulse);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check UI
                checkBloodPressureUI();

                // Save Study Data
                String bloodPressureData = getBloodPressureUI();

                Bundle arguments = new Bundle();
                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                String menstruating = ((StudyActivity) activity).getUserSetting("Menstruating");
                String cpap = ((StudyActivity) activity).getUserSetting("CPAP");
                String protocol = ((StudyActivity) activity).getUserSetting("Protocol");
                Calendar today = Calendar.getInstance();
                boolean isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

                switch(mBPScreenType) {
                    case AfterQuestions:
                        ((StudyActivity) activity).setBloodPressure("BloodPressure1", bloodPressureData);
                        if (dayCount % 5 == 2) {
                            // every 5 days with offset 1 (day 2, 7, 12...)
                            Navigation.findNavController(v).navigate(R.id.action_BP1ToDayQueryTest);
                        } else if ((dayCount >= 15 && protocol.equals("SR")) ||
                                (isFriday && protocol.equals("IMT"))) {
                            // (SR only) start from day 15 or (IMT only) on Friday
                            Navigation.findNavController(v).navigate(R.id.action_BP1ToPlanQueryTest);
                        } else if (menstruating.equals("Yes") || cpap.equals("Yes")) {
                            // can menstruating or use CPAP
                            Navigation.findNavController(v).navigate(R.id.action_BP1ToUserQueryTest);
                        } else {
                            Navigation.findNavController(v).navigate(R.id.action_BP1ToConnPulseOx);
                        }
                        break;
                    case AfterRest:
                        ((StudyActivity) activity).setBloodPressure("BloodPressure2", bloodPressureData);
                        arguments.putSerializable("RestType", Config.RestType.Independent);
                        Navigation.findNavController(v).navigate(R.id.action_BP2ToRest1, arguments);
                        break;
                    case AfterValsalva:
                        ((StudyActivity) activity).setBloodPressure("BloodPressure3", bloodPressureData);
                        arguments.putSerializable("RestType", Config.RestType.AfterValsalva);
                        Navigation.findNavController(v).navigate(R.id.action_BP3ToRest3, arguments);
                        break;
                    case BeforeStressReduce:
                        ((StudyActivity) activity).setBloodPressure("BloodPressure4", bloodPressureData);
                        // every 6 days with offset 0 (day 1, 7, 13...)
                        if (dayCount % 6 == 1) Navigation.findNavController(v).navigate(R.id.action_BP4ToRest5);
                        // every 6 days with offset 2 (day 3, 9, 15...)
                        if (dayCount % 6 == 3) Navigation.findNavController(v).navigate(R.id.action_BP4ToPvtTest);
                        // every 6 days with offset 4 (day 5, 11, 17...)
                        if (dayCount % 6 == 5) Navigation.findNavController(v).navigate(R.id.action_BP4ToStroopTest);
                        break;
                    case AfterStressReduce:
                        ((StudyActivity) activity).setBloodPressure("BloodPressure5", bloodPressureData);
                        Navigation.findNavController(v).navigate(R.id.action_BP5ToStressDownTest);
                        break;
                    default:
                        break;
                }
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                systolicBP.clearFocus(); diastolicBP.clearFocus(); pulseBP.clearFocus();

                // Check UI
                boolean validBloodPressure = checkBloodPressureUIWithToast();

                if(validBloodPressure) {
                    // Save Study Data
                    String bloodPressureData = getBloodPressureUI();

                    Bundle arguments = new Bundle();
                    String id = ((StudyActivity) activity).getUserSetting("ID");
                    int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                    String protocol = ((StudyActivity) activity).getUserSetting("Protocol");

                    switch(mBPScreenType) {
                        case AfterQuestions:
                            ((StudyActivity) activity).setBloodPressure("BloodPressure1", bloodPressureData);
                            ((StudyActivity) activity).setCheckPointReached(true, 0);
                            ((StudyActivity) activity).autoSaveUserStudy();

                            arguments.putSerializable("RestType", Config.RestType.Independent);
                            Navigation.findNavController(v).navigate(R.id.action_BP1ToRest1, arguments);
                            break;
                        case AfterRest:
                            ((StudyActivity) activity).setBloodPressure("BloodPressure2", bloodPressureData);
                            ((StudyActivity) activity).setCheckPointReached(true, 1);
                            ((StudyActivity) activity).autoSaveUserStudy();

                            Navigation.findNavController(v).navigate(R.id.action_BP2ToValsalva);
                            break;
                        case AfterValsalva:
                            ((StudyActivity) activity).setBloodPressure("BloodPressure3", bloodPressureData);
                            ((StudyActivity) activity).setCheckPointReached(true, 2);
                            ((StudyActivity) activity).autoSaveUserStudy();

                            if (dayCount % 6 == 1) {
                                // every 6 days with offset 0 (day 1, 7, 13...)
                                Navigation.findNavController(v).navigate(R.id.action_BP3ToBreathHold);
                            } else if (dayCount % 6 == 3) {
                                // every 6 days with offset 2 (day 3, 9, 15...)
                                Navigation.findNavController(v).navigate(R.id.action_BP3ToPvt);
                            } else if (dayCount % 6 == 5) {
                                // every 6 days with offset 4 (day 5, 11, 17...)
                                Navigation.findNavController(v).navigate(R.id.action_BP3ToStroop);
                            } else if ((dayCount >= 15) && protocol.equals("SR")) {
                                // (SR only) start from day 15
                                Navigation.findNavController(v).navigate(R.id.action_BP3ToStressDown);
                            } else {
                                Navigation.findNavController(v).navigate(R.id.action_BP3ToFinish);
                            }
                            break;
                        case BeforeStressReduce:
                            ((StudyActivity) activity).setBloodPressure("BloodPressure4", bloodPressureData);
                            ((StudyActivity) activity).setCheckPointReached(true, 3);
                            ((StudyActivity) activity).autoSaveUserStudy();

                            if ((dayCount >= 15) && protocol.equals("SR")) {
                                // (SR only) start from day 15
                                Navigation.findNavController(v).navigate(R.id.action_BP4ToStressDown);
                            } else {
                                Navigation.findNavController(v).navigate(R.id.action_BP4ToFinish);
                            }
                            break;
                        case AfterStressReduce:
                            ((StudyActivity) activity).setBloodPressure("BloodPressure5", bloodPressureData);
                            ((StudyActivity) activity).setCheckPointReached(true, 4);
                            ((StudyActivity) activity).autoSaveUserStudy();

                            Navigation.findNavController(v).navigate(R.id.action_BP5ToFinish);
                            break;
                        default:
                            break;
                    }
                }

            }
        });

        switch(mBPScreenType) {
            case AfterQuestions:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_bloodPressure1));
                break;
            case AfterRest:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_bloodPressure2));
                break;
            case AfterValsalva:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_bloodPressure3));
                break;
            case BeforeStressReduce:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_bloodPressure4));
                break;
            case AfterStressReduce:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_bloodPressure5));
                break;
            default:
                break;
        }

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

        Objects.requireNonNull(actionBar).setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));


        String bloodPressureData;
        switch(mBPScreenType) {
            case AfterQuestions:
                actionBar.setTitle(getResources().getString(R.string.title_bloodPressure1));
                bloodPressureData = ((StudyActivity) activity).getBloodPressure("BloodPressure1");
                setBloodPressureUI(bloodPressureData);
                break;
            case AfterRest:
                actionBar.setTitle(getResources().getString(R.string.title_bloodPressure2));
                bloodPressureData = ((StudyActivity) activity).getBloodPressure("BloodPressure2");
                setBloodPressureUI(bloodPressureData);
                break;
            case AfterValsalva:
                actionBar.setTitle(getResources().getString(R.string.title_bloodPressure3));
                bloodPressureData = ((StudyActivity) activity).getBloodPressure("BloodPressure3");
                setBloodPressureUI(bloodPressureData);
                break;
            case BeforeStressReduce:
                actionBar.setTitle(getResources().getString(R.string.title_bloodPressure4));
                bloodPressureData = ((StudyActivity) activity).getBloodPressure("BloodPressure4");
                setBloodPressureUI(bloodPressureData);
                break;
            case AfterStressReduce:
                actionBar.setTitle(getResources().getString(R.string.title_bloodPressure5));
                bloodPressureData = ((StudyActivity) activity).getBloodPressure("BloodPressure5");
                setBloodPressureUI(bloodPressureData);
                break;
            default:
                break;
        }

        systolicBP.clearFocus(); diastolicBP.clearFocus(); pulseBP.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public void checkBloodPressureUI() {
        if (systolicBP.getText().toString().equals("")) systolicBP.setText("0");
        if (diastolicBP.getText().toString().equals("")) diastolicBP.setText("0");
        if (pulseBP.getText().toString().equals("")) pulseBP.setText("0");
    }

    private boolean checkBloodPressureUIWithToast() {
        boolean validBloodPressure = true;
        String errorMessage = "";

        int[] bloodPressure = new int[3];

        if (systolicBP.getText().toString().equals("")) {
            systolicBP.setText("0");
            errorMessage += "Invalid systolic pressure";
        } else {
            bloodPressure[0] = Integer.parseInt(systolicBP.getText().toString());
            if (bloodPressure[0] <= 20 || bloodPressure[0] >= 500) {
                errorMessage += "Invalid systolic pressure";
            }
        }

        if (diastolicBP.getText().toString().equals("")) {
            diastolicBP.setText("0");
            if (!errorMessage.equals("")) errorMessage += "\n";
            errorMessage += "Invalid diastolic pressure";
        } else {
            bloodPressure[1] = Integer.parseInt(diastolicBP.getText().toString());
            if (bloodPressure[1] <= 20 || bloodPressure[1] >= 500) {
                if (!errorMessage.equals("")) errorMessage += "\n";
                errorMessage += "Invalid diastolic pressure";
            }
        }

        if (pulseBP.getText().toString().equals("")) {
            pulseBP.setText("0");
            if (!errorMessage.equals("")) errorMessage += "\n";
            errorMessage += "Invalid pulse";
        } else {
            bloodPressure[2] = Integer.parseInt(pulseBP.getText().toString());
            if (bloodPressure[2] <= 20 || bloodPressure[2] >= 500) {
                if (!errorMessage.equals("")) errorMessage += "\n";
                errorMessage += "Invalid pulse";
            }
        }

        if(bloodPressure[0] <= bloodPressure[1]) {
            if (!errorMessage.equals("")) errorMessage += "\n";
            errorMessage += "Invalid systolic/diastolic pair";
        }

        if (!errorMessage.equals("")) validBloodPressure = false;
        if (!validBloodPressure) {
            AppCompatActivity activity = (AppCompatActivity) requireActivity();

            Toast toast = Toast.makeText(activity.getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
            TextView toastView = toast.getView().findViewById(android.R.id.message);
            if (toastView != null) toastView.setGravity(Gravity.CENTER);
            toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                    ContextCompat.getColor(activity, R.color.Red), PorterDuff.Mode.SRC_IN));
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();
        }

        return validBloodPressure;
    }

    public String getBloodPressureUI() {
        // Blood Pressure: systolic, diastolic, pulse
        int[] bloodPressure = new int[3];
        bloodPressure[0] = Integer.parseInt(systolicBP.getText().toString());
        bloodPressure[1] = Integer.parseInt(diastolicBP.getText().toString());
        bloodPressure[2] = Integer.parseInt(pulseBP.getText().toString());

        return Arrays.toString(bloodPressure);
    }

    public void setBloodPressureUI(String bloodPressureData) {
        String[] bloodPressureStream = bloodPressureData != null ? bloodPressureData.substring(1,
                bloodPressureData.length() - 1).split(", ") : new String[] {"0", "0", "0"};
        if (bloodPressureStream.length == 3) {
            systolicBP.setText(bloodPressureStream[0]);
            diastolicBP.setText(bloodPressureStream[1]);
            pulseBP.setText(bloodPressureStream[2]);
        }
    }

}
