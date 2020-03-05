package com.thoughtly.utils;

import android.net.Uri;

import java.util.UUID;

/*
 * @ClassName: Thouhgt
 * @Description: This class will Represent the Thought object. This class will be
 * instantiated whenever a Thought is being added, or updated
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 18/07/2019
 */
public class Thought {
    public UUID thoughtId;
    public Uri imagePath;
    public Uri recordingPath;
    public String title;
    public String details;
    public String userId;

    public Thought(){}
}
