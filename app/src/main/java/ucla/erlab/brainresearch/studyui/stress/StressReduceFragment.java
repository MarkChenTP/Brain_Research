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
 *  StressReduceFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.stress;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class StressReduceFragment extends Fragment {

    //private StressReduceViewModel stressReduceViewModel;

    private RadioGroup musicOption;
    private RadioButton musicOptionYesGuide;
    private RadioButton musicOptionNoGuide;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stressreduce, container, false);
        /*
        stressReduceViewModel = new ViewModelProvider(this).get(StressReduceViewModel.class);

        final TextView textView = root.findViewById(R.id.text_StressReduce);
        stressReduceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and data
        musicOption = root.findViewById(R.id.radioGroup_audio);
        musicOption.check(R.id.radioButton_audio_yesGuide);

        musicOptionYesGuide = root.findViewById(R.id.radioButton_audio_yesGuide);
        musicOptionNoGuide = root.findViewById(R.id.radioButton_audio_noGuide);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setStressDownAudioChoice(getStressReduceUI());

                Bundle arguments = new Bundle();
                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));

                if (dayCount % 6 == 1 || dayCount % 6 == 3 || dayCount % 6 == 5) {
                    arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                    Navigation.findNavController(v).navigate(R.id.action_StressDownToBP4, arguments);
                } else {
                    arguments.putSerializable("BPType", Config.BPType.AfterValsalva);
                    Navigation.findNavController(v).navigate(R.id.action_StressDownToBP3, arguments);
                }
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Save Study Data
                ((StudyActivity) activity).setStressDownAudioChoice(getStressReduceUI());

                Navigation.findNavController(v).navigate(R.id.action_StressDownToStressDownTest);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_stressDown));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_stressDown));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));


        // Load Study Data
        setStressReduceUI(((StudyActivity) activity).getStressDownAudioChoice());

        int dayCount = Integer.parseInt(((StudyActivity) requireActivity()).getUserSetting("DayCount"));
        if (dayCount == 15) musicOptionNoGuide.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public String getStressReduceUI() {
        String musicChoiceData;
        // Music Choice: 0 = Music and Guide, 1 = Music Only
        int musicChoice_index = musicOption.indexOfChild(
                musicOption.findViewById(musicOption.getCheckedRadioButtonId()));
        switch (musicChoice_index) {
            case 0:
                musicChoiceData = "Music and Guide";
                break;
            case 1:
                musicChoiceData = "Music Only";
                break;
            default:
                musicChoiceData = "";
                break;
        }

        return musicChoiceData;
    }

    public void setStressReduceUI(String musicChoiceData) {
        switch (musicChoiceData != null ? musicChoiceData : "Music and Guide") {
            case "Music and Guide":
                musicOption.check(R.id.radioButton_audio_yesGuide);
                break;
            case "Music Only":
                musicOption.check(R.id.radioButton_audio_noGuide);
                break;
            default:
                break;
        }
    }

}
