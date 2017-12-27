package fr.eurecom.Ready2Meet;

import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import net.igenius.customcheckbox.CustomCheckBox;

import fr.eurecom.Ready2Meet.database.Event;

public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView txtTitle, txtCategories, txtDescription, txtStarttime, txtEndtime, txtDate,
            txtPlace, txtCurrent, txtCapacity;
    ImageView eventpicture;
    com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar prgProgressbar;
    net.igenius.customcheckbox.CustomCheckBox participatingcheckbox;
    LinearLayout participants;
    View layout;
    private Event event;

    public EventViewHolder(View itemView) {
        super(itemView);
        layout = itemView;

        txtTitle = (TextView) itemView.findViewById(R.id.txteventname);
        txtCategories = (TextView) itemView.findViewById(R.id.txtcategories);
        txtStarttime = (TextView) itemView.findViewById(R.id.txtstarttime);
        txtEndtime = (TextView) itemView.findViewById(R.id.txtendtime);
        txtDate = (TextView) itemView.findViewById(R.id.txtdate);
        txtPlace = (TextView) itemView.findViewById(R.id.txtlocation);
        txtCurrent = (TextView) itemView.findViewById(R.id.txtcurrent);
        txtCapacity = (TextView) itemView.findViewById(R.id.txtcapacity);
        txtDescription = (TextView) itemView.findViewById(R.id.txteventdescription);
        eventpicture = (ImageView) itemView.findViewById(R.id.eventpicture);
        participatingcheckbox = (CustomCheckBox) itemView.findViewById(R.id.participatingcheckbox);
        participants = (LinearLayout) itemView.findViewById(R.id.participants);
        prgProgressbar = (RoundCornerProgressBar) itemView.findViewById(R.id.eventprogress);
        itemView.setOnClickListener(this);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public void onClick(View view) {
        ((Activity) view.getContext()).findViewById(R.id.tabs).setVisibility(View.GONE);
        EventDetailFragment fragment = new EventDetailFragment();
        fragment.setEvent(event.id);
        FragmentTransaction ft = ((Main2Activity) view.getContext()).getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.frame_all_events, fragment);
        ft.addToBackStack(Main2Activity.TAG_EVENT_DETAIL_FRAGMENT);
        ft.commit();
    }
}
