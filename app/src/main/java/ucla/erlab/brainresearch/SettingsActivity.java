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
 *  SettingsActivity.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch;

import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.LinkedHashMap;


public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FileManager mFileManager;
    private Point screenSize;

    // Study Data Fields
    private String id, sex, group;
    private String dayCount, protocol;
    private String menstruating, useCPAP;
    private String testingMode;
    private String dateModified;

    private EditText subjectId;
    private RadioGroup subjectSex;
    private RadioButton subjectSexMale, subjectSexFemale;
    private EditText studyDayCount;
    private EditText subjectGroup;
    private Spinner studyProtocol;
    private RadioGroup subjectMenstruating;
    private RadioButton subjectMenstruatingYes, subjectMenstruatingNo;
    private RadioGroup subjectCPAP;
    private RadioGroup studyTestingMode;

    private Button resetButton;
    private Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        // Initialize UI and Data
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFileManager = new FileManager(this);

        subjectId = findViewById(R.id.editText_subjectId);
        subjectSex = findViewById(R.id.radioGroup_sex);
        studyDayCount = findViewById(R.id.editText_dayCount);
        subjectGroup = findViewById(R.id.editText_subjectGroup);
        studyProtocol = findViewById(R.id.spinner_protocol);
        subjectMenstruating = findViewById(R.id.radioGroup_menstruating);
        subjectCPAP = findViewById(R.id.radioGroup_CPAP);
        studyTestingMode = findViewById(R.id.radioGroup_testMode);

        subjectSexMale = findViewById(R.id.radioButton_sex_male);
        subjectSexFemale = findViewById(R.id.radioButton_sex_female);
        subjectMenstruatingYes = findViewById(R.id.radioButton_menstruating_yes);
        subjectMenstruatingNo = findViewById(R.id.radioButton_menstruating_no);

        subjectSexMale.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subjectMenstruating.check(R.id.radioButton_menstruating_no);
                subjectMenstruatingYes.setEnabled(false);
            }
        });

        subjectSexFemale.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subjectMenstruatingYes.setEnabled(true);
            }
        });

        resetButton = findViewById(R.id.button_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subjectId.clearFocus(); studyDayCount.clearFocus(); subjectGroup.clearFocus();

                // Reset UI
                setSettingsUI();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Reset to " + id + "'s settings", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y*0.35));
                toast.show();
            }
        });

        saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subjectId.clearFocus(); studyDayCount.clearFocus(); subjectGroup.clearFocus();

                // Check UI
                boolean validSettings = checkSettingsUI();

                if(validSettings) {
                    // Save Settings Data
                    getSettingsUI();

                    HashMap<String, String> settingsData = saveUserSettings();
                    UploadUserSettings();

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Save " + settingsData.get("ID") + "'s settings", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                    toast.show();

                    finish(); // Destroy Current Activity
                }
            }
        });

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
        HashMap<String, String> settingsData;
        settingsData = mFileManager.readSettingsData();

        if (settingsData.isEmpty()) {
            newUserSettings(settingsData);
        } else {
            loadUserSettings(settingsData);
        }

        setSettingsUI();

        Toast toast = Toast.makeText(getApplicationContext(),
                "Load " + id + "'s settings", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y*0.35));
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle selected item in the menu of the action bar
        int id = item.getItemId();

        if (id == R.id.action_exitSettings) {
            finish(); // Destroy Current Activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private boolean checkSettingsUI() {
        boolean validSettings = true;
        String errorMessage = "";

        if (subjectId.getText().toString().equals("")) {
            subjectId.setText("0");
            errorMessage += "Invalid subject ID";
        }

        if (studyDayCount.getText().toString().equals("")) {
            studyDayCount.setText("1");
            if (!errorMessage.equals("")) errorMessage += "\n";
            errorMessage += "Invalid day count";
        }

        if (subjectGroup.getText().toString().equals("")) {
            subjectGroup.setText("0");
            if (!errorMessage.equals("")) errorMessage += "\n";
            errorMessage += "Invalid group";
        }

        if (!errorMessage.equals("")) validSettings = false;
        if (!validSettings) {
            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
            TextView toastView = toast.getView().findViewById(android.R.id.message);
            if (toastView != null) toastView.setGravity(Gravity.CENTER);
            toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.Red), PorterDuff.Mode.SRC_IN));
            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
            toast.show();
        }

        return validSettings;
    }

    public void getSettingsUI() {
        id = subjectId.getText().toString();

        // sex: 0 = male, 1 = female
        int sex_index = subjectSex.indexOfChild(
                subjectSex.findViewById(subjectSex.getCheckedRadioButtonId()));
        switch (sex_index) {
            case 0:
                sex = "Male";
                break;
            case 1:
                sex = "Female";
                break;
            default:
                sex = "";
                break;
        }

        dayCount = studyDayCount.getText().toString();

        group = subjectGroup.getText().toString();

        // protocol: 0 = SR, 1 = IMT
        int protocol_index = studyProtocol.getSelectedItemPosition();
        switch (protocol_index) {
            case 0:
                protocol = "SR";
                break;
            case 1:
                protocol = "IMT";
                break;
            default:
                protocol = "";
                break;
        }

        // menstruating: 0 = yes, 1 = no
        int menstruating_index = subjectMenstruating.indexOfChild(
                subjectMenstruating.findViewById(subjectMenstruating.getCheckedRadioButtonId()));
        switch (menstruating_index) {
            case 0:
                menstruating = "Yes";
                break;
            case 1:
                menstruating = "No";
                break;
            default:
                menstruating = "";
                break;
        }

        // cpap: 0 = yes, 1 = no
        int cpap_index = subjectCPAP.indexOfChild(
                subjectCPAP.findViewById(subjectCPAP.getCheckedRadioButtonId()));
        switch (cpap_index) {
            case 0:
                useCPAP = "Yes";
                break;
            case 1:
                useCPAP = "No";
                break;
            default:
                useCPAP = "";
                break;
        }

        // testingMode: 0 = yes, 1 = no
        int testingMode_index = studyTestingMode.indexOfChild(
                studyTestingMode.findViewById(studyTestingMode.getCheckedRadioButtonId()));
        switch (testingMode_index) {
            case 0:
                testingMode = "Yes";
                break;
            case 1:
                testingMode = "No";
                break;
            default:
                testingMode = "";
                break;
        }
    }

    public void setSettingsUI() {
        subjectId.setText(id);

        switch (sex) {
            case "Male":
                subjectSex.check(R.id.radioButton_sex_male);
                subjectMenstruatingYes.setEnabled(false);
                break;
            case "Female":
                subjectSex.check(R.id.radioButton_sex_female);
                subjectMenstruatingYes.setEnabled(true);
                break;
            default:
                break;
        }

        studyDayCount.setText(dayCount);

        subjectGroup.setText(group);

        switch (protocol) {
            case "SR":
                studyProtocol.setSelection(0);
                break;
            case "IMT":
                studyProtocol.setSelection(1);
                break;
            default:
                break;
        }

        switch (menstruating) {
            case "Yes":
                subjectMenstruating.check(R.id.radioButton_menstruating_yes);
                break;
            case "No":
                subjectMenstruating.check(R.id.radioButton_menstruating_no);
                break;
            default:
                break;
        }

        switch (useCPAP) {
            case "Yes":
                subjectCPAP.check(R.id.radioButton_CPAP_yes);
                break;
            case "No":
                subjectCPAP.check(R.id.radioButton_CPAP_no);
                break;
            default:
                break;
        }

        switch (testingMode) {
            case "Yes":
                studyTestingMode.check(R.id.radioButton_testMode_yes);
                break;
            case "No":
                studyTestingMode.check(R.id.radioButton_testMode_no);
                break;
            default:
                break;
        }
    }

    private void newUserSettings(HashMap<String, String> settingsData) {
        // New(Default) User Settings Data Fields
        id = "0";
        sex = "Male";
        dayCount = "1";
        group = "0";
        protocol = "SR";
        menstruating = "No";
        useCPAP = "No";
        testingMode = "No";

        settingsData.put("ID", id);
        settingsData.put("Sex", sex);
        settingsData.put("DayCount", dayCount);
        settingsData.put("Group", group);
        settingsData.put("Protocol", protocol);
        settingsData.put("Menstruating", menstruating);
        settingsData.put("CPAP", useCPAP);
        settingsData.put("TestingMode", testingMode);

        mFileManager.writeSettingsData(settingsData);
    }

    private void loadUserSettings(HashMap<String, String> settingsData) {
        // Load User Settings Data Fields
        id = settingsData.get("ID");
        sex = settingsData.get("Sex");
        dayCount = settingsData.get("DayCount");
        group = settingsData.get("Group");
        protocol = settingsData.get("Protocol");
        menstruating = settingsData.get("Menstruating");
        useCPAP = settingsData.get("CPAP");
        testingMode = settingsData.get("TestingMode");
    }

    private HashMap<String, String> saveUserSettings() {
        // Save User Settings Data Fields
        HashMap<String, String> settingsData = new LinkedHashMap<>();

        settingsData.put("ID", id);
        settingsData.put("Sex", sex);
        settingsData.put("DayCount", dayCount);
        settingsData.put("Group", group);
        settingsData.put("Protocol", protocol);
        settingsData.put("Menstruating", menstruating);
        settingsData.put("CPAP", useCPAP);
        settingsData.put("TestingMode", testingMode);

        mFileManager.writeSettingsData(settingsData);

        return settingsData;
    }

    private void UploadUserSettings() {
        DatabaseReference userProfile = mDatabase.child(id).child("Settings");
        userProfile.child("Sex").setValue(sex);
        userProfile.child("DayCount").setValue(dayCount);
        userProfile.child("Group").setValue(group);
        userProfile.child("Protocol").setValue(protocol);
        userProfile.child("Menstruating").setValue(menstruating);
        userProfile.child("CPAP").setValue(useCPAP);
        userProfile.child("TestingMode").setValue(testingMode);
    }

}