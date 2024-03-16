package com.anshu.antakshari;

import static com.anshu.antakshari.MainActivity.main_time_in_milli_sec;
import static com.anshu.antakshari.MainActivity.timeWord;
import static com.anshu.antakshari.R.layout.roomjoinactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainSelection extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference roomNo=db.document("Room No/RoomCode");
    TextView roomNoOut,outName,noOfPlayersOut;
    int iterator=1;
    //int noOfPlayerCount=0;
    boolean createRoomClicked=false,joinRoomClicked=false;
    EditText inputRoomNo;
    String getRoomNo;
    static String currRoomNo,nameForStartGame="";
    Map<String,String> names_CreateRoom,roomno,names_JoinRoom,startGame,startGameInit,namesOnServer;
    Map<String,Long> noOfPoeple_CreateRoom,noOfPoeple_JoinRoom;
    static boolean isHost=false;
    String n="",nameofPlayer;
    boolean firstcall=true,isOnline=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(roomjoinactivity);
        roomNoOut= findViewById(R.id.RoomNo);
        outName= findViewById(R.id.nameOfPlayers);
        inputRoomNo= findViewById(R.id.InputRoomNo);
        //noOfPlayersOut=findViewById(R.id.playersCount);
        roomno = new HashMap<>();
        names_CreateRoom = new HashMap<>();
        names_JoinRoom = new HashMap<>();
        noOfPoeple_CreateRoom = new HashMap<>();
        noOfPoeple_CreateRoom.put("No", 1L);
        noOfPoeple_JoinRoom = new HashMap<>();
        startGame=new HashMap<>();
        startGameInit=new HashMap<>();
        namesOnServer=new HashMap<>();
        startGame.put("Status","start");
        startGameInit.put("Status","Stop");
        SharedPreferences getShared=getSharedPreferences("digANT",MODE_PRIVATE);
        nameofPlayer=getShared.getString("Name","Null");




    }
        public void createRoom(View v)
        {
            /*first getting last played room no from server
            then incrementing it by 1 which acts as current room no
            then taking name of player from textfield
            then stroing the name by creating anew collection with room no as its name
            on success updating database with current room no for next match
            then setting default no of people 1
            */
          if(!createRoomClicked) {
              createRoomClicked = true;
              isHost = true;
              roomNo.get()      //document reference for current room no
                      .addOnSuccessListener(documentSnapshot -> {
                          getRoomNo = documentSnapshot.getString("Code");//code is field whose value is current room no
                          assert getRoomNo != null;
                          currRoomNo = String.valueOf(Integer.parseInt(getRoomNo) + 1);//roomno incremented by 1 as the value returned is prv room no
                          roomNoOut.setText("Room No is:" + currRoomNo);
                          names_CreateRoom.put("name", nameofPlayer);//hashmap store data
                          db.collection(currRoomNo).document("name1")
                                  .set(names_CreateRoom)  //inserting host name into database
                                  .addOnSuccessListener(aVoid -> {
                                      roomno.put("Code", currRoomNo);//hashmap
                                      roomNo.set(roomno);//updating database with current room no
                                      db.collection(currRoomNo).document("NoOfPlayers")
                                              .set(noOfPoeple_CreateRoom);//setting default no of people 1
                                      Toast.makeText(MainSelection.this, "Room Created Successfully", Toast.LENGTH_SHORT).show();
                                      if (firstcall) {
                                          playersDisplay();
                                          StartOnline();
                                      }
                                      firstcall = false;
                                     // noOfPlayerCount++;
                                     // noOfPlayersOut.setText("No of Players : "+noOfPlayerCount);
                                  })
                                  .addOnFailureListener(e -> {

                                  });
                      })
                      .addOnFailureListener(e -> Toast.makeText(MainSelection.this, "Document doesn't exist", Toast.LENGTH_SHORT).show());
          }
          else
              Toast.makeText(MainSelection.this, "Please Wait!", Toast.LENGTH_SHORT).show();
        }
        public void joinRoom(View v)
        {
           if(!joinRoomClicked) {
               joinRoomClicked = true;
               String InputRoomNo = inputRoomNo.getText().toString();//taking inputroom no
               currRoomNo = InputRoomNo;
               db.collection(InputRoomNo).document("NoOfPlayers").get()
                       .addOnSuccessListener(documentSnapshot -> {
                           final long noPlayer = documentSnapshot.getLong("No");//getting no of players in current room
                           names_JoinRoom.put("name", nameofPlayer);//getting input of name and setting value
                           if (firstcall) {
                               playersDisplay();
                               StartOnline();
                           }
                           firstcall = false;
                           db.collection(inputRoomNo.getText().toString()).document("name" + (noPlayer + 1)) //giving the current player  +1 value than prv
                                   .set(names_JoinRoom).addOnSuccessListener(aVoid -> {
                                       Toast.makeText(MainSelection.this, "Room Joined Successfully", Toast.LENGTH_SHORT).show();
                                       //noOfPlayerCount=(int)noPlayer+1;
                                       roomNoOut.setText("Room No : "+InputRoomNo);
                                       noOfPoeple_JoinRoom.put("No", noPlayer + 1);//updating database for no of player
                                       db.collection(InputRoomNo).document("NoOfPlayers").set(noOfPoeple_JoinRoom);
                                   });
                       });
           }
           else
               Toast.makeText(MainSelection.this, "Joining Room... Please Wait!", Toast.LENGTH_SHORT).show();
        }

        public void Start(View v)
        {
            //game start method
            if(isHost) {

                db.collection(currRoomNo).document("startStatus").set(startGame);
           }
            else
                Toast.makeText(MainSelection.this,"You are not a host. You cannot start the game!", Toast.LENGTH_SHORT).show();

        }

        public void playersDisplay() {
            if (isHost) {
                db.collection(currRoomNo).document("NoOfPlayers").addSnapshotListener((value, error) -> db.collection(currRoomNo).document("name" + iterator).get(). //retrieving names of players in current room
                        addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                n = documentSnapshot.getString("name");
                                nameForStartGame += n + ",";//storing names in a string
                                namesOnServer.put("Names",nameForStartGame); //init the name list hashmap to put on server
                                db.collection(currRoomNo).document("NamesString").set(namesOnServer);// setting names on server
                                outName.setText(nameForStartGame);//outputting names
                                iterator++;
                            }
                        }));
            }
            else
            {
                db.collection(currRoomNo).document("NamesString").addSnapshotListener((value, error) -> db.collection(currRoomNo).document("NamesString").get().addOnSuccessListener(documentSnapshot -> {
                    nameForStartGame=documentSnapshot.getString("Names");
                    outName.setText(nameForStartGame);

                }));

            }
        }

        public void StartOnline()
        {
            //game init method
            db.collection(currRoomNo).document("startStatus").set(startGameInit).addOnSuccessListener(unused -> db.collection(currRoomNo).document("startStatus").addSnapshotListener((value, error) -> {
                assert value != null;
                if (Objects.requireNonNull(value.getString("Status")).equalsIgnoreCase("Start")) {
                    main_time_in_milli_sec = 600000;
                    timeWord = 30000;
                    int len = nameForStartGame.length();
                    String[] name = nameForStartGame.substring(0, len).split(",");
                    char nextLetter = 'c';
                    isOnline = true;
                    nameForStartGame="";
                    outName.setText("");
                    roomNoOut.setText("");
                    inputRoomNo.setText("");
                    iterator=1;
                    createRoomClicked=joinRoomClicked=false;
                    Intent game = new Intent(MainSelection.this, game.class);
                    game.putExtra("nameArray",name);
                    game.putExtra("isOnline",isOnline);
                    game.putExtra("nextL",nextLetter);
                    startActivity(game);
                    finish();
                }
            }));
        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nameForStartGame = "";
        outName.setText("");
        roomNoOut.setText("");
        inputRoomNo.setText("");
        iterator = 1;
        //startActivity(new Intent(this, GameMode.class));
        //finish();
    }
}
