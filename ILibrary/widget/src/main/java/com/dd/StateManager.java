package com.dd;

class StateManager {

    private boolean mIsEnabled;
    private int mProgress;

    StateManager(CircularProgressButton progressButton) {
        mIsEnabled = progressButton.isEnabled();
        mProgress = progressButton.getProgress();
    }

    StateManager(CircularProgressFButton progressFButton) {
        mIsEnabled = progressFButton.isEnabled();
        mProgress = progressFButton.getProgress();
    }

    void saveProgress(CircularProgressButton progressButton) {
        mProgress = progressButton.getProgress();
    }

    void saveProgress(CircularProgressFButton progressFButton) {
        mProgress = progressFButton.getProgress();
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public int getProgress() {
        return mProgress;
    }

    void checkState(CircularProgressButton progressButton) {
        if (progressButton.getProgress() != getProgress()) {
            progressButton.setProgress(progressButton.getProgress());
        } else if (progressButton.isEnabled() != isEnabled()) {
            progressButton.setEnabled(progressButton.isEnabled());
        }
    }

    void checkState(CircularProgressFButton progressFButton) {
        if (progressFButton.getProgress() != getProgress()) {
            progressFButton.setProgress(progressFButton.getProgress());
        } else if (progressFButton.isEnabled() != isEnabled()) {
            progressFButton.setEnabled(progressFButton.isEnabled());
        }
    }
}
