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
public class Message implements Serializable{
    String data;

    public Message(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
    
    
    
}
