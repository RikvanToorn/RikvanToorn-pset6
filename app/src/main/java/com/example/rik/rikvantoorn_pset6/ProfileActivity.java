package com.example.rik.rikvantoorn_pset6;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {



    private FirebaseAuth firebaseAuth;

    private TextView textViewUserEmail;
    private Button artistButton;
    private ImageButton settings;
    private DatabaseReference databaseReference;
    private String selectedFromList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        settings = (ImageButton) findViewById(R.id.imageButtonSettings);
        settings.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(user.getUid());
        DatabaseReference childref = databaseReference.child("Artists");

        final ListView addedArtists = (ListView) findViewById(R.id.addedArtists);

        final List<Artist> Artists = new ArrayList<>();

        final ArtistAdapter adapter = new ArtistAdapter(getApplicationContext(), R.layout.artist_row, Artists);
        addedArtists.setAdapter(adapter);


        childref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, String> map= (Map)dataSnapshot.getValue();


                    String name = map.get("name");
                    String events = map.get("events");
                    Artist artist = new Artist(name, events);
                    Artists.add(artist);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map<String, String> map= (Map)dataSnapshot.getValue();
                String name = map.get("name");

                Iterator<Artist> it = Artists.iterator();
                while (it.hasNext()) {
                    Artist artist = it.next();
                    if (artist.getname().equals(name)) {
                        it.remove();
                    }
                }
                Toast.makeText(ProfileActivity.this, name + " Removed" , Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addedArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view.findViewById(R.id.nameTextView);
                String nameOfArtist = text.getText().toString();
                selectedFromList = nameOfArtist;
                new getArtist().execute();
            }
        });





        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);

        textViewUserEmail.setText("Welcome " + user.getEmail());

        artistButton = (Button) findViewById(R.id.artistSearchButton);

        artistButton.setOnClickListener(this);
    }
    public boolean showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
        return true;
    }



    @Override
    public void onClick(View v) {
        if(v == settings) {
            showPopup(v);
        }
        if(v == artistButton) {
            new getArtist().execute();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                return true;
            default:
                return false;

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

            if (selectedFromList != null) {
                artist = selectedFromList.replace(" ", "%20");
                selectedFromList = null;
            } else {
                artist = artist.replace(" ", "%20");
            }

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


            if(jObject.has("name")) {
                Intent movieInfo = new Intent(ProfileActivity.this, ArtistActivity.class);
                movieInfo.putExtra("artist", jsonString);
                movieInfo.putExtra("artistEvent", jsonStringEvent);
                startActivity(movieInfo);
            } else {
                Toast.makeText(ProfileActivity.this, "Can't find the artist!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class ArtistAdapter extends ArrayAdapter {

        private List<Artist> artistList;
        private int resource;
        private LayoutInflater inflater;
        public ArtistAdapter(Context context, int resource, List<Artist> objects) {
            super(context, resource, objects);
            artistList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = inflater.inflate(resource, null);
            }

            TextView nameTextView;
            TextView eventsTextView;

            nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            eventsTextView = (TextView) convertView.findViewById(R.id.eventsTextView);

            nameTextView.setText(artistList.get(position).getname());
            eventsTextView.setText(artistList.get(position).getevents());


            return convertView;
        }
    }
}

