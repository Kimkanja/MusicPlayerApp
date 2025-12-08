package com.kimani.musicplayerapp;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class PlayerActivityTest {

    // Launch PlayerActivity before running tests
    @Rule
    public ActivityScenarioRule<PlayerActivity> activityRule =
            new ActivityScenarioRule<>(PlayerActivity.class);

    @Test
    public void testPlayerButtons() {
        // Check that all buttons are displayed
        onView(withId(R.id.playpauseBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.nextBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.previousBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.shuffleBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.repeatBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));

        // Simulate button clicks
        onView(withId(R.id.playpauseBtn)).perform(click());   // Play / Pause
        onView(withId(R.id.nextBtn)).perform(click());        // Next track
        onView(withId(R.id.previousBtn)).perform(click());    // Previous track
        onView(withId(R.id.shuffleBtn)).perform(click());     // Toggle shuffle
        onView(withId(R.id.repeatBtn)).perform(click());      // Toggle repeat
        onView(withId(R.id.backBtn)).perform(click());        // Back button
    }
}
