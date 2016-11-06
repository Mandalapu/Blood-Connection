package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button button1;
    TextView tv1;
    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button);
        tv1 = (TextView) findViewById(R.id.textView1);
        prgDialog = new ProgressDialog(this);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgDialog.setMessage("Please wait calling the local asyncmethod...");
                // Set Cancelable as False
                prgDialog.setCancelable(false);
                //Add logic here to make a Async call.
                Log.d("log", "before button clicked");
                new invokeWebService().execute();
                tv1.setVisibility(View.VISIBLE);
            }
        });
    }

    private class invokeWebService extends AsyncTask<Void, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            pdLoading.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String urls = "http://10.0.0.2:8080/GiveAPint/getAllUsers";
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            try
            {
                Log.d("log", "before making the url call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("log", "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
            catch(MalformedURLException e)
            {
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                Log.d("IOException",e.getMessage());
            }
            catch( Exception e)
            {
                Log.d("Exception",e.getMessage());
            }
            finally
            {
                urlConnection.disconnect();
            }
            Log.d("log","about to return the resul to postexceute");
            return result.toString();

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdLoading.setMessage("call success to the server");
            Log.d("postexecute", result);
            tv1.setText(result);
            //this method will be running on UI thread
            pdLoading.dismiss();
        }

    }
}
