package fr.eurecom.Ready2Meet;

/**
 * Created by koksa on 9.11.2017.
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import fr.eurecom.Ready2Meet.database.Event;


public class ListViewAdapter_Event  extends ArrayAdapter<Event> {

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
    public int getCount()
    {
        if (events != null)
            return events.size();
        else
            return 0;
    }


    @Override
    public Event       getItem(int position)
    {
        return events.get(position);
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public static class ViewHolder
    {
        TextView txtTitle, txtCategories, txtDescription, txtStarttime, txtEndtime, txtDate, txtPlace, txtCurrent, txtCapacity;
        ImageView eventpicture;
        net.igenius.customcheckbox.CustomCheckBox participatingcheckbox;
        LinearLayout participants;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        viewHolder = null;


        if (convertView == null)
        {
            convertView = layoutInflater.inflate(layoutResourceId, parent,false);

            viewHolder = new ViewHolder();

            viewHolder.txtTitle = (TextView)convertView.findViewById(R.id.txteventname);
            viewHolder.txtCategories = (TextView)convertView.findViewById(R.id.txtcategories);
            viewHolder.txtStarttime = (TextView)convertView.findViewById(R.id.txtstarttime);
            viewHolder.txtEndtime = (TextView)convertView.findViewById(R.id.txtendtime);
            viewHolder.txtDate = (TextView)convertView.findViewById(R.id.txtdate);
            viewHolder.txtPlace = (TextView)convertView.findViewById(R.id.txtlocation);
            viewHolder.txtCurrent = (TextView)convertView.findViewById(R.id.txtcurrent);
            viewHolder.txtCapacity = (TextView)convertView.findViewById(R.id.txtcapacity);
            viewHolder.txtDescription = (TextView)convertView.findViewById(R.id.txteventdescription);
            viewHolder.eventpicture = (ImageView)convertView.findViewById(R.id.eventpicture);
            viewHolder.participatingcheckbox=(net.igenius.customcheckbox.CustomCheckBox) convertView.findViewById(R.id.participatingcheckbox);
            viewHolder.participants = (LinearLayout) convertView.findViewById(R.id.participants);
            /*
            TODO: Font will be changed using the Helvetica.otf file. Code is written file is missing currently.
            Typeface type = Typeface.createFromAsset(getContext().getAssets(),"fonts/Helvetica2.otf");
            viewHolder.txtDescription.setTypeface(type);
            viewHolder.txtTitle.setTypeface(type);
            viewHolder.txtStarttime.setTypeface(type);
            viewHolder.txtCategories.setTypeface(type);
            viewHolder.txtEndtime.setTypeface(type);
            viewHolder.txtDate.setTypeface(type);
            viewHolder.txtPlace.setTypeface(type);
            viewHolder.txtCurrent.setTypeface(type);
            viewHolder.txtCapacity.setTypeface(type);
            */
            convertView.setTag(viewHolder);

        }
        else
        {
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

        String parseddate = (temp.substring(temp.lastIndexOf(" at")  +3 ));
        parseddate = (temp.replaceAll((temp.substring(temp.lastIndexOf(" at")  +3 )), "")).replaceAll(" at","");
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
            if(key.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
                viewHolder.participatingcheckbox.setChecked(Boolean.TRUE);
            }

        }
/*
        Picasso
                .with(getContext())
                .load("https://firebasestorage.googleapis.com/v0/b/ready2meet-e0286.appspot.com/o/ProfilePictures%2FDefaultProfilePicture.jpg?alt=media&token=56bc3fe3-c68d-4d6e-80aa-135c762c0635")
                .into(ii);
*/

        for (String key : info.Participants.keySet()) {


            StorageReference storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures").child(key);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {

                                    ImageView ii = new ImageView(getContext());
                                    Picasso
                                            .with(getContext())
                                            .load(uri.toString())
                                            .into(ii);
                                    ii.setPadding(0, 0, 4, 0);
                                    ii.setLayoutParams(new LinearLayout.LayoutParams(viewHolder.participants.getHeight(), viewHolder.participants.getHeight()));
                                    viewHolder.participants.addView(ii);



                            // Got the download URL for 'users/me/profile.png'
                            //viewHolder.participants.addView(ii);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

        }



        //This deleted elements after the limit. but not working properly
                viewHolder.participants.post(new Runnable() {
                    public void run() {
                        if (viewHolder.participants.getChildCount() > 0 && info.Participants.size() != 0) {
                            while (info.Participants.size() < viewHolder.participants.getChildCount()) {
                                viewHolder.participants.removeViewAt(viewHolder.participants.getChildCount() - 1);
                            }
                        }
                    }
                });

        return convertView;
    }








}