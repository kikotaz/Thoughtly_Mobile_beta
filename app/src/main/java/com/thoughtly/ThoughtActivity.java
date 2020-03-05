package com.thoughtly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.thoughtly.utils.CustomMessageBox;
import com.thoughtly.utils.Thought;
import com.thoughtly.utils.ThoughtDAL;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/*
 * @ClassName: ThoughtActivity
 * @Description: This class is Activity for recording a new Thought. It will be
 * activated after user clicks + Button in the HomeFragment. It will allow user to snap an image
 * using the phone's camera,  record a bit about his thought, and will save it directly to database
 * using Thought DAL
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 22/07/2019
 */
public class ThoughtActivity extends AppCompatActivity {

    static final int RequestImageCapture = 1;
    Intent cameraIntent;
    ImageView thoughtBackground;
    ImageButton recordThoughtBtn;
    Chronometer recordTimer;
    MediaRecorder recorder;
    Thought newThought;
    ThoughtDAL thoughtDAL;
    ProgressBar insertThoughtProgress;
    ProgressBar thoughtsProgress;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thought);

        //Initializing new Thought and data access layer
        newThought = new Thought();
        thoughtDAL = ThoughtDAL.getInstance(getBaseContext());

        //Initializing new UUID for the Thought
        newThought.thoughtId = UUID.randomUUID();
        //Initializing user session and getting userId
        preferences = getApplicationContext().getSharedPreferences(
                "prefs", 0);
        newThought.userId = preferences.getString("userId","");
        newThought.details = "";

        thoughtBackground = (ImageView) findViewById(R.id.thoughtBackground);
        recordThoughtBtn = (ImageButton) findViewById(R.id.recordThoughtBtn);
        recordTimer = (Chronometer) findViewById(R.id.recordTimer);
        insertThoughtProgress = (ProgressBar)findViewById(R.id.insertThoughtProgress);
        insertThoughtProgress.setVisibility(View.INVISIBLE);

        //Starting Camera intent to snap a picture
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File thoughtFile = null;

            try {
                thoughtFile = createThoughtImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Saving the thought image to internal storage folder for SnapThought
            if (thoughtFile != null) {
                Uri thoughtUri = FileProvider.getUriForFile(this,
                        "com.thoughtly.provider", thoughtFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, thoughtUri);
                startActivityForResult(cameraIntent, RequestImageCapture);
            }
        }
    }

    private File createThoughtImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File thoughtImage = File.createTempFile(imageFileName, ".jpg", storageDir);

        newThought.imagePath = Uri.parse(thoughtImage.getAbsolutePath());
        //Returning the new Thought's image file to be used by Camera intent
        return thoughtImage;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Return back to MainActivity if the camera intent is closed
        if (resultCode != Activity.RESULT_OK) {
            finishActivity(RequestImageCapture);
            onBackPressed();
        } else {
            thoughtBackground.setImageURI(newThought.imagePath);

            recordThoughtBtn.setOnTouchListener(new RecordThoughtAudio());
        }
    }

    //Class that will define the behavior recordThoughtBtn
    public class RecordThoughtAudio implements View.OnTouchListener {
        //Handler for the timer UI
        Handler timerHandler;

        //Defining the behavior onTouch
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                //This case for starting to touch the button
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    return true;
                //This case for releasing the touch from the button
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    break;
            }

            return false;
        }

        private String createThoughtAudioFile() throws IOException {
            //Create a recording file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String recordFileName = "Audio_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
            File thoughtRecord = File.createTempFile(recordFileName, ".3gp", storageDir);

            //Saving the record path to save
            newThought.recordingPath = Uri.parse(thoughtRecord.getAbsolutePath());
            return thoughtRecord.getAbsolutePath();
        }

        //Start recording will start once record button touched
        private void startRecording() {
            //Starting the recorder time counter (stopwatch)
            recordTimer.setBase(SystemClock.elapsedRealtime());
            recordTimer.start();
            //Changing the record button background color while recording
            recordThoughtBtn.setBackgroundResource(R.drawable.border_transparent);
            //Starting record session
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                recorder.setOutputFile(createThoughtAudioFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Stop recording will start once record button released
        private void stopRecording() {
            //stopping the timer for recorder
            recordTimer.stop();
            //Changing recording button background color to normal
            recordThoughtBtn.setBackgroundResource(R.drawable.border);

            //Ending record session
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                //Starting insert Thought task
                InsertThoughtsTask insertThoughtsTask = new InsertThoughtsTask();
                insertThoughtsTask.execute();
            }
        }
    }

    private class InsertThoughtsTask extends AsyncTask<Void, Void, String> {
        //Execute before task starts
        @Override
        protected void onPreExecute() {
            insertThoughtProgress.setVisibility(View.VISIBLE);
        }
        //Execute task
        @Override
        protected String doInBackground(Void... voids) {
            return thoughtDAL.insertNewThought(newThought);
        }
        //After task is executed
        @Override
        protected void onPostExecute(String queryStatus) {
            super.onPostExecute(queryStatus);
            try{
                if(queryStatus.equals("false")){
                    throw new IllegalArgumentException();
                }
            }
            catch (IllegalArgumentException iaEx){
                CustomMessageBox box = new CustomMessageBox("Error inserting Thought",
                        "There was an error while inserting the new Thought.",
                        ThoughtActivity.this);
                box.showMessage();
            }
            insertThoughtProgress.setVisibility(View.GONE);
            finish();
        }
    }
}
