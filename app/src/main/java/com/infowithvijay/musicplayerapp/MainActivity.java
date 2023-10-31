package com.infowithvijay.musicplayerapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.infowithvijay.musicplayerapp.Adapter.ViewPagerAdapter;
import com.infowithvijay.musicplayerapp.DB.FavoritesOperations;
import com.infowithvijay.musicplayerapp.Fragments.AllSongFragment;
import com.infowithvijay.musicplayerapp.Fragments.FavSongFragment;
import com.infowithvijay.musicplayerapp.Model.SongsList;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , AllSongFragment.createDataParse
        , FavSongFragment.createDataParsed

{


    private ImageButton imgBtnPlayPause, imgBtnReplay, imgBtnPrev, imgBtnNext, imgBtnSetting;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SeekBar seekBarContoller;

    private TextView tvCurrentTime, tvTotalTime;

    private Menu menu;

    private final int MY_PERMISSION_REQUEST = 100;
    private int allSongLength;

    private ArrayList<SongsList> songList;
    private int currentPosition;
    private String searchText = "";

    private boolean checkFlag = false, repeatFlag = false,
            playContinueFlag = false, favFlag = true, playlistFlag = false;


    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        grantedPermission();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgBtnReplay = findViewById(R.id.img_btn_replay);
        imgBtnSetting = findViewById(R.id.img_btn_setting);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        FloatingActionButton refreshSongs = findViewById(R.id.btn_refresh);
        seekBarContoller = findViewById(R.id.seekbar_controller);

        viewPager = findViewById(R.id.songs_viewpager);

        imgBtnPlayPause = findViewById(R.id.img_btn_play);
        Toolbar toolbar = findViewById(R.id.toolbar);

        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        toolbar.setTitleTextColor(getColor(R.color.textColor));
        setSupportActionBar(toolbar);


        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgBtnReplay.setOnClickListener(this);
        refreshSongs.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnSetting.setOnClickListener(this);


    }

    private void grantedPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Provide the Storage Permissions!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            setPagerLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                        setPagerLayout();
                    } else {
                        Toast.makeText(this, "Provide the Storage Permission", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        }
    }

    private void setPagerLayout() {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),getContentResolver());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        tabLayout = findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu,menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchText = newText;
                queryText();
                setPagerLayout();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){


            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_favorites:
                if (checkFlag)
                    if (mediaPlayer != null){
                        if (favFlag){

                            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                            item.setIcon(R.drawable.ic_favorite_filled);
                            SongsList favList = new SongsList(songList.get(currentPosition).getTitle(),
                                    songList.get(currentPosition).getSubTitle(),songList.get(currentPosition).getPath()
                            );

                            FavoritesOperations favoritesOperations = new FavoritesOperations(this);
                            favoritesOperations.addSongFav(favList);
                            setPagerLayout();
                            favFlag = false;

                        }else {
                            item.setIcon(R.drawable.favorite_icon);
                            favFlag = true;
                        }
                    }
                return true;


            case android.R.id.home:
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

       switch (v.getId())
       {
           case R.id.img_btn_play:
               if (checkFlag){
                   if (mediaPlayer.isPlaying()){
                       mediaPlayer.pause();
                       imgBtnPlayPause.setImageResource(R.drawable.ic_play);
                   }else if (!mediaPlayer.isPlaying()){
                       mediaPlayer.start();
                       imgBtnPlayPause.setImageResource(R.drawable.ic_pause);
                       playCycle();
                   }
               }else {
                   Toast.makeText(this, "Select the Song", Toast.LENGTH_SHORT).show();
               }
               break;
           case R.id.btn_refresh:
               Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
               setPagerLayout();
               break;
           case R.id.img_btn_replay:

              if (repeatFlag) {
                  Toast.makeText(this, "Replay Removed", Toast.LENGTH_SHORT).show();
                  mediaPlayer.setLooping(false);
                  repeatFlag = false;
              }else{
                  Toast.makeText(this, "Replay Added", Toast.LENGTH_SHORT).show();
                  mediaPlayer.setLooping(true);
                  repeatFlag = true;
              }

              break;

           case R.id.img_btn_next:
               if (checkFlag){
                   if (currentPosition + 1 < songList.size()){
                       attachMusic(songList.get(currentPosition + 1).getTitle()
                               ,songList.get(currentPosition + 1).getPath());
                       currentPosition += 1;
                   }else {
                       Toast.makeText(this, "PlayList Ended", Toast.LENGTH_SHORT).show();
                   }
               }else {
                   Toast.makeText(this, "Select the Song", Toast.LENGTH_SHORT).show();
               }
               break;

           case R.id.img_btn_previous:
               if (checkFlag){

                   if (mediaPlayer.getCurrentPosition() > 10){
                       if (currentPosition - 1 > -1){
                           attachMusic(songList.get(currentPosition - 1).getTitle()
                                   ,songList.get(currentPosition - 1).getPath());
                           currentPosition = currentPosition - 1;
                       }else {

                           attachMusic(songList.get(currentPosition).getTitle()
                                   ,songList.get(currentPosition).getPath());
                       }
                   }else {

                       attachMusic(songList.get(currentPosition).getTitle()
                               ,songList.get(currentPosition).getPath());
                   }

               }else {
                   Toast.makeText(this, "Select the Song", Toast.LENGTH_SHORT).show();
               }

               break;

           case R.id.img_btn_setting:

               if (!playContinueFlag){
                   playContinueFlag = true;
                   Toast.makeText(this, "Loop Added", Toast.LENGTH_SHORT).show();
               }else {
                   playContinueFlag = false;
                   Toast.makeText(this, "Loop Removed", Toast.LENGTH_SHORT).show();
               }
               break;

       }


    }

    private void attachMusic(String name,String path){

        imgBtnPlayPause.setImageResource(R.drawable.ic_play);
        setTitle(name);
        menu.getItem(1).setIcon(R.drawable.favorite_icon);

        favFlag = true;

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            setControls();
        }catch (Exception e){
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imgBtnPlayPause.setImageResource(R.drawable.ic_play);
                if (playContinueFlag){
                    if (currentPosition + 1 < songList.size()){
                        attachMusic(songList.get(currentPosition + 1).getTitle(),
                                   songList.get(currentPosition + 1).getPath());
                        currentPosition += 1;
                    }else {
                        Toast.makeText(MainActivity.this, "PlayList Ended", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void setControls(){

        seekBarContoller.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        checkFlag = true;
        if (mediaPlayer.isPlaying()){
            imgBtnPlayPause.setImageResource(R.drawable.ic_pause);
            tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
        }

        seekBarContoller.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if (fromUser){
                   mediaPlayer.seekTo(progress);
                   tvCurrentTime.setText(getTimeFormatted(progress));
               }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void playCycle(){

        try {

            seekBarContoller.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()){
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();
                    }
                };

                handler.postDelayed(runnable,100);
            }

        }catch (Exception e){
            e.printStackTrace();
        }


   }

    private String getTimeFormatted(long milliSeconds){

        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliSeconds/3600000);
        int minutes = (int) (milliSeconds % 3600000 /60000);
        int seconds = (int) (milliSeconds % 3600000 % 60000 / 1000);

        if (hours>0)
            finalTimerString = hours + ":";

        if (seconds < 10 )
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;

    }

    @Override
    public void onDataPass(String name, String path) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        attachMusic(name,path);
    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {

        this.songList = songList;
        this.currentPosition = position;
        this.playlistFlag = songList.size() == allSongLength;
        this.playContinueFlag = !playlistFlag;

    }

    @Override
    public int getPosition() {
        return currentPosition;
    }

    @Override
    public String queryText() {
        return searchText.toLowerCase();
    }

    @Override
    public void getLength(int length) {
        this.allSongLength = length;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
        handler.removeCallbacks(runnable);

    }

}
