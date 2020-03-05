package com.thoughtly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.FormatException;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thoughtly.utils.CustomMessageBox;
import com.thoughtly.utils.User;
import com.thoughtly.utils.UserDAL;
import com.thoughtly.utils.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.AccessControlException;
import java.util.ArrayList;

/*
 * @ClassName: LoginActivity
 * @Description: This class is Activity for handling Login for Thoughtly users. It will be
 * activated after the splash screen if user is not already logged in.
 * This Activity will have 2 EditText fields that will record e-mail, and
 * password, and authenticate the user against the database using User data access layer.
 * It will also allow users to navigate to RegisterActivity, if they wish to have new account
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 20/07/2019
 */
public class LoginActivity extends AppCompatActivity {

    private User user;
    private EditText emailEditLogin;
    private EditText passwordEditLogin;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar loginProgress;
    private UserDAL userDAL;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initializing all EditTExt fields
        emailEditLogin = (EditText) findViewById(R.id.emailEditLogin);
        passwordEditLogin = (EditText) findViewById(R.id.passwordEditLogin);
        //Initializing all Buttons
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        //Initializing and hiding ProgressBar
        loginProgress = (ProgressBar) findViewById(R.id.loginProgress);
        loginProgress.setIndeterminate(true);//Creating new User object
        loginProgress.setVisibility(View.GONE);
        //Setting OnClickListeners for buttons
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this,
                        RegisterUserActivity.class);
                startActivity(registerIntent);
            }
        });
        loginButton.setOnClickListener(new OnLoginClickListener());
        //Getting instance of User Data Access Layer
        userDAL = UserDAL.getInstance(this);
        //Initializing user session
        preferences = getApplicationContext().getSharedPreferences(
                "prefs", 0);
    }

    //This class will handle Login process when user clicks Login button
    private class OnLoginClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //Creating new list of Login Activity fields
            ArrayList<EditText> loginFields = new ArrayList<>();
            loginFields.add(emailEditLogin);
            loginFields.add(passwordEditLogin);

            //Validating through custom validator
            Validator validator = new Validator();
            try {
                validator.isEmpty(loginFields);
            } catch (IllegalArgumentException iEx) {
                int fieldId = Integer.parseInt(iEx.getMessage());
                loginFields.get(fieldId).setError("Field can't be empty");
                loginFields.get(fieldId).requestFocus();
                return;
            }
            try {
                validator.checkValidEmail(emailEditLogin);
            } catch (FormatException fEx) {
                emailEditLogin.setError("Please provide valid e-mail", null);
                emailEditLogin.requestFocus();
                return;
            }
            try {
                validator.checkValidPassword(passwordEditLogin);
            } catch (FormatException fEx) {
                passwordEditLogin.setError("Must be 8:12, have 1 letter, 1 lowercase, 1 uppercase, " +
                        "and 1 special character");
                passwordEditLogin.requestFocus();
                return;
            }

            GetUserTask getUserTask = new GetUserTask();
            getUserTask.execute();
        }
    }
    //Overriding onStart to check if there is active session, start MainActivity
    @Override
    protected void onStart() {
        super.onStart();
        if (preferences.getString("userMail", "").length() != 0){
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
        }
    }

    private class GetUserTask extends AsyncTask<Void, Void, String> {

        JSONObject requestedUser;

        //Execute before task starts
        @Override
        protected void onPreExecute() {
            loginButton.setClickable(false);
            registerButton.setClickable(false);
            loginProgress.setVisibility(View.VISIBLE);
        }

        //Execute Task
        @Override
        protected String doInBackground(Void... voids) {

            String queryStatus = userDAL.getUser(emailEditLogin.getText().toString(),
                    passwordEditLogin.getText().toString());
            return queryStatus;
        }

        //After executing the task
        @Override
        protected void onPostExecute(String queryStatus) {
            super.onPostExecute(queryStatus);
            //If user doesn't exist or wrong password, throw exceptions
            try {
                if (queryStatus.equals("\"false\"")) {
                    throw new Resources.NotFoundException();
                } else if (queryStatus.equals("\"Unauthorized\"")) {
                    throw new AccessControlException("Unauthorized");
                } else {
                    requestedUser = new JSONObject(queryStatus);
                    //Creating new User session and start MainActivity
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userId", requestedUser.getString("userId"));
                    editor.putString("userMail", requestedUser.getString("userMail"));
                    editor.putString("userName", requestedUser.getString("userName"));
                    editor.putString("userDOB", requestedUser.getString("userDOB"));
                    editor.commit();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                }
            } catch (Resources.NotFoundException nfEx) {
                CustomMessageBox box = new CustomMessageBox("User not found",
                        "The requested user is not found. Please check the e-mail provided",
                        LoginActivity.this);
                box.showMessage();
            } catch (AccessControlException acEx) {
                CustomMessageBox box = new CustomMessageBox("Wrong password",
                        "The password provided is not correct, please check the password",
                        LoginActivity.this);
                box.showMessage();
            } catch (JSONException e) {
                CustomMessageBox box = new CustomMessageBox("Wrong data retrieved",
                        "The retrieved data cannot be processed. Please contact developer",
                        LoginActivity.this);
                box.showMessage();
            }
            loginButton.setClickable(true);
            registerButton.setClickable(true);
            loginProgress.setVisibility(View.GONE);
        }
    }
}
