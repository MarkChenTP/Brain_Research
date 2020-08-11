/**
 *  SubjectQuestionsTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.questions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SubjectQuestionsTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SubjectQuestionsTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Subject Questions Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}