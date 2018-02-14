package fr.eurecom.Ready2Meet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.igenius.customcheckbox.CustomCheckBox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.Event;
import fr.eurecom.Ready2Meet.uiExtensions.ImageArrayAdapter;
import fr.eurecom.Ready2Meet.weather.JSONParser;
import fr.eurecom.Ready2Meet.weather.WeatherData;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Show all event details. Either pass an event directly or by its ID. In case the ID is used to
 * identify the event, the event data are fetched from the Firebase database and thus should be
 * automatically updated when they change.
 */
public class EventDetailFragment extends Fragment implements OnMapReadyCallback, LoaderManager
        .LoaderCallbacks<List<WeatherData>> {

    private Event event;
    private Date start = null;
    private Date end = null;
    private View view;

    private static final int PICK_GALLERY = 1;

    public EventDetailFragment() {
    }

    /**
     * Retrieve the event to display using the ID of an event and fetching the data from the
     * Firebase depending on the eventId.
     * <p>
     * This allows to react changing values in the fragment.
     *
     * @param eventId - The ID of the event
     */
    public void setEvent(final String eventId) {
        Log.d("EventDetailFragment", "EventID: " + eventId);
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child
                ("Events/" + eventId);
        eventRef.keepSynced(true);
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                event.id = eventId;
                if(event != null && view != null) {
                    createEventDetails();
                } else if(event == null) {
                    Log.w("EventDetailFragment", "Event is null");
                } else {
                    Log.w("EventDetailFragment", "View is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Directly sets the event whose information should be displayed.
     * <p>
     * Deprecated as it does not refresh the event details if something changes in the database. Use
     * {@link #setEvent(String)} instead.
     *
     * @param event - The event which should be set
     */
    @Deprecated
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Build the string to represent the categories of an event.
     *
     * @return The string representation of the event categories.
     */
    private String getCategories() {
        if(event.categories == null) return null;

        StringBuilder categories = new StringBuilder("");
        for(Map.Entry<String, Boolean> c : event.categories.entrySet()) {
            if(c.getValue()) {
                if(categories.toString().equals("")) {
                    categories.append(c.getKey());
                } else {
                    categories.append(", ").append(c.getKey());
                }
            }
        }
        return categories.toString();
    }

    /**
     * Setup the listeners for the checkbox which is used to join and leave an event.
     *
     * @param checkBox - The checkbox which is used for joining/leaving
     * @param chatButton - The button for the chat which can be shown or hidden
     */
    private void setupParticipatingCheckbox(final CustomCheckBox checkBox, final Button
            chatButton) {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        boolean participating = event.Participants.containsKey(uid) && event.Participants.get(uid);
        checkBox.setChecked(participating);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("EventId", event.id);
                intent.putExtra("EventTitle", event.title);
                startActivity(intent);
            }
        });

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
                    // Join the event: Add participant to event and add the event to the user
                    if(! isFull) {
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id
                                + "/Participants").child(uid).setValue(true);
                        event.current++;
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id
                                + "/current").setValue(event.current);
                        FirebaseDatabase.getInstance().getReference().child("Users/" + uid +
                                "/ParticipatingEvents").child(event.id).setValue(true);
                        chatButton.setVisibility(View.VISIBLE);
                        checkBox.setChecked(true);
                    } else {
                        EventFull_Toast.show();
                        checkBox.setChecked(false);
                        chatButton.setVisibility(View.GONE);
                        checkBox.setChecked(false);
                    }
                } else {
                    // Leave the event: Remove participant to event and remove the event to the user
                    FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                            "/Participants").child(uid).removeValue();
                    event.current--;
                    FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                            "/current").setValue(event.current);
                    FirebaseDatabase.getInstance().getReference().child("Users/" + uid +
                            "/ParticipatingEvents/" + event.id).removeValue();
                    chatButton.setVisibility(View.GONE);
                    checkBox.setChecked(false);
                }
            }
        });
    }

    private void showImageGallery() {
        LinearLayout eventImages = (LinearLayout) view.findViewById(R.id.event_photos);

        eventImages.removeAllViews();

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(event
                .picture);
        ImageButton ii = new ImageButton(eventImages.getContext());

        ii.setPadding(0, 0, 4, 0);
        LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(120, 120);
        test.gravity = Gravity.CENTER;
        ii.setLayoutParams(test);
        ii.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        ii.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        Glide.with(eventImages.getContext()).using(new FirebaseImageLoader()).load(storageRef)
                .fitCenter().into(ii);
        eventImages.addView(ii);
        final StorageReference ref = storageRef;
        ii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(getContext()).using(new FirebaseImageLoader()).load(ref).fitCenter()
                        .centerCrop().into((ImageView) view.findViewById(R.id.eventpicture));
            }
        });

        if(event.images != null) {
            for(Map.Entry<String, String> entry : event.images.entrySet()) {
                storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(entry.getValue());
                ii = new ImageButton(eventImages.getContext());

                ii.setPadding(5, 0, 0, 0);
                ii.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                test = new LinearLayout.LayoutParams(120, 120);
                test.gravity = Gravity.CENTER;
                ii.setLayoutParams(test);
                Glide.with(eventImages.getContext()).using(new FirebaseImageLoader()).load
                        (storageRef).fitCenter().into(ii);

                eventImages.addView(ii);
                final StorageReference sRef = storageRef;
                ii.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Glide.with(getContext()).using(new FirebaseImageLoader()).load(sRef)
                                .fitCenter().centerCrop().into((ImageView) view.findViewById(R.id
                                .eventpicture));
                    }
                });
            }
        }

        // Add button to take pictures dynamically to the end of the picture gallery.
        ImageButton takePicture = new ImageButton(eventImages.getContext());
        takePicture.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        //takePicture.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
        // .colorPrimary));
        takePicture.setPadding(5, 0, 0, 0);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(120, 120);
        layout.gravity = Gravity.CENTER;
        takePicture.setLayoutParams(layout);
        takePicture.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_a_photo_black));
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_GALLERY);
            }
        });
        eventImages.addView(takePicture);

        view.findViewById(R.id.right_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.image_scroll_view).scrollBy((int) Math.floor(Resources
                        .getSystem().getDisplayMetrics().widthPixels * 0.8), 0);
            }
        });
        view.findViewById(R.id.left_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.image_scroll_view).scrollBy(- (int) Math.floor(Resources
                        .getSystem().getDisplayMetrics().widthPixels * 0.8), 0);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String lastPath = uri.getLastPathSegment();
            FirebaseStorage.getInstance().getReference().child("EventPhotos").child(event.id)
                    .child(lastPath).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                            "/images").push().setValue(downloadUri.toString());
                }
            });

        }
    }

    /**
     * Shows the options which are reserved for the owner of an event if the user of the app is
     * the owner of the event.
     */
    private void showOwnerOptions() {
        if(! event.owner.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            view.findViewById(R.id.owner_options).setVisibility(LinearLayout.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for(String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(getContext(), p) ==
                    PERMISSION_GRANTED;
        }

        if(! permissions) {
            requestPermissions(permissionsId, callbackId);
        } else {
            checkCalendarStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case 42:
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    checkCalendarStatus();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Checks if the calendar is free during the event. In case something can be found in the
     * calendar which overlaps with the event, the user is notified about it.
     */
    private void checkCalendarStatus() {
        String[] proj = new String[] {CalendarContract.Instances._ID, CalendarContract.Instances
                .BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.EVENT_ID};
        Cursor cursor = CalendarContract.Instances.query(getContext().getContentResolver(), proj,
                start.getTime(), end.getTime());
        if(cursor.getCount() > 0) {
            ((TextView) view.findViewById(R.id.txt_collision)).setText("Conflict in calendar");
        } else {
            ((TextView) view.findViewById(R.id.txt_collision)).setText("No conflicts in calendar");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.event, container, false);
        if(event != null) {
            createEventDetails();
        }
        return view;
    }

    /**
     * Show all the details of an event.
     */
    private void createEventDetails() {
        ((TextView) view.findViewById(R.id.txteventname)).setText(event.title);
        ((TextView) view.findViewById(R.id.txtcategories)).setText(getCategories());
        ((TextView) view.findViewById(R.id.txteventdescription)).setText(event.description);

        // Parse and display start and end date
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' hh:mm a", Locale.US);
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.US);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        try {
            start = format.parse(event.startTime);
            ((TextView) view.findViewById(R.id.txtstarttime)).setText(formatTime.format(start));
            ((TextView) view.findViewById(R.id.start_date)).setText(formatDate.format(start));
        } catch(ParseException e) {
            e.printStackTrace();
        }
        try {
            end = format.parse(event.endTime);
            ((TextView) view.findViewById(R.id.txtendtime)).setText(formatTime.format(end));
            ((TextView) view.findViewById(R.id.end_date)).setText(formatDate.format(end));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        if(start != null && end != null) {
            // Try to check calendar collisions
            final int callbackId = 42;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions(callbackId, READ_CALENDAR, WRITE_CALENDAR);
            } else {
                checkCalendarStatus();
            }
        }

        setupParticipatingCheckbox((CustomCheckBox) view.findViewById(R.id.participatingcheckbox)
                , (Button) view.findViewById(R.id.chat_button));

        // Show the images of all participants. The owner is shown with a red circle.
        LinearLayout participantImages = (LinearLayout) view.findViewById(R.id.participants);

        //add owner first!
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + event.owner);
        CircleImageView ii = new CircleImageView(participantImages.getContext());

        ii.setBorderWidth(5);
        ii.setBorderColor(Color.RED);
        ii.setPadding(0, 0, 4, 0);
        LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
        test.gravity = Gravity.CENTER;
        ii.setLayoutParams(test);
        Glide.with(participantImages.getContext()).using(new FirebaseImageLoader()).load
                (storageRef).fitCenter().into(ii);
        participantImages.addView(ii);

        // Iterate over all participants and add their image
        for(String key : event.Participants.keySet()) {
            if(! event.Participants.get(key)) continue;
            if(key.equals(event.owner)) {continue;}

            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);
            ii = new de.hdodenhof.circleimageview.CircleImageView(participantImages.getContext());
            ii.setBorderWidth(5);
            ii.setBorderColor(Color.TRANSPARENT);

            ii.setPadding(0, 0, 4, 0);
            test = new LinearLayout.LayoutParams(100, 100);
            test.gravity = Gravity.CENTER;
            ii.setLayoutParams(test);
            Glide.with(participantImages.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).fitCenter().into(ii);
            participantImages.addView(ii);
        }

        ((TextView) view.findViewById(R.id.txtcurrent)).setText(String.valueOf(event.current));
        ((TextView) view.findViewById(R.id.txtcapacity)).setText(String.valueOf(event.capacity));

        // Show progress bar
        ((RoundCornerProgressBar) view.findViewById(R.id.eventprogress)).setProgress(Float
                .parseFloat(String.valueOf(event.current)));
        ((RoundCornerProgressBar) view.findViewById(R.id.eventprogress)).setMax(Float.parseFloat
                (String.valueOf(event.capacity.toString())));

        ((TextView) view.findViewById(R.id.txtlocation)).setText(event.place);

        // TODO: Make picture gallery (if registered to event)

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageView imageView = (ImageView) view.findViewById(R.id.eventpicture);
        Picasso.with(getContext()).load(event.picture).fit().centerCrop().into(imageView);

        showImageGallery();

        showOwnerOptions();
        cancelEvent();
        removeParticipants();

        if(! end.before(new Date()) && (start.getTime() - (new Date()).getTime()) < 5 * 24 * 60 *
                60 * 1000) {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        } else {
            view.findViewById(R.id.forecast).setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //this.googleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if(event.latitude != null && event.longitude != null) {
            LatLng location = new LatLng(event.latitude, event.longitude);
            googleMap.addMarker(new MarkerOptions().position(location));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom
                    (10).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android
                    .Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PERMISSION_GRANTED) {
                // TODO: Try to get permissions
                return;
            }
            googleMap.setMyLocationEnabled(true);

        }
    }

    /**
     * Set up spinner to delete participants from the event (in the database) if the user owns
     * the event.
     */
    private void removeParticipants() {
        final Spinner spinner = (Spinner) view.findViewById(R.id.participants_spinner);
        final List<String> participants = new ArrayList<>();
        for(Map.Entry<String, Boolean> p : event.Participants.entrySet()) {
            if(! p.getKey().equals(event.owner) && p.getValue()) {
                participants.add(p.getKey());
            }

        }
        SpinnerAdapter adapter = new ImageArrayAdapter(getContext(), R.layout
                .participant_spinner_row, participants);
        spinner.setAdapter(adapter);

        view.findViewById(R.id.remove_participant_button).setOnClickListener(new View
                .OnClickListener() {
            private Long current;

            @Override
            public void onClick(View v) {
                String toRemove = participants.get(spinner.getSelectedItemPosition());
                FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                        "/Participants/" + toRemove).removeValue();
                // Decrease value of current by one
                FirebaseDatabase.getInstance().getReference().child("Events/" + event.id +
                        "/current").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        current = (Long) dataSnapshot.getValue();
                        current--;
                        FirebaseDatabase.getInstance().getReference().child("Events/" + event.id
                                + "/current").setValue(current);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        current = (long) - 1;
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("Users/" + toRemove +
                        "/ParticipatingEvents/" + event.id).removeValue();
            }
        });
    }

    /**
     * Set the listener of the cancel event button. Removes the whole event from the Firebase and
     * also removes the event from all the users' participating events list.
     */
    private void cancelEvent() {
        view.findViewById(R.id.cancel_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LinearLayout layout = new LinearLayout(getContext());
                TextView alertText = new TextView(getContext());

                alertText.setText("Are you sure that you want to cancel this event?\n\nThis " +
                        "action cannot be reversed.");
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

    @Override
    public Loader<List<WeatherData>> onCreateLoader(int id, Bundle args) {
        if(! end.before(new Date()) && (start.getTime() - (new Date()).getTime()) < 5 * 24 * 60 *
                60 * 1000) {
            JSONParser loader = new JSONParser(getContext(), new LatLng(event.latitude, event
                    .longitude));
            loader.forceLoad();
            return loader;
        } else {
            // Hide weather forecast thing if nothing to show
            view.findViewById(R.id.forecast).setVisibility(View.GONE);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<WeatherData>> loader, List<WeatherData> data) {
        if(loader == null || data == null) {
            // Hide weather forecast thing if nothing to show
            view.findViewById(R.id.forecast).setVisibility(View.GONE);
            return;
        }

        TextView temperature = (TextView) view.findViewById(R.id.weather_temperature);
        ImageView weatherImage = (ImageView) view.findViewById(R.id.weather_icon);
        WeatherData forecastData = null;
        if(start.before(new Date())) {
            forecastData = data.get(0);
        } else {
            for(WeatherData weatherData : data) {
                if(new Date().getTime() > weatherData.dt) {
                    forecastData = weatherData;
                    break;
                }
            }
            if(forecastData == null) {
                temperature.setText("No weather forecast available at the moment");
                return;
            }
        }
        temperature.setText(forecastData.temp + " °C");

        String imageURL = "http://openweathermap.org/img/w/" + forecastData.icon + ".png";
        Picasso.with(getContext()).load(imageURL).into(weatherImage);
    }

    @Override
    public void onLoaderReset(Loader<List<WeatherData>> loader) {
        if(loader == null) return;

    }
}
