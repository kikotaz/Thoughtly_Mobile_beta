package com.thoughtly;


import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import android.text.format.DateFormat;

import com.thoughtly.utils.NotificationReceiver;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReminderFragment extends Fragment {
    static Switch reminderSwitch;
    private static TextView reminderText;

    public ReminderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_reminder, container, false);

        //Initializing reminder switch and setting listener
        reminderSwitch = (Switch)view.findViewById(R.id.reminderSwitch);
        reminderSwitch.setOnCheckedChangeListener(new OnReminderSwitchChanged());

        reminderText = (TextView)view.findViewById(R.id.reminderText);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //This class will handle the reminder switch change
    private class OnReminderSwitchChanged implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getFragmentManager(), "timePicker");
            }
        }
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener{

        //Creating new dialog
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            //To get current time as default for the picker
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            //Create new instance of picker dialog
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            Intent reminderIntent = new Intent(getContext(), NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0,
                    reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)getContext().getSystemService(getContext().ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                    pendingIntent);

            reminderText.setVisibility(View.VISIBLE);
            reminderText.setText("You will receive notification every day at " + hourOfDay +
                    ":" + minute);
        }

        //If user clicked cancel in the dialog, set the switch to off
        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            reminderSwitch.setChecked(false);
        }
    }
}
