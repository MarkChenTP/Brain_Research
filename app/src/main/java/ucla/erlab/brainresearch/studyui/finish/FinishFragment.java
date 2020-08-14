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
 *  FinishFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.finish;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class FinishFragment extends Fragment {

    //private FinishViewModel finishViewModel;

    private TextView studyDayFinished;

    private Button backButton;
    private Button exitButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_finish, container, false);
        /*
        finishViewModel = new ViewModelProvider(this).get(FinishViewModel.class);

        final TextView textView = root.findViewById(R.id.text_Finish);
        finishViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and data
        studyDayFinished = root.findViewById(R.id.textView_finishDay);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();

                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                String protocol = ((StudyActivity) activity).getUserSetting("Protocol");

                if ((dayCount >= 15) && protocol.equals("SR")) {
                    arguments.putSerializable("BPType", Config.BPType.AfterStressReduce);
                    Navigation.findNavController(v).navigate(R.id.action_FinishToBP5, arguments);
                } else if (dayCount % 6 == 1 || dayCount % 6 == 3 || dayCount % 6 == 5) {
                    arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                    Navigation.findNavController(v).navigate(R.id.action_FinishToBP4, arguments);
                } else {
                    arguments.putSerializable("BPType", Config.BPType.AfterValsalva);
                    Navigation.findNavController(v).navigate(R.id.action_FinishToBP3, arguments);
                }
            }
        });

        exitButton = root.findViewById(R.id.button_Exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((StudyActivity) activity).finishUserStudy();
                activity.finish();
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_finish));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_finish));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        String dayCount = ((StudyActivity) activity).getUserSetting("DayCount");
        studyDayFinished.setText(dayCount);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
