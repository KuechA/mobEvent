package fr.eurecom.Ready2Meet;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.Message;

public class RecyclerViewAdapter_Message extends RecyclerView.Adapter<MessageViewHolder> {
    public List<Message> messageList;
    private Context context;

    public RecyclerViewAdapter_Message(Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_message, parent, false);
        MessageViewHolder vh = new MessageViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final Message message = messageList.get(position);
        holder.message.setText(message.message);

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                ("gs://ready2meet-e0286.appspot.com/ProfilePictures/" + message.senderId);
        CircleImageView ii = holder.sender;
        ii.setBorderWidth(5);
        ii.setBorderColor(Color.RED);
        ii.setPadding(0, 0, 4, 0);
        LinearLayout.LayoutParams test = new LinearLayout.LayoutParams(100, 100);
        test.gravity = Gravity.CENTER;
        ii.setLayoutParams(test);
        Glide.with(holder.sender.getContext()).using(new FirebaseImageLoader()).load(storageRef)
                .into(ii);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
