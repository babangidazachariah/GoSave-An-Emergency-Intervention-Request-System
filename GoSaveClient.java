/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 * Part of the code is from Bikram Shrestha (2019)https://github.com/Bikram-Shrestha/Java-Socket-Programming-Chat-Application/blob/master/ChatClient.java
 */
package gosaveclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Daddy
 */
public class GoSaveClient extends Application {
    
    //Set group font styles and size
    Font titleFont = Font.font("Times New Roman", FontWeight.BOLD, 25);
    Font labelButtonFont = Font.font("Times New Roman", FontWeight.BOLD, 14);
    Font userFont = Font.font("Times New Roman", FontWeight.NORMAL, 14);
    
    Label lblLoginTitle, lblLoginError, lblRegTitle, lblRegError, lblNewUserGuide, lblRetUserGuide;
    Label lblEmgcTitle, lblEmgcSubTitle, lblEmgcMsgActiveUsers, lblEmgcCorner, lblMessages, lblEmgcSituation, lblClient;
    Button btnLogin, btnRegister, btnRetUserLogin, btnNewUserReg, btnSendMessage, btnViolence,btnSecurity,btnHealth, btnAlert ;  
    
    TextField txtUsername, txtRegFirstname,txtRegLastname, txtRegUsername, txtMessage ;
    PasswordField txtPassword, txtRegPassword, txtRegConfPassword;
    
    BorderPane emgcBorderPane, userEmgc;
    GridPane userLogin, userRegister;
    Scene loginScene, regScene, emgcScene;
    
    
    ArrayList<String> userList = new ArrayList<>();
    ArrayList<String> emgncList = new ArrayList<>();
    ArrayList<String> chatMessages = new ArrayList<>();

    // List view for user  and message  was declared.
    ListView<String> userListView = new ListView<>();
    ListView<String> emgncListView = new ListView<>();
    ListView<String> messageListView = new ListView<>();

    ObservableList<String> userItems =
            FXCollections.observableArrayList (userList);
    
    ObservableList<String> emgncItems =
            FXCollections.observableArrayList (emgncList);

    ObservableList<String> messageItem =
            FXCollections.observableArrayList (chatMessages);
    
    String userName = "";
    //Define output stream and input stream handlers
    DataInputStream receiveData;
    DataOutputStream sendData;
    private boolean connection = true;
    //Define a socket
    Socket clientConnection;
    boolean joined = false;
    Stage emgcPrimaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Initialize Login Interface controls/nodes
        lblLoginTitle = new Label();
        lblLoginError = new Label();
        lblNewUserGuide = new Label();
        lblEmgcCorner = new Label();
        btnNewUserReg = new Button("Register");
        txtPassword = new PasswordField();
        btnLogin = new Button("Login");
        txtUsername = new TextField();
         
        //Initialize Emergency Comunnity Interface controls/nodes
        txtMessage = new TextField();
        btnAlert = new Button("");
        btnViolence = new Button("Violence");
        btnSecurity = new Button("Security");
        btnHealth = new Button("Health");
        btnSendMessage = new Button("Send");
        lblMessages = new Label();
        lblClient = new Label();
        lblEmgcSituation = new Label();
        lblEmgcTitle = new Label();
        lblEmgcSubTitle = new Label();
        lblEmgcMsgActiveUsers = new Label();
        
        //Initialize Registration Interface Control/nodes
        lblRegError = new Label();
        lblRegTitle = new Label();
        txtRegFirstname = new TextField();
        txtRegLastname = new TextField();
        txtRegUsername = new TextField();        
        txtRegPassword = new PasswordField();
        txtRegConfPassword = new PasswordField();
        btnRegister = new Button("Register");
        btnRegister.setFont(labelButtonFont);
        btnRetUserLogin = new Button("<<Login Interface");
        lblRetUserGuide = new Label();
        
        
        
        userEmgc = new BorderPane();
        
        userLogin = new GridPane();
        userRegister = new GridPane();
        
        
        userLogin = LoginInterface();
        userRegister = RegisterInterface();
        userEmgc = EmergencyInterface();
        //Create Application Scenes
        
        loginScene = new Scene(userLogin );
        regScene = new Scene(userRegister );
        //emgcScene = new Scene(userEmgc, 700, 500 );
        emgcScene = new Scene(userEmgc);
        
        
        //Query Database to authenticate User
        btnLogin.setOnAction(e -> Authenticate(primaryStage));
        
        //Validate User input and commit to Database
        btnRegister.setOnAction(e -> Register());
        
        
        btnRetUserLogin.setOnAction(e -> primaryStage.setScene(loginScene));
        
        btnNewUserReg.setOnAction(e -> primaryStage.setScene(regScene));
        btnAlert.setOnAction(e->DisableAlert());
        
        btnViolence.setOnAction(e->SendEmergencyAlert("Violence"));
        btnSecurity.setOnAction(e->SendEmergencyAlert("Security"));
        btnHealth.setOnAction(e->SendEmergencyAlert("Health"));
        
        btnSendMessage.setOnAction(e->Send());
        
        primaryStage.setOnCloseRequest(e -> closeSocketExit());
        //emgcPrimaryStage.setOnCloseRequest(e -> closeSocketExit());
        //Set up client for and make connection to server
        try{
            clientConnection = new Socket("localhost",2525);
            receiveData = new DataInputStream(clientConnection.getInputStream());
            sendData = new DataOutputStream(clientConnection.getOutputStream());

            // Start a new thread for receiving messages
            new Thread(() -> ReceiveMessages()).start();

        }catch(IOException ex) {
            Platform.runLater(()->{
                lblRegError.setTextFill(Color.RED);
                lblRegError.setText("Unable to establish connection.");
                System.err.println("Connection refused.");
            });
        }
        
       
        emgcPrimaryStage = primaryStage;
        primaryStage.setTitle("GoSAVE: An Emergency Intervention Request System");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    GridPane LoginInterface(){
       //Login interface nodes/controls
        
        lblLoginTitle.setText("User Login");
        lblLoginTitle.setFont(titleFont);
        lblLoginTitle.setTextAlignment(TextAlignment.CENTER);
        
        
        //Login Prompt/Error Message Label
        
        lblLoginError.setText("Error Message Goes Here");
        lblLoginError.setFont(userFont);
        lblLoginError.setTextAlignment(TextAlignment.CENTER);
        lblLoginError.setTextFill(Color.web("#eb0909"));
         
        
        //VBox for Title and Error Message
        VBox genPrompts = new VBox();
        genPrompts.setSpacing(10.0);
        genPrompts.setAlignment(Pos.CENTER);
        
        genPrompts.getChildren().addAll(lblLoginTitle, lblLoginError);
       /* 
        Label username = new Label();
        username.setText("Username: ");
        username.setFont(labelButtonFont);
        */
        
        txtUsername.setPromptText("Enter Username (Email) Here!!!");
        txtUsername.setFont(userFont);
        /*
        Label password = new Label();
        username.setText("Password: ");
        username.setFont(labelButtonFont);
        */
        
        
        txtPassword.setPromptText("Enter Password Here!!!");
        txtPassword.setFont(userFont);
        
        
        btnLogin.setFont(labelButtonFont);
        btnLogin.setTooltip(new Tooltip("Click to Submit-"));
        
        
        btnNewUserReg.setFont(labelButtonFont);
        btnNewUserReg.setTooltip(new Tooltip("Click Here to Register"));
        
        
        lblNewUserGuide.setText("New User? Click Button Below to Register!!!");
        lblNewUserGuide.setFont(labelButtonFont);
        
        // Creating GridPane for the Login Layout BorderPane.
        GridPane loginGridPane = new GridPane();
        loginGridPane.setPadding(new Insets(10));
        loginGridPane.setHgap(20);
        loginGridPane.setVgap(10);
        
        loginGridPane.setAlignment(Pos.CENTER);
        
         // Adding nodes/controls to the loginGridPane
        loginGridPane.add(genPrompts,0,0);
        //loginGridPane.add(lblLoginError, 0, 2);
        loginGridPane.add(txtUsername,0,4);
        loginGridPane.add(txtPassword,0,5);
        loginGridPane.add(btnLogin,0,7);
        loginGridPane.add(lblNewUserGuide,0,9);
        loginGridPane.add(btnNewUserReg,0,10);
        
        //Set max width/height of interface
        loginGridPane.setMinHeight(300);
        loginGridPane.setMaxWidth(500);
        
        return  loginGridPane;
    }
    GridPane RegisterInterface(){
       //Login interface nodes/controls
        
        lblRegTitle.setText("User Registration");
        lblRegTitle.setFont(titleFont);
        lblRegTitle.setTextAlignment(TextAlignment.CENTER);
        
        
        //Login Prompt/Error Message Label
        
        lblRegError.setText("Error Message Goes Here");
        lblRegError.setFont(userFont);
        lblRegError.setTextAlignment(TextAlignment.CENTER);
        lblRegError.setTextFill(Color.web("#eb0909"));
         
         
        //VBox for Title and Error Message
        VBox genPrompts = new VBox();
        genPrompts.setSpacing(10.0);
        genPrompts.setAlignment(Pos.CENTER);
        
        genPrompts.getChildren().addAll(lblRegTitle, lblRegError);
       /* 
        Label username = new Label();
        username.setText("Username: ");
        username.setFont(labelButtonFont);
        */
       
        txtRegFirstname.setPromptText("Enter Firstname Here!!!");
        txtRegFirstname.setFont(userFont);
        
        
        txtRegLastname.setPromptText("Enter Lastname Here!!!");
        txtRegLastname.setFont(userFont);
        
        txtRegUsername.setPromptText("Enter Username (Email) Here!!!");
        txtRegUsername.setFont(userFont);
        /*
        Label password = new Label();
        username.setText("Password: ");
        username.setFont(labelButtonFont);
        */
        
        
        txtRegPassword.setPromptText("Enter Password Here!!!");
        txtRegPassword.setFont(userFont);
        txtRegConfPassword.setPromptText("Re-enter Password Here!!!");
        txtRegConfPassword.setFont(userFont);
        
         
        btnRegister.setTooltip(new Tooltip("Click to Submit Form"));
        
        
        btnRetUserLogin.setFont(labelButtonFont);
        btnRetUserLogin.setTooltip(new Tooltip("Click to Return to Login Interface"));
        
        
        lblRetUserGuide.setText("Already have account? Click Button Below to Login!!!");
        lblRetUserGuide.setFont(labelButtonFont);
        
        // Creating GridPane for the Login Layout BorderPane.
        GridPane regGridPane = new GridPane();
        regGridPane.setPadding(new Insets(10));
        regGridPane.setHgap(20);
        regGridPane.setVgap(10);
        
        regGridPane.setAlignment(Pos.CENTER);
        
         // Adding nodes/controls to the loginGridPane
        regGridPane.add(genPrompts,0,0);
        //loginGridPane.add(lblLoginError, 0, 2);
        regGridPane.add(txtRegFirstname,0,4);
        regGridPane.add(txtRegLastname,0,5);
        regGridPane.add(txtRegUsername,0,6);
        regGridPane.add(txtRegPassword,0,7);
        regGridPane.add(txtRegConfPassword,0,8);
        regGridPane.add(btnRegister,0,9);
        regGridPane.add(lblRetUserGuide,0,11);
        regGridPane.add(btnRetUserLogin,0,12);
        
        //Set size of the interface
        regGridPane.setMinHeight(500);
        regGridPane.setMaxWidth(500);
        
        System.out.println("Registration Interface Exucuted");
        
        return  regGridPane;
    }
    BorderPane EmergencyInterface(){
        emgcBorderPane = new BorderPane();
        
        //Define the GridPanes for the main interface
        GridPane leftPane = new GridPane();
        GridPane topPane = new GridPane();
        GridPane rightPane  = new GridPane();
        GridPane centerPane = new GridPane();
        GridPane bottomPane = new GridPane();
        
        
        leftPane.setPadding(new Insets(10));
        leftPane.setHgap(20);
        leftPane.setVgap(10);
        
        topPane.setPadding(new Insets(10));
        topPane.setHgap(20);
        topPane.setVgap(10);
        topPane.setAlignment(Pos.CENTER);
        
        rightPane.setPadding(new Insets(10));
        rightPane.setHgap(20);
        rightPane.setVgap(10);
        
        centerPane.setPadding(new Insets(10));
        centerPane.setHgap(20);
        centerPane.setVgap(10);
        
        bottomPane.setPadding(new Insets(10));
        bottomPane.setHgap(20);
        bottomPane.setVgap(10);
        
       
        
        //Define the nodes for LeftPane
        // user and message list view is made uneditable.
        userListView.setEditable(false);
        messageListView.setEditable(false);
        // Setting size of user ListView.
        userListView.setMaxWidth(200);
        
        emgncListView.setEditable(false);
        // Setting size of user ListView.
        emgncListView.setMaxWidth(200);
        emgncListView.setMaxHeight(100);
        
        
       
        
        lblEmgcTitle.setText("GoSAVE Community Platform: Its Okay not to be Okay!!!");
        lblEmgcTitle.setFont(titleFont);
        
        
        lblEmgcSubTitle.setText("... support is just a button press away.");
        lblEmgcSubTitle.setFont(labelButtonFont);
        //lblEmgcSubTitle.setAlignment(Pos.CENTER);
        
        
        lblEmgcMsgActiveUsers.setText("GoSave Active Users:");
        lblEmgcMsgActiveUsers.setFont(labelButtonFont);
        
        //Add the associated controls/Nodes to leftPane
        leftPane.add(lblEmgcMsgActiveUsers, 0,0);
        leftPane.add(userListView, 0, 2);
        
        userListView.setItems(userItems);
        
        emgncListView.setItems(emgncItems);
        
        //define controls/nodes for centerPane
        
        lblMessages.setText("Comminity Chats:");
        lblMessages.setFont(labelButtonFont);
        
        
        lblEmgcSituation.setText("Emergency Categories:");
        lblEmgcSituation.setFont(labelButtonFont);
        
        lblEmgcCorner.setText("List of Emergencies:");
        lblEmgcCorner.setFont(labelButtonFont);
        
        lblClient.setText("Logged In as : XXXXXXXX XXXXXXX");
        lblClient.setMaxWidth(182);
        lblClient.setMinWidth(182);
        //Setting content to display for the ListVIew
        messageListView.setItems(messageItem);
        
        //Button to send message
        
        btnSendMessage.setFont(labelButtonFont);
        
        //text field for typing message
        
        txtMessage.setPromptText("Enter Message Here!!!");
        txtMessage.setFont(userFont);
        
        
        btnViolence.setFont(titleFont);
        btnSecurity.setFont(titleFont);
        btnHealth.setFont(titleFont);
        
        btnViolence.setMinWidth(100);
        btnViolence.setMaxWidth(200);
        btnSecurity.setMinWidth(100);
        btnSecurity.setMaxWidth(200);
        btnHealth.setMinWidth(100);
        btnHealth.setMaxWidth(200);
        
        
        btnAlert.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        btnAlert.setVisible(false);
        
        //Add nodes/controls to centerPane
        centerPane.add(lblMessages, 0,0, 2,1);
        centerPane.add(lblEmgcSituation, 3,0);
        centerPane.add(messageListView, 0,2,1,9);
        
        centerPane.add(btnViolence, 3,2);
        centerPane.add(btnSecurity, 3,4);
        centerPane.add(btnHealth, 3,6);
        centerPane.add(emgncListView, 3,7);
        centerPane.add(btnAlert, 3,8);
        
        //set topPane noces/conttrols
        topPane.add(lblEmgcTitle, 2,0);
        topPane.add(lblEmgcSubTitle, 2,1);
        
        //Set bottom pane controls/nodes
        bottomPane.add(txtMessage, 2, 0);
        bottomPane.add(btnSendMessage,3,0);
        bottomPane.add(lblClient, 0, 0);
        
        //centerPane.add(txtMessage, 0,7);
        //centerPane.add(btnSendMessage, 2,7);
        
        emgcBorderPane.setLeft(leftPane);
        emgcBorderPane.setTop(topPane);
        emgcBorderPane.setRight(rightPane);
        emgcBorderPane.setCenter(centerPane);
        emgcBorderPane.setBottom(bottomPane);
        
        emgcBorderPane.setMaxHeight(500);
        emgcBorderPane.setMaxWidth(700);
        
        System.out.println("Emergency Interface Exucuted");
        
        return emgcBorderPane;
    }
    
    
    
    
    void Authenticate(Stage primaryStage){
        System.err.println("Client Authenticate() Method Start Executing.");
        userName = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        String msg, errMsg = "";
        
        if(userName.isEmpty()){
            errMsg = "User Email Field Cannot be Empty!!!";
            
        }
        if(pass.isEmpty()){
            errMsg = "\nUser Password Field Cannot be Empty!!!";
        }
        
        if(errMsg.isEmpty()){
            try{
                //Prepare data and send to Server
                //format: FunctionName:Parameter1#Parameter2#...#ParameterN
                msg = "AuthenticateUser:"+ userName+"#"+pass;
                sendData.writeUTF(msg);
                
            }catch (IOException ex) {
                System.err.println(ex);
            }
        }else{
            final String err = errMsg;
            Platform.runLater(()->{
                lblLoginError.setTextFill(Color.RED);
                lblLoginError.setText(err);
                System.err.println("Connection refused.");
            });
        }
        /*
        //After sucessful authentication of user, then GoSAVE main interface is launched
        Platform.runLater(() -> {
            userEmgc = EmergencyInterface();
            emgcScene = new Scene(userEmgc);
            primaryStage.setTitle("GoSAVE: An Emergency Intervention Request System");
            primaryStage.setScene(emgcScene);
            primaryStage.setResizable(false);
            primaryStage.show();
        });
        */
        System.err.println("Client Authenticate() Method finished Executing.");
    }
    
    
    
    
    void Register(){
        System.err.println("Client Register() Method started Executing.");
        String fname,lname,email, pass, cPass, msg, errMsg="";
        //boolean val = false;
        
        fname = txtRegFirstname.getText().trim();
        lname = txtRegLastname.getText().trim();
        email = txtRegUsername.getText().trim();
        pass = txtRegPassword.getText();
        cPass = txtRegConfPassword.getText();
        userName = email;
        if(fname.isEmpty()){
            //val = true;
            errMsg = "Firstname Field Cannot be Empty!!!";
        }
        if(lname.isEmpty()){
            //val = true;
            errMsg += "\nLastname Field Cannot be Empty!!!";
        }
        if(email.isEmpty()){
            errMsg += "\nEmail Field Cannot be Empty!!!";
        }
        if(pass.isEmpty() || cPass.isEmpty()){
             
            errMsg += "\nPassword or Confirm Password Field Cannot be Empty!!!";
        }
        if(!pass.equals(cPass)){
            errMsg += "\nPassword Mismatch!!!";
        }
        if(errMsg.isEmpty()){
            try{
                //Prepare data and send to Server
                //format: FunctionName:Parameter1#Parameter2#...#ParameterN
                msg = "RegisterUser:"+ fname+"#"+lname+"#"+email+"#"+pass;
                sendData.writeUTF(msg);
            }catch (IOException ex) {
                System.err.println(ex);
            }
        }else{
            //Update User interface with corresponding error message
            final String err = errMsg;
            Platform.runLater(()->{
                lblRegError.setTextFill(Color.RED);
                //lblRegError.setText("Registration Unsuccessful. Try Again!!!");
                lblRegError.setText(err);
                System.err.println("There was problem registering account. try again."+err);
            });
            
        }
        System.out.println("Client Register() finished execution.");
    }
    
    
    
    void ReceiveEmergencyAlert(String s){
        Platform.runLater(()->{
            //String s = "Health Emergency @ 2509 Snow Khight Drive Oshawa, Ontario";
            System.out.println(s);
            btnAlert.setText("");
            btnAlert.setWrapText(true);
            btnAlert.setVisible(true);
            //btnAlert.maxWidth(20);
            btnAlert.setMaxWidth(150);
            btnAlert.setMinHeight(50);
            btnAlert.setFont(labelButtonFont);
            
            btnAlert.setText(s);
            //Label label = new Label("Blink");
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), btnAlert);

            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setCycleCount(Animation.INDEFINITE);
            fadeTransition.play();
        });
    }
    void SendEmergencyAlert(String s){
        
        //Emergency Alert button Clicked. Send the alert to server for publicity/Intervention request
        
        try{
            String emgcMsg = s +": "+ userName;
            sendData.writeUTF(emgcMsg);
            
        }catch(IOException e){
            System.err.println(e);
        }
        
        
    }
    
    
    
    
    void DisableAlert(){
        
        //Users can stop alert from boozing lights
        btnAlert.setVisible(false);
    }
    
    
    
    
    private void closeSocketExit() {
        try {
            System.out.println("Client Clicked Close Button to terminate application.");
            //If socket doesn't exit, no need to close.
            if(clientConnection!=null){
                clientConnection.close();
            }
            Platform.exit();    // Close UI.
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    void ReceiveMessages(){
        
        System.out.println("Client ReceivedMessages() Started Execution.");
        try{
            
            while(connection){
                
                String msg = receiveData.readUTF();
                System.out.println(msg);
                if(!joined){
                    if (msg.startsWith("UserAccount:Successfully") || (msg.startsWith("AuthenticateUser:Successfully"))){
                        Platform.runLater(() -> {
                            System.out.println("User Connected as "+ userName);
                            userEmgc = EmergencyInterface();
                            emgcScene = new Scene(userEmgc);
                            lblClient.setTextFill(Color.GREEN);
                            lblClient.setText("Joined as " + userName);

                            emgcPrimaryStage.setTitle("GoSAVE: An Emergency Intervention Request System");
                            emgcPrimaryStage.setScene(emgcScene);
                            emgcPrimaryStage.setResizable(false);
                            emgcPrimaryStage.show();
                            

                        });
                    }else if(msg.startsWith("UserAccount:Unsuccessful Registration")){
                        Platform.runLater(() -> {
                            System.out.println("Unsuccessful User Registration: "+ userName);
                            lblLoginError.setText("Unsuccessful Registration. User Already Registered!!!");
                            
                        });
                    }else if(msg.startsWith("AuthenticateUser:InvalidCredential") ){
                        Platform.runLater(() -> {
                            System.out.println("User Inavlid Credential/Authentication of user: "+ userName);
                            lblLoginError.setText("Invalid Credentials. Try Again!!!");
                            
                        });
                    
                    }else if(msg.startsWith("[")){
                        //msg = receiveData.readUTF();
                        AppendMessageListView(msg);
                    }else if((msg.startsWith("Violence")) || (msg.startsWith("Health")) ||(msg.startsWith("Security"))){
                        //String ms[] = msg.split(":");
                        ReceiveEmergencyAlert(msg);
                        AppendEmgncListView(msg);
                        
                    }else{
                        Platform.runLater(() -> {
                            messageItem.add(msg);
                        });
                    }/*else if(msg.equals(userName)){
                        Platform.runLater(() -> {
                            // Update UI here.
                            txtUsername.clear();
                            lblLoginError.setTextFill(Color.RED);
                            lblLoginError.setText("User with same name exist.");
                        });
                    }
                    */
                    //CheckAuthentication();
                }
                
                
            }
            
        }catch (IOException ex) {
            System.out.println("Socket is closed.receive");
            Platform.runLater(() -> {
                lblLoginError.setTextFill(Color.RED);
                lblLoginError.setText("Connection Error. Try Again!!!");
            });
            connection = false;
        }
        System.out.println("Client ReceiveMessages() Finished Executin.");
    }
    
    private void CheckAuthentication()  {
        System.out.println("Client CheckedAuthentication() started execution.");
        String response;
        try {
            response = receiveData.readUTF();
            System.out.println(response);
            if (response.startsWith("UserAccount:Successfully")){
                joined = true;
                //After sucessful authentication of user, then GoSAVE main interface is launched
        
                Platform.runLater(() -> {
                    System.out.println("User Connected as "+ userName);
                    userEmgc = EmergencyInterface();
                    emgcScene = new Scene(userEmgc);
                    lblClient.setTextFill(Color.GREEN);
                    lblClient.setText("Joined as " + userName);
                    
                    emgcPrimaryStage.setTitle("GoSAVE: An Emergency Intervention Request System");
                    emgcPrimaryStage.setScene(emgcScene);
                    emgcPrimaryStage.setResizable(false);
                    emgcPrimaryStage.show();
                    
                });
                
            }else if(response.equals(userName)){
                Platform.runLater(() -> {
                    // Update UI here.
                    txtUsername.clear();
                    lblLoginError.setTextFill(Color.RED);
                    lblLoginError.setText("User with same name exist.");
                });
            }
        } catch (IOException e) {
            System.out.println("Socket is closed.add");
            Platform.runLater(() -> {
                lblLoginError.setTextFill(Color.RED);
                lblLoginError.setText("Unable to establish connection.");
                connection = false;
            });
        }
        System.out.println("Client CheckAuthentication() finished execution.");
    }
    
    private void AppendMessageListView(String s) {
        List<String> list =
                Arrays.asList(
                        s.substring(1, s.length() - 1).split(", ")
        );
        Platform.runLater(() -> {
            // Update UI here.
            userItems.clear();
            for(int i = 0; i < list.size(); i++){
                if(!(list.get(i).equals(userName))){
                    userItems.add(list.get(i));
                }
            }
        });
        
        
    }
    
    private void AppendEmgncListView(String s) {
       Platform.runLater(() -> {
            // Update UI here.
            emgncItems.add(s);

        });
        
        
    }
    

    void Send(){
        String msg = txtMessage.getText().trim();
        if(!msg.isEmpty()){
            try{
                msg = userName + ": \n"+ msg;
                sendData.writeUTF(msg);
                txtMessage.setText("");
            }catch(IOException e){
                System.err.println(e);
            }
        }
    }
}
