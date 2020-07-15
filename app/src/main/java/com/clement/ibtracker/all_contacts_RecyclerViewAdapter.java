package com.clement.ibtracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class all_contacts_RecyclerViewAdapter extends RecyclerView.Adapter<all_contacts_RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "all_contacts_RVA";


    private ArrayList<all_contacts.Contact_info> mContact_info = new ArrayList<>(); //contact info
    private Context mContext;

    public all_contacts_RecyclerViewAdapter(Context Context, ArrayList<all_contacts.Contact_info> all_contacts_info) {
        this.mContext = Context;
        this.mContact_info = all_contacts_info;
    }

    @NonNull
    @Override
    public all_contacts_RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "ViewHolder OnCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_all_contacts, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"OnBindViewHolder called - new item added to Recycler View (all contacts)");
        Log.d(TAG,"mContactInfo array =" + mContact_info);
        holder.uuid.setText(mContact_info.get(position).UUID);
        holder.Total_ContactLength.setText(mContact_info.get(position).TimeInContact.toString() + "minutes"); //setText only accepts string
        holder.LastContactDate.setText(mContact_info.get(position).Date_time_last_Contact);
        /*holder.parent_layout.setOnClickListener(new View.OnClickListener() { //todo:add back later since it just says invoke virtual method and I have no time to solve it right now July 14
            @Override
            public void onClick(View view){
                Log.d(TAG,"on Click: cliked on" + mContact_info.get(position));
                Toast.makeText(mContext,mContact_info.get(position).UUID,Toast.LENGTH_LONG).show();
            }
        });*/

    }


    @Override
    public int getItemCount() {
        return mContact_info.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView uuid;
        ConstraintLayout parent_layout;
        TextView LastContactDate;
        TextView Total_ContactLength;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            uuid = itemView.findViewById(R.id.widget_all_contacts_uuid);
            LastContactDate = itemView.findViewById(R.id.widget_all_contacts_last_date_in_contact);
            Total_ContactLength = itemView.findViewById(R.id.widget_all_contacts_contact_length);
            parent_layout = itemView.findViewById(R.id.all_contacts_parent_layout);
        }
    }
}
