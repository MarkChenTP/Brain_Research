/**
 *  StroopTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.stroop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StroopTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StroopTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Stroop Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}