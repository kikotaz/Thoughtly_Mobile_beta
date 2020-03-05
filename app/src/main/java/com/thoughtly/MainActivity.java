package com.thoughtly;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/*
 * @ClassName: MainActivity
 * @Description: This class is the main Activity of Thoughtly. It will be
 * activated after the splash screen if the user login, or after the user registers
 * new User account. This Activity will have bottom navigation menu, which allows user
 * to navigate through the application Fragments.
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class MainActivity extends AppCompatActivity {
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing the main toolbar on top
        Toolbar mainToolBar = (Toolbar)findViewById(R.id.mainToolBar);
        setSupportActionBar(mainToolBar);
        setTitle("");

        //Handling fragments to be shown in the MainActivity
        final FragmentManager fragmentManager = getSupportFragmentManager();

        //Creating an active fragment that will change with different item chosen
        activeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.mainContentFragment, activeFragment).commit();

        //Initializing the bottom navigation menu
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationMenu);
        bottomNavigation.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment chosenFragment = new HomeFragment();

                switch (menuItem.getItemId()){
                    case R.id.mainButton :
                        chosenFragment = new HomeFragment();
                        break;
                    case R.id.accountButton :
                        chosenFragment = new AccountFragment();
                        break;
                    case R.id.reminderButton:
                        chosenFragment = new ReminderFragment();
                        break;
                    case R.id.aboutButton:
                        chosenFragment = new AboutFragment();
                        break;
                }
                fragmentManager.beginTransaction().hide(activeFragment).commit();
                fragmentManager.beginTransaction().replace(R.id.mainContentFragment, chosenFragment).commit();
                activeFragment = chosenFragment;
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
