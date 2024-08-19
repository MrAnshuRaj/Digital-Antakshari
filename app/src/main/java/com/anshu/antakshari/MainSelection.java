package com.anshu.antakshari;

import static com.anshu.antakshari.MainActivity.main_time_in_milli_sec;
import static com.anshu.antakshari.MainActivity.timeWord;
import static com.anshu.antakshari.R.layout.roomjoinactivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainSelection extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference roomNo=db.document("Room No/RoomCode");
    private StorageReference storageRef;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    TextView roomNoOut,outName,noOfPlayersOut,waitTextTv;
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
    private boolean isGoogleSignIn;
    private ArrayList<String> urlList;
    Button createRoomBtn,joinRoomBtn;
    CardView createRoomCard,joinRoomCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(roomjoinactivity);
        roomNoOut= findViewById(R.id.RoomNo);
        outName= findViewById(R.id.nameOfPlayers);
        inputRoomNo= findViewById(R.id.InputRoomNo);
        createRoomBtn= findViewById(R.id.button10);
        joinRoomBtn= findViewById(R.id.button9);
        createRoomCard= findViewById(R.id.CreateRoomCardView);
        joinRoomCard= findViewById(R.id.JoinRoomCardView);
        waitTextTv= findViewById(R.id.textView23);
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
        urlList=new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        currentUser=auth.getCurrentUser();
        SharedPreferences getShared=getSharedPreferences("digANT",MODE_PRIVATE);
        nameofPlayer=getShared.getString("Name","Null");
        isGoogleSignIn=getShared.getBoolean("GoogleSignIn", false);




    }
        @SuppressLint("SetTextI18n")
        public void createRoom(View v)
        {
            /*first getting last played room no from server
            then incrementing it by 1 which acts as current room no
            then taking name of player from text field
            then storing the name by creating anew collection with room no as its name
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
                  currRoomNo = String.valueOf(Integer.parseInt(getRoomNo) + 1);//room no incremented by 1 as the value returned is prv room no
                  roomNoOut.setText("Room No is:" + currRoomNo);
                  names_CreateRoom.put("name", nameofPlayer);//hashmap store data
                  db.collection(currRoomNo).document("name1")
                  .set(names_CreateRoom)  //inserting host name into database
                  .addOnSuccessListener(aVoid -> {
                      roomno.put("Code", currRoomNo);//hashmap
                      roomNo.set(roomno);//updating database with current room no
                      db.collection(currRoomNo).document("NoOfPlayers")
                              .set(noOfPoeple_CreateRoom);//setting default no of people 1
                      joinRoomCard.setVisibility(View.GONE);
                      createRoomBtn.setVisibility(View.GONE);
                      Toast.makeText(MainSelection.this, "Room Created Successfully", Toast.LENGTH_SHORT).show();
                      if (firstcall) {
                          playersDisplay();
                          StartOnline();
                      }
                      firstcall = false;
                     // noOfPlayersOut.setText("No of Players : "+noOfPlayerCount);
                  });
                  if(isGoogleSignIn) {
                      HashMap<String, String> hashMap = new HashMap<>();
                      hashMap.put("URL", currentUser.getPhotoUrl().toString());
                      db.collection(currRoomNo).document("UserURL1").
                              set(hashMap);
                  }
                  else{
                      db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if (task.isSuccessful() && task.getResult() != null) {
                                  DocumentSnapshot documentSnapshot = task.getResult();
                                  if (documentSnapshot.exists()) {
                                      String URL = documentSnapshot.getString("ownerImage");
                                      HashMap<String,String> hashMap=new HashMap<>();
                                      hashMap.put("URL",URL);
                                      db.collection(currRoomNo).document("UserURL1").
                                              set(hashMap);
                                  }
                              }
                          }
                      });
                  }

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
               waitTextTv.setVisibility(View.VISIBLE);
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
                           joinRoomBtn.setVisibility(View.GONE);
                           createRoomCard.setVisibility(View.GONE);
                   });
                   if(isGoogleSignIn)
                   {
                       HashMap<String,String> hashMap=new HashMap<>();
                       hashMap.put("URL",currentUser.getPhotoUrl().toString());
                       db.collection(inputRoomNo.getText().toString()).document("UserURL"+(noPlayer+1)).
                               set(hashMap);
                   }
                       db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                               if (task.isSuccessful() && task.getResult() != null) {
                                   DocumentSnapshot documentSnapshot = task.getResult();
                                   if (documentSnapshot.exists()) {
                                       String URL = documentSnapshot.getString("ownerImage");
                                       HashMap<String,String> hashMap=new HashMap<>();
                                       hashMap.put("URL",URL);
                                       db.collection(currRoomNo).document("UserURL"+(noPlayer+1)).
                                               set(hashMap);
                                   }
                               }
                           }
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
           // Toast.makeText(this,"Starting the game... Please wait! ",Toast.LENGTH_SHORT).show();
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
                    nameForStartGame = "";
                    outName.setText("");
                    roomNoOut.setText("");
                    inputRoomNo.setText("");
                    iterator = 1;
                    createRoomClicked = joinRoomClicked = false;
                    CollectionReference collectionRef = db.collection(currRoomNo);
                    collectionRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String documentId = document.getId();
                                    if (documentId.startsWith("UserURL") && document.contains("URL")) {
                                        String url = document.getString("URL");
                                        if (url != null) {
                                            //Toast.makeText(MainSelection.this,"URL IS : "+url,Toast.LENGTH_LONG).show();
                                            urlList.add(documentId + "|" + url);  // Store the document ID and URL together
                                        }
                                    }
                                }

                                // Sort the URLs based on the document ID lexicographically
                                    Collections.sort(urlList);
                                    String urlArray[];
                                    // Convert the list to an array (storing only URLs)
                                    urlArray = new String[urlList.size()];
                                    for (int i = 0; i < urlList.size(); i++) {
                                        urlArray[i] = urlList.get(i).split("\\|")[1];
                                       // Toast.makeText(MainSelection.this,"URL IS : "+urlArray[i],Toast.LENGTH_LONG).show();
                                    }

                                Intent game = new Intent(MainSelection.this, game.class);

                                game.putExtra("urlList",urlArray);
                                game.putExtra("nameArray",name);
                                game.putExtra("isOnline",isOnline);
                                game.putExtra("nextL",nextLetter);
                                startActivity(game);
                                finish();

                            }

                        } else {
//                            Intent game = new Intent(MainSelection.this, game.class);
//                            game.putExtra("urlList",new String[1]);
//                            game.putExtra("nameArray",name);
//                            game.putExtra("isOnline",isOnline);
//                            game.putExtra("nextL",nextLetter);
//                            startActivity(game);
//                            finish();
                            Toast.makeText(MainSelection.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
    }
}
