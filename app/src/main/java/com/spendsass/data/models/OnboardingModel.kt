package com.spendsass.data.models

import android.content.SharedPreferences

/**
 * OnboardingModel
 *
 * Handles data logic for the onboarding feature.
 *
 * RESPONSIBILITY: This class knows NOTHING about the UI.
 * It only reads and writes the onboarding completion flag.
 */
class OnboardingModel(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

        // Total number of intro pages (0-indexed).
        // Page 0 = Welcome, Page 1 = Meet the Piggy
        const val TOTAL_PAGES = 2
    }

    //onboarding complete
    fun markOnboardingComplete() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
    }

    //skips onboarding
    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }
}