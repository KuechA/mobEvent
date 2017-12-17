package fr.eurecom.Ready2Meet.uiExtensions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageArrayAdapter extends ArrayAdapter<String> {
    private List<String> participants;

    public ImageArrayAdapter(@NonNull Context context, int resource, @NonNull List<String>
            objects) {
        super(context, resource, objects);
        participants = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getViewForPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewForPosition(position);
    }

    private View getViewForPosition(int position) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + participants.get(position));
        ImageView imageView = new CircleImageView(getContext());
        imageView.setLayoutParams(new AbsListView.LayoutParams(100, 100));
        Glide.with(layout.getContext()).using(new FirebaseImageLoader()).load(storageRef)
                .fitCenter().into(imageView);

        layout.addView(imageView);

        final TextView textView = new TextView(getContext());
        DatabaseReference userName = FirebaseDatabase.getInstance().getReference().child("Users/"
                + participants.get(position) + "/DisplayName");
        userName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textView.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO
            }
        });

        layout.addView(textView);

        return layout;
    }
}
