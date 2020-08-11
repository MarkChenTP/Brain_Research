/**
 *  RestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.rest;

import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class RestFragment extends Fragment {

    // private RestViewModel restViewModel;
    private Config.RestType mRestScreenType;
    private boolean restDone;

    private TextView restGuide;

    private ImageView mAlarmClock;
    private TextView restTimer;
    private long restTimeRemain;
    private CountDownTimer mCountDownTimer;

    private MediaPlayer mMediaPlayer;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_rest, container, false);
        /*
        restViewModel = new ViewModelProvider(this).get(RestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_Rest);
        restViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        Bundle arguments = getArguments();
        assert arguments != null;
        mRestScreenType = (Config.RestType) arguments.getSerializable("RestType");
        assert mRestScreenType != null;
        restDone = false;

        restGuide = root.findViewById(R.id.textView_RestGuide);

        mAlarmClock = root.findViewById(R.id.imageView_restTimer);
        restTimer = root.findViewById(R.id.textView_restTimer);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                switch(mRestScreenType) {
                    case Independent:
                        arguments.putSerializable("BPType", Config.BPType.AfterQuestions);
                        Navigation.findNavController(v).navigate(R.id.action_Rest1ToBP1, arguments);
                        break;
                    case BeforeValsalva:
                        Navigation.findNavController(v).navigate(R.id.action_Rest2ToValsalva);
                        break;
                    case AfterValsalva:
                        Navigation.findNavController(v).navigate(R.id.action_Rest3ToValsalvaTest);
                        break;
                    case BeforeBreathHold:
                        Navigation.findNavController(v).navigate(R.id.action_Rest4ToBreathHold);
                        break;
                    case AfterBreathHold:
                        Navigation.findNavController(v).navigate(R.id.action_Rest5ToBreathHoldTest);
                        break;
                    default:
                        break;
                }
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                switch (mRestScreenType) {
                    case Independent:
                        arguments.putSerializable("BPType", Config.BPType.AfterRest);
                        Navigation.findNavController(v).navigate(R.id.action_Rest1ToBP2, arguments);
                        break;
                    case BeforeValsalva:
                        Navigation.findNavController(v).navigate(R.id.action_Rest2ToValsalvaTest);
                        break;
                    case AfterValsalva:
                        arguments.putSerializable("BPType", Config.BPType.AfterValsalva);
                        Navigation.findNavController(v).navigate(R.id.action_Rest3ToBP3, arguments);
                        break;
                    case BeforeBreathHold:
                        Navigation.findNavController(v).navigate(R.id.action_Rest4ToBreathHoldTest);
                        break;
                    case AfterBreathHold:
                        arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                        Navigation.findNavController(v).navigate(R.id.action_Rest5ToBP4, arguments);
                        break;
                    default:
                        break;
                }
            }
        });

        switch (mRestScreenType) {
            case Independent:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_rest1));
                break;
            case BeforeValsalva:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_rest2));
                break;
            case AfterValsalva:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_rest3));
                break;
            case BeforeBreathHold:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_rest4));
                break;
            case AfterBreathHold:
                ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_rest5));
                break;
            default:
                break;
        }

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Pause UI and Data
        pauseCountDownTimer();
        stopAudioFile();
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


        switch (mRestScreenType) {
            case Independent:
                actionBar.setTitle(getResources().getString(R.string.title_rest1));
                restGuide.setText(R.string.rest_independent_guide);
                restTimeRemain = ((StudyActivity) activity).getRemainRestTime(0);
                restDone = ((StudyActivity) activity).getRestDone(0);
                break;
            case BeforeValsalva:
                actionBar.setTitle(getResources().getString(R.string.title_rest2));
                restGuide.setText(R.string.rest_preValsalva_guide);
                restTimeRemain = ((StudyActivity) activity).getRemainRestTime(1);
                restDone = ((StudyActivity) activity).getRestDone(1);
                break;
            case AfterValsalva:
                actionBar.setTitle(getResources().getString(R.string.title_rest3));
                restGuide.setText(R.string.rest_postValsalva_guide);
                restTimeRemain = ((StudyActivity) activity).getRemainRestTime(2);
                restDone = ((StudyActivity) activity).getRestDone(2);
                break;
            case BeforeBreathHold:
                actionBar.setTitle(getResources().getString(R.string.title_rest4));
                restGuide.setText(R.string.rest_breathHold_guide1);
                restTimeRemain = ((StudyActivity)activity).getRemainRestTime(3);
                restDone = ((StudyActivity) activity).getRestDone(3);
                break;
            case AfterBreathHold:
                actionBar.setTitle(getResources().getString(R.string.title_rest5));
                restGuide.setText(R.string.rest_breathHold_guide2);
                restTimeRemain = ((StudyActivity) activity).getRemainRestTime(4);
                restDone = ((StudyActivity) activity).getRestDone(4);
                break;
            default:
                break;
        }

        if (!restDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);
            restTimer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        } else {
            backButton.setEnabled(true);
            nextButton.setEnabled(true);
            restTimer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        }
        runCountDownTimer();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void runCountDownTimer() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        if (mCountDownTimer == null) {
            mAlarmClock.setImageResource(R.drawable.ic_alarm_clock_off);
            mCountDownTimer = new CountDownTimer(restTimeRemain, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    restTimeRemain = millisUntilFinished;
                    restTimer.setText(String.format(Locale.US, "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
                @Override
                public void onFinish() {
                    restTimeRemain = 0;
                    mAlarmClock.setImageResource(R.drawable.ic_alarm_clock_on);

                    if(!restDone) {
                        String TimeEnd = "00:00:00";
                        restTimer.setText(TimeEnd);

                        switch (mRestScreenType) {
                            case Independent:
                                ((StudyActivity) activity).setRestDone(true, 0);
                                break;
                            case BeforeValsalva:
                                ((StudyActivity) activity).setRestDone(true, 1);
                                break;
                            case AfterValsalva:
                                ((StudyActivity) activity).setRestDone(true, 2);
                                break;
                            case BeforeBreathHold:
                                ((StudyActivity) activity).setRestDone(true, 3);
                                break;
                            case AfterBreathHold:
                                ((StudyActivity) activity).setRestDone(true, 4);
                                break;
                            default:
                                break;
                        }
                        nextButton.setEnabled(true);

                        playAudioFile();

                    } else {

                        String restMessage = "Task Completed!";
                        restTimer.setText(restMessage);
                    }
                }
            };
            mCountDownTimer.start();
        }
    }

    private void pauseCountDownTimer() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        switch (mRestScreenType) {
            case Independent:
                ((StudyActivity) activity).setRemainRestTime(restTimeRemain, 0);
                break;
            case BeforeValsalva:
                ((StudyActivity) activity).setRemainRestTime(restTimeRemain, 1);
                break;
            case AfterValsalva:
                ((StudyActivity) activity).setRemainRestTime(restTimeRemain, 2);
                break;
            case BeforeBreathHold:
                ((StudyActivity) activity).setRemainRestTime(restTimeRemain, 3);
                break;
            case AfterBreathHold:
                ((StudyActivity) activity).setRemainRestTime(restTimeRemain, 4);
                break;
            default:
                break;
        }
    }


    private void playAudioFile() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        View view = requireView();

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(activity, R.raw.rest_beeping);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudioFile();

                    Bundle arguments = new Bundle();
                    switch (mRestScreenType) {
                        case Independent:
                            arguments.putSerializable("BPType", Config.BPType.AfterRest);
                            Navigation.findNavController(view).navigate(R.id.action_Rest1ToBP2, arguments);
                            break;
                        case BeforeValsalva:
                            Navigation.findNavController(view).navigate(R.id.action_Rest2ToValsalvaTest);
                            break;
                        case AfterValsalva:
                            arguments.putSerializable("BPType", Config.BPType.AfterValsalva);
                            Navigation.findNavController(view).navigate(R.id.action_Rest3ToBP3, arguments);
                            break;
                        case BeforeBreathHold:
                            Navigation.findNavController(view).navigate(R.id.action_Rest4ToBreathHoldTest);
                            break;
                        case AfterBreathHold:
                            arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                            Navigation.findNavController(view).navigate(R.id.action_Rest5ToBP4, arguments);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        mMediaPlayer.start();
    }

    private void resumeAudioFile() {
        if (mMediaPlayer != null) mMediaPlayer.start();
    }

    private void pauseAudioFile() {
        if (mMediaPlayer != null) mMediaPlayer.pause();
    }

    private void stopAudioFile() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
