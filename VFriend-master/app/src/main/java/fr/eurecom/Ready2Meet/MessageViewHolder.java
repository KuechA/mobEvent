package fr.eurecom.Ready2Meet;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    TextView message;
    TextView time;
    de.hdodenhof.circleimageview.CircleImageView sender;

    public MessageViewHolder(View itemView) {
        super(itemView);

        message = (TextView) itemView.findViewById(R.id.message);
        time = (TextView) itemView.findViewById(R.id.message_time);
        sender = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.sender);
    }
}
