package com.thoughtly.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/*
 * @ClassName: CustomMessageBox
 * @Description: This class will be responsible for creating a customized Message box using
 * AlertDialog and show the message to the user
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 23/07/2019
 */
public class CustomMessageBox {
    private String title;
    private String message;
    private Context context;
    //Constructor to assign the variables
    public CustomMessageBox(String _title, String _message, Context _context){
        title = _title;
        message = _message;
        context = _context;
    }
    //Method to show the message required
    public void showMessage(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.create().dismiss();
            }
        });
        alert.create().show();
    }
}
