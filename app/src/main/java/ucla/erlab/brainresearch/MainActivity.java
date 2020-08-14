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
 *  MainActivity.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_READ = 100;  // Request code for External Read
    private static final int REQUEST_EXTERNAL_WRITE = 101; // Request code for External Write

    private FileManager mFileManager;
    private TimeManager mTimeManager;
    private Point screenSize;

    // Settings Data Fields
    private HashMap<String, String> userSettings;

    private Button studyButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Display the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        // Request Phone Permission
        requestExternalReadAccess();
        requestExternalWriteAccess();

        // Initialize UI and Data
        mFileManager = new FileManager(this);
        mTimeManager = new TimeManager(this);

        TextView versionName = findViewById(R.id.textView_appVersion);
        versionName.setText("V " + BuildConfig.VERSION_NAME);

        studyButton = findViewById(R.id.button_Study);
        studyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Load Study Data
                HashMap<String, String> studyData =
                        mFileManager.readStudyData(userSettings.get("ID"), userSettings.get("DayCount"));

                if (studyData.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                    startActivity(intent);

                } else {

                    String studyDate = Objects.requireNonNull(studyData.get("StudyDate"));
                    String savePoint = Objects.requireNonNull(studyData.get("SavePoint"));

                    if (mTimeManager.getStartDate().equals(studyDate)) {
                        if (savePoint.equals(getResources().getString(R.string.title_finish))) {
                            Toast toast = Toast.makeText(getApplicationContext(), userSettings.get("ID")
                                    + "'s Day " + userSettings.get("DayCount") + " study "
                                    + "already finished", Toast.LENGTH_SHORT);
                            toast.getView().getBackground().setColorFilter(new PorterDuffColorFilter(
                                    ContextCompat.getColor(v.getContext(), R.color.Red), PorterDuff.Mode.SRC_IN));
                            toast.setGravity(Gravity.CENTER, 0, (int) (screenSize.y * 0.35));
                            toast.show();
                        } else {
                            Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        int dayCount = Integer.parseInt(Objects.requireNonNull(userSettings.get("DayCount")));
                        userSettings.put("DayCount", String.valueOf(dayCount + 1));
                        saveUserSettings();

                        Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        settingsButton = findViewById(R.id.button_Settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Load Settings Data
        HashMap<String, String> settingsData = mFileManager.readSettingsData();
        if (settingsData.isEmpty()) {
            newUserSettings();
        } else {
            loadUserSettings(settingsData);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle selected item in the menu of the action bar
        int id = item.getItemId();

        if (id == R.id.action_exitMain) {
            finish(); // Destroy Current Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void saveUserSettings() {
        // Save User Settings Data Fields
        mFileManager.writeSettingsData(userSettings);
    }



    /**
     * Request External Write Permission from the phone.
     */
    public void requestExternalWriteAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_READ);
        }
    }

    /**
     * Request External Write Permission from the phone.
     */
    public void requestExternalReadAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_WRITE);
        }
    }
}
