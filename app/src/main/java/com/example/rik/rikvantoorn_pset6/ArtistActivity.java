package com.example.rik.rikvantoorn_pset6;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArtistActivity extends AppCompatActivity implements View.OnClickListener {

    // fire database authhetication
    private FirebaseAuth firebaseAuth;

    // get database reference
    private DatabaseReference databaseReference;

    // declare views
    private Button addButton;
    private TextView artistNameView;

    // declare strings
    private String artist;
    private String artistEvent;
    private String artistName;
    private TextView artistNameViewEvent;
    private ImageView artistImage;
    private String artistImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        //  get the JSON object and JSON array from the last activity
        Bundle extras = getIntent().getExtras();
        artist = extras.getString("artist");
        artistEvent = extras.getString("artistEvent");

        // make firebase connection
        firebaseAuth = FirebaseAuth.getInstance();

        // get user and check if logged in
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        // get unique user id to store artists in the database per user
        databaseReference = FirebaseDatabase.getInstance().getReference(user.getUid());

        // get the text and imageviews from the layout
        artistNameView = (TextView) findViewById(R.id.artistNameView);
        artistImage = (ImageView) findViewById(R.id.artistImage);
        addButton = (Button) findViewById(R.id.addToWatchList);

        // get Listview for all the events
        ListView artistsEventsList = (ListView) findViewById(R.id.artistsEventsList);

        // add onclicklistener to button
        addButton.setOnClickListener(this);

        // use data from JSON object and JSON array
        try {
            JSONObject jObject = new JSONObject(artist);
            JSONArray jsonArray = new JSONArray(artistEvent);

            List<ArtistEvents> ArtistEventsList = new ArrayList<>();

            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject eventObject = new JSONObject(jsonArray.getString(i));
                String title = eventObject.getString("title");
                String date = eventObject.getString("formatted_datetime");
                String venue = eventObject.getString("formatted_location");
                ArtistEvents artistEvents = new ArtistEvents(title, date, venue);

                ArtistEventsList.add(artistEvents);
            }

            EventAdapter adapter = new EventAdapter(getApplicationContext(), R.layout.event_row, ArtistEventsList);
            artistsEventsList.setAdapter(adapter);







            // set Text and imageview with the info from the JSON Object
            artistName = jObject.getString("name");
            artistImageUrl = jObject.getString("image_url");
            new ImageLoadTask(artistImageUrl, artistImage).execute();

            artistNameView.setText(artistName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // function to add a new artist to the listy in the database
    private void addToList() {
        DatabaseReference childRef = databaseReference.child("todo").push();
        childRef.setValue(artistName);
    }

    // onclicklisteners
    @Override
    public void onClick(View v) {
        if(v == addButton) {
            addToList();
        }


    }

    // gets a bitmap from the imageUrl
    // I got this from: http://stackoverflow.com/questions/18953632/how-to-set-image-from-url-for-imageview
    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }


        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    public class EventAdapter extends ArrayAdapter {

        private List<ArtistEvents> artistEventsList;
        private int resource;
        private LayoutInflater inflater;
        public EventAdapter(Context context, int resource, List<ArtistEvents> objects) {
            super(context, resource, objects);
            artistEventsList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = inflater.inflate(resource, null);
            }

            TextView titleTextView ;
            TextView dateTextView;
            TextView venueTextView;

            titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            venueTextView = (TextView) convertView.findViewById(R.id.venueTextView);

            titleTextView.setText(artistEventsList.get(position).gettitle());
            dateTextView.setText(artistEventsList.get(position).getdate());
            venueTextView.setText(artistEventsList.get(position).getvenue());


            return convertView;
        }
    }

    ;

}
