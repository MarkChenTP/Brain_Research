/**
 *  ConnectPulseOxFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.connpulseox;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Calendar;
import java.util.Objects;

import ucla.erlab.brainresearch.Config;
import ucla.erlab.brainresearch.R;
import ucla.erlab.brainresearch.StudyActivity;


public class ConnectPulseOxFragment extends Fragment {

    //private ConnectPulseOxViewModel connectPulseOxViewModel;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 102;   // Request code for Bluetooth Secure Connection
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 103; // Request code for Bluetooth Insecure Connection

    private ProgressBar mProgressBar;
    private ImageView mConnectionError;
    private TextView pulseOxStatus;

    private Button backButton;
    private Button nextButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_connpulseox, container, false);
        /*
        connectPulseOxViewModel = new ViewModelProvider(this).get(ConnectPulseOxViewModel.class);

        final TextView textView = root.findViewById(R.id.text_ConnPulseOx);
        connectPulseOxViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        // Initialize UI and Data
        mProgressBar = root.findViewById(R.id.progressBar_pulseOxData);
        mConnectionError = root.findViewById(R.id.imageView_pulseOxData);
        pulseOxStatus = root.findViewById(R.id.textView_pulseOxData);

        backButton = root.findViewById(R.id.button_Back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_ConnPulseOxToIntro);
            }
        });

        nextButton = root.findViewById(R.id.button_Next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle arguments = new Bundle();
                int dayCount = Integer.parseInt(((StudyActivity) activity).getUserSetting("DayCount"));
                String protocol = ((StudyActivity) activity).getUserSetting("Protocol");
                String menstruating = ((StudyActivity) activity).getUserSetting("Menstruating");
                String cpap = ((StudyActivity) activity).getUserSetting("CPAP");
                Calendar today = Calendar.getInstance();
                boolean isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

                if (menstruating.equals("Yes") || cpap.equals("Yes")) {
                    // can menstruating or use CPAP
                    Navigation.findNavController(v).navigate(R.id.action_ConnPulseOxToUserQuery);
                } else if ((dayCount >= 15 && protocol.equals("SR")) ||
                        (isFriday && protocol.equals("IMT"))) {
                    // (SR only) start from day 15 or (IMT only) on Friday
                    Navigation.findNavController(v).navigate(R.id.action_ConnPulseOxToPlanQuery);
                } else if (dayCount % 5 == 2) {
                    // every 5 days with offset 1 (day 2, 7, 12...)
                    Navigation.findNavController(v).navigate(R.id.action_ConnPulseOxToDayQuery);
                } else {
                    arguments.putSerializable("BPType", Config.BPType.AfterQuestions);
                    Navigation.findNavController(v).navigate(R.id.action_ConnPulseOxToBP1, arguments);
                }
            }
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.setFragmentResultListener("DeviceList", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String key, @NonNull Bundle bundle) {

                int socketType = bundle.getInt("SocketType");
                String deviceAddress = bundle.getString("DeviceAddress");
                switch (socketType) {
                    case REQUEST_CONNECT_DEVICE_SECURE:
                        ((StudyActivity) activity).setPulseOxSelected(true);
                        ((StudyActivity) activity).connectBluetoothDevice(deviceAddress, true);
                        break;
                    case REQUEST_CONNECT_DEVICE_INSECURE:
                        ((StudyActivity) activity).setPulseOxSelected(true);
                        ((StudyActivity) activity).connectBluetoothDevice(deviceAddress, false);
                        break;
                    default:
                        break;
                }
            }
        });

        ((StudyActivity) activity).setStudySavePoint(getResources().getString(R.string.title_connPulseOx));

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

        Objects.requireNonNull(actionBar).setTitle(getResources().getString(R.string.title_connPulseOx));
        actionBar.setBackgroundDrawable(new ColorDrawable(
                ContextCompat.getColor(activity, R.color.colorPrimary)));
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));


        boolean pulseOxSelected = ((StudyActivity) activity).getPulseOxSelected();
        Fragment dialogFragment = getChildFragmentManager().findFragmentByTag("ConnPulseOxToDeviceLost");

        if (!pulseOxSelected) {
            ((StudyActivity) activity).initBluetoothService();
            if (dialogFragment == null) showPairedPulseOxDevices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public void setConnPulseOxUI(String pulseOxImage, String pulseOxText) {
        switch(pulseOxImage) {
            case "ProgressBar":
                mProgressBar.setVisibility(View.VISIBLE);
                mConnectionError.setVisibility(View.GONE);
                break;
            case "ImageView":
                mProgressBar.setVisibility(View.GONE);
                mConnectionError.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        if (pulseOxText.contains("Pulse")) {
            nextButton.setEnabled(true);
        } else {
            nextButton.setEnabled(false);
        }

        pulseOxStatus.setText(pulseOxText);
    }

    private void showPairedPulseOxDevices() {
        FragmentManager fragmentManager = getChildFragmentManager();

        DialogFragment dialogFragment = new DeviceListFragment();
        dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        Bundle arguments = new Bundle();
        // Select socket type based on the design of Nonin Pulse Oximeter
        arguments.putInt("SocketType", REQUEST_CONNECT_DEVICE_SECURE);
        //arguments.putInt("SocketType", REQUEST_CONNECT_DEVICE_INSECURE);
        dialogFragment.setArguments(arguments);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        dialogFragment.show(transaction, "ConnPulseOxToDeviceLost");
    }

}
