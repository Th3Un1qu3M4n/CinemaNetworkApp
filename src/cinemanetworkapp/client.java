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
import java.util.Scanner;

/**
 *
 * @author Ahmed
 */
public class client {
    
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
    
    
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
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
        client myClient = new client(5000,7000,IPAddress);
        
        
        Movie dataObject;
        
        while(true){
            
            //Showing Menu
            System.out.println("MAIN MENU");
            System.out.println("1. ADD MOVIE DATA");
            System.out.println("2. VIEW MOVIE RECORDS");
            System.out.println("3. SEARCH MOVIE RECORD by ID");
            System.out.println("4. EXIT");
            
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
    
    
    
}
