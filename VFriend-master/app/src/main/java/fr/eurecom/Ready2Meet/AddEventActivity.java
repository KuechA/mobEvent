package fr.eurecom.Ready2Meet;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.Ready2Meet.database.Event;
import fr.eurecom.Ready2Meet.uiExtensions.MultiSelectSpinner;
import fr.eurecom.Ready2Meet.uiExtensions.ToolbarActivity;

public class AddEventActivity extends ToolbarActivity {

    private FirebaseUser user;

    private SimpleDateFormat format = new SimpleDateFormat("EE, MMM dd, yyyy 'at' hh:mm a");
    private Calendar startDate = null;
    private String pictureUri = null;
    private String eventId = null;
    private Map<String, Boolean> categories;

    private double longitude;
    private double latitude;

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PICK_GALLERY = 2;

    private String[] eventCategories = {"Sport", "Party", "Outdoor", "Others"};

    private void setUiElements() {
        // Set default picture for ImageButton
        Uri defaultPicture = Uri.parse("https://firebasestorage.googleapis" + "" + "" + "" + "" +
                ".com/v0/b/ready2meet-e0286.appspot.com/o/EventPhotos%2FDefault" + "" + "" + "" +
                ".jpg?alt=media&token=c6a16086-728b-43a5-b169-fae8b07a5070");
        Picasso.with(getApplicationContext()).load(defaultPicture).fit().centerCrop().into(
                (ImageButton) findViewById(R.id.eventImage));

        // Set spinner for radius selection
        Spinner areaUnitSpinner = (Spinner) findViewById(R.id.spinner_area_unit);
        List<String> areaUnits = new ArrayList<>();
        areaUnits.add("km");
        areaUnits.add("m");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item, areaUnits);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaUnitSpinner.setAdapter(dataAdapter);

        categories = new HashMap<>();

        MultiSelectSpinner categorySpinner = (MultiSelectSpinner) findViewById(R.id
                .category_selector);
        categorySpinner.setItems(eventCategories);
        categorySpinner.setListener(new MultiSelectSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedStrings(List<String> strings) {
                for(String category : strings) {
                    categories.put(category, true);
                }
            }
        });
    }

    private void createEvent() {

        Button createEventButton = (Button) findViewById(R.id.createevent);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Push empty object to list of events in the database to create a unique key
                final DatabaseReference eventData = FirebaseDatabase.getInstance().getReference()
                        .child("Events").push();
                eventId = eventData.getKey();

                String eventTitle = ((EditText) findViewById(R.id.edittext_title)).getText()
                        .toString();
                String eventDescription = ((EditText) findViewById(R.id.edittext_description))
                        .getText().toString();
                String place = ((Button) findViewById(R.id.find_location_button)).getText()
                        .toString();
                Long capacity = Long.parseLong(((EditText) findViewById(R.id.edittext_capacity))
                        .getText().toString());
                String startTime = ((Button) findViewById(R.id.show_starttime_button)).getText()
                        .toString();
                String endTime = ((Button) findViewById(R.id.show_endtime_button)).getText()
                        .toString();

                Long notificationArea = Long.parseLong(((EditText) findViewById(R.id
                        .edittext_area)).getText().toString());
                notificationArea = ((Spinner) findViewById(R.id.spinner_area_unit))
                        .getSelectedItem().toString().equals("km") ? notificationArea * 1000 :
                        notificationArea;

                // Restrict area from 0 to 10 km
                if(notificationArea < 0) {
                    notificationArea = Long.valueOf(0);
                } else if(notificationArea > 10000) {
                    notificationArea = Long.valueOf(10000);
                }

                Long current = Long.valueOf(1);
                Map<String, Boolean> whoReported = new HashMap<>();
                Map<String, Boolean> participants = new HashMap<>();
                participants.put(user.getUid(), true);
                String owner = user.getUid();

                Event newEvent = new Event(eventTitle, eventDescription, owner, current,
                        categories, capacity, pictureUri, place, startTime, endTime,
                        participants, whoReported, notificationArea, latitude, longitude);

                eventData.setValue(newEvent).addOnSuccessListener(new OnSuccessListener() {

                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getApplicationContext(), "Event successfully added", Toast
                                .LENGTH_LONG);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "An error occurred while adding " +
                                "the event.", Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        toolbar = (Toolbar) findViewById(R.id.toolbar_add_event);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_add_event);
        setToolbar();
        setUiElements();
        createEvent();

        user = FirebaseAuth.getInstance().getCurrentUser();

        final Button showStartTimeButton = (Button) findViewById(R.id.show_starttime_button);
        showStartTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DateTimePickerDialog dialog = new DateTimePickerDialog(AddEventActivity.this);
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogI) {
                        Calendar result = dialog.getResult();
                        if(result != null) {
                            showStartTimeButton.setText(format.format(result.getTime()));
                            startDate = result;
                        }
                    }
                });
            }
        });

        final Button showEndTimeButton = (Button) findViewById(R.id.show_endtime_button);
        showEndTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DateTimePickerDialog dialog = new DateTimePickerDialog(AddEventActivity.this);
                dialog.setMinDate(startDate);
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogI) {
                        Calendar result = dialog.getResult();
                        if(result != null) {
                            showEndTimeButton.setText(format.format(result.getTime()));
                        }
                    }
                });
            }
        });

        Button getLocationButton = (Button) findViewById(R.id.find_location_button);
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(AddEventActivity.this),
                            PLACE_PICKER_REQUEST);
                } catch(GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch(GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        ImageButton photoButton = (ImageButton) findViewById(R.id.eventImage);
        photoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_GALLERY);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Button getLocationButton = (Button) findViewById(R.id.find_location_button);
                getLocationButton.setText(place.getAddress());
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
            }
        } else if(requestCode == PICK_GALLERY && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            StorageReference storage = FirebaseStorage.getInstance().getReference().child
                    ("EventPhotos").child(eventId + "/startPhoto");

            storage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask
                    .TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddEventActivity.this, "Successfully uploaded image to " +
                            "database", Toast.LENGTH_LONG);

                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    pictureUri = downloadUri.toString();

                    Picasso.with(getApplicationContext()).load(downloadUri).fit().centerCrop()
                            .into((ImageButton) findViewById(R.id.eventImage));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddEventActivity.this, "Couldn't upload image to database",
                            Toast.LENGTH_LONG);
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_add_event) {
            // Do nothing
        } else if(id == R.id.nav_allevents) {

            // TODO: Select fragment
            startActivity(new Intent(AddEventActivity.this, Main2Activity.class));

        } else if(id == R.id.nav_messages) {

        } else if(id == R.id.nav_manage) {

            // TODO: Select fragment
            startActivity(new Intent(AddEventActivity.this, Main2Activity.class));

        } else if(id == R.id.nav_share) {

        } else if(id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_add_event);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
