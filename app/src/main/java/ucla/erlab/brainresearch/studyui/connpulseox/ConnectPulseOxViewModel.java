/**
 *  ConnectPulseOxViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.connpulseox;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectPulseOxViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ConnectPulseOxViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Connect PulseOx fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}