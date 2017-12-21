package fr.eurecom.Ready2Meet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.Ready2Meet.database.Event;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private double maxPeople = 1;

    public DashboardFragment() {
        // Required empty public constructor
    }

    private List<Event> eventlist = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
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

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        FirebaseDatabase.getInstance().getReference().child("Events")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventlist.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event eventread = snapshot.getValue(Event.class);
                    eventread.id = snapshot.getKey();
                    eventlist.add(eventread);
                    mapFragment.getMapAsync(DashboardFragment.this);
                    if(eventread.current > maxPeople) {
                        maxPeople = eventread.current;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission
                (getActivity().getApplicationContext(), android.Manifest.permission
                        .ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(getActivity().getApplicationContext(), android.Manifest
                        .permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            Log.w("DashboardFragment", "No permission to get location");
            // TODO: Try to get permissions
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        for(Event event : eventlist) {
            if(event.latitude != null && event.longitude != null) {
                LatLng location = new LatLng(event.latitude, event.longitude);
                MarkerOptions marker = new MarkerOptions().position(location);
                marker.title(event.title);

                // Get icon and scale its size depending on the number of current people in the
                // event
                Bitmap icon;
                Drawable drawableicon = getResources().getDrawable(R.drawable.ic_location_red);
                if(drawableicon instanceof BitmapDrawable) {
                    icon = ((BitmapDrawable) drawableicon).getBitmap();
                } else {
                    icon = Bitmap.createBitmap(drawableicon.getIntrinsicWidth(), drawableicon
                            .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(icon);
                    drawableicon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawableicon.draw(canvas);
                }
                marker.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(icon,
                        (int) Math.ceil(icon.getWidth() * 1.5 * event.current / maxPeople), (int)
                                Math.ceil(icon.getHeight() * 1.5 * event.current / maxPeople),
                        false)));
                googleMap.addMarker(marker).setTag(event.id);
            }
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                EventDetailFragment fragment = new EventDetailFragment();
                fragment.setEventId(marker.getTag().toString());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_new, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[]
            grantResults) {
        switch(requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat
                            .checkSelfPermission(getContext(), Manifest.permission
                                    .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest
                            .permission.ACCESS_COARSE_LOCATION) != PackageManager
                            .PERMISSION_GRANTED) {
                        // This block can never occur.
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                } else {
                    // permission denied, boo!
                }
                return;
            }
        }
    }
}
