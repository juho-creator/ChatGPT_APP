package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

import model.Message;
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    List<Message> messageList; // List to store chat message data

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList; // Constructor to initialize the messageList
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the chat message item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        // Create a new ViewHolder to hold the views
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position); // Get the message at the current position
        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            // If the message was sent by the user, hide the left view and show the right view

            holder.left_chat_view.setVisibility(View.GONE);
            holder.right_chat_view.setVisibility(View.VISIBLE);
            holder.right_chat_tv.setText(message.getMessage()); // Set the message text
        } else {
            // If the message was not sent by the user (system message), hide the right view and show the left view
            holder.right_chat_view.setVisibility(View.GONE);
            holder.left_chat_view.setVisibility(View.VISIBLE);
            holder.left_chat_tv.setText(message.getMessage()); // Set the message text
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of chat messages in the list
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout left_chat_view, right_chat_view; // Views for left and right chat bubbles
        TextView left_chat_tv, right_chat_tv; // TextViews for left and right chat messages

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views within the ViewHolder
            left_chat_view = itemView.findViewById(R.id.left_chat_view);
            right_chat_view = itemView.findViewById(R.id.right_chat_view);
            left_chat_tv = itemView.findViewById(R.id.left_chat_tv);
            right_chat_tv = itemView.findViewById(R.id.right_chat_tv);
        }
    }
}
