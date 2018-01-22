package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.Message;

public class RecyclerViewAdapter_Message extends RecyclerView.Adapter<MessageViewHolder> {
    public List<Message> messageList;
    private Context context;
    private String uid;
    private static final int VIEW_TYPE_OUTGOING = 0;
    private static final int VIEW_TYPE_INCOMING = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");

    public RecyclerViewAdapter_Message(Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if(viewType == VIEW_TYPE_OUTGOING) {
            v = inflater.inflate(R.layout.row_message_sent, parent, false);
            v.findViewById(R.id.message_bubble_sent).getBackground().setColorFilter(context
                    .getResources().getColor(R.color.light_grey), PorterDuff.Mode.MULTIPLY);

            /*de.hdodenhof.circleimageview.CircleImageView senderpicture = (de.hdodenhof
                    .circleimageview.CircleImageView) v.findViewById(R.id.sender);

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + FirebaseAuth
                            .getInstance().getCurrentUser().getUid());

            Glide.with(v.getContext()).using(new FirebaseImageLoader()).load(storageRef)
                    .fitCenter().into(senderpicture);*/
        } else {
            v = inflater.inflate(R.layout.row_message, parent, false);
            v.findViewById(R.id.message_bubble).getBackground().setColorFilter(context
                    .getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }
        MessageViewHolder vh = new MessageViewHolder(v);
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).senderId.equals(uid) ? VIEW_TYPE_OUTGOING :
                VIEW_TYPE_INCOMING;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final Message message = messageList.get(position);
        holder.message.setText(message.message);
        try {
            Date date = ChatActivity.MESSAGE_DATE.parse(message.time);
            holder.time.setText(dateFormat.format(date));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        if(! message.senderId.equals(uid)) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                    ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + message.senderId);

            CircleImageView ii = holder.sender;
            ii.setPadding(0, 0, 4, 0);
            LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
            test.gravity = Gravity.CENTER;
            ii.setLayoutParams(test);
            Glide.with(holder.sender.getContext()).using(new FirebaseImageLoader()).load
                    (storageRef).into(ii);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
