package com.thoughtly;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thoughtly.utils.Thought;
import com.thoughtly.utils.ThoughtDAL;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
/*
 * @ClassName: HomeFragment
 * @Description: This class is the Fragment for Home page. It will be
 * activated by default at the beginning of the application and
 * on the click of Home button in the Bottom Navigation Bar. This fragment will
 * show the Thoughts gallery, where user can navigate throw Recycler view of Thoughts cards.
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class HomeFragment extends Fragment {

    ImageButton newThoughtButton;
    private RecyclerView thoughtsGallery;
    private RecyclerView.Adapter adapter;
    private static ArrayList<Thought> thoughtsObjects;
    ThoughtDAL thoughtDAL;
    private SharedPreferences preferences;
    ProgressBar thoughtsProgress;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Initializing new Thought Button and the Thoughts gallery
        newThoughtButton = (ImageButton) view.findViewById(R.id.newThoughtBtn);
        thoughtsGallery = (RecyclerView) view.findViewById(R.id.thoughtsGalleryView);
        thoughtsProgress = (ProgressBar) view.findViewById(R.id.thoughtsProgress);
        thoughtsProgress.setVisibility(View.INVISIBLE);

        //Setting onClickListener to add Thought Button
        newThoughtButton.setOnClickListener(new StartNewThought());

        //Configuring the Thoughts gallery
        thoughtsGallery.setHasFixedSize(true);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(thoughtsGallery);

        //Initializing thoughts adapter
        adapter = new ThoughtsGalleryAdapter(thoughtsObjects);
        thoughtsGallery.setAdapter(adapter);

        //Initializing user session
        preferences = getContext().getSharedPreferences(
                "prefs", 0);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializing new Thoughts paths ArrayList
        thoughtsObjects = new ArrayList<Thought>();
        //Initializing all the thoughts to be inflated to the RecyclerView
        thoughtDAL = ThoughtDAL.getInstance(getContext());
    }
    //Refreshing the list of Thoughts every time the fragment is resumed
    @Override
    public void onResume() {
        thoughtsProgress.setVisibility(View.VISIBLE);
        super.onResume();
        thoughtsObjects.clear();
        thoughtsObjects.addAll(thoughtDAL.
                getAllThoughts(preferences.getString("userId","")));
        thoughtsProgress.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

    //OnClickListener for new Thought button
    public class StartNewThought implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent newThoughtIntent = new Intent(getActivity(), ThoughtActivity.class);
            newThoughtIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(newThoughtIntent);
        }
    }

    public class ThoughtsGalleryAdapter extends RecyclerView.Adapter<ThoughtsGalleryAdapter.ThoughtsViewHolder> {

        private ArrayList<Thought> thoughts;
        Context context;

        //This class will keep track of all Thoughts after scrolling
        public class ThoughtsViewHolder extends RecyclerView.ViewHolder {
            public CardView thoughtCard;
            public ImageView thoughtCardImage;
            public TextView thoughtTitle;

            public ThoughtsViewHolder(View v) {
                super(v);
                thoughtCard = (CardView) v.findViewById(R.id.thoughtCard);
                thoughtCardImage = (ImageView) v.findViewById(R.id.thoughtCardImage);
                thoughtTitle = (TextView) v.findViewById(R.id.thoughtTitle);
            }
        }

        //Assigning the list of thoughts in the constructor
        public ThoughtsGalleryAdapter(ArrayList<Thought> data) {
            thoughts = data;
        }

        //Inflating Thoughts to cards in the ViewHolder
        @NonNull
        @Override
        public ThoughtsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.thought_card, viewGroup, false);

            ThoughtsViewHolder vh = new ThoughtsViewHolder(v);
            context = viewGroup.getContext();
            return vh;
        }

        //Binding the data of each Thought to the card
        @Override
        public void onBindViewHolder(@NonNull ThoughtsViewHolder thoughtsViewHolder, int i) {
            thoughtsViewHolder.thoughtCardImage.setImageURI(thoughts.get(i).imagePath);
            thoughtsViewHolder.thoughtTitle.setText(thoughts.get(i).title);
            //Assigning the index of the Thought to a tag to use in the OnClickListener
            thoughtsViewHolder.thoughtCard.setTag(i);

            thoughtsViewHolder.thoughtCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Passing the chosen Thought values to new activity
                    Thought chosenThought = thoughts.get((int) v.getTag());
                    Intent thoughtContentIntent = new Intent(getActivity(), ThougtContentActivity.class);
                    thoughtContentIntent.putExtra("id", chosenThought.thoughtId.toString());
                    thoughtContentIntent.putExtra("image", chosenThought.imagePath.getPath());
                    thoughtContentIntent.putExtra("recording", chosenThought.recordingPath.getPath());
                    thoughtContentIntent.putExtra("title", chosenThought.title);
                    thoughtContentIntent.putExtra("details", chosenThought.details);
                    startActivity(thoughtContentIntent);
                }
            });
        }

        //Getting the count of all thoughts
        @Override
        public int getItemCount() {
            return thoughts.size();
        }
    }
}
