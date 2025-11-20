package org.miniboot.app.controllers;

import org.miniboot.app.auth.PasswordService;

public class UserController {
    public static String hashPassword(String password) {
        return PasswordService.hashPasswordWithSalt(password);
    }
}
