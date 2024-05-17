/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 * Part of the code is from Bikram Shrestha (2019)https://github.com/Bikram-Shrestha/Java-Socket-Programming-Chat-Application/commit/e64abcee381265ff1f705b664331161806dd30e1
 */
package gosaveserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Daddy
 */
public class GoSaveServer extends Application {
    
    Font titleFont = Font.font("Times New Roman", FontWeight.BOLD, 25);
    Font labelButtonFont = Font.font("Times New Roman", FontWeight.BOLD, 14);
    Font userFont = Font.font("Times New Roman", FontWeight.NORMAL, 14);
    
    //Server Interface labels
    Label lblUserMsgs = new Label("User Messages");
    
    Label lblActiveUser = new Label("Active User");
    
    //Create Data structure for the active users and messages
    private ArrayList<String> userMessages = new ArrayList<>();
    private ArrayList<String> activeUsers = new ArrayList<>();

    //Create ListView Control/node for the active users and user messages
    ListView<String> userMsgsListView = new ListView<String>();
    ListView<String> activeUsersListViews = new ListView<String>();
    
    //Associate user and messages' data structure to respective collections
    ObservableList<String> userMsgsCollections =
            FXCollections.observableArrayList (userMessages);
    ObservableList<String> activeUsersCollections =
            FXCollections.observableArrayList (activeUsers);
   
    // Mapping of sockets to output streams
    private Hashtable outputStreams = new Hashtable();

    //ArrayList of all open Socket.
    private ArrayList<Socket> socketList = new ArrayList<>();

    // Server socket
    private ServerSocket serverSocket;

    private final String encrptKey = "Solace12@GoSave34";
    
    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        
    	try {
                lblUserMsgs.setFont(labelButtonFont);
                lblActiveUser.setFont(labelButtonFont);
                
	        //Setting content to display for the ListVIew
	        activeUsersListViews.setItems(activeUsersCollections);
	        userMsgsListView.setItems(userMsgsCollections);
	        userMsgsListView.setMinWidth(430);
	
	        // Creating GridPane to arrange all the node.
	        GridPane gridPane = new GridPane();
	        gridPane.setPadding(new Insets(10));
	
	        //All the nodes are added to the gridPane.
	        gridPane.add(lblUserMsgs,0,0);
	        gridPane.add(userMsgsListView,0,1);
	        gridPane.add(lblActiveUser,0,2);
	        gridPane.add(activeUsersListViews,0,3);
	        // Create a scene and place it in the stage
	        Scene scene = new Scene(gridPane, 450, 400);
	        primaryStage.setTitle("GoSAVE: An Emergency Intervention Request System"); // Set the stage title
	        primaryStage.setScene(scene); // Place the scene in the stage
                primaryStage.setResizable(false);
	        primaryStage.show(); // Display the stage
	        /*
	        Special care is taken to make sure that all the connection
	        to the client is been closed properly before closing the
	        application.
	         */
	        primaryStage.setOnCloseRequest(t -> closeSocketExit());
	
	        // Start a new thread to listen for connection.
	        new Thread(() -> listen()).start();
    	} catch(Exception e) {
			e.printStackTrace();
		}
    }
    public static void main(String[] args) {
		launch(args);
	}

    /*
    When this method is called, it make sure that all the
    open socket, or connection to the client is terminated
    properly.
     */
    private void closeSocketExit() {
        try {
            for(Socket socket:socketList){
                //If socket doesn't exit, no need to close.
                if(socket!=null){
                    socket.close();
                }
            }
            Platform.exit();    // Close UI.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    This thread create a new serverSocket using the port 8000
    and wait for user to connect. This is done in a loop so
    that this server will be waiting and creating a new
    connection as user join the server.
     */
    private void listen() {
        try {
            // Create a server socket
            serverSocket = new ServerSocket(2525);
            System.out.println("Server Socket opened on port 2525. Server listening for client connection");
            Platform.runLater(() ->
                    userMsgsCollections.add("MultiThreadServer started at " + new Date()));

            while (true) {// Listen for a new connection request
                Socket socket = serverSocket.accept();

                //Add accepted socket to the socketList.
                socketList.add(socket);

                // Display the client socket information and time connected.
                Platform.runLater(() ->
                        userMsgsCollections.add("Connection from " + socket + " at " + new Date()));

                // Create output stream
                DataOutputStream dataOutputStream =
                        new DataOutputStream(socket.getOutputStream());

                // Save output stream to hashtable
                outputStreams.put(socket, dataOutputStream);

                // Create a new thread for the connection
                new ServerThread(this, socket);
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }


    // This method dispatch activeUsers to all user in the server.
    private void dispatchUserList() {
        this.sendToAll(activeUsers.toString());
    }


    // Used to get the output streams
    Enumeration getOutputStreams(){
        return outputStreams.elements();
    }


    // Used to send message to all clients
    void sendToAll(String message){
        // Go through hashtable and send message to each output stream
        for (Enumeration e = getOutputStreams(); e.hasMoreElements();){
            DataOutputStream dout = (DataOutputStream)e.nextElement();
            try {
                // Write message
                dout.writeUTF(message);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    // This method send onlineStatus to all the user excluding self.
    void sendOnlineStatus(Socket socket,String message){
        for (Enumeration e = getOutputStreams(); e.hasMoreElements();){
            DataOutputStream dataOutputStream = (DataOutputStream)e.nextElement();
            try {
                //If it is same socket then don't send the message.
                if(!(outputStreams.get(socket) == dataOutputStream)){
                    // Write message
                    dataOutputStream.writeUTF(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    
    
    
    
    

    /*
    Declaring a ServerThread class so that it can be
    used to create a multi-thread server serving
    a each socket in different thread.
     */
    class ServerThread extends Thread {
        private GoSaveServer server;
        private Socket socket;
        String userName, recMsg;    // Default null;
        boolean userJoined; // Default false;

        
        
        /** Construct a thread */
        public ServerThread(GoSaveServer server, Socket socket) {
            this.socket = socket;
            this.server = server;
            start();
        }

        /** Run a thread */
        @Override
        public void run() {
            recMsg = "";
            System.out.println("Server Thread Started execution.");
            try {
                //server.RegisterUser("Register:Abigail#");
                // Create data input and output streams
                DataInputStream dataInputStream =
                        new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream =
                        new DataOutputStream(socket.getOutputStream());

                // Continuously serve the client
                while (true) {
                    String lgnMsg = "";
                    recMsg = dataInputStream.readUTF();
                    System.out.println("Message Received Client: "+ recMsg);
                    
                    if(!userJoined){//User is yet authenticated
                        //recMsg = dataInputStream.readUTF();
                        /*if(activeUsers.contains(userName)){
                            dataOutputStream.writeUTF(userName);
                            System.out.println(userName + " already exist.");
                        */
                        //System.out.println("Checking Client joined Variable: "+ userJoined);
                        if(recMsg.startsWith("AuthenticateUser")){
                            //System.out.println("Server AuthenticateUser Started execution.");
                            lgnMsg = LoginUser(recMsg);
                            //System.out.println("Login Message: " + lgnMsg);
                            //System.out.println("Server AuthenticateUser finished execution.");
                            if(!lgnMsg.startsWith("AuthenticateUser:InvalidCredential")){ //String is empty because no error message wasreturned from the Login function
                                System.out.println("Server AuthenticateUser finished execution.");
                                dataOutputStream.writeUTF("AuthenticateUser:Successfully");
                                System.out.println(userName +" joined the chat room");
                                activeUsers.add(userName);
                                
                                server.dispatchUserList();
                                
                                userJoined = true;
                                String userNotification = userName + " joined the chat room.";
                                Platform.runLater(() ->
                                        userMsgsCollections.add(userName + " joined the chat room."));
                                server.sendOnlineStatus(socket,userNotification);
                                activeUsersCollections.clear();
                                activeUsersCollections.addAll(activeUsers);
                                
                                //Notify User of succeful login
                                dataOutputStream.writeUTF("AuthenticateUser:Successfully");
                                System.out.println(userName + " A new User Account was successfully created.");
                               
                            }else{
                                
                                 //Notify User of unsuccessful user login
                                dataOutputStream.writeUTF(lgnMsg);
                            }
                        }else if(recMsg.startsWith("RegisterUser")){
                            try{
                                System.out.println("Server RegisterUser Started execution.");
                                
                                boolean stat = RegisterUser(recMsg);
                                if(stat){
                                    dataOutputStream.writeUTF("UserAccount:Successfully");
                                    System.out.println(userName + " A new User Account was successfully created.");
                                    System.out.println("Server RegisterUser finished execution.");
                                }else{
                                    //User Account Creation not successful
                                    dataOutputStream.writeUTF("UserAccount:Unsuccessful Registration");
                                }
                            }catch(Exception e){
                                
                           }
                        }
                        
                    }
                     /*
                    Once it join it can receive the message from the other
                    user in broadcast mode.
                    */
                    else if(userJoined){
                        //Check if its an emergency button that was clicked by the user
                        if((recMsg.startsWith("Violence")) || (recMsg.startsWith("Health")) ||(recMsg.startsWith("Security"))){
                            //Query user geolocation, and retrieve the address thereof for broadcasting to GoSave Users
                            server.sendToAll(recMsg);
                        }else{
                            System.out.println(userName + ": User Joined and now sending message!!!");
                            // User Message
                            //String string = dataInputStream.readUTF();

                            // Send text back to the clients
                            server.sendToAll(recMsg);
                            server.dispatchUserList();

                            // Add chat to the server jta
                            Platform.runLater(() ->userMsgsCollections.add(recMsg));
                        }
                    }
                }
            }

        
            /*
            When a client close/end their connection, an error is raised and handled as below
             */
            catch(IOException ex) {
                System.out.println("Connection Closed for " + userName);
                Platform.runLater(() ->
                        userMsgsCollections.add("Connection Closed for " + userName));

                if(!userName.equals(null)){
                    activeUsers.remove(userName);
                }
                outputStreams.remove(socket);
                server.dispatchUserList();
                if (!userName.equals(null)){
                    server.sendToAll(userName + " has left the chat room.");
                }
                Platform.runLater(() ->{
                    activeUsersCollections.clear();
                    activeUsersCollections.addAll(activeUsers);
                });
            }
        }
        boolean RegisterUser(String userInfor) throws ClassNotFoundException{
            
            System.out.println("Server RegisterUser Started execution.");
           try{
               //System.out.println("Message Received @ Server RegisterUser: "+ userInfor);
                String userMsg[] = userInfor.split(":");//Seperate FunctionName from Parameters
                //System.out.println("Message Received @ Server RegisterUser - Parameters: "+ userMsg[1]);
                String userInfo [] = userMsg[1].split("#"); //Seperate parameter bundle into entities
                Class.forName("com.mysql.cj.jdbc.Driver");  

                Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/dbagosave","root","");  

                String pass = userInfo[3];
                userName = userInfo[2];
                //Check if user account previously registered
                String sql = "SELECT * FROM tbleUsers WHERE userEmail ='"+userName+"";
                PreparedStatement preparedStmt = con.prepareStatement(sql);
                Statement stmt=con.createStatement();  
                ResultSet rs=stmt.executeQuery("SELECT userFirstName, userEmail, userPassword FROM tblUsers WHERE userEmail ='"+userName+"'");  
                rs.last();
                if(rs.getRow()< 1){//Meaning User is not not registred
                    sql = "INSERT INTO tblUsers (userFirstName, userLastName, userEmail, userPassword) VALUES (?,?,?,?)";
                
                    //String pass = Encrypt(userInfo[3], encrptKey);

                    preparedStmt = con.prepareStatement(sql);
                    preparedStmt.setString(1, userInfo[0]);
                    preparedStmt.setString(2, userInfo[1]);
                    preparedStmt.setString(3, userName);
                    preparedStmt.setString(4, pass);

                    // execute the preparedstatement
                    preparedStmt.execute();
                    userName = userInfo[0] + " {"+userName +"}";

                }else{//Meaning User is already registered
                    return false;
                }
                
            }catch(Exception e){ System.out.println(e);  
                return false; //meaning an error occured.
            } 
           System.out.println("Server RegisterUser() finished execution.");
            return true; //meaning insertion of record was successfull.
        }

        
                
        String LoginUser(String userInfor){

            String status = "";
            System.out.println("Server LoginUser Started execution.");
            try{
                String userMsg[] = userInfor.split(":");//Seperate FunctionName from Parameters
                String userInfo [] = userMsg[1].split("#"); //Seperate parameter bundle into entities

                Class.forName("com.mysql.cj.jdbc.Driver"); 

                Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/dbagosave","root","");  
                //here sonoo is database name, root is username and password
                String pass = "";
                Statement stmt=con.createStatement();  
                ResultSet rs=stmt.executeQuery("SELECT userFirstName, userEmail, userPassword FROM tblUsers WHERE userEmail ='"+userInfo[0]+"'");  
                rs.last();
                if(rs.getRow()>0){
                    rs.beforeFirst();
                    while(rs.next()){ 
                        System.out.println("Here executed");
                        userName = rs.getString(1)+ " {"+ rs.getString(2)+"}";
                        System.out.println(userName);
                        pass =rs.getString(3);
                        //String pass2 = userInfo[1];
                        //pass = Decrypt(pass, encrptKey);
                        System.out.println(" Query results being retrieved with password as: "+ pass+ " : against "+ userInfo[1]);
                        if(userInfo[1].equals(pass)){
                            System.out.println(" Password Validation successful");
                            status = rs.getString(1);  
                        }else{
                            status = "AuthenticateUser:InvalidCredential";
                        }
                    }
                }else{
                    System.out.println("Number of rows is less than or equal to zero");
                    status = "AuthenticateUser:InvalidCredential";
                }
                    
                
                con.close(); 
                
            }catch(Exception e){ 
                System.out.println(e);
                status = "AuthenticateUser:InvalidCredential";
            }
            System.out.println("Server LoginUser() finished execution.");
            System.out.println(status);
            return status;
        }



        public String Encrypt(String strClearText,String strKey) throws Exception{
            String strData="";

            try {
                    SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
                    Cipher cipher=Cipher.getInstance("Blowfish");
                    cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
                    byte[] encrypted=cipher.doFinal(strClearText.getBytes());
                    strData=new String(encrypted);

            } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception(e);
            }
            return strData;
        }
        public String Decrypt(String strEncrypted,String strKey) throws Exception{
            String strData="";

            try {
                    SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
                    Cipher cipher=Cipher.getInstance("Blowfish");
                    cipher.init(Cipher.DECRYPT_MODE, skeyspec);
                    byte[] decrypted=cipher.doFinal(strEncrypted.getBytes());
                    strData=new String(decrypted);

            } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception(e);
            }
            return strData;
        }



    }
}
