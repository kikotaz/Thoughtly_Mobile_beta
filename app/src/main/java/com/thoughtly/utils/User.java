package com.thoughtly.utils;

import java.util.UUID;

/*
 * @ClassName: User
 * @Description: This class will Represent the User object. This class will be
 * instantiated whenever a User is being added or updated in the database
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 17/07/2019
 */
public class User {
    public UUID userId;
    public String userName;
    public String  userMail;
    public String userDOB;
    public String password;
    public User(){}
}
