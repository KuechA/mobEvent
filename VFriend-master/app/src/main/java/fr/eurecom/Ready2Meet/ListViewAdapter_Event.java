package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        final Event info = events.get(position);
        holder.setEvent(info);
        holder.txtDescription.setText(info.description);
        holder.txtTitle.setText(info.title);

        //TODO: Loop the categories to generate one big string of list for it
        holder.txtCategories.setText("Sport");

        //TODO: Parse dates of start end and date of day
        String parsedstarttime = info.startTime;
        parsedstarttime = (parsedstarttime.substring(parsedstarttime.lastIndexOf(" at") + 3));
        String parsedendtime = info.endTime;
        parsedendtime = (parsedendtime.substring(parsedendtime.lastIndexOf(" at") + 3));

        holder.txtEndtime.setText(parsedendtime);
        holder.txtStarttime.setText(parsedstarttime);

        String temp = info.startTime;

        String parseddate = temp.substring(temp.lastIndexOf(" at") + 3);
        parseddate = (temp.replaceAll((temp.substring(temp.lastIndexOf(" at") + 3)), ""))
                .replaceAll(" at", "");
        holder.txtDate.setText(parseddate);

        holder.txtPlace.setText(info.place);
        holder.txtCurrent.setText(Long.toString(info.current));
        holder.txtCapacity.setText(Long.toString(info.capacity));

        Picasso.with(context).load(info.picture).into(holder.eventpicture);

        holder.participatingcheckbox.setChecked(Boolean.FALSE);
        for(String key : info.Participants.keySet()) {
            if(key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.participatingcheckbox.setChecked(Boolean.TRUE);
                break;
            }
        }

        holder.participatingcheckbox.setOnCheckedChangeListener(new CustomCheckBox
                .OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                Map<String, Object> updateRequest = new HashMap(1);
                updateRequest.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), isChecked);
                FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                        "/Participants").updateChildren(updateRequest);
            }
        });

        for(String key : info.Participants.keySet()) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);
            ImageView ii = new ImageView(holder.participants.getContext());
            ii.setPadding(0, 0, 4, 0);
            ii.setLayoutParams(new LinearLayout.LayoutParams(holder.participants.getHeight(),
                    holder.participants.getHeight()));
            holder.participants.addView(ii);
            Glide.with(holder.participants.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).into(ii);
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

}