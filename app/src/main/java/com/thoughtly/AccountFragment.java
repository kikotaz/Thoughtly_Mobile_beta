package com.thoughtly;


import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.thoughtly.utils.CustomMessageBox;
import com.thoughtly.utils.User;
import com.thoughtly.utils.UserDAL;
import com.thoughtly.utils.Validator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
/*
 * @ClassName: AccountFragment
 * @Description: This class is the Fragment for User Account information. It will be
 * activated on the click of Account button in the Bottom Navigation Bar. This fragment will
 * show User name, email, and birthdate. It will allow the user to edit his name, email
 * and password as well.
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 21/07/2019
 */
public class AccountFragment extends Fragment {

    private ViewSwitcher userNameTextSwitcher;
    private ViewSwitcher emailTextSwitcher;
    private ViewSwitcher passwordTextSwitcher;
    private TextView userNameAccountText;
    private EditText userNameAccountEdit;
    private TextView emailAccountText;
    private EditText emailAccountEdit;
    private TextView passwordAccountText;
    private EditText passwordAccountEdit;
    private TextView dobAccountText;
    private Button editAccountButton;
    private Button logoutAccountButton;
    private UserDAL userDAL;
    private User updatedUser;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressBar editProgress;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        //Initializing User data access layer
        userDAL = UserDAL.getInstance(getContext());
        //Initializing user session
        preferences = getContext().getSharedPreferences(
                "prefs", 0);
        editor = preferences.edit();

        //Initializing TextViews and TextEdits and ViewSwitchers
        userNameTextSwitcher = (ViewSwitcher)view.findViewById(R.id.userNameTextSwitcher);
        emailTextSwitcher = (ViewSwitcher)view.findViewById(R.id.emailTextSwitcher);
        passwordTextSwitcher = (ViewSwitcher)view.findViewById(R.id.passwordTextSwitcher);
        userNameAccountText = (TextView) view.findViewById(R.id.userNameAccountText);
        emailAccountText = (TextView) view.findViewById(R.id.emailAccountText);
        dobAccountText = (TextView) view.findViewById(R.id.dobAccountText);
        passwordAccountText = (TextView)view.findViewById(R.id.passwordAccountText);
        userNameAccountEdit = (EditText) view.findViewById(R.id.userNameAccountEdit);
        emailAccountEdit = (EditText) view.findViewById(R.id.emailAccountEdit);
        passwordAccountEdit = (EditText) view.findViewById(R.id.passwordAccountEdit);

        //Getting user details from Session into TextViews
        userNameAccountText.setText(preferences.getString("userName",""));
        emailAccountText.setText(preferences.getString("userMail", ""));
        dobAccountText.setText(preferences.getString("userDOB", ""));

        //Initializing edit button
        editAccountButton = (Button)view.findViewById(R.id.editAccountButton);
        editAccountButton.setOnClickListener(new OnEditButtonClick());
        logoutAccountButton = (Button)view.findViewById(R.id.logoutAccountButton);
        logoutAccountButton.setOnClickListener(new OnLogoutButtonClick());

        //Initializing Edit Progress Bar
        editProgress = (ProgressBar)view.findViewById(R.id.editProgress);
        editProgress.setVisibility(View.INVISIBLE);
        return view;
    }

    //This class will handle editing the account information
    private class OnEditButtonClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(editAccountButton.getText().equals("Edit")){
                userNameAccountEdit.setText(userNameAccountText.getText());
                emailAccountEdit.setText(emailAccountText.getText());
                userNameTextSwitcher.showNext();
                emailTextSwitcher.showNext();
                passwordTextSwitcher.showNext();
                editAccountButton.setText("Done");
            }
            else{
                //Creating list of fields to use in Validation
                ArrayList<EditText> editFields = new ArrayList<>();
                editFields.add(userNameAccountEdit);
                editFields.add(emailAccountEdit);
                //Validating through custom validator
                Validator validator = new Validator();
                try{
                    validator.isEmpty(editFields);
                }
                catch (IllegalArgumentException iEx){
                    int fieldId = Integer.parseInt(iEx.getMessage());
                    editFields.get(fieldId).setError("Field can't be empty");
                    editFields.get(fieldId).requestFocus();
                    return;
                }

                try{
                    validator.checkValidEmail(emailAccountEdit);
                }
                catch (FormatException fEx){
                    emailAccountEdit.setError("Please provide valid e-mail", null);
                    emailAccountEdit.requestFocus();
                    return;
                }
                //Check if the password field is chanegd
                if(!passwordAccountEdit.getText().toString().equals("")){
                    try{
                        validator.checkValidPassword(passwordAccountEdit);
                    }
                    catch (FormatException fEx){
                        passwordAccountEdit.setError("Must be 8:12, have 1 letter, 1 lowercase, 1 uppercase, " +
                                "and 1 special character");
                        passwordAccountEdit.requestFocus();
                        return;
                    }
                }

                EditUserTask editUserTask = new EditUserTask();
                editUserTask.execute();
            }
        }
    }

    //This class will handle logging out from account
    private class OnLogoutButtonClick implements View.OnClickListener{
        //Delete all session data and navigate to LoginActivity
        @Override
        public void onClick(View v) {
            editor.remove("userId");
            editor.remove("userName");
            editor.remove("userMail");
            editor.remove("userDOB");
            editor.commit();

            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    //Task that will handle editing the user details in the database
    private class EditUserTask extends AsyncTask<Void, Void, String>{

        //Execute before task starts
        @Override
        protected void onPreExecute() {
            editAccountButton.setClickable(false);
            logoutAccountButton.setClickable(false);
            editProgress.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... voids) {
            updatedUser = new User();
            updatedUser.userId = UUID.fromString(preferences.getString("userId", ""));
            updatedUser.userName = userNameAccountEdit.getText().toString();
            updatedUser.userMail = emailAccountEdit.getText().toString();
            updatedUser.password = passwordAccountEdit.getText().toString();
            String queryStatus = userDAL.updateUser(updatedUser);
            return queryStatus;
        }
        //After executing the task
        @Override
        protected void onPostExecute(String queryStatus) {
            super.onPostExecute(queryStatus);
            //If the query is successful, update the User state
            try{
                if(queryStatus.equals("true")){
                    editor.putString("userName", userNameAccountEdit.getText().toString());
                    editor.putString("userMail", emailAccountEdit.getText().toString());
                    editor.apply();
                    userNameAccountText.setText(userNameAccountEdit.getText().toString());
                    emailAccountText.setText(emailAccountEdit.getText().toString());
                    passwordAccountText.setText("");
                }
                else {throw new IllegalStateException();}
            }
            catch (IllegalStateException ilsEx){
                CustomMessageBox box = new CustomMessageBox("Updating user error",
                        "There was an error while updating user, please try again",
                        getContext());
                box.showMessage();
            }
            userNameTextSwitcher.showNext();
            emailTextSwitcher.showNext();
            passwordTextSwitcher.showNext();
            editAccountButton.setText("Edit");
            editAccountButton.setClickable(true);
            logoutAccountButton.setClickable(true);
            editProgress.setVisibility(View.INVISIBLE);
        }
    }
}
