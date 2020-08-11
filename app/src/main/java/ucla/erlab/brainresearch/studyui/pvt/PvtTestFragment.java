/**
 *  PvtTestFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.pvt;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class PvtTestFragment extends Fragment {

    //private PvtTestViewModel pvtTestViewModel;

    private ArrayList<Long> showRedDotTimes;
    private ArrayList<Long> touchRedDotTimes;

    private long pvtDuration, pvtReactTime;
    private Handler mHandler;
    private Runnable myPvtTask;
    private boolean mPvtTaskInit = false;
    private int pvtTaskCount;
    private long pvtStartTime;

    private LinearLayout pvtTestArea;
    private int pvtTestAreaWidth = 0;
    private int pvtTestAreaHeight = 0;

    private LinearLayout pvtTestTextArea;
    private TextView pvtText;

    private Button backButton;
    private Button nextButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_pvt_test, container, false);
        /*
        pvtTestViewModel = new ViewModelProvider(this).get(PvtTestViewModel.class);

        final TextView textView = root.findViewById(R.id.text_BreathHoldTest);
        pvtTestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        showRedDotTimes = new ArrayList<>();
        touchRedDotTimes = new ArrayList<>();

        pvtTestArea = root.findViewById(R.id.linearLayout_PvtTestArea);
        pvtTestArea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pvtTestAreaWidth = pvtTestArea.getWidth();
                pvtTestAreaHeight = pvtTestArea.getHeight();
            }
        });

        pvtTestTextArea = root.findViewById(R.id.linearLayout_PvtTestText);
        pvtText = root.findViewById(R.id.textView_pvtTestText);

        pvtDuration = ((StudyActivity) activity).getPvtDuration();
        pvtReactTime = ((StudyActivity) activity).getPvtReactTime();

        if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());

        if (myPvtTask == null) {
            myPvtTask = new Runnable() {
                public void run() {
                    boolean reschedule = checkReschedule();
                    if(!reschedule) {

                        ((StudyActivity) activity).setPvtDone(true);

                        // Save Study Data
                        ArrayList<String> showRedDotTimesData = formatRedDotTimes("show");
                        ArrayList<String> touchRedDotTimesData = formatRedDotTimes("touch");
                        ((StudyActivity) activity).setPvtShowRedDotTimes(showRedDotTimesData);
                        ((StudyActivity) activity).setPvtTouchRedDotTimes(touchRedDotTimesData);

                        Bundle arguments = new Bundle();
                        arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                        Navigation.findNavController(root).navigate(R.id.action_PvtTestToBP4, arguments);

                    } else {

                        TextView mRedDot = root.findViewById(R.id.textView_redDot);
                        mRedDot.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                touchRedDotTimes.set(pvtTaskCount - 1, System.nanoTime() - pvtStartTime);
                                mRedDot.setVisibility(View.GONE);
                            }
                        });

                        Random rand = new Random();
                        int x = rand.nextInt(Integer.MAX_VALUE) % pvtTestAreaWidth;
                        int y = rand.nextInt(Integer.MAX_VALUE) % pvtTestAreaHeight;

                        int margin = 70;
                        if (x < margin) x += margin;
                        else if (x > pvtTestAreaWidth - margin) x -= margin;
                        if (y < margin) y += margin;
                        else if (y > pvtTestAreaHeight - margin) y -= margin;
                        mRedDot.setX(x);
                        mRedDot.setY(y);

                        mRedDot.setVisibility(View.VISIBLE);
                        showRedDotTimes.add(System.nanoTime() - pvtStartTime);
                        touchRedDotTimes.add(0L);

                        pvtTaskCount++;

                        mHandler.postDelayed(this, pvtReactTime);
                    }
                }

                private boolean checkReschedule() {
                    boolean reschedule = true;
                    if ((System.nanoTime() - pvtStartTime) >= pvtDuration) {
                        reschedule = false;
                    }
                    return reschedule;
                }
            };
        }

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_PvtTestToPvt);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                arguments.putSerializable("BPType", Config.BPType.BeforeStressReduce);
                Navigation.findNavController(v).navigate(R.id.action_PvtTestToBP4, arguments);
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_pvtTest));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Resume UI and Data
        ActionBar actionBar = activity.getSupportActionBar();
        Window window = activity.getWindow();

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_pvtTest));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimaryAlternate)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarkAlternate));


        boolean pvtDone = ((StudyActivity) activity).getPvtDone();
        if (!pvtDone) {
            backButton.setEnabled(false);
            nextButton.setEnabled(false);

            pvtTestTextArea.setVisibility(View.GONE);

            if (!mPvtTaskInit) {
                showRedDotTimes.clear();
                touchRedDotTimes.clear();
                pvtTaskCount = 0;

                mPvtTaskInit = true;

                pvtStartTime = System.nanoTime();
                mHandler.postDelayed(myPvtTask, 1000);

            } else {

                backButton.setEnabled(true);

                String pvtMessage = "PVT interrupted!\n Re-enter this page to restart";
                pvtText.setText(pvtMessage);

                pvtTestTextArea.setVisibility(View.VISIBLE);
            }

        } else {
            backButton.setEnabled(true);
            nextButton.setEnabled(true);

            String pvtMessage = "Task Completed!";
            pvtText.setText(pvtMessage);

            pvtTestTextArea.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private ArrayList<String> formatRedDotTimes(String keyword){
        ArrayList<String> redDotTimesData = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS", Locale.US);
        Calendar calendar = Calendar.getInstance();

        if (keyword.equals("show")) {
            for (int i = 0; i < showRedDotTimes.size(); i++) {
                long redDotTimeNano = showRedDotTimes.get(i);
                long redDotTimeMilli = TimeUnit.MILLISECONDS.convert(redDotTimeNano, TimeUnit.NANOSECONDS);

                calendar.setTimeInMillis(redDotTimeMilli);
                String redDotTime = formatter.format(calendar.getTime());
                redDotTimesData.add(redDotTime);
            }
        }

        if (keyword.equals("touch")) {
            for (int i = 0; i < touchRedDotTimes.size(); i++) {
                long redDotTimeNano = touchRedDotTimes.get(i);
                long redDotTimeMilli = TimeUnit.MILLISECONDS.convert(redDotTimeNano, TimeUnit.NANOSECONDS);

                calendar.setTimeInMillis(redDotTimeMilli);
                String redDotTime = formatter.format(calendar.getTime());
                redDotTimesData.add(redDotTime);
            }
        }

        return redDotTimesData;
    }
}
