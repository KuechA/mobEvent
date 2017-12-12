package fr.eurecom.Ready2Meet.uiExtensions;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.R;
import fr.eurecom.Ready2Meet.database.User;

public abstract class ToolbarActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    protected FirebaseAuth auth;
    protected Toolbar toolbar;
    protected DrawerLayout drawer;

    protected void setToolbar() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null) {
            View header = navigationView.getHeaderView(0);

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null) {
                String uid = user.getUid();

                final TextView textForName = (TextView) header.findViewById(R.id.textView);
                final CircleImageView imgView = (CircleImageView) header.findViewById(R.id
                        .imageView);

                TextView text2 = (TextView) header.findViewById(R.id.textView2);
                text2.setText(user.getEmail());

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference
                        ("Users/" + uid);
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        textForName.setText(user.DisplayName);
                        Picasso.with(getApplicationContext()).load(user.ProfilePictureURL).fit()
                                .into(imgView);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
            }
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        Button changeProfilePicture = (Button) headerView.findViewById(R.id.changeprofilepicture);
        changeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
