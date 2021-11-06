/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.Serializable;

/**
 *
 * @author Ahmed
 */
public class User implements Serializable {
    String username;
    String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" + "username=" + username + ", password=" + password + '}';
    }
    
    
}
