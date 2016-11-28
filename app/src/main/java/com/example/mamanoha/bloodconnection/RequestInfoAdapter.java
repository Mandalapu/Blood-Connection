package com.example.mamanoha.bloodconnection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.DataObjects.Acceptor;

import java.util.List;

/**
 * Created by Manu on 11/27/2016.
 */

public class RequestInfoAdapter extends BaseAdapter {
    private Activity activity;
    private BtnClickListener listener;
    private List<Acceptor> requests;
    private static LayoutInflater inflater = null;
    private final String TAG = RequestInfoAdapter.this.getClass().getSimpleName();

    public RequestInfoAdapter(Activity activity, List<Acceptor> requests, BtnClickListener listener ) {
        Log.d(TAG, "Atleast the adapter constructor is triggered");
        this.activity = activity;
        this.requests = requests;
        this.listener = listener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        if( requests.size() <= 0 )
            return 1;
        return requests.size();

    }

    @Override
    public Object getItem(int i)
    {
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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View vi = convertView;
        Log.d(TAG, "Came inside the getView method in the RequestInfo adapter");
        try {
            if (convertView == null)
                vi = inflater.inflate(R.layout.requestinfo_listitem, viewGroup, false);

            // TODO: 11/27/2016 Need to retrieve proper fields and set the values to them.  Must be careful with the R.id elements.
            //Set the fields into the listview.
            Acceptor acceptor = requests.get(position);
            TextView name = (TextView) vi.findViewById(R.id.requestinfo_acceptorname);
            TextView distance = (TextView) vi.findViewById(R.id.requestinfo_distancevalue);
            TextView age = (TextView) vi.findViewById(R.id.requestinfo_agevalue);
            TextView bloodGroup = (TextView)vi.findViewById(R.id.requestinfo_bloodgroupvalue);
            Button connectButton = (Button) vi.findViewById(R.id.requestinfo_connectbutton);
            Log.d(TAG, "Should get all the references at this point");

            name.setText(acceptor.getFirstName());
            String distanceInMiles = acceptor.getDistance() + " miles";
            distance.setText(distanceInMiles);
            age.setText(String.valueOf(acceptor.getAge()));
            bloodGroup.setText(acceptor.getBloodGroup());
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "made a function call to onBtnClick");
                    listener.onBtnClick(position);
                }
            });
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
