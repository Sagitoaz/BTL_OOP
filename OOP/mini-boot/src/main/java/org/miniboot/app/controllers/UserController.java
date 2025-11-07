package org.miniboot.app.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserController {
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }
}
