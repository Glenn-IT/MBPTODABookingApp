package com.example.mbptodabookingapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mbptodabookingapp.ui.auth.LoginActivity
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 12.2.1 — Login screen input validation.
 * Actual credential login is skipped here — it requires a live server.
 * Integration testing (enter real creds → navigate to home) is covered
 * in the end-to-end test matrix (6.10).
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun emptyEmail_showsEmailError() {
        onView(withId(R.id.etEmail)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("anypass"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        // TextInputLayout sets error — verify via the child EditText's error
        activityRule.scenario.onActivity { activity ->
            val error = activity.findViewById<com.google.android.material.textfield.TextInputLayout>(
                R.id.tilEmail
            ).error
            assert(!error.isNullOrEmpty()) { "Expected tilEmail to show an error for empty input" }
        }
    }

    @Test
    fun invalidEmailFormat_showsEmailError() {
        onView(withId(R.id.etEmail)).perform(typeText("not-an-email"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("anypass"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        activityRule.scenario.onActivity { activity ->
            val error = activity.findViewById<com.google.android.material.textfield.TextInputLayout>(
                R.id.tilEmail
            ).error
            assert(!error.isNullOrEmpty()) { "Expected tilEmail to show an error for invalid email" }
        }
    }

    @Test
    fun validEmail_emptyPassword_showsPasswordError() {
        onView(withId(R.id.etEmail)).perform(typeText("juan@test.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        activityRule.scenario.onActivity { activity ->
            val error = activity.findViewById<com.google.android.material.textfield.TextInputLayout>(
                R.id.tilPassword
            ).error
            assert(!error.isNullOrEmpty()) { "Expected tilPassword to show an error for empty password" }
        }
    }

    @Test
    fun validEmail_noEmailError() {
        onView(withId(R.id.etEmail)).perform(typeText("juan@test.com"), closeSoftKeyboard())

        activityRule.scenario.onActivity { activity ->
            val error = activity.findViewById<com.google.android.material.textfield.TextInputLayout>(
                R.id.tilEmail
            ).error
            // Before tapping login, no error should be set yet
            assert(error.isNullOrEmpty()) { "tilEmail should not have an error before submission" }
        }
    }
}
