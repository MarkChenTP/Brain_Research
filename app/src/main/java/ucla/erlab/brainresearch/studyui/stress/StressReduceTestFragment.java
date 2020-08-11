/**
 *  StressReduceTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.stress;

import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class StressReduceTestFragment extends Fragment {

    //private StressReduceTestViewModel stressReduceTestViewModel;

    private LinearLayout stressReduceTestTextArea;
    private MediaPlayer mMediaPlayer;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stressreduce_test, container, false);
        /*
        stressReduceTestViewModel = new ViewModelProvider(this).get(StressReduceTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_StressReduceTest);
        stressReduceTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        stressReduceTestTextArea = root.findViewById(R.id.linearLayout_StressReduceTestText);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_StressDownTestToStressDown);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Bundle arguments = new Bundle();
                arguments.putSerializable("BPType", Config.BPType.AfterStressReduce);
                Navigation.findNavController(v).navigate(R.id.action_StressDownTestToBP5, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_stressDownTest));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Pause UI and Data
        stopAudioFile();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Resume UI and Data
        ActionBar actionBar = activity.getSupportActionBar();
        Window window = activity.getWindow();

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_stressDownTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimaryAlternate)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarkAlternate));


        boolean stressDownDone = ((StudyActivity) activity).getStressDownDone();
        if (!stressDownDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);
            stressReduceTestTextArea.setVisibility(View.GONE);

            String musicChoice = ((StudyActivity) activity).getStressDownAudioChoice();
            switch (musicChoice) {
                case "Music and Guide":
                    playAudioFile(R.raw.breatheawareness_musicandguide);
                    break;
                case "Music Only":
                    playAudioFile(R.raw.breatheawareness_music);
                    break;
                default:
                    break;
            }

        } else {

            backButton.setEnabled(true);
            nextButton.setEnabled(true);
            stressReduceTestTextArea.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void playAudioFile(int musicId) {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        View view = requireView();

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(activity, musicId);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudioFile();

                    ((StudyActivity) activity).setStressDownDone(true);

                    Bundle arguments = new Bundle();
                    arguments.putSerializable("BPType", Config.BPType.AfterStressReduce);
                    Navigation.findNavController(view).navigate(R.id.action_StressDownTestToBP5, arguments);
                }
            });
        }
        mMediaPlayer.seekTo(((StudyActivity) activity).getStressDownAudioPosition());
        mMediaPlayer.start();
    }

    private void pauseAudioFile() {
        if (mMediaPlayer != null) mMediaPlayer.pause();
    }

    private void resumeAudioFile() {
        if (mMediaPlayer != null) mMediaPlayer.start();
    }

    private void stopAudioFile() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        if (mMediaPlayer != null) {
            int musicPosition = mMediaPlayer.getCurrentPosition();
            ((StudyActivity) activity).setStressDownAudioPosition(musicPosition);

            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
