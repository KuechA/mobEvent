package fr.eurecom.Ready2Meet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private List<Event> eventlist = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        Button allEventsButton = (Button) view.findViewById(R.id.goto_all_events);
        allEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new AllEvents();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_new, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button addEventButton = (Button) view.findViewById(R.id.goto_add_event);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddEventActivity.class));
            }
        });

        // TODO: Retrieve list of events from firebase and add tem to the listView.

        final RecyclerView listView = (RecyclerView) view.findViewById(R.id
                .start_page_list_of_events);
        FirebaseDatabase.getInstance().getReference().child("Events")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventlist.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event eventread = snapshot.getValue(Event.class);
                    eventlist.add(eventread);
                }
                ListViewAdapter_Event adapter = new ListViewAdapter_Event(getContext(), eventlist);
                listView.setAdapter(adapter);
                listView.setLayoutManager(new LinearLayoutManager(DashboardFragment.this
                        .getContext()));
                adapter.notifyItemMoved(0, adapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }

}
