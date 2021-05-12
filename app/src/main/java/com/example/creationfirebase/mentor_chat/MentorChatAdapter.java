package com.example.creationfirebase.mentor_chat;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.RequestManager;
import com.example.creationfirebase.R;
import com.example.creationfirebase.databinding.ActivityMentorChatBinding;
import com.example.creationfirebase.databinding.ActivityMentorChatItemBinding;
import com.example.creationfirebase.models.Message;
import com.firebase.ui.firestore.FirestoreArray;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MentorChatAdapter extends FirestoreRecyclerAdapter<Message, MessageViewHolder> {

    private ActivityMentorChatItemBinding binding;

        public interface Listener {
            void onDataChanged();
        }

        //FOR DATA
        private final RequestManager glide;
        private final String idCurrentUser;

        //FOR COMMUNICATION
        private Listener callback;

    //public MessageViewHolder(ActivityMentorChatItemBinding b) {
       // super(b.getRoot());

        public MentorChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, RequestManager glide, Listener callback, String idCurrentUser) {
            super(options);
            this.glide = glide;
            this.callback = callback;
            this.idCurrentUser = idCurrentUser;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
            holder.updateWithMessage(model, this.idCurrentUser, this.glide);
        }

    /*@Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder( ActivityMentorChatItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                 parent , false));
    }*/

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_mentor_chat_item, parent, false));
    }

        @Override
        public void onDataChanged() {
            super.onDataChanged();
            this.callback.onDataChanged();
        }

}
