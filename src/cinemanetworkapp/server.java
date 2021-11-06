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
                            writeDataToFile();
                            
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
            
        }
        
    }
    
    
    
    public static void writeDataToFile() throws FileNotFoundException, IOException{
        FileOutputStream fos = new FileOutputStream("movies.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);   
        oos.writeObject(mylist);
        oos.flush();
        oos.close();
    }
    
    public static void readDataFromFile() throws FileNotFoundException, IOException, ClassNotFoundException{
        
        File movieFile = new File("movies.txt");
        if(!movieFile.exists() || movieFile.length()==0){
            return;
        }
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("movies.txt"));

        mylist = (ArrayList<Movie>) ois.readObject();
        ois.close();
    }
}
