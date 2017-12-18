package fr.eurecom.Ready2Meet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import fr.eurecom.Ready2Meet.uiExtensions.ToolbarActivity;

public class Main2Activity extends ToolbarActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setToolbar();
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
                    String pictureUri = "";
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    pictureUri = downloadUri.toString();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(auth
                            .getCurrentUser().getUid()).child("ProfilePictureURL").setValue
                            (pictureUri.toString());
                    Toast.makeText(getApplication(), "Done", Toast.LENGTH_LONG);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplication(), "Couldn't upload image to database", Toast
                            .LENGTH_LONG);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if(id == R.id.nav_add_event) {

            startActivity(new Intent(Main2Activity.this, AddEventActivity.class));

        } else if(id == R.id.nav_allevents) {
            fragment = new AllEvents();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container_new, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else if(id == R.id.nav_messages) {
            // TODO
        } else if(id == R.id.nav_manage) {
            fragment = new AccountOptions();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container_new, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else if(id == R.id.nav_share) {
            // TODO
        } else if(id == R.id.nav_send) {
            // TODO
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
