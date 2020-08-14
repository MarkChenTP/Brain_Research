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
 *  DeviceListFragment.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.connpulseox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.Set;

import ucla.erlab.brainresearch.R;


public class DeviceListFragment extends DialogFragment {

    //private DeviceListViewModel deviceListViewModel;

    private ListView pairedDeviceList;
    private ArrayAdapter<String> pairedDeviceAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_devicelist, container, false);
        /*
        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);

        final TextView textView = root.findViewById(R.id.text_DeviceList);
        deviceListViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        Context context = requireContext();

        // Initialize UI and Data
        pairedDeviceList = root.findViewById(R.id.listView_PairedDevice);
        pairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  {
                String deviceInfo = ((TextView) view).getText().toString();
                String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);

                Bundle arguments = getArguments();
                assert arguments != null;
                int socketType = arguments.getInt("SocketType");

                Bundle results = new Bundle();
                results.putInt("SocketType", socketType);
                results.putString("DeviceAddress", deviceAddress);

                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.setFragmentResult("DeviceList", results);
                dismiss();
            }
        });

        pairedDeviceAdapter = new ArrayAdapter<>(context, R.layout.device_name);
        pairedDeviceList.setAdapter(pairedDeviceAdapter);

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume UI and Data
        pairedDeviceAdapter.clear();
        pairedDeviceAdapter.notifyDataSetChanged();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        int pairedNoninDevices = 0;

        // If there are paired Nonin devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().contains("Nonin_Medical_Inc")) {
                    pairedDeviceAdapter.add(device.getName() + "\n"
                            + device.getAddress());
                    pairedNoninDevices++;
                }
            }
        }

        if(pairedNoninDevices == 0) {
            String message = "No Nonin devices have been paired";
            pairedDeviceAdapter.add(message);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
