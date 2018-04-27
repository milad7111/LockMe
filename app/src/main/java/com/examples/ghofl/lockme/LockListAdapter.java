package com.examples.ghofl.lockme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by family on 4/25/2018.
 */

public class LockListAdapter extends RecyclerView.Adapter<LockListAdapter.ViewHolder> {

    private List<JSONObject> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    LockListAdapter(Context context, List<JSONObject> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public LockListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_lock_list_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LockListAdapter.ViewHolder holder, int position) {
        boolean connection_status = false;
        boolean lock_status = false;
        String lock_name = "my_lock";
        boolean admin_status= true;
        try {
            connection_status = mData.get(position).getBoolean(Utilities.TABLE_LOCK_COLUMN_CONNECTION_STATUS);
            lock_status = mData.get(position).getBoolean(Utilities.TABLE_LOCK_COLUMN_LOCK_STATUS);
            admin_status = mData.get(position).getBoolean(Utilities.TABLE_USER_LOCK_COLUMN_ADMIN_STATUS);
            lock_name =  mData.get(position).getString(Utilities.TABLE_USER_LOCK_COLUMN_LOCK_NAME);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder._img_connection_status_list.setImageResource((connection_status)? R.drawable.ic_cloud_done:R.drawable.ic_cloud_off_black_24dp );
        holder._img_connection_status_list.setColorFilter((connection_status)? android.R.color.holo_green_dark:android.R.color.holo_red_dark);
        holder._txv_lock_name.setText(lock_name);
        holder._txv_is_admin.setText((admin_status)? "you are admin" : "member");
        holder._img_lock_status_list.setImageResource((lock_status)? R.drawable.img_close_lock:R.drawable.img_open_lock);
        holder._img_lock_status_list.setColorFilter((lock_status)?android.R.color.holo_green_dark:android.R.color.holo_red_dark);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView _img_connection_status_list;
        TextView _txv_lock_name;
        TextView _txv_is_admin;
        ImageView _img_lock_status_list;


        ViewHolder(View itemView) {
            super(itemView);
//            myTextView = itemView.findViewById(R.id.tvAnimalName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    JSONObject getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
