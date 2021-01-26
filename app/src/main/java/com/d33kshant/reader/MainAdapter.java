package com.d33kshant.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    ArrayList<ItemRow> mList;
    onItemClickListener mListener;

    public void setOnItemClickListener(onItemClickListener listener){
        mListener = listener;
    }

    public interface onItemClickListener{
        void onCardClicked(int position);
        void onDeleteClicked(int position);
        void onShareClicked(int position);
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder{
        ImageView ImagePrev;
        ImageButton share, delete;
        TextView Title, Info;
        public MainViewHolder(@NonNull View itemView, onItemClickListener listener) {
            super(itemView);
            ImagePrev = itemView.findViewById(R.id.item_prev);
            Title = itemView.findViewById(R.id.item_title);
            Info = itemView.findViewById(R.id.item_info);
            share = itemView.findViewById(R.id.share_item);
            delete = itemView.findViewById(R.id.delete_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onCardClicked(position);
                        }
                    }
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onShareClicked(position);
                        }
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDeleteClicked(position);
                        }
                    }
                }
            });
        }
    }

    public MainAdapter(ArrayList<ItemRow> list) {
        mList = list;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_row, parent,false);
        return new MainViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        ItemRow current = mList.get(position);
        holder.Title.setText(current.getTittle());
        holder.Info.setText(current.getInfo());
        holder.ImagePrev.setImageBitmap(current.getBitmap());
        //holder.ImagePrev.setImageURI();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
