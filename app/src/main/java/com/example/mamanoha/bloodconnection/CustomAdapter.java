package com.example.mamanoha.bloodconnection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mamanoha.bloodconnection.DataObjects.UserRequests;

import java.util.List;

/**
 * Created by Manu on 11/27/2016.
 */
public class CustomAdapter extends BaseAdapter{

    private Activity activity;
    private List<UserRequests> requests;
    private static LayoutInflater inflater = null;
    private final String TAG = CustomAdapter.this.getClass().getSimpleName();

    public CustomAdapter(Activity activity, List<UserRequests> requests ) {
        Log.d(TAG, "Atleast the adapter constructor is triggered");
        this.activity = activity;
        this.requests = requests;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        if( requests.size() <= 0 )
            return 1;
        return requests.size();

    }

    @Override
    public Object getItem(int i) {
        return requests.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Implement the logic to fill the data into the list view.
     * @param position
     * @param convertView
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View vi = convertView;
        Log.d(TAG, "Came inside the getView method in the custom adapter");
        try {
            if (convertView == null)
                vi = inflater.inflate(R.layout.requests_listitem, viewGroup, false);

            //Set the fields into the listview.
            UserRequests request = requests.get(position);
            TextView requestedDate = (TextView) vi.findViewById(R.id.myrequests_listitem_timestamp);
            TextView requestedBloodGroup = (TextView) vi.findViewById(R.id.myrequests_listitem_bloodgroup);
            TextView status = (TextView) vi.findViewById(R.id.myrequests_listitem_status_value);
            RatingBar emergency = (RatingBar) vi.findViewById(R.id.myrequests_listitem_emergency_value);
            Log.d(TAG, "Should get all the references at this point");

            //Set the values to the fields retrieved.
            requestedDate.setText(request.getRequestDate());
            emergency.setRating((float) request.getEmergencyLevel());
            requestedBloodGroup.setText(request.getBloodGroup());
            status.setText(request.getStatus());
            Log.d(TAG, "All the corresponding fields are set into the listview");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception occurred while constructing the list view");
        }
        return vi;
    }
}
