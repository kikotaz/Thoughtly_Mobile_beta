package com.thoughtly.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/*
 * @ClassName: ThoughtDAL
 * @Description: This class will Represent the Data Access Layer for the Thought
 * object, it will act as a middle tier between the database and the user interface
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class ThoughtDAL {

    private String hostAddress;
    private static ThoughtDAL instance = null;
    private String queryStatus;
    private static ArrayList<Thought> allThoughts;
    private static Context context;

    //Constructor for Data Access Layer to pass the Context of the activity
    //It will also initialize list of Thoughts that will be used across the app life cycle
    public ThoughtDAL(Context _context) {
        hostAddress = "https://thoughlty-dev-app.azurewebsites.net/";
        queryStatus = "";
        allThoughts = new ArrayList<Thought>();
        context = _context;
    }

    //Following Singleton pattern to create single instance of the Data Access Layer
    public static ThoughtDAL getInstance(Context context) {
        if (instance == null) {
            instance = new ThoughtDAL(context);
        }
        return instance;
    }

    //Function to insert new Thought using Thread
    public String insertNewThought(Thought thought) {
        Runnable insertThought = new InsertThoughtThread(thought);
        Thread insertThoughtThread = new Thread(insertThought);
        insertThoughtThread.start();
        try {
            insertThoughtThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queryStatus;
    }

    //Function to get all Thoughts using Thread
    public ArrayList<Thought> getAllThoughts(String userId) {

        Thread getThoughtsThread = new GetThoughtsThread(userId);
        getThoughtsThread.start();
        try {
            getThoughtsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Creating list of Thoughts from JSON response
        if (!queryStatus.equals("\"false\"")) {
            try {
                JSONObject thoughtsJson = new JSONObject(queryStatus);
                JSONArray thoughtArray = thoughtsJson.getJSONArray("thoughts");
                for (int i = 0; i < thoughtArray.length(); i++) {
                    Thought newThought = new Thought();
                    newThought.thoughtId = UUID.fromString(thoughtArray.getJSONObject(i).
                            getString("thoughtId"));
                    newThought.imagePath = Uri.parse(thoughtArray.getJSONObject(i).
                            getString("imagePath"));
                    newThought.recordingPath = Uri.parse(thoughtArray.getJSONObject(i).
                            getString("recordingPath"));
                    newThought.title = thoughtArray.getJSONObject(i).getString("thoughtTitle");
                    newThought.details = thoughtArray.getJSONObject(i).getString("thoughtDetails");
                    //If the physical file of the image and recording are on desk add to allThoughts
                    File image = new File(newThought.imagePath.getPath());
                    File record = new File(newThought.recordingPath.getPath());
                    if (image.exists() && record.exists()) {
                        allThoughts.add(newThought);
                    }
                }
            } catch (JSONException e) {
                CustomMessageBox box = new CustomMessageBox("Fetching Thought error",
                        "There was an error while fetching thoughts. Please try again later",
                        context);
                box.showMessage();
            }
        }
        return allThoughts;
    }

    //Function to update a Thought using Thread
    public String updateThought(Thought thought) {
        Thread updateThoughtThread = new UpdateThougtThread(thought);
        updateThoughtThread.start();
        try {
            updateThoughtThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queryStatus;
    }

    //Thread class to get all Thoughts
    private class GetThoughtsThread extends Thread {

        private String userId;

        //Constructor to pass userId which all Thoughts are required to be retrieved
        public GetThoughtsThread(String _userId) {
            userId = _userId;
        }

        public void run() {
            allThoughts.clear();
            StringBuilder builder = new StringBuilder();
            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "getThoughts.php");
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                String parameters = "userId=" + userId;

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

    //Runnable class to insert New Thought
    private class InsertThoughtThread implements Runnable {

        Thought newThought;

        public InsertThoughtThread(Thought thought) {
            this.newThought = thought;
        }

        @Override
        public void run() {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            newThought.title = timeStamp;
            StringBuilder builder = new StringBuilder();

            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "createThought.php");
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                String parameters = "thoughtId=" + newThought.thoughtId.toString() +
                        "&imagePath=" + URLEncoder.encode(newThought.imagePath.getPath(), "UTF-8") +
                        "&recordingPath=" + URLEncoder.encode(newThought.recordingPath.getPath(), "UTF-8")
                        + "&thoughtTitle=" + newThought.title + "&thoughtDetails=" + newThought.details
                        + "&userId=" + newThought.userId;
                //Sending parameters String in request body
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
            } catch (MalformedURLException urlEx) {
                CustomMessageBox box = new CustomMessageBox("Create Thought error",
                        "There was an error while creating the Thought. Please try again later",
                        context);
                box.showMessage();

            } catch (IOException ioException) {
                CustomMessageBox box = new CustomMessageBox("Create Thought error",
                        "There was an error while creating the Thought. Please try again later",
                        context);
                box.showMessage();
            }
            //Returning the response
            queryStatus = builder.toString();
        }
    }

    //Thread class to update Thought
    private class UpdateThougtThread extends Thread {

        Thought updatedThought;

        public UpdateThougtThread(Thought thought) {
            this.updatedThought = thought;
        }

        public void run() {
            StringBuilder builder = new StringBuilder();

            try {
                //Creating URL that will carry the request and parameters
                URL requestUrl = new URL(hostAddress + "updateThought.php");
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                String parameters = "thoughtId=" + updatedThought.thoughtId.toString() +
                        "&thoughtTitle=" + updatedThought.title + "&thoughtDetails=" +
                        updatedThought.details;
                //Sending parameters String in request body
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
            } catch (MalformedURLException urlEx) {
                CustomMessageBox box = new CustomMessageBox("Update Thought error",
                        "There was an error while updating the Thought. Please try again later",
                        context);
                box.showMessage();

            } catch (IOException ioException) {
                CustomMessageBox box = new CustomMessageBox("Update Thought error",
                        "There was an error while updating the Thought. Please try again later",
                        context);
                box.showMessage();
            }
            //Returning the response
            queryStatus = builder.toString();
        }
    }
}

