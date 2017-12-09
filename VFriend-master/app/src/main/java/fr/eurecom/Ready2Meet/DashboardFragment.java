package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.Ready2Meet.database.Event;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    final List<Event> eventlist = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // TODO: Retrieve list of events from firebase and add tem to the listView.
/*
        final ListView listView = (ListView) view.findViewById(R.id.start_page_list_of_events);
        FirebaseDatabase.getInstance().getReference().child("Events")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        eventlist.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event eventread = snapshot.getValue(Event.class);
                            eventlist.add(eventread);
                        }
                        ListViewAdapter_Event adapter = new ListViewAdapter_Event(getContext(), R
                        .layout.row_events,eventlist);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                */
        return view;
    }

}
