package com.dataxy;

import android.support.test.espresso.IdlingResource;

final class DataXYSenderIdlingResource implements IdlingResource {
    private ResourceCallback mResourceCallback;

    private int mCounter;
    private int mMaxCounter;

    DataXYSenderIdlingResource(int maxCounter) {
        mMaxCounter = maxCounter;
    }

    void increaseCounter() {
        mCounter++;
    }

    @Override
    public String getName() {
        return "DataXYSenderIdlingResource[" + mCounter + "/" + mMaxCounter + "]";
    }

    @Override
    public boolean isIdleNow() {
        if (mCounter != mMaxCounter) {
            return false;
        }

        mResourceCallback.onTransitionToIdle();
        return true;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.mResourceCallback = callback;
    }
}
