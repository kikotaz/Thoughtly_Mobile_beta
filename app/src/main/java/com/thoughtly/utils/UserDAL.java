package com.thoughtly.utils;

import android.content.Context;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/* @ClassName: UserDAL
 * @Description: This class will Represent the Data Access Layer for the User
 * object, it will act as a middle tier between the database and the user interface
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class UserDAL {
    private String hostAddress;
    private static UserDAL instance = null;
    private String queryStatus;
    private static Context context;

    //Constructor for Data Access Layer to initialize the host address
    //It will also initialize the User object that will be used across the app life cycle
    public UserDAL(Context _context) {
        hostAddress = "https://thoughlty-dev-app.azurewebsites.net/";
        queryStatus = "";
        context = _context;
    }

    //Following Singleton pattern to create single instance of the Data Access Layer
    public static UserDAL getInstance(Context context) {
        if (instance == null) {
            instance = new UserDAL(context);
        }

        return instance;
    }

    //Function to insert new User using Thread
    public String insertNewUser(User user) {
        Runnable insertUser = new InsertUserThread(user);
        Thread insertUserThread = new Thread(insertUser);
        insertUserThread.start();
        try {
            insertUserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return queryStatus;
    }

    //Function to get User details using Thread
    public String getUser(String userMail, String password) {

        Thread getUserThread = new GetUserThread(userMail, password);
        getUserThread.start();
        try {
            getUserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queryStatus;
    }

    //Function to update a user using Thread
    public String updateUser(User user) {
        Thread updateUserThread = new UpdateUserThread(user);
        updateUserThread.start();
        try {
            updateUserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return queryStatus;
    }

    //Runnable class to insert New User
    private class InsertUserThread implements Runnable {

        User newUser;
        public InsertUserThread(User user) {
            this.newUser = user;
        }

        @Override
        public void run() {
            StringBuilder builder = new StringBuilder();

            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "createUser.php");
                URLConnection connection = requestUrl.openConnection();

                //GSON library made by Google to convert objects to JSON
                Gson gson = new Gson();
                //Converting the User object to JSON String
                String newUserJson = gson.toJson(newUser);
                //Sending JSON String in request body
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(newUserJson);
                writer.flush();
                //Reading response from server
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = "";
                while ((response = reader.readLine()) != null) {
                    builder.append(response);
                }
            } catch (MalformedURLException urlEx) {
                CustomMessageBox box = new CustomMessageBox("Create user error",
                        "There was an error while creating the user. Please try again later",
                        context);
                box.showMessage();

            } catch (IOException ioException) {
                CustomMessageBox box = new CustomMessageBox("Create user error",
                        "There was an error while creating the user. Please try again later",
                        context);
                box.showMessage();
            }
            //Returning the response
            queryStatus = builder.toString();
        }
    }


    //Thread class to get User details
    private class GetUserThread extends Thread {

        private String userMail;
        private String password;

        //Constructor to pass the user Email and Password to the thread
        public GetUserThread(String _userMail, String _password) {
            userMail = _userMail;
            password = _password;
        }

        public void run() {
            StringBuilder builder = new StringBuilder();
            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "getUser.php");
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                String parameters = "userMail=" + userMail + "&password=" + password;

                //Sending parameters in POST request
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(parameters);
                writer.flush();
                //Reading response from server
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = "";
                while ((response = reader.readLine()) != null) {
                    builder.append(response);
                }
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }
            //Returning the response
            queryStatus = builder.toString();
        }
    }

    //Thread class to update User
    private class UpdateUserThread extends Thread {

        User updatedUser;

        public UpdateUserThread(User user) {
            this.updatedUser = user;
        }

        public void run() {
            StringBuilder builder = new StringBuilder();

            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "updateUser.php");
                URLConnection connection = requestUrl.openConnection();

                //GSON library made by Google to convert objects to JSON
                Gson gson = new Gson();
                //Converting the User object to JSON String
                String updatedUserJson = gson.toJson(updatedUser);
                //Sending JSON String in request body
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(updatedUserJson);
                writer.flush();
                //Reading response from server
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = "";
                while ((response = reader.readLine()) != null) {
                    builder.append(response);
                }
            } catch (MalformedURLException urlEx) {
                CustomMessageBox box = new CustomMessageBox("Create user error",
                        "There was an error while creating the user. Please try again later",
                        context);
                box.showMessage();

            } catch (IOException ioException) {
                CustomMessageBox box = new CustomMessageBox("Create user error",
                        "There was an error while creating the user. Please try again later",
                        context);
                box.showMessage();
            }
            //Returning the response
            queryStatus = builder.toString();
        }
    }
}

