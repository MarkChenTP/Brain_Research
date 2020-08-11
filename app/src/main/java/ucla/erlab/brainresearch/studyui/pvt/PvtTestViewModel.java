/**
 *  PvtTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.pvt;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PvtTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PvtTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is PVT Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}