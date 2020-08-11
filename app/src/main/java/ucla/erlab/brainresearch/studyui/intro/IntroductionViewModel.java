/**
 *  IntroductionViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.intro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IntroductionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public IntroductionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Introduction fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}