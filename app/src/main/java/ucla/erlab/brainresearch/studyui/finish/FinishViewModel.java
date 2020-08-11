/**
 *  FinishViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */
package ucla.erlab.brainresearch.studyui.finish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FinishViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FinishViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Finish fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}