package com.thoughtly;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
/*
 * @ClassName: AboutFragment
 * @Description: This class is the Fragment for About Developer information. It will be
 * activated on the click of About button in the Bottom Navigation Bar. This fragment will
 * show developer name, his contact details, and AIS location map
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 25/07/2019
 */
public class AboutFragment extends Fragment implements OnMapReadyCallback {

    private Button callMeButton;
    private Button textMeButton;
    private Button visitAisButton;
    private MapView aboutMap;
    private GoogleMap googleMap;

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        //Initializing buttons
        callMeButton = (Button)view.findViewById(R.id.callMeButton);
        textMeButton = (Button)view.findViewById(R.id.textMeButton);
        visitAisButton = (Button)view.findViewById(R.id.visitAisButton);
        //Assigning OnClickListeners
        callMeButton.setOnClickListener(new OnCallMeClickListener());
        textMeButton.setOnClickListener(new OnTextMeClickListener());
        visitAisButton.setOnClickListener(new OnVisitAisClickListener());

        //Initializing Map componenets
        aboutMap = (MapView)view.findViewById(R.id.aboutMap);
        aboutMap.onCreate(savedInstanceState);
        aboutMap.getMapAsync(this);

        return view;
    }

    //This function will show the location of AIS on the map
    @Override
    public void onMapReady(GoogleMap contextMap) {
        googleMap = contextMap;

        LatLng coordinates = new LatLng(-36.8735295, 174.7208905);

        MarkerOptions marker = new MarkerOptions();
        marker.position(coordinates);
        marker.title("AIS Location");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates,17));
        Marker pinMarker = googleMap.addMarker(marker);
        pinMarker.showInfoWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        aboutMap.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        aboutMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        aboutMap.onStop();
    }
    @Override
    public void onPause() {
        aboutMap.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        aboutMap.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        aboutMap.onLowMemory();
    }

    //This class will start phone dialer with developer phone number
    private class OnCallMeClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:02041589374"));
            startActivity(callIntent);
        }
    }

    //This class will start SMS activity with developer phone number
    private class OnTextMeClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent textIntent = new Intent(Intent.ACTION_VIEW);
            textIntent.setData(Uri.parse("smsto:02041589374"));
            textIntent.putExtra("sms_body", "");

            if(textIntent.resolveActivity(getContext().getPackageManager()) != null){
                startActivity(textIntent);
            }
        }
    }

    //This class will start AIS website
    private class OnVisitAisClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String aisUrl = "https://www.ais.ac.nz/";

            Intent aisIntent = new Intent(Intent.ACTION_VIEW);
            aisIntent.setData(Uri.parse(aisUrl));
            startActivity(aisIntent);
        }
    }
}
