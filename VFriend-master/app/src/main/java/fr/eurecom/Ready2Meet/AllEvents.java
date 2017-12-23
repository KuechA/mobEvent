package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.Ready2Meet.database.Event;

public class AllEvents extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    final List<Event> eventlist = new ArrayList<>();

    public AllEvents() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     *
     * @return A new instance of fragment AllEvents.
     */
    // TODO: Rename and change types and number of parameters
    public static AllEvents newInstance(String param1, String param2) {
        AllEvents fragment = new AllEvents();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_events, container, false);

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
                ListViewAdapter_Event adapter = new ListViewAdapter_Event(getContext(), eventlist);

                listView.setAdapter(adapter);
                listView.setLayoutManager(new LinearLayoutManager(AllEvents.this.getContext()));
                adapter.notifyItemMoved(0, adapter.getItemCount() - 1);
                adapter.notifyDataSetChanged();
                adapter.getFilter().filter("Outdoor");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }
}
