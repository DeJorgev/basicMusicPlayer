package com.jgarcia.musemusicplayerv01.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jgarcia.musemusicplayerv01.R;
import com.jgarcia.musemusicplayerv01.Song;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {
    private ListView songListView;
    private String[]  songNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songListView = findViewById((R.id.SongListView));

        if (!EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(MainActivity.this, "Requesting permission to access storage", 102, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        displaySongs();

    }

//Methods for fetching Songs
    //Groups the necessary code in a more readable and comprehensive way, or so I hope.
    private List<Song> manageSongsFetch(){
        //Prepares the Query statement values;
        Uri songFolderUri = checkDeviceVersion();
        String [] projection = projectionFabric();
        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        //Executes the Query and saves the selected song´s data in a List
        List<Song> songsList = fetchSongs(songFolderUri, projection, sortOrder);

        Toast debug = Toast.makeText(MainActivity.this, "Number of Songs: " + songsList.size(), Toast.LENGTH_SHORT);
        debug.show();

        return songsList;
    }
    
    private Uri checkDeviceVersion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            return  MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        else
            return  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    //Creates a String array with the column´s name of song´s data to get
    private String [] projectionFabric(){
        String [] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,};

        return projection;
    }

    //TODO: Add a switch with different types of sorts dependant of the parameter value
    private String generateSortOrder(int sortTipe){
        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + "ASC";

        return sortOrder;
    }

    private List<Song> fetchSongs(Uri songFolderUri,String [] projection,String sortOrder){
        List<Song> songsList = new ArrayList<>();

        //Querying
        try (Cursor cursor = getContentResolver().query(songFolderUri, projection,null,null,sortOrder)) {
            //Columns in the Query
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int albumIDColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            //getting the actual values for each column of each file read and applied
            while(cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                long albumID = cursor.getLong(albumIDColumn);

                Uri songuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                Uri albumImageUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);

                //Removing extension on name.
                name = name.substring(0,name.lastIndexOf("."));

                //Creating and adding song Item to List
                Song song = new Song(id, albumID, duration, name, songuri,albumImageUri);
                songsList.add(song);
            }
        }
        return songsList;
    }

//Displays the results of manageSongsFetch() on the list view item using (For the moment) a default adapter.
    private void displaySongs(){
        List<Song> songsInDevice = manageSongsFetch();

        songNames = new String [songsInDevice.size()];

        for (int i = 0; i < songNames.length; i++)
            songNames[i] = songsInDevice.get(i).getName();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,songNames);
        songListView.setAdapter(arrayAdapter);

        launchPlayerActivity(songsInDevice);
    }

//Methods to manage data and creation between activities
    private void launchPlayerActivity(List<Song> songsInDevice){
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) songListView.getItemAtPosition(i);

                Intent intentLaunch = new Intent(getApplicationContext(),PlayerActivity.class);
                intentLaunch
                        .putExtra("songUri", songsInDevice.get(i).getSongUri().toString())
                        .putExtra("songName", songName)
                        .putExtra("positionOnListView",i );

                startActivityForResult(intentLaunch, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast lastSongName;
        if (data!= null)
            lastSongName = Toast.makeText(this,"The last Song was: \n" + data.getStringExtra("lastSongName"), Toast.LENGTH_SHORT);
        else
            lastSongName = Toast.makeText(this, "data Null", Toast.LENGTH_SHORT);

        lastSongName.show();
    }

//Various
    //Overrides using EasyPermissions library for a simpler code. By Google
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}