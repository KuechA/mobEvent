package fr.eurecom.Ready2Meet;

/**
 * Created by koksa on 9.11.2017.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import net.igenius.customcheckbox.CustomCheckBox;

import fr.eurecom.Ready2Meet.database.Event;


public class ListViewAdapter_Event extends ArrayAdapter<Event> {

    private int layoutResourceId;
    public List<Event> events;
    public LayoutInflater layoutInflater;
    public ViewHolder viewHolder;


    public ListViewAdapter_Event(Context context, int layoutResourceId, List<Event> eventlist) {

        super(context, layoutResourceId, eventlist);

        this.layoutResourceId = layoutResourceId;
        this.events = eventlist;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        if (events != null)
            return events.size();
        else
            return 0;
    }


    @Override
    public Event getItem(int position) {
        return events.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        TextView txtTitle, txtCategories, txtDescription, txtStarttime, txtEndtime, txtDate, txtPlace, txtCurrent, txtCapacity;
        ImageView eventpicture;
        net.igenius.customcheckbox.CustomCheckBox participatingcheckbox;
        LinearLayout participants;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        viewHolder = null;


        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResourceId, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.txteventname);
            viewHolder.txtCategories = (TextView) convertView.findViewById(R.id.txtcategories);
            viewHolder.txtStarttime = (TextView) convertView.findViewById(R.id.txtstarttime);
            viewHolder.txtEndtime = (TextView) convertView.findViewById(R.id.txtendtime);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txtdate);
            viewHolder.txtPlace = (TextView) convertView.findViewById(R.id.txtlocation);
            viewHolder.txtCurrent = (TextView) convertView.findViewById(R.id.txtcurrent);
            viewHolder.txtCapacity = (TextView) convertView.findViewById(R.id.txtcapacity);
            viewHolder.txtDescription = (TextView) convertView.findViewById(R.id.txteventdescription);
            viewHolder.eventpicture = (ImageView) convertView.findViewById(R.id.eventpicture);
            viewHolder.participatingcheckbox = (net.igenius.customcheckbox.CustomCheckBox) convertView.findViewById(R.id.participatingcheckbox);
            viewHolder.participants = (LinearLayout) convertView.findViewById(R.id.participants);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Event info = events.get(position);
        viewHolder.txtDescription.setText(info.description);
        viewHolder.txtTitle.setText(info.title);

        //TODO: Loop the categories to generate one big string of list for it
        viewHolder.txtCategories.setText("Sport");


        //TODO: Parse dates of start end and date of day


        String parsedstarttime = info.startTime;
        parsedstarttime = (parsedstarttime.substring(parsedstarttime.lastIndexOf(" at") + 3));
        String parsedendtime = info.endTime;
        parsedendtime = (parsedendtime.substring(parsedendtime.lastIndexOf(" at") + 3));


        viewHolder.txtEndtime.setText(parsedendtime);
        viewHolder.txtStarttime.setText(parsedstarttime);

        String temp = info.startTime;

        String parseddate = temp.substring(temp.lastIndexOf(" at") + 3);
        parseddate = (temp.replaceAll((temp.substring(temp.lastIndexOf(" at") + 3)), "")).replaceAll(" at", "");
        viewHolder.txtDate.setText(parseddate);


        viewHolder.txtPlace.setText(info.place);
        viewHolder.txtCurrent.setText(Long.toString(info.current));
        viewHolder.txtCapacity.setText(Long.toString(info.capacity));


        Picasso
                .with(getContext())
                .load(info.picture)
                .into(viewHolder.eventpicture);

        viewHolder.participatingcheckbox.setChecked(Boolean.FALSE);
        for (String key : info.Participants.keySet()) {
            if (key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                viewHolder.participatingcheckbox.setChecked(Boolean.TRUE);
                break;
            }
        }

        viewHolder.participatingcheckbox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                Map<String, Object> updateRequest = new HashMap(1);
                updateRequest.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), isChecked);
                FirebaseDatabase.getInstance().getReference().child("Events/" + info.id + "/Participants").updateChildren(updateRequest);
            }
        });

        for (String key : info.Participants.keySet()) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + key);
            ImageView ii = new ImageView(viewHolder.participants.getContext());
            ii.setPadding(0, 0, 4, 0);
            ii.setLayoutParams(new LinearLayout.LayoutParams(viewHolder.participants.getHeight(), viewHolder.participants.getHeight()));
            viewHolder.participants.addView(ii);
            Glide.with(viewHolder.participants.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(ii);
        }

        //This deleted elements after the limit. but not working properly
        viewHolder.participants.post(new Runnable() {
            public void run() {
                while (info.Participants.size() < viewHolder.participants.getChildCount()) {
                    viewHolder.participants.removeViewAt(viewHolder.participants.getChildCount() - 1);
                }
            }
        });

        return convertView;
    }

}