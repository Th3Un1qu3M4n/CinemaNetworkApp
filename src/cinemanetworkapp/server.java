/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author Ahmed
 */
public class server {
    static DatagramSocket serverSocket;
    static ArrayList<Movie> mylist = new ArrayList<>();
    static ArrayList<Ticket> myTickets = new ArrayList<>();
    static ArrayList<User> myUsers = new ArrayList<>();
    private InetAddress packetIP;
    private int packetPort;
    
    public server(int localPort){
        try {
            serverSocket = new DatagramSocket(7000);
            
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }
    
    public Object receiveObject(){
        try{
            byte[] receiveData = new byte[1024];
        
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            serverSocket.receive(receivePacket);
            byte[] recData = receivePacket.getData();
            packetIP = receivePacket.getAddress();
            packetPort = receivePacket.getPort();

            ByteArrayInputStream bais = new ByteArrayInputStream(recData);

            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        
    }
    
    public void sendResponse(Object sendObject, InetAddress IPAddress, int port) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(sendObject);

        byte[] data = baos.toByteArray();


        DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, port);

        serverSocket.send(packet);
    }
    
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
        //Reading Data from file
        readDataFromFile();
        
        //creating server
        server myServer = new server(7000);
        
        while(true){
            //Receiving Data
            Object receivedObject = myServer.receiveObject();
            
            //Processing Received Data
            if(receivedObject instanceof Movie){
                
                Movie movieData = (Movie) receivedObject;
                
                int op = movieData.getOperation();
                
                switch (op){
                    //FOR ADDING DATA
                        case 0:
                            System.out.println("Request to add Movie Data");
                            System.out.println(movieData.toString());
                            mylist.add(movieData);
                            
                            //Saving Data to file
                            writeDataToFile("M");
                            
                            //Sending Response
                            myServer.sendResponse(new Message("DATA ADDED SUCCESSFULLY"), myServer.packetIP, myServer.packetPort);
                            System.out.println("Response Sent");
                            break;
                    //FOR VIEWING ALL DATA
                        case 1:
                            System.out.println("Request to View All movies");
                            
                            //Calculating size of data to be sent
                            int movieCount = mylist.size();
                            if(movieCount<1){
                                myServer.sendResponse(new Message("No Movie record found!"), myServer.packetIP, myServer.packetPort);
                                break;
                            }
                            
                            //Sending All data as Multiple Responses
                            myServer.sendResponse(new Message("array-"+movieCount), myServer.packetIP, myServer.packetPort);
                            for (Movie mov: mylist){
                                myServer.sendResponse(mov, myServer.packetIP, myServer.packetPort);
                                System.out.println("Sending an Object");
                            }
                            break;
                    //FOR SEARCHING DATA        
                        case 2:
                            System.out.println("Request to Search by id");
                            int searchId = movieData.id;
                            boolean isFound=false;
                            
                            //Searching for id in list
                            for (Movie mov: mylist){
                                if(mov.id == searchId){
                                    
                                //sending response if found
                                myServer.sendResponse(mov, myServer.packetIP, myServer.packetPort);
                                System.out.println("Sending an Object");
                                
                                isFound=true;
                                break;
                                }
                                
                            }
                            if(!isFound){
                                myServer.sendResponse(new Message("No Movie Record with ID Found"), myServer.packetIP, myServer.packetPort);
                            }
                            break;
                            
                        default:
                            System.out.println("Invalid Operation");
                            break;
                }
                
            }
            else if(receivedObject instanceof Message){
                System.out.println("Received Message");
                Message msgbj = (Message) receivedObject;
                
                String[] operation = msgbj.getData().split(",");
                
                if(operation[1].trim().equals("U")){
                    
                    User userObject = (User) myServer.receiveObject();
                    
                    if(operation[0].trim().equals("0")){
                        System.out.println("Request to add User Data");
                        System.out.println(userObject.toString());
                        myUsers.add(userObject);

                        //Saving Data to file
                        writeDataToFile("U");

                        //Sending Response
                        myServer.sendResponse(new Message("success"), myServer.packetIP, myServer.packetPort);
                        System.out.println("Response Sent");
                        
                    }else if(operation[0].trim().equals("1")){
                        System.out.println("Request to Search by id");
                        String searchUsnername = userObject.username;
                        String searchPassword = userObject.password;
                        boolean isFound=false;

                        //Searching for username and password in list
                        for (User usr: myUsers){
                            if(usr.username.equalsIgnoreCase(searchUsnername) && usr.password.equalsIgnoreCase(searchPassword)){
                                

                            //sending response if found
                            myServer.sendResponse(new Message("success"), myServer.packetIP, myServer.packetPort);
                            System.out.println("Sending an Object\n");

                            isFound=true;
                            break;
                            }

                        }
                        if(!isFound){
                            myServer.sendResponse(new Message("Not Found\n"), myServer.packetIP, myServer.packetPort);
                        }
                    }
                    
                    
                }else if(operation[1].trim().equals("T")){
                    
                    
                    
                    if(operation[0].trim().equals("0")){
                        Ticket ticketObject = (Ticket) myServer.receiveObject();
                        
                        System.out.println("Request to add Ticket Data");
                        System.out.println(ticketObject.toString());
                        myTickets.add(ticketObject);

                        //Saving Data to file
                        writeDataToFile("T");

                        //Sending Response
                        myServer.sendResponse(new Message("Ticket Added Successfully"), myServer.packetIP, myServer.packetPort);
                        System.out.println("Response Sent\n");
                        
                    }else if(operation[0].trim().equals("1")){
                        System.out.println("Request to View All Tickets");
                            
                        //Calculating size of data to be sent
                        int ticketCount = myTickets.size();
                        if(ticketCount<1){
                            myServer.sendResponse(new Message("No Ticket record found!"), myServer.packetIP, myServer.packetPort);
                        }else{
                            //Sending All data as Multiple Responses
                            myServer.sendResponse(new Message("array-"+ticketCount), myServer.packetIP, myServer.packetPort);
                            for (Ticket ticket: myTickets){
                                myServer.sendResponse(ticket, myServer.packetIP, myServer.packetPort);
                                System.out.println("Sending an Object\n");
                            }
                        }
                    }else if(operation[0].trim().equals("2")){
                        Ticket ticketObject = (Ticket) myServer.receiveObject();
                        System.out.println("Request to Search by id");
                        int searchid = ticketObject.id;
                        boolean isFound=false;

                        //Searching for username and password in list
                        for (Ticket tkt: myTickets){
                            if(tkt.id==searchid){
                                

                            //sending response if found
                            myServer.sendResponse(tkt, myServer.packetIP, myServer.packetPort);
                            System.out.println("Sending an Object\n");

                            isFound=true;
                            break;
                            }

                        }
                        if(!isFound){
                            myServer.sendResponse(new Message("Not Found"), myServer.packetIP, myServer.packetPort);
                        }
                    }
                }
                
            }
            
        }
        
    }
    
    
    
    public static void writeDataToFile(String data) throws FileNotFoundException, IOException{
        System.out.println("Writing Data\n");
        if(data.equals("M")){
            FileOutputStream fos = new FileOutputStream("movies.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);   
            oos.writeObject(mylist);
            oos.flush();
            oos.close();
        }
        
        else if(data.equals("T")){
            System.out.println("Writing Data to Tickets File");
            FileOutputStream fos = new FileOutputStream("tickets.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);   
            oos.writeObject(myTickets);
            oos.flush();
            oos.close();
        }
        
        else if(data.equals("U")){
            System.out.println("Writing Data to Users File");
            FileOutputStream fos = new FileOutputStream("users.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);   
            oos.writeObject(myUsers);
            oos.flush();
            oos.close();
        }
        
    }
    
    public static void readDataFromFile() throws FileNotFoundException, IOException, ClassNotFoundException{
        
        File movieFile = new File("movies.txt");
        File ticketFile = new File("tickets.txt");
        File userFile = new File("users.txt");
        
        if(movieFile.exists() || movieFile.length() != 0){
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("movies.txt"));
            mylist = (ArrayList<Movie>) ois.readObject();

        }
        if(ticketFile.exists() || ticketFile.length() != 0){
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("tickets.txt"));
            myTickets = (ArrayList<Ticket>) ois.readObject();
        
        }
        if(userFile.exists() || userFile.length() != 0){
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.txt"));
            myUsers = (ArrayList<User>) ois.readObject();
        }
        
    }
}
