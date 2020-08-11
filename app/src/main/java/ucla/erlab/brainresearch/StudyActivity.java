/**
 *  StudyActivity.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */
package ucla.erlab.brainresearch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import ucla.erlab.brainresearch.studyui.bloodpressure.BloodPressureFragment;
import ucla.erlab.brainresearch.studyui.connpulseox.ConnectPulseOxFragment;
import ucla.erlab.brainresearch.studyui.questions.DayQuestionsTestFragment;
import ucla.erlab.brainresearch.studyui.questions.ProtocolQuestionsTestFragment;
import ucla.erlab.brainresearch.studyui.questions.SubjectQuestionsTestFragment;
import ucla.erlab.brainresearch.studyui.stress.StressReduceFragment;


/**
 *  A study can include the following phases:
 *
 *  •  Introduction
 *  -> Equipment Setup (Blood Pressure Cuff, Pulse Oximeter, etc.)
 *  -> Subject Question -> Protocol Question -> Day Questions -> Blood Pressure #1
 *  -> Rest -> Blood Pressure #2
 *  -> Valsalva Intro -> Pre-Valsalva Rest -> Valsalva Action -> Post-Valsalva Rest -> Blood Pressure #3
 *  -> (Option 1) BreathHold Intro -> Pre-BreathHold Rest ->
 *                      BreathHold Action -> Post-BreathHold Rest -> Blood Pressure #4
 *     or
 *     (Option 2) PVT Intro -> PVT Action -> Blood Pressure #4
 *     or
 *     (Option 3) Stroop Intro -> Stroop Action -> Blood Pressure #4
 *  -> Stress Reduction Intro -> Stress Reduction Action -> Blood Pressure #5
 *  -> Finish
 */
public class StudyActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FileManager mFileManager;
    private TimeManager mTimeManager;
    private Point screenSize;

    private boolean initStudyActivity;
    private boolean bluetoothEnabled;

    // Bluetooth Fields
    private boolean pulseOxSelected;
    private boolean bluetoothServiceStopped;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private String mBluetoothDeviceName;
    private BluetoothService mBluetoothService;

    // Settings Data Fields
    private HashMap<String, String> userSettings;

    // Study Data Fields
    private String studyDate;
    private String studySavePoint;
    private boolean[] checkPointsReached;               // Checkpoints are Blood Pressure #1 to #5
    private HashMap<String, String> subjectQuestions;
    private HashMap<String, String> protocolQuestions;
    private HashMap<String, String> dayQuestions;
    private ArrayList<String> pulsesAndSpO2s;           // pulse, spO2 values from pulse oximeter
    private HashMap<String, String> bloodPressures;     // systolic, diastolic, pulse values from blood pressure cuff
    private ArrayList<String> pvtShowRedDotTimes;
    private ArrayList<String> pvtTouchRedDotTimes;
    private ArrayList<String> stroopColors;
    private ArrayList<String> stroopResponses;
    private ArrayList<String> stroopResults;
    private String stressDownAudioChoice;

    // Study Action Fields
    private long[] restDurations;
    private long[] restRemainTimes;
    private boolean[] restDones;

    private long valsalvaDuration;
    private boolean valsalvaDone;

    private long breathHoldDuration;
    private boolean breathHoldDone;

    private long pvtDuration;
    private long pvtReactTime;
    private boolean pvtDone;

    private int stroopRounds;
    private boolean stroopDone;

    private int stressDownAudioPosition;
    private boolean stressDownDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        // Display the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        // Initialize UI and Data
        initStudyActivity = true;
        bluetoothEnabled = false;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFileManager = new FileManager(this);
        mTimeManager = new TimeManager(this);

        pulseOxSelected = false;
        bluetoothServiceStopped = false;
        initBluetooth();

        // Load Settings Data
        HashMap<String, String> settingsData = mFileManager.readSettingsData();
        if (settingsData.isEmpty()) {
            newUserSettings();
        } else {
            loadUserSettings(settingsData);
        }

        // Load Study Data
        newUserStudy();

        boolean studyDataExist =
                mFileManager.checkStudyData(userSettings.get("ID"), userSettings.get("DayCount"));
        if (studyDataExist) {
            loadUserStudy();

            Toast toast = Toast.makeText(getApplicationContext(), "Resume " + userSettings.get("ID")
                    + "'s Day " + userSettings.get("DayCount") + " study", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();

        } else {

            Toast toast = Toast.makeText(getApplicationContext(), "New " + userSettings.get("ID")
                    + "'s Day " + userSettings.get("DayCount") + " study", Toast.LENGTH_SHORT);
            toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.DodgerBlue), PorterDuff.Mode.SRC_IN));
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Custom Animation while switching Activity
        //overridePendingTransition(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume UI and Data
        if (!initStudyActivity && bluetoothEnabled) {
            Toast toast = Toast.makeText(getApplicationContext(), "Day " + userSettings.get("DayCount")
                    + " study as " + userSettings.get("ID"), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();
        }
        initStudyActivity = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_study, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle selected item in the menu of the action bar
        int id = item.getItemId();

        if (id == R.id.action_loadData) {
            // Load Study Data
            HashMap<String, String> studyData = loadUserStudy();

            if (studyData.isEmpty()) {
                Toast toast = Toast.makeText(getApplicationContext(), userSettings.get("ID") + "'s Day " +
                        userSettings.get("DayCount") + " data NOT available", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                toast.show();

            } else {
                // Update current UI fragment with Study Data
                updateUIWithUserStudy();

                Toast toast = Toast.makeText(getApplicationContext(), "Load " + studyData.get("ID") + "'s Day " +
                        studyData.get("DayCount") + " data", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                toast.show();
            }
            return true;
        }


        if (id == R.id.action_saveData) {
            // Update Study Data with current UI fragment
            updateUserStudyWithUI();

            // Save Study Data
            HashMap<String, String> studyData = saveUserStudy();
            uploadUserStudy();

            Toast toast = Toast.makeText(getApplicationContext(), "Save " + studyData.get("ID") + "'s Day " +
                    studyData.get("DayCount") + " data", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();

            return true;
        }

        if (id == R.id.action_exitStudy) {
            // Update Study Data with current UI fragment
            updateUserStudyWithUI();

            // Save and Upload Study Data
            autoSaveUserStudy();

            finish(); // Destroy Current Activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop Bluetooth Service
        if (mBluetoothService != null) {
            mBluetoothService.stop();
            bluetoothServiceStopped = true;

            if (mBluetoothDeviceName != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Disconnected from "
                        + mBluetoothDeviceName, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                toast.show();
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }



    public String getUserSetting(String key) {
        return userSettings.get(key);
    }

    public String getStudySavePoint() {
        return studySavePoint;
    }
    public void setStudySavePoint(String savePoint) {
        studySavePoint = savePoint;
    }

    public boolean getCheckPointReached(int index) {
        return checkPointsReached[index];
    }
    public void setCheckPointReached(boolean bool, int index) {
        checkPointsReached[index] = bool;
    }

    public HashMap<String, String> getSubjectQuestions() {
        return subjectQuestions;
    }
    public void setSubjectQuestions(HashMap<String, String> subjectQuestionsData) {
        subjectQuestions = subjectQuestionsData;
    }

    public HashMap<String, String> getProtocolQuestions() {
        return protocolQuestions;
    }
    public void setProtocolQuestions(HashMap<String, String> protocolQuestionsData) {
        protocolQuestions = protocolQuestionsData;
    }

    public HashMap<String, String> getDayQuestions() {
        return dayQuestions;
    }
    public void setDayQuestions(HashMap<String, String> dayQuestionsData) {
        dayQuestions = dayQuestionsData;
    }

    public String getBloodPressure(String key) {
        return bloodPressures.get(key);
    }
    public void setBloodPressure(String key, String bloodPressureData) {
        bloodPressures.put(key, bloodPressureData);
    }

    public void setPvtShowRedDotTimes(ArrayList<String> showRedDotTimesData) {
        pvtShowRedDotTimes = showRedDotTimesData;
    }
    public void setPvtTouchRedDotTimes(ArrayList<String> touchRedDotTimesData) {
        pvtTouchRedDotTimes = touchRedDotTimesData;
    }

    public void setStroopColors(ArrayList<String> colorNamesData) {
        stroopColors = colorNamesData;
    }
    public void setStroopResponses(ArrayList<String> colorResponsesData) {
        stroopResponses = colorResponsesData;
    }
    public void setStroopResults(ArrayList<String> colorResultsData) {
        stroopResults = colorResultsData;
    }

    public String getStressDownAudioChoice() {
        return stressDownAudioChoice;
    }
    public void setStressDownAudioChoice(String audioChoiceData) {
        stressDownAudioChoice = audioChoiceData;
    }



    public boolean getPulseOxSelected() {
        return pulseOxSelected;
    }
    public void setPulseOxSelected(boolean bool) {
        pulseOxSelected = bool;
    }

    public long getRemainRestTime(int index) {
        return restRemainTimes[index];
    }
    public void setRemainRestTime(long time, int index) {
        restRemainTimes[index] = time;
    }
    public boolean getRestDone(int index) {
        return restDones[index];
    }
    public void setRestDone(boolean bool, int index) {
        restDones[index] = bool;
    }

    public long getValsalvaDuration() {
        return valsalvaDuration;
    }
    public boolean getValsalvaDone() {
        return valsalvaDone;
    }
    public void setValsalvaDone(boolean bool) {
        valsalvaDone = bool;
    }

    public long getBreathHoldDuration() {
        return breathHoldDuration;
    }
    public boolean getBreathHoldDone() {
        return breathHoldDone;
    }
    public void setBreathHoldDone(boolean bool) {
        breathHoldDone = bool;
    }

    public long getPvtDuration() {
        return pvtDuration;
    }
    public long getPvtReactTime() {
        return pvtReactTime;
    }
    public boolean getPvtDone() {
        return pvtDone;
    }
    public void setPvtDone(boolean bool) {
        pvtDone = bool;
    }

    public int getStroopRounds() {
        return stroopRounds;
    }
    public boolean getStroopDone() {
        return stroopDone;
    }
    public void setStroopDone(boolean bool) {
        stroopDone = bool;
    }

    public int getStressDownAudioPosition() {
        return stressDownAudioPosition;
    }
    public void setStressDownAudioPosition(int position) {
        stressDownAudioPosition = position;
    }
    public boolean getStressDownDone() {
        return stressDownDone;
    }
    public void setStressDownDone(boolean bool) {
        stressDownDone = bool;
    }



    private void newUserSettings() {
        // New(Default) User Settings Data Fields
        userSettings = new LinkedHashMap<>();
        userSettings.put("ID", "0");
        userSettings.put("Sex", "Male");
        userSettings.put("DayCount", "1");
        userSettings.put("Group", "0");
        userSettings.put("Protocol", "SR");
        userSettings.put("Menstruating", "No");
        userSettings.put("CPAP", "No");
        userSettings.put("Testing Mode", "No");

        mFileManager.writeSettingsData(userSettings);
    }

    private void loadUserSettings(HashMap<String, String> settingsData) {
        // Load User Settings Data Fields
        userSettings = new LinkedHashMap<>();
        userSettings.put("ID", settingsData.get("ID"));
        userSettings.put("Sex", settingsData.get("Sex"));
        userSettings.put("DayCount", settingsData.get("DayCount"));
        userSettings.put("Group", settingsData.get("Group"));
        userSettings.put("Protocol",settingsData.get("Protocol"));
        userSettings.put("Menstruating",settingsData.get("Menstruating"));
        userSettings.put("CPAP", settingsData.get("CPAP"));
        userSettings.put("TestingMode", settingsData.get("TestingMode"));
    }

    private void newUserStudy() {
        // New Study Data Fields
        studyDate = mTimeManager.getStartDate();
        studySavePoint = "Introduction";
        checkPointsReached = new boolean[] { false, false, false, false, false };

        subjectQuestions = new LinkedHashMap<>();
        subjectQuestions.put("MenstruateStarted", "No");
        subjectQuestions.put("CPAPTime", "0:0");

        protocolQuestions = new LinkedHashMap<>();
        protocolQuestions.put("NoticeBreathingTime", "0 times");
        protocolQuestions.put("BreathingExercise1", "[0.0, 0.0, 0.0]");
        protocolQuestions.put("BreathingExercise2", "[0.0, 0.0, 0.0]");
        protocolQuestions.put("BreathingExercise3", "[0.0, 0.0, 0.0]");
        protocolQuestions.put("BreathingExerciseNote", "");
        protocolQuestions.put("ChangeNote", "");

        dayQuestions = new LinkedHashMap<>();
        dayQuestions.put("SleepTime", "0:0");
        dayQuestions.put("SleepRank", "5");
        dayQuestions.put("NewExperience", "No");
        dayQuestions.put("NewExperienceNote", "");
        dayQuestions.put("BadLuckEmotion", "Never");
        dayQuestions.put("HardLifeEmotion", "Never");
        dayQuestions.put("BoredEmotion", "Not at all");
        dayQuestions.put("DepressEmotion", "Not at all");
        dayQuestions.put("NervousEmotion", "Not at all");
        dayQuestions.put("WorryEmotion", "Not at all");
        dayQuestions.put("ExerciseLevel", "Minimal");

        pulsesAndSpO2s = new ArrayList<>();

        bloodPressures = new LinkedHashMap<>();
        bloodPressures.put("BloodPressure1", "[0, 0, 0]");
        bloodPressures.put("BloodPressure2", "[0, 0, 0]");
        bloodPressures.put("BloodPressure3", "[0, 0, 0]");
        bloodPressures.put("BloodPressure4", "[0, 0, 0]");
        bloodPressures.put("BloodPressure5", "[0, 0, 0]");

        pvtShowRedDotTimes = new ArrayList<>();
        pvtTouchRedDotTimes = new ArrayList<>();

        stroopColors = new ArrayList<>();
        stroopResponses = new ArrayList<>();
        stroopResults = new ArrayList<>();

        stressDownAudioChoice = "Music and Guide";

        // New Study Action Fields
        // TODO: Update precise values for each protocol
        switch (Objects.requireNonNull(userSettings.get("Protocol"))) {
            case "SR":
                // +1 sec to if animation is used
                restDurations = new long[]{ 61000, 31000, 31000, 31000, 31000 }; // millisecond
                restRemainTimes = new long[]{ 61000, 31000, 31000, 31000, 31000 }; // millisecond
                valsalvaDuration = 19000; // millisecond
                breathHoldDuration = 31000; // millisecond
                pvtDuration = 60000000000L; // nanosecond
                pvtReactTime = 3000; // millisecond
                stroopRounds = 10;
                stressDownAudioPosition = 0; // millisecond
                break;
            case "IMT":
                // +1 sec to if animation is used
                restDurations = new long[]{ 61000, 46000, 31000, 46000, 31000 }; // millisecond
                restRemainTimes = new long[]{ 61000, 46000, 31000, 46000, 31000 }; // millisecond
                valsalvaDuration = 19000; // millisecond
                breathHoldDuration = 31000; // millisecond
                pvtDuration = 60000000000L; // nanosecond
                pvtReactTime = 3000; // millisecond
                stroopRounds = 10;
                stressDownAudioPosition = 0; // millisecond
                break;
            default:
                break;
        }

        restDones = new boolean[]{ false, false, false, false, false };
        valsalvaDone = false;
        breathHoldDone = false;
        pvtDone = false;
        stroopDone = false;
        stressDownDone = false;
    }

    private HashMap<String, String> loadUserStudy() {
        HashMap<String, String> studyData =
                mFileManager.readStudyData(userSettings.get("ID"), userSettings.get("DayCount"));

        if (!studyData.isEmpty()) {
            // Load Study Data Fields
            studyDate = studyData.get("StudyDate");
            studySavePoint = studyData.get("SavePoint");

            String checkPointReachedData = studyData.get("CheckPointReached");
            if (checkPointReachedData == null) checkPointReachedData = "[false, false, false, false, false]";
            String[] checkPointReachedStream = checkPointReachedData.substring(1,
                    checkPointReachedData.length() - 1).split(", ");
            for (int i = 0; i < 5; i++) {
                checkPointsReached[i] = Boolean.parseBoolean(checkPointReachedStream[i]);
            }


            subjectQuestions.put("MenstruateStarted", studyData.get("MenstruateStarted"));
            subjectQuestions.put("CPAPTime", studyData.get("CPAPTime"));

            protocolQuestions.put("NoticeBreathingTime",studyData.get("NoticeBreathingTime"));
            protocolQuestions.put("BreathingExercise1", studyData.get("BreathingExercise1"));
            protocolQuestions.put("BreathingExercise2", studyData.get("BreathingExercise2"));
            protocolQuestions.put("BreathingExercise3", studyData.get("BreathingExercise3"));
            protocolQuestions.put("BreathingExerciseNote", studyData.get("BreathingExerciseNote"));
            protocolQuestions.put("ChangeNote", studyData.get("ChangeNote"));

            dayQuestions.put("SleepTime", studyData.get("SleepTime"));
            dayQuestions.put("SleepRank", studyData.get("SleepRank"));
            dayQuestions.put("NewExperience", studyData.get("NewExperience"));
            dayQuestions.put("NewExperienceNote", studyData.get("NewExperienceNote"));
            dayQuestions.put("BadLuckEmotion", studyData.get("BadLuckEmotion"));
            dayQuestions.put("HardLifeEmotion", studyData.get("HardLifeEmotion"));
            dayQuestions.put("BoredEmotion",  studyData.get("BoredEmotion"));
            dayQuestions.put("DepressEmotion", studyData.get("DepressEmotion"));
            dayQuestions.put("NervousEmotion", studyData.get("NervousEmotion"));
            dayQuestions.put("WorryEmotion", studyData.get("WorryEmotion"));
            dayQuestions.put("ExerciseLevel", studyData.get("ExerciseLevel"));

            bloodPressures.put("BloodPressure1", studyData.get("BloodPressure1"));
            bloodPressures.put("BloodPressure2", studyData.get("BloodPressure2"));
            bloodPressures.put("BloodPressure3", studyData.get("BloodPressure3"));
            bloodPressures.put("BloodPressure4", studyData.get("BloodPressure4"));
            bloodPressures.put("BloodPressure5", studyData.get("BloodPressure5"));

            // Load Study Action Fields
            if (checkPointsReached[1]) {
                restRemainTimes[0] = 0; restDones[0] = true;
            }
            if (checkPointsReached[2]) {
                restRemainTimes[1] = 0; restDones[1] = true;
                restRemainTimes[2] = 0; restDones[2] = true;
                valsalvaDone = true;
            }
            if (checkPointsReached[3]) {
                restRemainTimes[3] = 0; restDones[3] = true;
                restRemainTimes[4] = 0; restDones[4] = true;
                breathHoldDone = true;

                String pvtShowRedDotData = studyData.get("PVTShowRedDot");
                if (pvtShowRedDotData == null) pvtShowRedDotData = "";
                if (!pvtShowRedDotData.equals("")) {
                    String[] pvtShowRedDotStream = pvtShowRedDotData.split(", ");
                    pvtShowRedDotTimes = new ArrayList<>(Arrays.asList(pvtShowRedDotStream));
                }
                String pvtTouchRedDotData = studyData.get("PVTTouchRedDot");
                if (pvtTouchRedDotData == null) pvtTouchRedDotData = "";
                if (!pvtTouchRedDotData.equals("")) {
                    String[] pvtTouchRedDotStream = pvtTouchRedDotData.split(", ");
                    pvtTouchRedDotTimes = new ArrayList<>(Arrays.asList(pvtTouchRedDotStream));
                }
                pvtDone = true;

                String stroopColorData = studyData.get("StroopColor");
                if (stroopColorData == null) stroopColorData = "";
                if (!stroopColorData.equals("")) {
                    String[] stroopColorStream = stroopColorData.split(", ");
                    stroopColors = new ArrayList<>(Arrays.asList(stroopColorStream));
                }
                String stroopResponseData = studyData.get("StroopResponse");
                if (stroopResponseData == null) stroopResponseData = "";
                if (!stroopResponseData.equals("")) {
                    String[] stroopResponseStream = stroopResponseData.split(", ");
                    stroopResponses = new ArrayList<>(Arrays.asList(stroopResponseStream));
                }
                String stroopResultData = studyData.get("StroopResult");
                if (stroopResultData == null) stroopResultData = "";
                if (!stroopResultData.equals("")) {
                    String[] stroopResultStream = stroopResultData.split(", ");
                    stroopResults = new ArrayList<>(Arrays.asList(stroopResultStream));
                }
                stroopDone = true;
            }
            if (checkPointsReached[4]) {
                stressDownAudioChoice = studyData.get("StressReduceMusic");
                stressDownDone = true;
            }
        }

        return studyData;
    }

    private void updateUserStudyWithUI() {
        Fragment studyNavHost = getSupportFragmentManager().findFragmentById(R.id.study_nav_fragment);
        assert studyNavHost != null;
        Fragment currStudyFragment = studyNavHost.getChildFragmentManager().getFragments().get(0);

        if (currStudyFragment instanceof SubjectQuestionsTestFragment) {
            setSubjectQuestions(((SubjectQuestionsTestFragment) currStudyFragment).getSubjectQuestionsUI());
        }

        if (currStudyFragment instanceof ProtocolQuestionsTestFragment) {
            ((ProtocolQuestionsTestFragment) currStudyFragment).checkProtocolQuestionsUI();
            setProtocolQuestions(((ProtocolQuestionsTestFragment) currStudyFragment).getProtocolQuestionsUI());
        }

        if (currStudyFragment instanceof DayQuestionsTestFragment) {
            setDayQuestions(((DayQuestionsTestFragment) currStudyFragment).getDayQuestionsUI());
        }

        if (currStudyFragment instanceof BloodPressureFragment) {
            Bundle arguments = currStudyFragment.getArguments();
            assert arguments != null;
            Config.BPType mBPScreenType = (Config.BPType) arguments.getSerializable("BPType");
            assert mBPScreenType != null;

            ((BloodPressureFragment) currStudyFragment).checkBloodPressureUI();
            switch (mBPScreenType) {
                case AfterQuestions:
                    setBloodPressure("BloodPressure1", ((BloodPressureFragment) currStudyFragment).getBloodPressureUI());
                    break;
                case AfterRest:
                    setBloodPressure("BloodPressure2", ((BloodPressureFragment) currStudyFragment).getBloodPressureUI());
                    break;
                case AfterValsalva:
                    setBloodPressure("BloodPressure3", ((BloodPressureFragment) currStudyFragment).getBloodPressureUI());
                    break;
                case BeforeStressReduce:
                    setBloodPressure("BloodPressure4", ((BloodPressureFragment) currStudyFragment).getBloodPressureUI());
                    break;
                case AfterStressReduce:
                    setBloodPressure("BloodPressure5", ((BloodPressureFragment) currStudyFragment).getBloodPressureUI());
                    break;
                default:
                    break;
            }
        }

        if (currStudyFragment instanceof StressReduceFragment) {
            setStressDownAudioChoice(((StressReduceFragment) currStudyFragment).getStressReduceUI());
        }
    }

    private void updateUIWithUserStudy() {
        Fragment studyNavHost = getSupportFragmentManager().findFragmentById(R.id.study_nav_fragment);
        assert studyNavHost != null;
        Fragment currStudyFragment = studyNavHost.getChildFragmentManager().getFragments().get(0);

        if (currStudyFragment instanceof SubjectQuestionsTestFragment) {
            ((SubjectQuestionsTestFragment) currStudyFragment).setSubjectQuestionsUI(subjectQuestions);
        }

        if (currStudyFragment instanceof ProtocolQuestionsTestFragment) {
            ((ProtocolQuestionsTestFragment) currStudyFragment).setProtocolQuestionsUI(protocolQuestions);
        }

        if (currStudyFragment instanceof DayQuestionsTestFragment) {
            ((DayQuestionsTestFragment) currStudyFragment).setDayQuestionsUI(dayQuestions);
        }

        if (currStudyFragment instanceof BloodPressureFragment) {
            Bundle arguments = currStudyFragment.getArguments();
            assert arguments != null;
            Config.BPType mBPScreenType = (Config.BPType) arguments.getSerializable("BPType");
            assert mBPScreenType != null;

            switch (mBPScreenType) {
                case AfterQuestions:
                    ((BloodPressureFragment) currStudyFragment).setBloodPressureUI(getBloodPressure("BloodPressure1"));
                    break;
                case AfterRest:
                    ((BloodPressureFragment) currStudyFragment).setBloodPressureUI(getBloodPressure("BloodPressure2"));
                    break;
                case AfterValsalva:
                    ((BloodPressureFragment) currStudyFragment).setBloodPressureUI(getBloodPressure("BloodPressure3"));
                    break;
                case BeforeStressReduce:
                    ((BloodPressureFragment) currStudyFragment).setBloodPressureUI(getBloodPressure("BloodPressure4"));
                    break;
                case AfterStressReduce:
                    ((BloodPressureFragment) currStudyFragment).setBloodPressureUI(getBloodPressure("BloodPressure5"));
                    break;
                default:
                    break;
            }
        }

        if (currStudyFragment instanceof StressReduceFragment) {
            ((StressReduceFragment) currStudyFragment).setStressReduceUI(stressDownAudioChoice);
        }
    }

    public void autoSaveUserStudy() {
        // Update Study Data with current UI fragment
        updateUserStudyWithUI();

        saveUserStudy();
        uploadUserStudy();

        Toast toast = Toast.makeText(getApplicationContext(), "Auto-save " + userSettings.get("ID")
                + "'s Day " + userSettings.get("DayCount") + " data", Toast.LENGTH_SHORT);
        toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.DarkCyan), PorterDuff.Mode.SRC_IN));
        toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
        toast.show();
    }


    public void finishUserStudy() {
        // Update Study Data with current UI fragment
        updateUserStudyWithUI();

        saveUserStudy();
        uploadUserStudy();

        Toast toast = Toast.makeText(getApplicationContext(), userSettings.get("ID")
                + "'s Day " + userSettings.get("DayCount") + " study " + "completed", Toast.LENGTH_SHORT);
        toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.DodgerBlue), PorterDuff.Mode.SRC_IN));
        toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
        toast.show();
    }

    private HashMap<String, String> saveUserStudy() {
        // Save User Study Data Fields
        HashMap<String, String> studyData = new LinkedHashMap<>();

        studyData.put("StudyDate", studyDate);
        studyData.put("SavePoint", studySavePoint);
        String checkPointReached = Arrays.toString(checkPointsReached);
        studyData.put("CheckPointReached", checkPointReached);

        studyData.putAll(userSettings);
        studyData.putAll(subjectQuestions);
        studyData.putAll(protocolQuestions);
        studyData.putAll(dayQuestions);
        studyData.putAll(bloodPressures);

        String pvtShowRedDot = TextUtils.join(", ", pvtShowRedDotTimes);
        String pvtTouchRedDot = TextUtils.join(", ", pvtTouchRedDotTimes);
        studyData.put("PVTShowRedDot", pvtShowRedDot);
        studyData.put("PVTTouchRedDot", pvtTouchRedDot);

        String stroopColor = TextUtils.join(", ", stroopColors);
        String stroopResponse = TextUtils.join(", ", stroopResponses);
        String stroopResult = TextUtils.join(", ", stroopResults);
        studyData.put("StroopColor", stroopColor);
        studyData.put("StroopResponse", stroopResponse);
        studyData.put("StroopResult", stroopResult);

        studyData.put("StressReduceMusic", stressDownAudioChoice);

        mFileManager.writeStudyData(studyData);

        return studyData;
    }

    private void uploadUserStudy() {
        String id = Objects.requireNonNull(userSettings.get("ID"));
        String day = Objects.requireNonNull(userSettings.get("DayCount"));
        String protocol = Objects.requireNonNull(userSettings.get("Protocol"));
        String menstruating = Objects.requireNonNull(userSettings.get("Menstruating"));
        String cpap = Objects.requireNonNull(userSettings.get("CPAP"));
        Calendar today = Calendar.getInstance();
        boolean isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

        DatabaseReference userProfile = mDatabase.child(id).child("Day " + day + " Study");

        userProfile.child("StudyDate").setValue(studyDate);
        userProfile.child("SavePoint").setValue(studySavePoint);

        if (menstruating.equals("Yes")) {
            userProfile.child("Subject-MenstruateStarted").setValue(subjectQuestions.get("MenstruateStarted"));
        }

        if (cpap.equals("Yes")) {
            userProfile.child("Subject-CPAPTime").setValue(subjectQuestions.get("CPAPTime"));
        }

        if (isFriday && protocol.equals("IMT")) {
            userProfile.child("IMT-BreathingExercise1").setValue(protocolQuestions.get("BreathingExercise1"));
            userProfile.child("IMT-BreathingExercise2").setValue(protocolQuestions.get("BreathingExercise2"));
            userProfile.child("IMT-BreathingExercise3").setValue(protocolQuestions.get("BreathingExercise3"));
            userProfile.child("IMT-BreathingExerciseNote").setValue(protocolQuestions.get("BreathingExerciseNote"));
            userProfile.child("IMT-ChangeNote").setValue(protocolQuestions.get("ChangeNote"));
        }

        if (Integer.parseInt(day)  % 5 == 2) {
            userProfile.child("Day-SleepTime").setValue(dayQuestions.get("SleepTime"));
            userProfile.child("Day-SleepRank").setValue(dayQuestions.get("SleepRank"));
            userProfile.child("Day-NewExperience").setValue(dayQuestions.get("NewExperience"));
            userProfile.child("Day-NewExperienceNote").setValue(dayQuestions.get("NewExperienceNote"));
            userProfile.child("Day-BadLuckEmotion").setValue(dayQuestions.get("BadLuckEmotion"));
            userProfile.child("Day-HardLifeEmotion").setValue(dayQuestions.get("HardLifeEmotion"));
            userProfile.child("Day-BoredEmotion").setValue(dayQuestions.get("BoredEmotion"));
            userProfile.child("Day-DepressEmotion").setValue(dayQuestions.get("DepressEmotion"));
            userProfile.child("Day-NervousEmotion").setValue(dayQuestions.get("NervousEmotion"));
            userProfile.child("Day-WorryEmotion").setValue(dayQuestions.get("WorryEmotion"));
            userProfile.child("Day-ExerciseLevel").setValue(dayQuestions.get("ExerciseLevel"));
        }

        userProfile.child("BloodPressure-PostQuestion").setValue(bloodPressures.get("BloodPressure1"));

        userProfile.child("BloodPressure-PostRest").setValue(bloodPressures.get("BloodPressure2"));

        userProfile.child("BloodPressure-PostValsalva").setValue(bloodPressures.get("BloodPressure3"));

        if (Integer.parseInt(day) % 6 == 1) {
            // every 6 days with offset 0 (day 1, 7, 13...)
            userProfile.child("BloodPressure-PostBreathHold").setValue(bloodPressures.get("BloodPressure4"));
        }

        if (Integer.parseInt(day) % 6 == 3) {
            // every 6 days with offset 2 (day 3, 9, 15...)
            userProfile.child("BloodPressure-PostPVT").setValue(bloodPressures.get("BloodPressure4"));

            String pvtShowRedDot = "[" + TextUtils.join(", ", pvtShowRedDotTimes) + "]";
            String pvtTouchRedDot = "[" + TextUtils.join(", ", pvtTouchRedDotTimes) + "]";
            userProfile.child("PVT-ShowRedDotTimes").setValue(pvtShowRedDot);
            userProfile.child("PVT-TouchRedDotTimes").setValue(pvtTouchRedDot);
        }

        if (Integer.parseInt(day) % 6 == 5) {
            // every 6 days with offset 4 (day 5, 11, 17...)
            userProfile.child("BloodPressure-PostStroop").setValue(bloodPressures.get("BloodPressure4"));

            String stroopColor = "[" + TextUtils.join(", ", stroopColors) + "]";
            String stroopResponse = "[" + TextUtils.join(", ", stroopResponses) + "]";
            String stroopResult = "[" + TextUtils.join(", ", stroopResults) + "]";
            userProfile.child("Stroop-Colors").setValue(stroopColor);
            userProfile.child("Stroop-Responses").setValue(stroopResponse);
            userProfile.child("Stroop-Results").setValue(stroopResult);
        }

        if ((Integer.parseInt(day) >= 15) && protocol.equals("SR")) {
            userProfile.child("SR-NoticeBreathingTime").setValue(protocolQuestions.get("NoticeBreathingTime"));

            userProfile.child("BloodPressure-PostStressReduce").setValue(bloodPressures.get("BloodPressure5"));
            userProfile.child("StressReduce-MusicOption").setValue(stressDownAudioChoice);
        }
    }



    /**
     * Initialize Bluetooth
     */
    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Objects.requireNonNull(mBluetoothAdapter);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mBluetoothEnableResult.launch(enableIntent);
        } else {
            bluetoothEnabled = true;
        }
    }

    /**
     * Result Callback for Bluetooth Enable
     */
    ActivityResultLauncher<Intent> mBluetoothEnableResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        initBluetooth();
                    } else {
                        bluetoothEnabled = true;
                    }
                }
            });

    /**
     * Initialize the BluetoothService to perform bluetooth connections
     */
    public void initBluetoothService() {
        // Initialize the BluetoothService to perform bluetooth connections
        if (mBluetoothService == null) mBluetoothService = new BluetoothService(this, mHandler);
    }

    /**
     * Establish connection with a bluetooth device via BluetoothService
     *
     * @param deviceAddress A String of the targeted device's address.
     * @param secure        Socket Security type - Secure (true) , Insecure (false)
     */
    public void connectBluetoothDevice(String deviceAddress, boolean secure) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);

        assert mBluetoothService != null;
        mBluetoothService.connect(mBluetoothDevice, secure);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if (!bluetoothServiceStopped) {
                Fragment studyNavHost = getSupportFragmentManager().findFragmentById(R.id.study_nav_fragment);
                assert studyNavHost != null;
                Fragment currStudyFragment = studyNavHost.getChildFragmentManager().getFragments().get(0);
                String pulseOxStatusText = "";
                String pulseOxStatusImage = "";

                switch (msg.what) {
                    case BluetoothService.MESSAGE_STATE_CHANGE:
                        switch ((int) msg.obj) {
                            case BluetoothService.STATE_NONE:
                            case BluetoothService.STATE_LISTEN:
                                pulseOxStatusImage = "ImageView";
                                pulseOxStatusText = "Not connected";
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                pulseOxStatusImage = "ProgressBar";
                                pulseOxStatusText = "Connecting...";
                                break;
                            case BluetoothService.STATE_CONNECTED:
                                pulseOxStatusImage = "ProgressBar";
                                pulseOxStatusText = String.format(Locale.US, "Pulse - %d\nSpO\u2082 - %d", 0, 0);
                                break;
                            case BluetoothService.STATE_CONNECTFAILED:
                                pulseOxStatusImage = "ImageView";
                                pulseOxStatusText = "Connection failed";
                                break;
                            case BluetoothService.STATE_CONNECTLOST:
                                pulseOxStatusImage = "ImageView";
                                pulseOxStatusText = "Connection lost";
                                break;
                            default:
                                break;
                        }
                        if (currStudyFragment instanceof ConnectPulseOxFragment) {
                            ((ConnectPulseOxFragment) currStudyFragment).setConnPulseOxUI(
                                    pulseOxStatusImage, pulseOxStatusText);
                        }
                        break;

                    case BluetoothService.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        break;

                    case BluetoothService.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;

                        if (msg.arg1 != 0) {
                            String[] pulseOxData = parseNoninDataEight(readBuf, msg.arg2);

                            pulseOxStatusImage = "ProgressBar";
                            pulseOxStatusText = Arrays.toString(pulseOxData);
                            pulseOxStatusText = pulseOxStatusText.substring(1, pulseOxStatusText.length() - 1);

                            savePulseAndSpO2(pulseOxStatusText);
                            uploadPulseAndSpO2(pulseOxData);

                            pulseOxStatusText = String.format(Locale.US, "Pulse - %d\nSpO\u2082 - %d",
                                    Integer.parseInt(pulseOxData[0]), Integer.parseInt(pulseOxData[1]));

                            if (currStudyFragment instanceof ConnectPulseOxFragment) {
                                ((ConnectPulseOxFragment) currStudyFragment).setConnPulseOxUI(
                                        pulseOxStatusImage, pulseOxStatusText);
                            }
                        }
                        break;

                    case BluetoothService.MESSAGE_TOAST:
                        String topic = msg.getData().getString("Topic");
                        switch (topic != null ? topic : "") {
                            case "Connected":
                                mBluetoothDeviceName = msg.getData().getString("DeviceName");
                                break;
                            case "Failed":
                            case "Lost":
                                mBluetoothDeviceName = null;
                                pulseOxSelected = false;
                                break;
                            default:
                                break;
                        }

                        String toastMessage = msg.getData().getString("Message");
                        Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                        toast.show();
                        break;
                }
            }
            return false;
        }
    });

    /**
     * Sends a message to a bluetooth device via BluetoothService
     *
     * @param message A string of text to send.
     */
    private void sendTextMessage(String message) {
        assert mBluetoothService != null;

        // Check that we're actually connected before trying anything
        if (mBluetoothService.getBluetoothState() != BluetoothService.STATE_CONNECTED) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "You are not connected to a device", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();

            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }

    /**
     * Interpret packet data from Nonin Pulse Oximeter
     */
    public String[] parseNoninDataEight(byte[] noninByteData, int bytesRead) {
        String[] pulseOxData;

        String[] noninBinaryData = new String[]{
                String.format("%8s", Integer.toBinaryString(noninByteData[0] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(noninByteData[1] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(noninByteData[2] & 0xFF)).replace(' ', '0'),
                String.format("%8s", Integer.toBinaryString(noninByteData[3] & 0xFF)).replace(' ', '0'),
        };

        String binaryPulse = noninBinaryData[0].substring(noninBinaryData[0].length() - 2)
                + noninBinaryData[1].substring(1);
        String binarySpO2 = noninBinaryData[2].substring(1);

        int intPulse = Integer.parseInt(binaryPulse, 2);
        int intSpO2 = Integer.parseInt(binarySpO2, 2);
        String time = TimeManager.getFineTimeString();
        String date = TimeManager.getDateString();

        pulseOxData = new String[] { Integer.toString(intPulse), Integer.toString(intSpO2),
                studySavePoint, time, date };

        return pulseOxData;
    }

    private void savePulseAndSpO2(String pulseSpO2Data) {
        pulsesAndSpO2s.add(pulseSpO2Data);
        mFileManager.writePulseOxData(pulseSpO2Data,
                userSettings.get("ID"), userSettings.get("DayCount"));
    }

    private void uploadPulseAndSpO2(String[] pulseSpO2Data) {
        String id = Objects.requireNonNull(userSettings.get("ID"));
        String day = Objects.requireNonNull(userSettings.get("DayCount"));
        DatabaseReference userProfile = mDatabase.child(id).child("Day " + day + " PulseOx");

        if (pulseSpO2Data.length == 5) {
            // Firebase Database paths must not contain '.', '#', '$', '[', or ']'
            String pulseSpO2Key = pulseSpO2Data[4] + " " + pulseSpO2Data[3];
            pulseSpO2Key = pulseSpO2Key.replace(".", ":");

            String pulseSpO2Value = "[" + pulseSpO2Data[0] + ", " + pulseSpO2Data[1]
                    + ", " + pulseSpO2Data[2] + "]";

            userProfile.child(pulseSpO2Key).setValue(pulseSpO2Value);
        }
    }
}
