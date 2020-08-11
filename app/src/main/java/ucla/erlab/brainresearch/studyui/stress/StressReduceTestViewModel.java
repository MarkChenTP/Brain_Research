/**
 *  StressReduceTestViewModel.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch.studyui.stress;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StressReduceTestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StressReduceTestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Stress Reduce Test fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}