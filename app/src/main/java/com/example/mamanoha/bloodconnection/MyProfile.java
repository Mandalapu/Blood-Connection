package com.example.mamanoha.bloodconnection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by Manu on 11/28/2016.
 */
public class MyProfile extends AppCompatActivity {

    private SharedPreferences prefs;
    private Button submitButton;
    private EditText firstNameValue;
    private EditText lastNameValue;
    private EditText userNameValue;
    private EditText phoneValue;
    private Switch availabilty;
    private RadioButton male;
    private RadioButton female;
    private RadioButton other;
    private RadioGroup genderGroup;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        submitButton = (Button) findViewById(R.id.update);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String firstName = prefs.getString("firstName", "Sample User");
        String lastName = prefs.getString("lastName", "lastName");
        String userName = prefs.getString("userName", "userName");
        String phone = prefs.getString("phone", "4444444444");

        firstNameValue = (EditText) findViewById(R.id.firstName);
        lastNameValue = (EditText) findViewById(R.id.lastName);
        userNameValue = (EditText) findViewById(R.id.userName);
        phoneValue = (EditText) findViewById(R.id.phone);
        availabilty = (Switch) findViewById(R.id.availableswitch);
        male = (RadioButton) findViewById(R.id.profileMale);
        female = (RadioButton) findViewById(R.id.profileFemale);
        other = (RadioButton) findViewById(R.id.profileOther);
        genderGroup = (RadioGroup) findViewById(R.id.profileGendergroup);
        mSpinner = (Spinner) findViewById(R.id.profilebloodspinner);
        String gender = prefs.getString("gender", "other");
        firstNameValue.setText(firstName);
        lastNameValue.setText(lastName);
        userNameValue.setText(userName);
        phoneValue.setText(phone);
        availabilty.setChecked(true);
        Log.d("GENDER", gender);
        if(gender.equalsIgnoreCase("Male"))
        {
            male.setChecked(true);
        }
        else if(gender.equalsIgnoreCase("Female"))
        {
           female.setChecked(true);
        }
        else
        {
           other.setChecked(true);
        }

        // TODO: 12/1/2016 Need to set the value from the preference manager. For that we need to add a new field in the shared
        // preferences and retrive that value hgere to set the position
        String compareValue = "B+";
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.bloodgroups, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        if (!compareValue.equals(null)) {
            int spinnerPosition = adapter.getPosition(compareValue);
            mSpinner.setSelection(spinnerPosition);
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Response Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        //Feed information into the xml file and add OnclickListener  to the submit button.
        //Task1: feed the data into the text fields.
        // Task 2: add on clikc listener for the submit button.
    }

    @Override
    public void onBackPressed() {
        Log.d("OnBackButton", "Button pressed");
        Intent mapsAcitivy = new Intent(getApplicationContext(), MapsActivity.class );
        startActivity(mapsAcitivy);
        this.finish();
        return;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("back",""+1);
            this.finish();
            Log.d("OnBackButton", "Button pressed");
            Intent mapsAcitivy = new Intent(getApplicationContext(), MapsActivity.class );
            startActivity(mapsAcitivy);
        }
        return true;
    }
}

