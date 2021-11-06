/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author Ahmed
 */
public class client {
    
    //only in case of GUI
    static client myClient;
    
    
    static DatagramSocket clientSocket;
    InetAddress serverIPAddress;
    int serverPort;
    
    public client(int localPort, int port, InetAddress IPAddress){
        try{
            clientSocket=new DatagramSocket(localPort);
            serverIPAddress = IPAddress;
            serverPort = port;
        }
        catch(SocketException se){
            se.printStackTrace();
        }
        
    }
    
    public boolean sendObject(Object obj){
        try{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(obj);

        byte[] data = baos.toByteArray();



        DatagramPacket packet = new DatagramPacket(data, data.length, serverIPAddress, serverPort);

        clientSocket.send(packet);
        return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
            
        }
    }
    
    public Object receiveObject(){
        try{
            byte[] receiveData = new byte[1024];
        
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);
            byte[] recData = receivePacket.getData();

            ByteArrayInputStream bais = new ByteArrayInputStream(recData);

            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        
    }
    
    
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException, ParseException {
        Scanner input= new Scanner(System.in);
        InetAddress IPAddress;
        try {
            IPAddress=InetAddress.getByName("HP-15T");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            System.out.println("Host Not Found Please Restart Application");
            return;
        }
        
        //creating Client        
        myClient = new client(5000,7000,IPAddress);
        
        
        Movie dataObject;
        boolean userValidated = false;
        String currentUser = "";
        while(true){
            
            while(!userValidated){
                System.out.println("USER REGISTRATION");
                System.out.println("Enter Username (already registered enter 0)");
                String username = input.next();
                System.out.println("Enter Password");
                String password = input.next();
                
                if(username.equals("0")){
                    break;
                }
                
                boolean userRegistered = sendUserData(username,password,"0,U");
                if(userRegistered){
                    System.out.println("\n\nUser Registered\n\n");
                    currentUser = username;
                    break;
                }else{
                    System.out.println("\n\nInvalid Credentials\n\n");
                }
            }
            
            Thread.sleep(500);
            
            //----------------LOGIN----------------//
           
            while(!userValidated){
                System.out.println("USER LOGIN");
                System.out.println("Enter Username (no account enter 0) ");
                String username = input.next();
                System.out.println("Enter Password");
                String password = input.next();
                
                if(username.equals("0")){
                    break;
                }
                
                userValidated = sendUserData(username,password,"1,U");

                if(userValidated){
                    System.out.println("\n\nValid Credentials\n\n");
                    //move to MAIN MENU GUI
                    break;
                }else{
                    System.out.println("\n\nInvalid Credentials\n\n");
                }
            }
            
            Thread.sleep(500);
            
            //--------------MAIN MENU-------------//
            while(userValidated){
                System.out.println("MAIN MENU");
                System.out.println("1. MOVIE");
                System.out.println("2. TICKET");
                System.out.println("4. EXIT");
                
                int userInput = input.nextInt();
                
                //--------------MOVIE MENU-------------//
                if(userInput==1){
                    while(true){
                        //Showing Menu
                        System.out.println("MOVIE MENU");
                        System.out.println("1. ADD MOVIE DATA");
                        System.out.println("2. VIEW MOVIE RECORDS");
                        System.out.println("3. SEARCH MOVIE RECORD by ID");
                        System.out.println("4. BACK");

                        System.out.print("Enter Your Choice: ");
                        int userIn = input.nextInt();



                        //Processing User Input
                        if(userIn < 0 && userIn > 4){
                            continue;
                        }else if(userIn == 4){
                            break;
                        }else if (userIn == 1){
                            System.out.print("Enter Movie Id: ");
                            int id = input.nextInt();
                            System.out.print("Enter Movie Name: ");
                            String name = input.next();
                            System.out.print("Enter Movie Rating: ");
                            int rating = input.nextInt();
                            System.out.print("Enter Movie Year: ");
                            int year = input.nextInt();

                            dataObject=new Movie(id, name, rating, year);

                            dataObject.setOperation(0);

                        }else if (userIn == 2){
                            dataObject=new Movie(0, "", 0, 0);

                            dataObject.setOperation(1);
                        }else{
                            System.out.println("Enter Movie Id: ");
                            int id = input.nextInt();
                            dataObject=new Movie(id, "", 0, 0);

                            dataObject.setOperation(2);
                        }

                        //Sending Data
                        if(!myClient.sendObject(dataObject)){
                            System.out.println("Error Sending Data");
                            continue;
                        }

                        System.out.println("Waiting For Response");

                        //Waiting For Response
                        Thread.sleep(1000);


                        //Receving Data
                        Object receivedObject = myClient.receiveObject();

                        //Processing Received Data
                        if(receivedObject instanceof Message){
                            Message msgObj = (Message) receivedObject;

                            String msgData = msgObj.getData();

                            if(msgData.contains("array")){

                                String[] msg = msgData.split("-");
                                int count=Integer.parseInt(msg[1]);
                                System.out.println("Total Objects: "+count+"\n");

                                while(count>0){

                                    Movie moiveObj = (Movie) myClient.receiveObject();
                                    System.out.println(moiveObj.toString());
                                    count--;
                                }
                            }else{
                                System.out.println("\n"+msgData);
                            }
                        }else if(receivedObject instanceof Movie){
                            Movie movieData = (Movie)receivedObject;
                            System.out.println(movieData.toString());
                        }


                        System.out.println("\nDo you want to continue (Y\\N)");
                        String temp = input.next();
                        if(temp.equalsIgnoreCase("N")){
                            break;
                        }


                    }
                    
                }
                else if(userInput==2){
                    
                    while(true){
                        //Showing Menu
                        System.out.println("TICKET MENU");
                        System.out.println("1. BOOK TICKET DATA");
                        System.out.println("2. VIEW TICKET RECORDS");
                        System.out.println("3. SEARCH TICKET RECORD by ID");
                        System.out.println("4. BACK");

                        System.out.print("Enter Your Choice: ");
                        int userIn = input.nextInt();


                        Ticket ticketObject;
                        Message operation;
                        //Processing User Input
                        if(userIn < 0 && userIn > 4){
                            continue;
                        }else if(userIn == 4){
                            break;
                        }else if (userIn == 1){
                            System.out.print("Enter Ticket Id: ");
                            int id = input.nextInt();
                            System.out.print("Enter User Name: ");
                            String name = input.next();
                            System.out.print("Enter Movie Id: ");
                            int movieId = input.nextInt();
                            System.out.print("Enter Ticket Date: ");
                            Date ticketDate=new SimpleDateFormat("dd/MM/yyyy").parse(input.next());
                            System.out.print("Enter Movie Id: ");
                            int time = input.nextInt();

                            ticketObject=new Ticket(id, name,new Movie(movieId, "", 0, 0), ticketDate, time);
                            operation = new Message("0,T");
                            
                            if(!myClient.sendObject(operation)){
                                System.out.println("Error Sending Data");
                                continue;
                            }
                            
                            if(!myClient.sendObject(ticketObject)){
                                System.out.println("Error Sending Data");
                                continue;
                            }
                            

                        }else if (userIn == 2){
                            operation = new Message("1,T");
                            
                            if(!myClient.sendObject(operation)){
                                System.out.println("Error Sending Data");
                                continue;
                            }
                            
                        }else{
                            System.out.println("Enter ticket Id: ");
                            int id = input.nextInt();
                            ticketObject=new Ticket(id, "",new Movie(0, "", 0, 0), new Date(), 0);
                            operation = new Message("2,T");
                            
                            if(!myClient.sendObject(operation)){
                                System.out.println("Error Sending Data");
                                continue;
                            }
                            
                            if(!myClient.sendObject(ticketObject)){
                                System.out.println("Error Sending Data");
                                continue;
                            }
                            
                        }

                        //Sending Data
                        
                        

                        System.out.println("Waiting For Response");

                        //Waiting For Response
                        Thread.sleep(1000);


                        //Receving Data
                        Object receivedObject = myClient.receiveObject();

                        //Processing Received Data
                        if(receivedObject instanceof Message){
                            Message msgObj = (Message) receivedObject;

                            String msgData = msgObj.getData();

                            if(msgData.contains("array")){

                                String[] msg = msgData.split("-");
                                int count=Integer.parseInt(msg[1]);
                                System.out.println("Total Objects: "+count+"\n");

                                while(count>0){

                                    Ticket ticketObj = (Ticket) myClient.receiveObject();
                                    System.out.println(ticketObj.toString());
                                    count--;
                                }
                            }else{
                                System.out.println("\n"+msgData);
                            }
                        }else if(receivedObject instanceof Ticket){
                            Ticket ticketData = (Ticket)receivedObject;
                            System.out.println(ticketData.toString());
                        }


                        System.out.println("\nDo you want to continue (Y\\N)");
                        String temp = input.next();
                        if(temp.equalsIgnoreCase("N")){
                            break;
                        }


                    }
                }else{
                    return;
                }
            
            }
            
            
            
        }
        
    }
    
    public static boolean sendUserData(String username, String password, String operation){
        User newUser = new User(username,password);
            
            if(!myClient.sendObject(new Message(operation))){
                System.out.println("Error Sending Meta Data");
                return false;
            }
            
            if(!myClient.sendObject(newUser)){
                System.out.println("Error Sending User Data");
                return false;
            }
            
            
            Object receivedObject = myClient.receiveObject();
            
            Message msgObj = (Message) receivedObject;
                
            String msgData = msgObj.getData();
            
            if(msgData.equalsIgnoreCase("success")){
                return true;
            }else{
                return false;
            }
    }
    
    
    
}
