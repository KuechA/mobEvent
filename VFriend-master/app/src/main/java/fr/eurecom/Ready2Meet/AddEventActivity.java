package fr.eurecom.Ready2Meet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddEventActivity extends AppCompatActivity {
    public String ownername = "bla";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Button createEventButton = (Button) findViewById(R.id.createevent);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/title");
                myRef.setValue(((EditText) findViewById(R.id.edittext_title)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/description");
                myRef.setValue(((EditText) findViewById(R.id.edittext_description)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/time");
                myRef.setValue(((EditText) findViewById(R.id.edittext_time)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/place");
                myRef.setValue(((EditText) findViewById(R.id.edittext_place)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/capacity");
                myRef.setValue(((EditText) findViewById(R.id.edittext_capacity)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/category");
                myRef.setValue(((EditText) findViewById(R.id.edittext_category)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/picture");
                myRef.setValue(((EditText) findViewById(R.id.edittext_picture)).getText().toString());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/current");
                myRef.setValue("1");

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/owner");
                myRef.setValue(user.getUid());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/owner");
                myRef.setValue(user.getUid());

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/WhoReported");
                myRef.setValue("");

                myRef = database.getReference("Events/"+((EditText) findViewById(R.id.txt_eventid)).getText().toString()+"/Participants");
                myRef.setValue(user.getUid());

            }
        });



    }
}
