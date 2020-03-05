package com.thoughtly.utils;

import android.nfc.FormatException;
import android.service.autofill.RegexValidator;
import android.util.Patterns;
import android.widget.EditText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @ClassName: Validator
 * @Description: This class will be responsible for validating all data fields
 * across the application
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 23/07/2019
 */
public class Validator {
    //Empty constructor
    public Validator(){}

    //This method will check if any field is missing inside group of fields
    public void isEmpty(ArrayList<EditText> inputs){
        //Loop to check each field in the list if it is empty
        for (EditText field : inputs) {
            int fieldId = inputs.indexOf(field);
            if(field.getText().length() == 0){
                throw new IllegalArgumentException("" + fieldId);
            }
        }
    }

    //This method will check if the e-mail is valid e-mail
    public void checkValidEmail(EditText eMail) throws FormatException {
        if(!Patterns.EMAIL_ADDRESS.matcher(eMail.getText()).matches()){
            throw new FormatException();
        }
    }

    //This method will check if password is valid
    public void checkValidPassword(EditText password) throws FormatException {
        Pattern passwordPattern = Pattern.compile(
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        Matcher matcher = passwordPattern.matcher(password.getText());
        if(!matcher.matches()){
            throw new FormatException();
        }
    }
}
