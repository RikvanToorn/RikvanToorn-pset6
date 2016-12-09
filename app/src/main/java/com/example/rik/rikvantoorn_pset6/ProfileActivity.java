package com.example.rik.rikvantoorn_pset6;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    private TextView textViewUserEmail;
    private Button buttonLogout;
    private Button artistButton;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(user.getUid());
        DatabaseReference childref = databaseReference.child("todo");

        final ListView listView = (ListView) findViewById(R.id.addedArtists);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        listView.setAdapter(adapter);

        childref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                adapter.add(value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);

        textViewUserEmail.setText("Welcome "+ user.getEmail());

        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        artistButton = (Button) findViewById(R.id.artistSearchButton);

        buttonLogout.setOnClickListener(this);
        artistButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == buttonLogout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if(v == artistButton) {
            new getArtist().execute();
        }
    }

    class getArtist extends AsyncTask<Void, Void, Void> {

        JSONObject jObject;
        String jsonString = "";

        JSONObject jObjectEvent;
        String jsonStringEvent = "";

        EditText artistName = (EditText) findViewById(R.id.editTextSearch);
        String artist = artistName.getText().toString();



        @Override
        protected Void doInBackground(Void... voids) {

            artist = artist.replace(" ", "%20");

            try {
                URL artistLink = new URL("http://api.bandsintown.com/artists/" + artist + ".json?api_version=2.0&app_id=YOUR_APP_ID");
                URL artistEventLink = new URL("http://api.bandsintown.com/artists/" + artist + "/events/search.json?api_version=2.0&app_id=YOUR_APP_ID&location=use_geoip");

                try {

                    HttpURLConnection connection = (HttpURLConnection) artistLink.openConnection();
                    BufferedReader artistReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder artistInfo = new StringBuilder();
                    String line;

                    while ((line = artistReader.readLine()) != null){
                        artistInfo.append(line + "\n");
                    }

                    jsonString = artistInfo.toString();

                    try {
                        jObject = new JSONObject(jsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    HttpURLConnection connectionEvent = (HttpURLConnection) artistEventLink.openConnection();
                    BufferedReader artistReaderEvent = new BufferedReader(new InputStreamReader(connectionEvent.getInputStream()));
                    StringBuilder artistInfoEvent = new StringBuilder();
                    String lineEvent;

                    while ((lineEvent = artistReaderEvent.readLine()) != null){
                        artistInfoEvent.append(lineEvent + "\n");
                    }

                    jsonStringEvent = artistInfoEvent.toString();

                    try {
                        jObjectEvent = new JSONObject(jsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (jObject.has("errors")) {
                Toast.makeText(ProfileActivity.this, "Can't find the artist!", Toast.LENGTH_SHORT).show();
            } else {
                Intent movieInfo = new Intent(ProfileActivity.this, ArtistActivity.class);
                movieInfo.putExtra("artist", jsonString);
                movieInfo.putExtra("artistEvent", jsonStringEvent);
                startActivity(movieInfo);
            }
        }
    }
}
