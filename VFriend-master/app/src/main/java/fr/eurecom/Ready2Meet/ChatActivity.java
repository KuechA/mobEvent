package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import fr.eurecom.Ready2Meet.database.Message;

public class ChatActivity extends AppCompatActivity {
    private String eventId;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerViewAdapter_Message adapter;
    public static final SimpleDateFormat MESSAGE_DATE = new SimpleDateFormat("yyyy-MM-dd' " +
            "'HH:mm:ss.SSSSSS' 'ZZZZZ");

    private void sendMessages() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ImageButton sendButton = (ImageButton) findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = ((EditText) findViewById(R.id.message_field)).getText().toString();
                FirebaseDatabase.getInstance().getReference().child("Messages").child(eventId)
                        .push().setValue(new Message(message, uid, MESSAGE_DATE.format(new Date()
                )));
                ((EditText) findViewById(R.id.message_field)).setText("");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        eventId = getIntent().hasExtra("EventId") ? getIntent().getStringExtra("EventId") : null;
        String eventTitle = getIntent().hasExtra("EventTitle") ? getIntent().getStringExtra
                ("EventTitle") : "Chatroom";

        getSupportActionBar().show();
        getSupportActionBar().setTitle(eventTitle);

        final RecyclerView listView = (RecyclerView) findViewById(R.id.list_chat);
        final DatabaseReference messages = FirebaseDatabase.getInstance().getReference().child
                ("Messages").child(eventId);
        messages.keepSynced(true);
        messages.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals("notificationTokens")) {
                        continue;
                    }
                    Message message = snapshot.getValue(Message.class);
                    messageList.add(message);
                }
                Collections.sort(messageList, new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        try {
                            return MESSAGE_DATE.parse(o1.time).compareTo(MESSAGE_DATE.parse
                                    (o2.time));
                        } catch(ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
                adapter = new RecyclerViewAdapter_Message(getApplicationContext(), messageList);

                listView.setAdapter(adapter);
                listView.setLayoutManager(new LinearLayoutManager(ChatActivity.this
                        .getApplicationContext()));
                adapter.notifyItemMoved(0, adapter.getItemCount() - 1);
                adapter.notifyDataSetChanged();
                listView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        sendMessages();
    }
}
