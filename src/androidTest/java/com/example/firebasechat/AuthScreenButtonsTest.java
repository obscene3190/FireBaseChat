package com.example.firebasechat;


import android.os.IBinder;
import android.support.test.espresso.Root;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.allOf;



@LargeTest
@RunWith(AndroidJUnit4.class)
public class AuthScreenButtonsTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void authScreenButtonsTest() {
        ViewInteraction button = onView(
                allOf(withId(R.id.btn_sign_in),
                        childAtPosition(
                                allOf(withId(R.id.buttons),
                                        childAtPosition(
                                                withId(R.id.email_password),
                                                2)),
                                0),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btn_registration),
                        childAtPosition(
                                allOf(withId(R.id.buttons),
                                        childAtPosition(
                                                withId(R.id.email_password),
                                                2)),
                                1),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_sign_in), withText("Авторизация"),
                        childAtPosition(
                                allOf(withId(R.id.buttons),
                                        childAtPosition(
                                                withId(R.id.email_password),
                                                2)),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        onView(withText("Данные не введены")).inRoot(new ToastMatcher())
                .check(matches(withText("Данные не введены")));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btn_registration), withText("Регистрация"),
                        childAtPosition(
                                allOf(withId(R.id.buttons),
                                        childAtPosition(
                                                withId(R.id.email_password),
                                                2)),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText("Данные не введены")).inRoot(new ToastMatcher())
                .check(matches(withText("Данные не введены")));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView().getApplicationWindowToken();
                if (windowToken == appToken) {
                    //means this window isn't contained by any other windows.
                    return true;
                }
            }
            return false;
        }
    }
}
