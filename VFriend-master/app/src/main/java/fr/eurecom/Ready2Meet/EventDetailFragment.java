package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.eurecom.Ready2Meet.database.Event;

public class EventDetailFragment extends Fragment {

    private Event event;

    public EventDetailFragment() {
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.event, container, false);

        ((TextView) view.findViewById(R.id.txteventname)).setText(event.title);

        // TODO: Fill the other fields + Make picture gallery (if registered to event)

        // TODO: Disable chat button (set visibility to GONE) if not participating in event

        return view;
    }
}
