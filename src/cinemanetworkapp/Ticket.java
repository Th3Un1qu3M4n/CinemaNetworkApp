/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Ahmed
 */
public class Ticket implements Serializable {
    int id;
    String username;
    Movie bookedMovie;
    Date date;
    int time;

    public Ticket(int id, String username, Movie bookedMovie, Date date, int time) {
        this.id = id;
        this.username = username;
        this.bookedMovie = bookedMovie;
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Ticket{" + "id=" + id + ", username=" + username + ", bookedMovie=" + bookedMovie.toString() + ", date=" + date + ", time=" + time + '}';
    }
    
    
    
}
