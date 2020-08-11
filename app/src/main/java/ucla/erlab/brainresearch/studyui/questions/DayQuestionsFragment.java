/**
 *  DayQuestionsFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.questions;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import java.util.Objects;

import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class DayQuestionsFragment extends Fragment {

    //private DayQuestionsViewModel dayQuestionsViewModel;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dayquestions, container, false);
        /*
        dayQuestionsViewModel = new ViewModelProvider(this).get(DayQuestionsViewModel.class);

        final TextView textView = root.findViewById(R.id.text_DayQuestions);
        dayQuestionsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                String menstruating = ((StudyActivity) activity).getUserSetting("Menstruating");
                String cpap = ((StudyActivity) activity).getUserSetting("CPAP");
                String protocol = ((StudyActivity) activity).getUserSetting("Protocol");
                Calendar today = Calendar.getInstance();
                boolean isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

                if ((dayCount >= 15 && protocol.equals("SR")) ||
                        (isFriday && protocol.equals("IMT"))) {
                    // (SR only) start from day 15 or (IMT only) on Friday
                    Navigation.findNavController(v).navigate(R.id.action_DayQueryToPlanQueryTest);
                } else if (menstruating.equals("Yes") || cpap.equals("Yes")) {
                    // can menstruating or use CPAP
                    Navigation.findNavController(v).navigate(R.id.action_DayQueryToUserQueryTest);
                } else {
                    Navigation.findNavController(v).navigate(R.id.action_DayQueryToConnPulseOx);
                }
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_DayQueryToDayQueryTest);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_dayQuestions));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_dayQuestions));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
