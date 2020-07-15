package com.gunnarro.android.ughme.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

/**
 * The purpose of ViewModel is to encapsulate the data for a UI controller to let the data survive configuration changes.
 * <p>
 * Caution: A ViewModel must never reference a view, Lifecycle, or any class that may hold a reference to the activity context.
 */
class PageViewModel extends ViewModel {
    private final MutableLiveData<String> mTitle = new MutableLiveData<>();

    private final LiveData<String> mText = Transformations.map(mTitle, input -> "Contact not available in " + input);

    public void setIndex(String index) {
        mTitle.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}
