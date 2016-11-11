package com.example.mamanoha.bloodconnection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class SigninActivity extends AppCompatActivity {
    private final String[] DUMMYCREDENTIALS = new String[]{"yerra@usc.edu:bloodconnection",
            "abhipardhu@gmail.com:passcode"};
    private EditText username;
    private EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        username = (EditText)findViewById(R.id.editText1);
        username.requestFocus();
        password = (EditText)findViewById(R.id.editText2);
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Boolean credentialCheck = attemptLogin(username.getText().toString(), password.getText().toString());
                if (credentialCheck == true) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Wrong Credentials. Please Check.", Toast.LENGTH_SHORT).show();
                    username.setText("");
                    password.setText("");
                    username.requestFocus();
                }
            }
        });

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        //Listening to Register link.
        registerScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * Checks the credentials for login. Currently, it checks with dummy credentials.
     * @param username
     * @param password
     */
    private Boolean attemptLogin(String username, String password) {
        return true;
        /**if (!username.contains("@")) {
            return false;
        }
        for (String credential : DUMMYCREDENTIALS) {
            String[] pieces = credential.split(":");
            Log.i("From SigninActivity", pieces[0] + "  " + pieces[1] + "\n" + username + " " + password);
            if (pieces[0].equals(username)) {
                // Account exists, return true if the password matches.
                return pieces[1].equals(password);
            }
        }
        return false;**/
    }

    @Override
    public void onBackPressed() {

    }
}
