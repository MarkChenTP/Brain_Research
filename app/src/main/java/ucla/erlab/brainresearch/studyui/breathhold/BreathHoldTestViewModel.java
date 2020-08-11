/**
 *  BreathHoldTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.breathhold;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BreathHoldTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BreathHoldTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Breath Hold Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}