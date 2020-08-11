/**
 *  DeviceListViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.connpulseox;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceListViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DeviceListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Device List fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}