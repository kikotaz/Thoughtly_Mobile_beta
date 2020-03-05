package com.thoughtly;

import android.content.Context;
import it.sephiroth.android.library.exif2.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thoughtly.utils.CustomMessageBox;
import com.thoughtly.utils.Thought;
import com.thoughtly.utils.ThoughtDAL;

import java.io.IOException;
import java.util.UUID;

/*
 * @ClassName: ThoughtContentActivity
 * @Description: This class is Activity to show the Thought content. It will be
 * activated after the user clicks on specific Thought card from the Thoughts gallery in
 * the HomeFragment. It will allow the user to change the Thought title, and add more details to
 * the Thought.
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 20/07/2019
 */
public class ThougtContentActivity extends FragmentActivity implements OnMapReadyCallback {

    private Thought chosenThought;
    private ImageButton playButton;
    private ImageButton titleEditButton;
    private ImageButton detailsEditButton;
    private boolean isPlaying = false;
    private MediaPlayer player;
    private ScrollView contentScroll;
    private TextView thoughtContentTitle;
    private EditText thoughtContentTitleEdit;
    private TextView thoughtContentDetails;
    private EditText thoughtContentDetailsEdit;
    private ViewSwitcher editTitleTextSwitcher;
    private ViewSwitcher editTitleImageSwitcher;
    private ViewSwitcher editDetailsTextSwitcher;
    private ViewSwitcher editDetailsImageSwitcher;
    private ThoughtDAL thoughtDAL;
    private SupportMapFragment thoughtMapFragment;
    private GoogleMap googleMap;
    private LatLng coordinates;
    private ProgressBar editThoughtProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thougt_content);

        chosenThought = new Thought();
        //Retrieving the chosen Thought from the intent
        chosenThought.thoughtId = UUID.fromString(getIntent().getStringExtra("id"));
        chosenThought.imagePath = Uri.parse(getIntent().getStringExtra("image"));
        chosenThought.recordingPath = Uri.parse(getIntent().getStringExtra("recording"));
        chosenThought.title = getIntent().getStringExtra("title");
        chosenThought.details = getIntent().getStringExtra("details");

        //Initializing content scroll view
        contentScroll = (ScrollView)findViewById(R.id.contentScroll);

        //Initializing Thought image
        ImageView thoughtContentImage = (ImageView) findViewById(R.id.thoughtContentImage);
        thoughtContentImage.setImageURI(chosenThought.imagePath);

        //Initializing Thought content title components
        titleEditButton = (ImageButton) findViewById(R.id.titleEditButton);
        titleEditButton.setOnClickListener(new OnEditTitleListener());
        ImageButton titleEditButtonDone = (ImageButton) findViewById(R.id.titleEditButtonDone);
        titleEditButtonDone.setOnClickListener(new OnEditTitleDoneListener());
        thoughtContentTitle = (TextView)findViewById(R.id.thoughtContentTitle);
        thoughtContentTitle.setText(chosenThought.title);
        thoughtContentTitleEdit = (EditText)findViewById(R.id.thoughtContentTitleEdit);

        //Initializing Thought content details components
        detailsEditButton = (ImageButton) findViewById(R.id.detailsEditButton);
        detailsEditButton.setOnClickListener(new OnEditDetailsListener());
        ImageButton detailsEditButtonDone = (ImageButton) findViewById(R.id.detailsEditButtonDone);
        detailsEditButtonDone.setOnClickListener(new OnEditDetailsDoneListener());
        thoughtContentDetails = (TextView)findViewById(R.id.thoughtContentDetails);
        thoughtContentDetailsEdit = (EditText)findViewById(R.id.thoughtContentDetailsEdit);

        //Checking if the details of the Thought is already edited or not
        if(chosenThought.details != null){
            thoughtContentDetails.setText(chosenThought.details);
        }

        //Initializing ViewSwitchers for editing the title
        editTitleImageSwitcher = (ViewSwitcher)findViewById(R.id.editTitleImageSwitcher);
        editTitleTextSwitcher = (ViewSwitcher)findViewById(R.id.editTitleTextSwitcher);

        //Initializing ViewSwitchers for editing the details
        editDetailsImageSwitcher = (ViewSwitcher)findViewById(R.id.editDetailsImageSwitcher);
        editDetailsTextSwitcher = (ViewSwitcher)findViewById(R.id.editDetailsTextSwitcher);

        //Initializing ProgressBar
        editThoughtProgress = (ProgressBar)findViewById(R.id.editThoughtProgress);
        editThoughtProgress.setVisibility(View.INVISIBLE);

        //Initializing Thought Data Access Layer
        thoughtDAL = ThoughtDAL.getInstance(getBaseContext());

        //Initializing stop and play buttons
        playButton = (ImageButton)findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            int length = 0;
            @Override
            public void onClick(View v) {

                if(!isPlaying){
                    playButton.setImageResource(R.drawable.ic_stop);
                    isPlaying = true;
                    player = MediaPlayer.create(ThougtContentActivity.this, chosenThought.recordingPath);
                    player.seekTo(length);
                    player.start();
                }
                else{
                    playButton.setImageResource(R.drawable.ic_play);
                    isPlaying = false;
                    player.pause();
                    length = player.getCurrentPosition();
                }

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playButton.setImageResource(R.drawable.ic_play);
                        isPlaying = false;
                        length = 0;
                    }
                });
            }
        });

        //Initializing down button
        ImageButton downButton = (ImageButton)findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentScroll.smoothScrollTo(0, editDetailsImageSwitcher.getBottom() + 100);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Initializing the map fragment
        thoughtMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(
                R.id.contentMap);
        thoughtMapFragment.getMapAsync(this);

        try {
            ExifInterface exif = new ExifInterface();
            exif.readExif(chosenThought.imagePath.getPath(), ExifInterface.Options.OPTION_IFD_GPS);

            double[] gpsData = exif.getLatLongAsDoubles();

            if(gpsData == null){
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(thoughtMapFragment);
                transaction.commit();
                coordinates = new LatLng(0.0,0.0);
            }
            else{
                coordinates = new LatLng(gpsData[0], gpsData[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This method will update the Thought in the database with the final Thought state
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UpdateThoughtTask updateThoughtTask = new UpdateThoughtTask();
        updateThoughtTask.execute();
    }

    //This function will set the location marker on the map
    @Override
    public void onMapReady(GoogleMap contextMap) {
        googleMap = contextMap;

        MarkerOptions marker = new MarkerOptions();
        marker.position(coordinates);
        marker.title("Your thought was recorded here");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates,17));
        Marker pinMarker = googleMap.addMarker(marker);
        pinMarker.showInfoWindow();
    }

    //This class will handle title edit button click
    private class OnEditTitleListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            editTitleImageSwitcher.showNext();
            editTitleTextSwitcher.showNext();
            thoughtContentTitleEdit.requestFocus();
            InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            input.showSoftInput(thoughtContentTitleEdit, InputMethodManager.SHOW_IMPLICIT);
            thoughtContentTitleEdit.setText(thoughtContentTitle.getText());
        }
    }

    //This class will handle title edit done button click
    private class OnEditTitleDoneListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            String newTitle = thoughtContentTitleEdit.getText().toString();
            if(newTitle.matches("")){
                thoughtContentTitle.setText(chosenThought.title);
            }
            else{
                thoughtContentTitle.setText(thoughtContentTitleEdit.getText());
                chosenThought.title = newTitle;
            }
            editTitleImageSwitcher.showNext();
            editTitleTextSwitcher.showNext();
        }
    }

    //This class will handle details button click
    private class OnEditDetailsListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            editDetailsImageSwitcher.showNext();
            editDetailsTextSwitcher.showNext();
            thoughtContentDetailsEdit.requestFocus();
            InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            input.showSoftInput(thoughtContentDetailsEdit, InputMethodManager.SHOW_IMPLICIT);
            thoughtContentDetailsEdit.setText(thoughtContentDetails.getText());
        }
    }

    //This class will handle details done button click
    private class OnEditDetailsDoneListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String newDetails = thoughtContentDetailsEdit.getText().toString();
            if(newDetails.matches("")){
                thoughtContentDetails.setText(chosenThought.details);
            }
            else{
                thoughtContentDetails.setText(thoughtContentDetailsEdit.getText());
                chosenThought.details = newDetails;
            }
            editDetailsImageSwitcher.showNext();
            editDetailsTextSwitcher.showNext();
        }
    }

    private class UpdateThoughtTask extends AsyncTask<Void, Void, String>{

        //Execute before task starts
        @Override
        protected void onPreExecute() {
            titleEditButton.setClickable(false);
            detailsEditButton.setClickable(false);
            editThoughtProgress.setVisibility(View.VISIBLE);
        }
        //Executing the task
        @Override
        protected String doInBackground(Void... voids) {
            return thoughtDAL.updateThought(chosenThought);
        }
        //After executing the task
        protected void onPostExecute(String queryStatus) {
            super.onPostExecute(queryStatus);
            try{
                if(queryStatus.equals("true")){
                    finish();
                }
                else{
                    throw new IllegalStateException();
                }
            }catch (IllegalStateException ilsEx){
                CustomMessageBox box = new CustomMessageBox("Updating thought error",
                        "There was an error while updating thought, please try again",
                        ThougtContentActivity.this);
                box.showMessage();
            }
            titleEditButton.setClickable(true);
            detailsEditButton.setClickable(true);
            editThoughtProgress.setVisibility(View.INVISIBLE);
        }
    }
}
