package fr.eurecom.Ready2Meet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import net.igenius.customcheckbox.CustomCheckBox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.eurecom.Ready2Meet.database.Event;
import fr.eurecom.Ready2Meet.uiExtensions.ImageArrayAdapter;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback {

    private Event event;
    private boolean participating;

    private GoogleMap googleMap;

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

    private void setupParticipatingCheckbox(final CustomCheckBox checkBox, final Button
            chatButton) {
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

        final boolean isFull = (event.current >= event.capacity);
        final Toast EventFull_Toast = Toast.makeText(getContext(), "This event is full :(", Toast
                .LENGTH_LONG);

        checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean participating = checkBox.isChecked();

                if(! participating) {
                    if(! isFull) {
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id
                                + "/Participants").child(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid()).setValue(true);
                        event.current++;
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id
                                + "/current").setValue(event.current);
                        chatButton.setVisibility(View.VISIBLE);
                        checkBox.setChecked(true);
                    } else {
                        EventFull_Toast.show();
                        checkBox.setChecked(false);
                        chatButton.setVisibility(View.GONE);
                        checkBox.setChecked(false);
                    }

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                            "/Participants").child(FirebaseAuth.getInstance().getCurrentUser()
                            .getUid()).removeValue();
                    event.current--;
                    FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                            "/current").setValue(event.current);
                    chatButton.setVisibility(View.GONE);
                    checkBox.setChecked(false);
                }

            }
        });
    }

    private void showOwnerOptions(View view) {
        if(! event.owner.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            view.findViewById(R.id.owner_options).setVisibility(LinearLayout.GONE);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.event, container, false);

        ((TextView) view.findViewById(R.id.txteventname)).setText(event.title);
        ((TextView) view.findViewById(R.id.txtcategories)).setText(getCategories());
        ((TextView) view.findViewById(R.id.txteventdescription)).setText(event.description);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' hh:mm a");
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

        //add owner first!
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + event.owner);
        de.hdodenhof.circleimageview.CircleImageView ii = new de.hdodenhof.circleimageview
                .CircleImageView(participantImages.getContext());

        ii.setBorderWidth(5);
        ii.setBorderColor(Color.RED);
        ii.setPadding(0, 0, 4, 0);
        LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
        test.gravity = Gravity.CENTER;
        ii.setLayoutParams(test);
        Glide.with(participantImages.getContext()).using(new FirebaseImageLoader()).load
                (storageRef).fitCenter().into(ii);
        participantImages.addView(ii);
        participants++;

        for(String key : event.Participants.keySet()) {
            if(! event.Participants.get(key)) continue;

            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);
            ii = new de.hdodenhof.circleimageview.CircleImageView(participantImages.getContext());
            ii.setBorderWidth(5);
            ii.setBorderColor(Color.TRANSPARENT);
            if(key.equals(event.owner)) {continue;}

            ii.setPadding(0, 0, 4, 0);
            test = new LinearLayout.LayoutParams(100, 100);
            test.gravity = Gravity.CENTER;
            ii.setLayoutParams(test);
            Glide.with(participantImages.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).fitCenter().into(ii);
            participantImages.addView(ii);
            participants++;
        }

        ((TextView) view.findViewById(R.id.txtcurrent)).setText(String.valueOf(participants));
        ((TextView) view.findViewById(R.id.txtcapacity)).setText(event.capacity.toString());

        ((RoundCornerProgressBar) view.findViewById(R.id.eventprogress)).setProgress(Float
                .parseFloat(String.valueOf(participants)));
        ((RoundCornerProgressBar) view.findViewById(R.id.eventprogress)).setMax(Float.parseFloat
                (String.valueOf(event.capacity.toString())));

        ((TextView) view.findViewById(R.id.txtlocation)).setText(event.place);

        // TODO: Make picture gallery (if registered to event)

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id
                .map);
        mapFragment.getMapAsync(this);

        ImageView imageView = (ImageView) view.findViewById(R.id.eventpicture);
        Picasso.with(getContext()).load(event.picture).fit().centerCrop().into(imageView);

        showOwnerOptions(view);
        cancelEvent(view);
        removeParticipants(view);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        //map.setMyLocationEnabled(true);
        if(event.latitude != null && event.longitude != null) {
            LatLng location = new LatLng(event.latitude, event.longitude);
            googleMap.addMarker(new MarkerOptions().position(location));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom
                    (10).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android
                    .Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                    .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity()
                    .getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Try to get permissions
                return;
            }
            googleMap.setMyLocationEnabled(true);

        }
    }

    private void removeParticipants(View view) {
        Spinner spinner = (Spinner) view.findViewById(R.id.participants_spinner);
        List<String> participants = new ArrayList();
        for(Map.Entry<String, Boolean> p : event.Participants.entrySet()) {
            if(! p.getKey().equals(event.owner) && p.getValue()) {
                participants.add(p.getKey());
            }

        }
        SpinnerAdapter adapter = new ImageArrayAdapter(getContext(), R.layout
                .participant_spinner_row, participants);
        spinner.setAdapter(adapter);
    }

    private void cancelEvent(View view) {
        view.findViewById(R.id.cancel_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LinearLayout layout = new LinearLayout(getContext());
                TextView alertText = new TextView(getContext());

                alertText.setText("Are you sure that you want to cancel this event?\n\nThis " +
                        "action " + "cannot be reversed.");
                alertText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(alertText);
                layout.setPadding(50, 40, 50, 10);

                builder.setView(layout);

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton("Yes, cancel event", new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove event from all participants list of events
                        for(Map.Entry<String, Boolean> participant : event.Participants.entrySet
                                ()) {
                            FirebaseDatabase.getInstance().getReference().child("Users/" +
                                    participant.getKey() + "/ParticipatingEvents/" + event.id)
                                    .removeValue();
                        }

                        // Remove event from DB
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id)
                                .removeValue();
                    }
                });

                builder.create().show();
            }
        });
    }
}
