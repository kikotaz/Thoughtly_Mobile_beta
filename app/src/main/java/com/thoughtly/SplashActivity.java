package com.thoughtly;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.thoughtly.utils.CustomMessageBox;
import com.thoughtly.utils.UserDAL;

import java.net.ConnectException;

/*
 * @ClassName: SplashActivity
 * @Description: This class is the splash Activity of Thoughtly. It will be
 * activated first thing at the application load. It will first check Internet
  * connection and show error if there is no Internet, then show simple animation for the
 * application name and will navigate to the MainActivity if user is logged in, or to
 * LoginActivity if the user is not registered. It wil use User DAL to verify if
 * user is registered or not.
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class SplashActivity extends AppCompatActivity {

    TextView splashText;
    UserDAL userDAL;

    public static final int allPermissionsCode = 0;
    String[] listOfPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Check Internet connection and show error if no connection available
        try{
            if(isOnline() == false){
                throw new ConnectException();
            }
        }
        catch (ConnectException cEx){
            CustomMessageBox box = new CustomMessageBox("No Internet connection detected",
                    "Thoughtly require Internet connection to work, please enable your" +
                            " Internet to avoid issues", this);
            box.showMessage();
            return;
        }

        //initializing splash text and User Data Access Layer
        splashText = (TextView) findViewById(R.id.splashSnapThought);
        userDAL = UserDAL.getInstance(this);

        if(!hasPermission(this, listOfPermissions)){
            ActivityCompat.requestPermissions(this, listOfPermissions, allPermissionsCode);
        }
        else{
            //If permission is granted before, start Splash animation
            Runnable splashAnimator = new SplashAnimator();
            new Handler().post(splashAnimator);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        int permissionIndex = 0;
        for(String permission : permissions){
            try{
                //Show the permission request if it is not granted earlier
                if(grantResults.length > 0 && grantResults[permissionIndex] == PackageManager.PERMISSION_DENIED){
                    throw new SecurityException();
                }
            }catch (SecurityException ex){
                //If user rejected to grant permission, show message and close
                AlertDialog dialog = new AlertDialog.Builder(SplashActivity.this).create();
                dialog.setTitle("Permission Required");
                dialog.setMessage("Snap Thought requires granting using camera, location," +
                        " and recording permissions. " +
                        "Please restart the application and grant the camera, location," +
                        " or recording permission.");
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                });
                dialog.show();
            }finally {
                //If all permissions are granted, start the splash screen animation
                if(permissionIndex == 5 && grantResults[permissionIndex] == PackageManager.PERMISSION_GRANTED){
                    Runnable splashAnimator = new SplashAnimator();
                    new Handler().post(splashAnimator);
                }
            }
            permissionIndex++;
        }
    }

    //This function will check the list of permissions required if already granted or not
    public static boolean hasPermission(Context context, String[] permissions){
        if(context != null && permissions != null){
            for (String permission : permissions){
                if (ActivityCompat.checkSelfPermission(context,permission) !=
                PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    //This method will check Internet connectivity through Connectivity Manager
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    //Splash animation implementing Runnable
    class SplashAnimator implements Runnable {
        @Override
        public void run() {

            splashText.setAlpha(0f);
            splashText.setVisibility(View.VISIBLE);

            //Make animation for Snap Thought welcome text and start MainActivity
            splashText.animate().alpha(1f).setDuration(2000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //Check if user already logged in (from session) open MainActivity
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                            "prefs", 0);
                    if(preferences.contains("userId")){
                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                    else{
                        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                    }
                }
            });
        }
    }
}
