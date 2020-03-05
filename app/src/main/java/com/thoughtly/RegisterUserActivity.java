package com.thoughtly;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.thoughtly.utils.User;
import com.thoughtly.utils.UserDAL;
import com.thoughtly.utils.Validator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/*
 * @ClassName: RegisterUserActivity
 * @Description: This class is Activity for registering new user of Thoughtly. It will be
 * activated after user click Register button in Login Activity if the user is not registered.
 * This Activity will have 4 EditText fields that will record user name, e-mail, date of birth,
 * and password and save it to the database using User data access layer after validation
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 18/07/2019
 */
public class RegisterUserActivity extends AppCompatActivity {

    private EditText userNameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private static EditText dobEdit;
    private Button startAppButton;
    private UserDAL userDAL;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //Initializing and hiding ProgressBar
        progressBar = (ProgressBar)findViewById(R.id.registerProgress);
        progressBar.setIndeterminate(true);//Creating new User object
        progressBar.setVisibility(View.GONE);

        //Initializing all edit text fields
        userNameEdit = (EditText)findViewById(R.id.userNameEdit);
        emailEdit = (EditText)findViewById(R.id.emailEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);
        dobEdit = (EditText)findViewById(R.id.dobEdit);

        //Setting OnClickListener for date of birth field to show the date picker
        dobEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(),"datePicker");
            }
        });

        //Initializing start button
        startAppButton = (Button)findViewById(R.id.startAppButton);
        startAppButton.setOnClickListener(new OnStartAppClickListener());

        //Getting instnace of User data access layer
        userDAL = UserDAL.getInstance(this);
    }

    //This class will be responsible for showing the date picker and getting the user input
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        final Calendar calendar = Calendar.getInstance();
        //Creating the dialog
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            //returning new instance of the picker
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "YYYY/MM/dd";
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

            dobEdit.setText(formatter.format(calendar.getTime()));
        }
    }

    //This class will handle adding new user on clicking start button
    private class OnStartAppClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            //Creating list of fields to use in Validation
            ArrayList<EditText> registerFields = new ArrayList<>();
            registerFields.add(userNameEdit);
            registerFields.add(emailEdit);
            registerFields.add(dobEdit);
            registerFields.add(passwordEdit);
            //Validating through custom validator
            Validator validator = new Validator();
            try{
                validator.isEmpty(registerFields);
            }
            catch (IllegalArgumentException iEx){
                int fieldId = Integer.parseInt(iEx.getMessage());
                registerFields.get(fieldId).setError("Field can't be empty");
                registerFields.get(fieldId).requestFocus();
                return;
            }

            try{
                validator.checkValidEmail(emailEdit);
            }
            catch (FormatException fEx){
                emailEdit.setError("Please provide valid e-mail", null);
                emailEdit.requestFocus();
                return;
            }

            try{
                validator.checkValidPassword(passwordEdit);
            }
            catch (FormatException fEx){
                passwordEdit.setError("Must be 8:12, have 1 letter, 1 lowercase, 1 uppercase, " +
                        "and 1 special character");
                passwordEdit.requestFocus();
                return;
            }
            //Executing Insert User task
            InsertUserTask insertUserTask = new InsertUserTask();
            insertUserTask.execute();
        }
    }

    private class InsertUserTask extends AsyncTask<Void, Void, String>{

        private User newUser;

        //Initializing new User object
        private InsertUserTask() {
            newUser = new User();
        }
        //Execute before the task starts
        @Override
        protected void onPreExecute() {
            //Disabling the Start button to avoid crash
            startAppButton.setClickable(false);
            //Showing the ProgressBar
            progressBar.setVisibility(View.VISIBLE);
        }
        //Executing task
        @Override
        protected String doInBackground(Void... voids) {
            //Assigning data to User object
            newUser.userId = UUID.randomUUID();
            newUser.userName = userNameEdit.getText().toString();
            newUser.userMail = emailEdit.getText().toString();
            newUser.userDOB = dobEdit.getText().toString();
            newUser.password = passwordEdit.getText().toString();

            return userDAL.insertNewUser(newUser);
        }
        //After executing the task
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //If user is inserted successfully, create session and start Main Activity
            if(s.equals("1")){
                SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                        "prefs", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", newUser.userId.toString());
                editor.putString("userMail", newUser.userMail);
                editor.putString("userName", newUser.userName);
                editor.putString("userDOB", newUser.userDOB);
                editor.commit();

                Intent mainIntent = new Intent(RegisterUserActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
            //If user is not inserted, Enable Start button to retry
            else {
                startAppButton.setClickable(true);
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
