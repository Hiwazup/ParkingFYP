package com.martin.parkingfyp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CheckAvailabilityTests {
    private String title = "Availability";
    private String className = "CheckAvailability";
    private String TAG = "Test";

    @Rule
    public ActivityTestRule<CheckAvailability> mActivityRule = new ActivityTestRule<>(
            CheckAvailability.class);

    /*@Before
    public void initiate(){
        title = "Availability";
    }*/

    @Test
    public void checkToolbarText(){
        onView(withId(R.id.toolbar_title)).check(matches(withText(title)));
    }

    @Test
    public void checkClass(){
        //onView(withText(R.id.)).check(matches(withText(className)));
    }

    @Test
    public void clickSettings(){
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getContext());
        onView(withId(R.id.alpha_sort)).perform(click());
        //onView(withId(R.id.))
    }

    @Test
    public void checkSizeOfRecyclerView(){
    }

    public void logResult(String expected, String result){
        Log.i(TAG, "Expected: " + expected + ", Result: " + result);
    }
}
