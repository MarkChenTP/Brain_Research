/**
 *  BreathHoldViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.breathhold;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BreathHoldViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BreathHoldViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Breath Hold Introduction fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}