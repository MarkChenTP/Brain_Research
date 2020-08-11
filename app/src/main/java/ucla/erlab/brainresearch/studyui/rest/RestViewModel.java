/**
 *  RestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.rest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Rest fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}