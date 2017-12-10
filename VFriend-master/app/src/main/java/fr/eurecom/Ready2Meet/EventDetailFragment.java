package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.igenius.customcheckbox.CustomCheckBox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.eurecom.Ready2Meet.database.Event;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback {

    private Event event;
    private boolean participating;

    private MapView mapView;

    public EventDetailFragment() {
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private String getCategories() {
        if(event.categories == null) return null;

        String categories = "";
        for(Map.Entry<String, Boolean> c : event.categories.entrySet()) {
            if(c.getValue()) {
                if(categories == "") {
                    categories += c.getKey();
                } else {
                    categories += ", " + c.getKey();
                }
            }
        }
        return categories;
    }

    private void setupParticipatingCheckbox(CustomCheckBox checkBox, final Button chatButton) {
        participating = false;
        for(String key : event.Participants.keySet()) {
            if(key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                participating = true;
                break;
            }
        }
        checkBox.setChecked(participating);

        if(participating) {
            chatButton.setVisibility(View.VISIBLE);
        }

        checkBox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                Map<String, Object> updateRequest = new HashMap(1);
                updateRequest.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), isChecked);
                FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                        "/Participants").updateChildren(updateRequest);
                participating = isChecked;
                if(participating) {
                    chatButton.setVisibility(View.VISIBLE);
                } else {
                    chatButton.setVisibility(View.GONE);
                }
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.event, container, false);

        ((TextView) view.findViewById(R.id.txteventname)).setText(event.title);
        ((TextView) view.findViewById(R.id.txtcategories)).setText(getCategories());
        ((TextView) view.findViewById(R.id.txteventdescription)).setText(event.description);

        SimpleDateFormat format = new SimpleDateFormat("EE, MMM dd, yyyy 'at' hh:mm a");
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat formatDate = new SimpleDateFormat("MMM dd, yyyy");
        try {
            Date start = format.parse(event.startTime);
            ((TextView) view.findViewById(R.id.txtstarttime)).setText(formatTime.format(start));
            ((TextView) view.findViewById(R.id.start_date)).setText(formatDate.format(start));
        } catch(ParseException e) {
            e.printStackTrace();
        }
        try {
            Date end = format.parse(event.endTime);
            ((TextView) view.findViewById(R.id.txtendtime)).setText(formatTime.format(end));
            ((TextView) view.findViewById(R.id.end_date)).setText(formatDate.format(end));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        setupParticipatingCheckbox((CustomCheckBox) view.findViewById(R.id.participatingcheckbox)
                , (Button) view.findViewById(R.id.chat_button));

        LinearLayout participantImages = (LinearLayout) view.findViewById(R.id.participants);
        int participants = 0;
        for(String key : event.Participants.keySet()) {
            if(! event.Participants.get(key)) continue;

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);
            ImageView ii = new ImageView(participantImages.getContext());
            ii.setPadding(0, 0, 4, 0);
            ii.setLayoutParams(new LinearLayout.LayoutParams(participantImages.getHeight(),
                    participantImages.getHeight()));
            Glide.with(participantImages.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).fitCenter().into(ii);
            participantImages.addView(ii);
            participants++;
        }

        ((TextView) view.findViewById(R.id.txtcurrent)).setText(String.valueOf(participants));
        ((TextView) view.findViewById(R.id.txtcapacity)).setText(event.capacity.toString());

        ((TextView) view.findViewById(R.id.txtlocation)).setText(event.place);

        // TODO: Make picture gallery (if registered to event), show map, fix pictures

        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        //map.setMyLocationEnabled(true);
        if(event.latitude != null && event.longitude != null) {
            LatLng location = new LatLng(event.latitude, event.longitude);
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }
}
