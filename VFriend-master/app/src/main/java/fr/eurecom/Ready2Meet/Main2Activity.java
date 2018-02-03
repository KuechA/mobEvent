package fr.eurecom.Ready2Meet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import fr.eurecom.Ready2Meet.uiExtensions.MainPagerAdapter;
import fr.eurecom.Ready2Meet.uiExtensions.ToolbarActivity;

public class Main2Activity extends ToolbarActivity {
    private FirebaseAuth auth;

    public final static String TAG_EVENT_DETAIL_FRAGMENT = "EventDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setToolbar();

        // For swipe between fragments
        PagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Overview");
        tabLayout.getTabAt(1).setText("All Events");
        tabLayout.addTab(tabLayout.newTab().setText("My Events"));

        final String token = FirebaseInstanceId.getInstance().getToken();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child
                ("ParticipatingEvents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eventId = snapshot.getKey();
                    FirebaseDatabase.getInstance().getReference().child("MessageNotifications")
                            .child(eventId).child("notificationTokens").push().setValue(token);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error handling
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            StorageReference storage = FirebaseStorage.getInstance().getReference().child
                    ("ProfilePictures").child(auth.getCurrentUser().getUid());
            storage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask
                    .TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(auth
                            .getCurrentUser().getUid()).child("ProfilePictureURL").setValue
                            (taskSnapshot.getDownloadUrl().toString());
                    Toast.makeText(getApplication(), "Done", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplication(), "Couldn't upload image to database", Toast
                            .LENGTH_LONG).show();
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0 && getSupportFragmentManager
                ().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1)
                .getName().equals(TAG_EVENT_DETAIL_FRAGMENT)) {
            findViewById(R.id.tabs).setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // TODO: Rewrite this method to deal with the new fragment swipe stuff!
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if(id == R.id.nav_add_event) {

            startActivity(new Intent(Main2Activity.this, AddEventActivity.class));

        } else if(id == R.id.nav_allevents) {
            findViewById(R.id.tabs).setVisibility(View.GONE);
            fragment = new AllEvents();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame, fragment);
            ft.addToBackStack(TAG_EVENT_DETAIL_FRAGMENT);
            ft.commit();
        } else if(id == R.id.nav_messages) {
            // TODO
        } else if(id == R.id.nav_manage) {
            findViewById(R.id.tabs).setVisibility(View.GONE);
            fragment = new AccountOptions();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame, fragment);
            ft.addToBackStack(TAG_EVENT_DETAIL_FRAGMENT);
            ft.commit();
        } else if(id == R.id.nav_share) {
            // TODO
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
