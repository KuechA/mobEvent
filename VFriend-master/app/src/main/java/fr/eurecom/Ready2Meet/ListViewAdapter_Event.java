package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.Event;

public class ListViewAdapter_Event extends RecyclerView.Adapter<EventViewHolder> implements
        Filterable {
    public List<Event> events;
    private List<Event> allEvents;
    private Context context;

    public ListViewAdapter_Event(Context context, List<Event> eventlist) {
        this.events = eventlist;
        this.allEvents = eventlist;
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

        StringBuilder categories = new StringBuilder("");
        for(Map.Entry<String, Boolean> c : event.categories.entrySet()) {
            if(c.getValue()) {
                if(categories.toString().equals("")) {
                    categories.append(c.getKey());
                } else {
                    categories.append(", ").append(c.getKey());
                }
            }
        }
        return categories.toString();
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        final Event info = events.get(position);
        holder.setEvent(info);
        holder.txtDescription.setText(info.description);
        holder.txtTitle.setText(info.title);

        holder.txtCategories.setText(getCategories(info));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' hh:mm a", Locale.US);
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.US);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
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
        holder.txtCurrent.setText(String.valueOf(info.current));
        holder.txtCapacity.setText(String.valueOf(info.capacity));

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
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(! participating) {
                    if(! isFull) {
                        holder.participatingcheckbox.setChecked(true);
                        FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                                "/Participants").child(uid).setValue(true);
                        info.current++;
                        FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                                "/current").setValue(info.current);

                        FirebaseDatabase.getInstance().getReference().child("Users/" + uid +
                                "/ParticipatingEvents/" + info.id).setValue(true);
                    } else {
                        holder.participatingcheckbox.setChecked(false);
                        EventFull_Toast.show();
                        holder.participatingcheckbox.setChecked(false);
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                            "/Participants").child(uid).removeValue();
                    info.current--;
                    FirebaseDatabase.getInstance().getReference().child("Events/" + info.id +
                            "/current").setValue(info.current);
                    holder.participatingcheckbox.setChecked(false);

                    FirebaseDatabase.getInstance().getReference().child("Users/" + uid +
                            "/ParticipatingEvents/" + info.id).removeValue();
                }

            }
        });

        if(info.Participants != null) {

            //add owner first!
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + info.owner);
            CircleImageView ii = new CircleImageView(holder.participants.getContext());
            ii.setBorderWidth(5);
            ii.setBorderColor(Color.RED);
            ii.setPadding(0, 0, 4, 0);
            LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
            test.gravity = Gravity.CENTER;
            ii.setLayoutParams(test);
            holder.participants.addView(ii);
            Glide.with(holder.participants.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).into(ii);

            for(String key : info.Participants.keySet()) {
                if(key.equals(info.owner)) {continue;}
                storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                        ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);

                ii = new CircleImageView(holder.participants.getContext());
                ii.setBorderWidth(5);
                ii.setBorderColor(Color.TRANSPARENT);

                ii.setPadding(0, 0, 4, 0);
                test = new LinearLayout.LayoutParams(100, 100);
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

    @Override
    public Filter getFilter() {
        return new EventFilter(this);
    }

    public class EventFilter extends Filter {
        private ListViewAdapter_Event adapter;

        EventFilter(ListViewAdapter_Event adapter) {
            this.adapter = adapter;
        }

        void removeOldFilter() {
            events = new ArrayList<>();
        }

        void showWithoutFiltering() {
            events = allEvents;
            notifyDataSetChanged();
        }

        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            Filter.FilterResults results = new Filter.FilterResults();
            List<Event> FilteredArrayNames = new ArrayList<>();

            for(Event event : adapter.allEvents) {
                if(event.categories.containsKey(constraint.toString()) && event.categories.get
                        (constraint.toString())) {
                    FilteredArrayNames.add(event);
                }
            }

            results.count = FilteredArrayNames.size();
            results.values = FilteredArrayNames;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            adapter.events.addAll((List<Event>) results.values);
            notifyDataSetChanged();
        }

        void uniqueResults() {
            adapter.events = new ArrayList<>(new HashSet<>(adapter.events));
            notifyDataSetChanged();
        }
    }
}