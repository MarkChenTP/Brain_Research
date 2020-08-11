/**
 *  ValsalvaTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.valsalva;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ValsalvaTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ValsalvaTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Valsalva Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}