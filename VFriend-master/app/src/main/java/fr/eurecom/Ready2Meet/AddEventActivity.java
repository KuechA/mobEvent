package fr.eurecom.Ready2Meet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddEventActivity extends AppCompatActivity {
    private String ownername = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Button createEventButton = (Button) findViewById(R.id.createevent);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/title");
                myRef.setValue(findViewById(R.id.edittext_title).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/description");
                myRef.setValue(findViewById(R.id.edittext_description).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/time");
                myRef.setValue(findViewById(R.id.edittext_time).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/place");
                myRef.setValue(findViewById(R.id.edittext_place).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/capacity");
                myRef.setValue(findViewById(R.id.edittext_capacity).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/category");
                myRef.setValue(findViewById(R.id.edittext_category).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/picture");
                myRef.setValue(findViewById(R.id.edittext_picture).toString());

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/current");
                myRef.setValue("0");

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                myRef = database.getReference("Users/"+user.getUid()+"/DisplayName");

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        ownername = dataSnapshot.getValue(String.class);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value

                    }
                });


                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/owner");
                myRef.setValue(ownername);

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/WhoReported");
                myRef.setValue("");

                myRef = database.getReference("Events/"+findViewById(R.id.txt_eventid).toString()+"/Participants");
                myRef.setValue("");

            }
        });



    }
}
