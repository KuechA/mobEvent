package fr.eurecom.Ready2Meet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.Ready2Meet.database.Message;

public class ChatActivity extends AppCompatActivity {
    private String eventId;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerViewAdapter_Message adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        eventId = getIntent().hasExtra("EventId") ? getIntent().getStringExtra("EventId") : null;

        final RecyclerView listView = (RecyclerView) findViewById(R.id.list_chat);
        DatabaseReference messages = FirebaseDatabase.getInstance().getReference().child
                ("Messages").child(eventId);
        messages.keepSynced(true);
        messages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messageList.add(message);
                }
                adapter = new RecyclerViewAdapter_Message(getApplicationContext(), messageList);

                listView.setAdapter(adapter);
                listView.setLayoutManager(new LinearLayoutManager(ChatActivity.this
                        .getApplicationContext()));
                adapter.notifyItemMoved(0, adapter.getItemCount() - 1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
