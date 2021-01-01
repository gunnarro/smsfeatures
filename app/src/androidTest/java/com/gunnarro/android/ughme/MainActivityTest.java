package com.gunnarro.android.ughme;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    /*
    @Rule
    public ActivityTestRule<MainActivity> rule  = new  ActivityTestRule<>(MainActivity.class);

    @Test
    public void ensureListViewIsPresent() throws Exception {
        MainActivity activity = rule.getActivity();
        View viewById = activity.findViewById(R.id.listview);
        assertThat(viewById,notNullValue());
        assertThat(viewById, instanceOf(ListView.class));
        ListView listView = (ListView) viewById;
        ListAdapter adapter = listView.getAdapter();
        assertThat(adapter, instanceOf(ArrayAdapter.class));
        assertThat(adapter.getCount(), greaterThan(5));

    }

     */
}
