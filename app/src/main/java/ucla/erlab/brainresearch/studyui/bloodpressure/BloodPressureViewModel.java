/**
 *  BloodPressureViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.bloodpressure;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BloodPressureViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BloodPressureViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Blood Pressure fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}