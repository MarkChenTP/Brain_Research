/**
 *  FileManager.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;


public class FileManager {

    private Context mContext;
    private File mAppDataRootDir;
    private File mSharedDataRootDir;


    public FileManager(Context context) {
        mContext = context;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mAppDataRootDir = mContext.getExternalFilesDir(null);
            mSharedDataRootDir = Environment.getExternalStoragePublicDirectory("BrainResearch");
            if (!mSharedDataRootDir.exists()) {
                mSharedDataRootDir.mkdirs();
            }

        } else {
            // Cannot read and write the media
            Log.e("Data Storage", "Primary shared/external storage media not mounted");
        }
    }


    /**
     * Clear the content of a text file
     */
    private void clearFile(File file) {
        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter(new FileWriter(file));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert printWriter != null;
            printWriter.close();
            Log.d("Data Storage", "Successfully clear data in text file");
        }
    }

    /**
     * Write settings data to text files
     */
    public void writeSettingsData(HashMap<String, String> settingsData) {
        File file;
        PrintWriter printWriter = null;

        // Main file for open a study
        try {
            // get text file
            file = new File(mSharedDataRootDir, "Settings" + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                clearFile(file);
            }

            printWriter = new PrintWriter(new FileWriter(file, true));

            for (Map.Entry<String, String> pair : settingsData.entrySet()) {
                // write data into the text file
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }

            Log.d("Data Storage", "Successfully write settings data to text file");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot write settings data to text file");
            e.printStackTrace();

        } finally {
            if (printWriter != null) printWriter.close();
        }

        // Backup file for the user
        try {
            File mSharedDataUserDir = new File(mSharedDataRootDir, Objects.requireNonNull(settingsData.get("ID")));
            if (!mSharedDataUserDir.exists()) {
                mSharedDataUserDir.mkdirs();
            }

            // get text file
            file = new File(mSharedDataUserDir, "Settings_" + settingsData.get("ID") + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                clearFile(file);
            }

            printWriter = new PrintWriter(new FileWriter(file, true));

            for (Map.Entry<String, String> pair : settingsData.entrySet()) {
                // write data into the text file
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }

            Log.d("Data Storage", "Successfully write settings data to text file");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Data Storage", "Cannot write settings data to text file");

        } finally {
            if (printWriter != null) printWriter.close();
        }

    }

    /**
     * Read settings data from a text file
     */
    public HashMap<String, String> readSettingsData() {
        File file;
        BufferedReader bufferReader;
        HashMap<String, String> settingsData = new LinkedHashMap<>();

        // Main file for open a study
        try {
            // get text file
            file = new File(mSharedDataRootDir, "Settings" + ".txt");
            if (file.exists()) {
                bufferReader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = bufferReader.readLine()) != null) {
                    // read data from the text file
                    String[] pair = line.split("=");
                    if (pair.length == 1) {
                        settingsData.put(pair[0], "");
                    } else {
                        settingsData.put(pair[0], pair[1]);
                    }
                }

                bufferReader.close();
            }
            Log.d("Data Storage", "Successfully read settings data from text file");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot read settings data to text file");
            e.printStackTrace();
        }

        return settingsData;
    }

    /**
     * Check if study data exist as a text file
     */
    public boolean checkStudyData(String subjectId, String dayCount) {
        boolean studyDataExist = false;

        // Main file from the user
        try {
            File mSharedDataUserDir = new File(mSharedDataRootDir, subjectId);
            if (!mSharedDataUserDir.exists()) {
                mSharedDataUserDir.mkdirs();
            }

            // get matching file list
            File[] matchedfiles = mSharedDataUserDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().equals("Study_" + subjectId + "_Day_" + dayCount + ".txt");
                }
            });

            if (matchedfiles != null && matchedfiles.length == 1) studyDataExist = true;

            Log.d("Data Storage", "Successfully check if study data text file exists");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot check if study data text file exists");
            e.printStackTrace();
        }

        return studyDataExist;
    }

    /**
     * Write study data to a text file
     */
    public void writeStudyData(HashMap<String, String> studyData) {
        File file;
        PrintWriter printWriter = null;

        // Main file for the user
        try {
            File mSharedDataUserDir = new File(mSharedDataRootDir, Objects.requireNonNull(studyData.get("ID")));
            if (!mSharedDataUserDir.exists()) {
                mSharedDataUserDir.mkdirs();
            }

            // get text file
            file = new File(mSharedDataUserDir, "Study_" + studyData.get("ID") + "_Day_" + studyData.get("DayCount") + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                clearFile(file);
            }

            printWriter = new PrintWriter(new FileWriter(file, true));

            for (Map.Entry<String, String> pair : studyData.entrySet()) {
                // write data into the text file
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }

            Log.d("Data Storage", "Successfully write study data to text file");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot write study data to text file");
            e.printStackTrace();

        } finally {
            if (printWriter != null) printWriter.close();
        }

    }

    /**
     * Read study data from a text file
     */
    public HashMap<String, String> readStudyData(String subjectId, String dayCount) {
        File file;
        BufferedReader bufferReader;
        HashMap<String, String> studyData = new LinkedHashMap<>();

        // Main file from the user
        try {
            File mSharedDataUserDir = new File(mSharedDataRootDir, subjectId);
            if (!mSharedDataUserDir.exists()) {
                mSharedDataUserDir.mkdirs();
            }

            // get text file
            file = new File(mSharedDataUserDir, "Study_" + subjectId + "_Day_" + dayCount + ".txt");
            if (file.exists()) {
                bufferReader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = bufferReader.readLine()) != null) {
                    // read data from the text file
                    String[] pair = line.split("=");
                    if (pair.length == 1) {
                        studyData.put(pair[0], "");
                    } else {
                        studyData.put(pair[0], pair[1]);
                    }
                }

                bufferReader.close();
            }

            Log.d("Data Storage", "Successfully read study data from text file");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot read study data to text file");
            e.printStackTrace();
        }

        return studyData;
    }


    /**
     * Write study data to a text file
     */
    public void writePulseOxData(String pulseOxData, String subjectId, String dayCount) {
        File file;
        PrintWriter printWriter = null;

        // Main file for the user
        try {
            File mSharedDataUserDir = new File(mSharedDataRootDir, subjectId);
            if (!mSharedDataUserDir.exists()) {
                mSharedDataUserDir.mkdirs();
            }

            // get text file
            file = new File(mSharedDataUserDir, "PulseOx_" + subjectId + "_Day_" + dayCount + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            printWriter = new PrintWriter(new FileWriter(file, true));

            printWriter.println(pulseOxData);

            Log.d("Data Storage", "Successfully write pulse oximeter data to text file");

        } catch (Exception e) {
            Log.e("Data Storage", "Cannot write pulse oximeter data to text file");
            e.printStackTrace();

        } finally {
            if (printWriter != null) printWriter.close();
        }

    }
}

