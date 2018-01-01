package fr.eurecom.Ready2Meet;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    TextView message;
    de.hdodenhof.circleimageview.CircleImageView sender;

    public MessageViewHolder(View itemView) {
        super(itemView);

        message = (TextView) itemView.findViewById(R.id.message);
        sender = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.sender);
    }
}
