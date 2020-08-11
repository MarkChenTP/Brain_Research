/**
 *  ValsalvaViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.valsalva;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ValsalvaViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ValsalvaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Valsalva Introduction fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}