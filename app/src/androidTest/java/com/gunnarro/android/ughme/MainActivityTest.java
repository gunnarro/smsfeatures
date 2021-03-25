package com.gunnarro.android.ughme;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.gunnarro.android.ughme.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {


    /**
     * Use {@link ActivityScenarioRule} to create and launch the activity under test, and close it
     * after test completes.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    /*
    @Before
    public void init(){
        rule.getScenario().
                .getSupportFragmentManager().beginTransaction();
    }
*/
    @Test
    public void isBackupFragemntDisplayed() {
        onView(withId(R.id.nav_sms_backup)).perform(click());
    }

}
