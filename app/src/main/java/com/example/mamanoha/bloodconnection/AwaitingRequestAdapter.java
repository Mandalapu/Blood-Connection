package com.example.mamanoha.bloodconnection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mamanoha.bloodconnection.DataObjects.Acceptor;
import com.example.mamanoha.bloodconnection.DataObjects.AwaitingRequest;

import java.util.List;

/**
 * Created by Manu on 11/28/2016.
 */

public class AwaitingRequestAdapter extends BaseAdapter
{

    private Activity activity;
    private BtnClickListener listener;
    private List<AwaitingRequest> requests;
    private static LayoutInflater inflater = null;
    private final String TAG = AwaitingRequestAdapter.this.getClass().getSimpleName();

    public AwaitingRequestAdapter(Activity activity, List<AwaitingRequest> requests, BtnClickListener listener ) {
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
                vi = inflater.inflate(R.layout.awaitingrequests_listitem, viewGroup, false);

            // TODO: 11/27/2016 Need to retrieve proper fields and set the values to them.  Must be careful with the R.id elements.
            //Set the fields into the listview.
            if (position < requests.size()) {
                AwaitingRequest request = requests.get(position);
                TextView name = (TextView) vi.findViewById(R.id.awaitingrequests_requesternamevalue);
                TextView distance = (TextView) vi.findViewById(R.id.awaitingrequests_distancevalue);
                TextView bloodGroup = (TextView) vi.findViewById(R.id.awaitingrequests_bloodgroupvalue);
                RatingBar emergency = (RatingBar) vi.findViewById(R.id.awaitingrequests_emergency_value);
                Button acceptToRequest = (Button) vi.findViewById(R.id.awaitingrequests_acceptButton);
                Button declineToRequest = (Button) vi.findViewById(R.id.awaitingrequests_declineButton);
                //Button connectButton = (Button) vi.findViewById(R.id.requestinfo_connectbutton);
                Log.d(TAG, "Should get all the references at this point");
                String distancemiles = request.getDistance() + " mi";
                //Chnage the name and append both the firstName and lastName, may be in the AwaitingAdapter;
                name.setText(request.getRequesterName());
                distance.setText(distancemiles);
                bloodGroup.setText(request.getRequestBloodGroup());
                emergency.setRating((float) request.getEmergencyLevel());
                //onClickListener for accept button
                acceptToRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onBtnClickWithStatus(position, "Accept");
                    }
                });
                //onClickListener for decline button inside the list item.
                declineToRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onBtnClickWithStatus(position, "Decline");
                    }
                });
                Log.d(TAG, "All the corresponding fields are set into the listview");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception occurred while constructing the list view");
        }
        return vi;
    }
}
