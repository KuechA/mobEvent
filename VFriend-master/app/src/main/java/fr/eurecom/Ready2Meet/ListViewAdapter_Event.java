package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.Event;

public class ListViewAdapter_Event extends RecyclerView.Adapter<EventViewHolder> {
    public List<Event> events;
    private Context context;

    public ListViewAdapter_Event(Context context, List<Event> eventlist) {
        this.events = eventlist;
        this.context = context;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_events, parent, false);
        EventViewHolder vh = new EventViewHolder(v);
        return vh;
    }

    private String getCategories(Event event) {
        if(event.categories == null) return null;

        String categories = "";
        for(Map.Entry<String, Boolean> c : event.categories.entrySet()) {
            if(c.getValue()) {
                if(categories == "") {
                    categories += c.getKey();
                } else {
                    categories += ", " + c.getKey();
                }
            }
        }
        return categories;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        final Event info = events.get(position);
        holder.setEvent(info);
        holder.txtDescription.setText(info.description);
        holder.txtTitle.setText(info.title);

        holder.txtCategories.setText(getCategories(info));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd 'at' hh:mm a");
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat formatDate = new SimpleDateFormat("MMM dd, yyyy");
        try {
            Date start = format.parse(info.startTime);
            holder.txtStarttime.setText(formatTime.format(start));
            holder.txtDate.setText(formatDate.format(start));
        } catch(ParseException e) {
            e.printStackTrace();
        }
        try {
            Date end = format.parse(info.endTime);
            holder.txtEndtime.setText(formatTime.format(end));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        holder.txtPlace.setText(info.place);
        holder.txtCurrent.setText(Long.toString(info.current));
        holder.txtCapacity.setText(Long.toString(info.capacity));

        holder.prgProgressbar.setProgress(Float.parseFloat(String.valueOf(Long.toString(info
                .current))));
        holder.prgProgressbar.setMax(Float.parseFloat(String.valueOf(Long.toString(info.capacity)
        )));

        Picasso.with(context).load(info.picture).into(holder.eventpicture);

        holder.participatingcheckbox.setChecked(Boolean.FALSE);
        if(info.Participants != null) {
            for(String key : info.Participants.keySet()) {
                if(key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.participatingcheckbox.setChecked(Boolean.TRUE);
                    break;
                }
            }
        }

        final boolean isFull = (info.current >= info.capacity);
        final Toast EventFull_Toast = Toast.makeText(context, "This event is full :(", Toast
                .LENGTH_LONG);

        holder.participatingcheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean participating = holder.participatingcheckbox.isChecked();

                if(! participating) {
                    if(! isFull) {
                        holder.participatingcheckbox.setChecked(true);
                        FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                                "/Participants").child(FirebaseAuth.getInstance().getCurrentUser
                                ().getUid()).setValue(true);
                        info.current++;
                        FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                                "/current").setValue(info.current);

                        FirebaseDatabase.getInstance().getReference().child("Users/" +
                                FirebaseAuth.getInstance().getCurrentUser().getUid() +
                                "/ParticipatingEvents/" + info.id).setValue(true);
                    } else {
                        holder.participatingcheckbox.setChecked(false);
                        EventFull_Toast.show();
                        holder.participatingcheckbox.setChecked(false);
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                            "/Participants").child(FirebaseAuth.getInstance().getCurrentUser()
                            .getUid()).removeValue();
                    info.current--;
                    FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                            "/current").setValue(info.current);
                    holder.participatingcheckbox.setChecked(false);

                    FirebaseDatabase.getInstance().getReference().child("Users/" + FirebaseAuth
                            .getInstance().getCurrentUser().getUid() + "/ParticipatingEvents/" +
                            info.id).removeValue();
                }

            }
        });

        if(info.Participants != null) {
            for(String key : info.Participants.keySet()) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                        ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);

                CircleImageView ii = new CircleImageView(holder.participants.getContext());
                ii.setBorderWidth(2);
                ii.setBorderColor(Color.TRANSPARENT);

                ii.setPadding(0, 0, 4, 0);
                LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
                test.gravity = Gravity.CENTER;
                ii.setLayoutParams(test);
                holder.participants.addView(ii);
                Glide.with(holder.participants.getContext()).using(new FirebaseImageLoader())
                        .load(storageRef).into(ii);
            }

            //This deleted elements after the limit. but not working properly
            holder.participants.post(new Runnable() {
                public void run() {
                    while(info.Participants.size() < holder.participants.getChildCount()) {
                        holder.participants.removeViewAt(holder.participants.getChildCount() - 1);
                    }
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

}