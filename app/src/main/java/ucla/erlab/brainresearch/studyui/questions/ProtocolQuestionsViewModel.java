/**
 *  ProtocolQuestionsViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.questions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProtocolQuestionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProtocolQuestionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Protocol Questions fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}