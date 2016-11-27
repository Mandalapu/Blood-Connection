package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {


    RegistrationData registerData = new RegistrationData();
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText confirm_password;
    private EditText phone;
    private EditText dob;
    private Spinner bloodSpinner;
    private RadioGroup genderGroup;
    private String urls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        password = (EditText) findViewById(R.id.password);
        confirm_password = (EditText) findViewById(R.id.confirmpasscode);
        email = (EditText) findViewById(R.id.username);
        phone = (EditText) findViewById(R.id.phone);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        genderGroup = (RadioGroup) findViewById(R.id.gendergroup);
        dob = (EditText) findViewById(R.id.dob);
        bloodSpinner = (Spinner) findViewById(R.id.bloodspinner);

        final Button register = (Button) findViewById(R.id.Register);
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if (firstName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Firstname can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!emailCheck(email.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Email Address invalid.", Toast.LENGTH_SHORT).show();
                    email.setText("");
                    return;
                }

                if (password.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                    password.setText("");
                    confirm_password.setText("");
                    return;
                }
                if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    password.setText("");
                    confirm_password.setText("");
                    return;
                }

                String phoneNumber = phone.getText().toString();
                if( phoneNumber.equals("") ) {
                    Toast.makeText(getApplicationContext(), "Mobile number can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( !phoneFormCheck(phoneNumber) ) {
                    Toast.makeText(getApplicationContext(), "Mobile number invalid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                    Date date = (Date)formatter.parse(dob.getText().toString());
                    registerData.setDob(date);
                    // TODO: 11/10/2016 setting this value just to make the data base tuple values non-null.
                    registerData.setNextAvailableDate(date);
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Please select your Date of Birth.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (genderGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(), "Must select a gender.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if( bloodSpinner.getSelectedItem().toString().equals("Blood Group") ){
                    Toast.makeText(getApplicationContext(), "Please select a blood group.", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerData.setFirstName(firstName.getText().toString());
                registerData.setLastName(lastName.getText().toString());
                registerData.setUserName(email.getText().toString());
                registerData.setPasscode(password.getText().toString());
                registerData.setPhone(phone.getText().toString());
                registerData.setBloodGroup(bloodSpinner.getSelectedItem().toString());
                // TODO: 11/10/2016 make a call here to store the information in the back end.
                new RegisterUser().execute(registerData);
            }
        });

        final TextView login_link = (TextView) findViewById(R.id.loginlink);

        login_link.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });


    }

    private class RegisterUser extends AsyncTask<RegistrationData, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(RegistrationActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            //Constructing the query required to make a call to the service.
            urls = constructQuery();
            Log.d("Changed Query is:", urls);
            pdLoading.show();
        }

        private String constructQuery() {
            StringBuilder result = new StringBuilder();
            result.append("http://192.168.42.76:8080/GiveAPint/registerUser?");
            try {

                result.append(URLEncoder.encode("firstName", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getFirstName(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("lastName", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getLastName(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("userName", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getUserName(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("passcode", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getPasscode(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("dob", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(registerData.getDob()), "UTF-8")).append("&")
                        .append(URLEncoder.encode("gender", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getGender(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("nextAvailableDate", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(registerData.getNextAvailableDate()), "UTF-8")).append("&")
                        .append(URLEncoder.encode("bloodGroup", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getBloodGroup(), "UTF-8")).append("&")
                        .append(URLEncoder.encode("phone", "UTF-8")).append("=").append(URLEncoder.encode(registerData.getPhone(), "UTF-8"));
            }
            catch(UnsupportedEncodingException e)
            {
                Log.d("Exception", "Exception occurred while encoding the URL");
                e.printStackTrace();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected JSONObject doInBackground(RegistrationData... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
           JSONObject resultObject = new JSONObject();
            try
            {
                Log.d("log", "before making the url call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("log", "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultObject = new JSONObject(result.toString());
                Log.d("Result", result.toString());
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                e.printStackTrace();
                Log.d("IOException",e.getMessage());
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception occurred while parsing the Json object");
            }
            catch( Exception e)
            {
                Log.d("Exception", "Normal exception occured  while making a call");
                e.printStackTrace();
                //Log.d("Exception", e.getLocalizedMessage());
            }
            finally
            {
                urlConnection.disconnect();
            }
            Log.d("log","about to return the result");
            //Log.d("log", resultArray.toString());
            return resultObject;
            //return;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            pdLoading.setMessage("call success to the server");
            // TODO: 11/10/2016 need to check if the registration is successful.
            try
            {
                Log.d("OnPostExecute", "came inside the onPostExecute");
                if( !(result.getString("error").equals("")) )
                {
                //If the error string is set in the result. Toast display the error message and then do not propogate
                    // into the next intent.
                    Toast.makeText(getApplicationContext(), "Error: " +result.getString("error"), Toast.LENGTH_SHORT).show();
                }
                else {
                    //TODO Also need to store the information in the shared preferences which will be used by further requests.
                    Log.d("postexecute", result.toString());
                    Log.d("Shared Preferences", "Witing to shared preferences");
                    //Writing to a default file location.
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    //Storing only essential information.
                    editor.putString("userName", result.getString("userName"));
                    editor.putString("firstName", result.getString("firstName"));
                    editor.putString("lastName", result.getString("lastName"));
                    editor.putString("gender", result.getString("gender"));
                    editor.putString("dob", String.valueOf(result.get("dob")));
                    editor.putInt("userId", result.getInt("userID"));
                    editor.putString("passcode", result.getString("passcode"));
                    editor.putString("phone", result.getString("phone"));
                    //commit the changes.
                    //In future calls, we nned to update the key-value pairs keeping the above key values in mind.
                    editor.apply();
                    Log.d("Shared Preferences", "Successfully stored data into the shared preferences");
                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while parsing the Json result");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            //this method will be running on UI thread
            pdLoading.dismiss();
        }

    }


    /**
     * Get the date being selected.
     * @param v
     */
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragmentActivity();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onRadioButtonClicked(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        switch (v.getId()) {
            case R.id.male:
                if (checked)
                    registerData.setGender("Male");
                break;
            case R.id.female:
                if (checked)
                    registerData.setGender("Female");
                break;
            case R.id.other:
                if (checked)
                    registerData.setGender("Other");
            default:
                registerData.setGender("");
        }
    }

    public boolean emailCheck(String email) {

        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean phoneFormCheck(String phone) {
        String phonePattern = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}

/**
 * This class is only used for sending as parameter to AsyncTask. Once the call to service is successful, we need to store
 * values in the shared preferences. This class is potentially used only once when user if first registering for the App.
 */
class RegistrationData {

    private String firstName;
    private String lastName;
    private String userName;
    private String passcode;
    private int userid;
    private Date dob;
    private String gender;
    private String bloodGroup;
    private String phone;
    private Date nextAvailableDate;

    public String getFirstName()
    {
        return this.firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getUserName()
    {
        return this.userName;
    }

    public void setUserName(String email)
    {
        this.userName = email;
    }

    public String getPasscode()
    {
        return this.passcode;
    }

    public void setPasscode(String passcode)
    {
        this.passcode= passcode;
    }

    public int getUserid()
    {
        return this.userid;
    }

    public void setUserid(int userid)
    {
        this.userid = userid;
    }

    public Date getDob()
    {
        return this.dob;
    }

    public void setDob(Date dob)
    {
        this.dob = dob;
    }

    public String getGender()
    {
        return this.gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getBloodGroup()
    {
        return this.bloodGroup;
    }

    public void setBloodGroup(String bloodGroup)
    {
        this.bloodGroup = bloodGroup;
    }

    public String getPhone()
    {
        return this.phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Date getNextAvailableDate()
    {
        return this.nextAvailableDate;
    }

    public void setNextAvailableDate(Date nextAvailableDate)
    {
        this.nextAvailableDate = nextAvailableDate;
    }

}
