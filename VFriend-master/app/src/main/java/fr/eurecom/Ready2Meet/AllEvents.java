package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.eurecom.Ready2Meet.database.Event;
import fr.eurecom.Ready2Meet.uiExtensions.MultiSelectSpinner;

/**
 * Show all the events in a {@link RecyclerView}. Allows filtering depending on the different
 * categories which are possible for the events.
 */
public class AllEvents extends Fragment {

    private List<Event> eventlist = new ArrayList<>();
    private ListViewAdapter_Event adapter;
    public static final String FILTER_KEY = "CATEGORY_FILTER";

    /**
     * Set up the spinner for filtering events according to the categories and add the listener
     * to perform filtering of one or multiple event categories.
     *
     * @param view - The view of the page
     */
    private void setCategoriesFilter(View view) {
        final MultiSelectSpinner categorySpinner = (MultiSelectSpinner) view.findViewById(R.id
                .category_selector);

        ImageButton filterButton = (ImageButton) view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categorySpinner.performClick();
            }
        });

        ImageButton clearButton = (ImageButton) view.findViewById(R.id.clear_categories_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categorySpinner.removeAllSelections();
            }
        });

        categorySpinner.setItems(AddEventActivity.eventCategories);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        Set<String> filterStrings = sharedPref.getStringSet(AllEvents.FILTER_KEY, null);
        categorySpinner.setListener(new MultiSelectSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedStrings(List<String> strings) {
                // Write filter strings to shared preferences
                SharedPreferences.Editor editor = getActivity().getPreferences(Context
                        .MODE_PRIVATE).edit();
                editor.putStringSet(FILTER_KEY, new HashSet<>(strings));
                editor.commit();

                ListViewAdapter_Event.EventFilter filter = (ListViewAdapter_Event.EventFilter)
                        adapter.getFilter();
                if(strings.size() == 0) {
                    filter.showWithoutFiltering();
                    return;
                }

                filter.removeOldFilter();

                for(String s : strings) {
                    if(adapter != null) {
                        adapter.getFilter().filter(s);
                    } else {
                        Log.d("AllEvents", "No filtering as adapter is null");
                    }
                }
                filter.uniqueResults();
            }
        });
        categorySpinner.setSelection(filterStrings);
    }

    public AllEvents() {
        // Required empty public constructor
    }

    /**
     * Retrieve all events from the Firebase database and show them in the RecyclerView.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_all_events, container, false);

        final RecyclerView listView = (RecyclerView) view.findViewById(R.id.listofevents);
        DatabaseReference events = FirebaseDatabase.getInstance().getReference().child("Events");
        events.keepSynced(true);
        events.orderByChild("startTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventlist.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals("-L0AEWfuhQx3DjXz7H6Q")) {
                        continue;
                    }
                    Event eventread = snapshot.getValue(Event.class);
                    eventread.id = snapshot.getKey();
                    eventlist.add(eventread);
                }
                adapter = new ListViewAdapter_Event(getContext(), eventlist);

                listView.setAdapter(adapter);
                listView.setLayoutManager(new LinearLayoutManager(AllEvents.this.getContext()));
                adapter.notifyItemMoved(0, adapter.getItemCount() - 1);
                adapter.notifyDataSetChanged();

                setCategoriesFilter(view);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }
}
