package com.infowithvijay.musicplayerapp.Fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import com.infowithvijay.musicplayerapp.Adapter.SongAdapter;
import com.infowithvijay.musicplayerapp.DB.FavoritesOperations;
import com.infowithvijay.musicplayerapp.Model.SongsList;
import com.infowithvijay.musicplayerapp.R;

import java.util.ArrayList;

public class FavSongFragment extends ListFragment {

    private FavoritesOperations favoritesOperations;

    public ArrayList<SongsList> songsList;
    public ArrayList<SongsList> newList;
    private ListView listView;

    private createDataParsed createDataParsed;



    public static Fragment getInstance(int position) {

        Bundle bundle = new Bundle();
        bundle.putInt("pos",position);
        FavSongFragment tabFragment = new FavSongFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        createDataParsed = (createDataParsed) context;
        favoritesOperations = new FavoritesOperations(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        listView = view.findViewById(R.id.list_playlist);
        setContent();
    }


    public void setContent(){

        boolean searchedList = false;
        songsList = new ArrayList<>();
        newList = new ArrayList<>();
        songsList = favoritesOperations.getAllFavorites();
        SongAdapter adapter = new SongAdapter(getContext(),songsList);

        if (!createDataParsed.queryText().equals("")){

           adapter = onQueryTextChange();
           adapter.notifyDataSetChanged();
           searchedList = true;
        }else {
            searchedList = false;
        }

        listView.setAdapter(adapter);

        final boolean finalSearchedList = searchedList;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!finalSearchedList){
                    createDataParsed.onDataPass(songsList.get(position).getTitle(),songsList.get(position).getPath());
                    createDataParsed.fullSongList(songsList,position);
                }else {

                    createDataParsed.onDataPass(newList.get(position).getTitle(),newList.get(position).getPath());
                    createDataParsed.fullSongList(songsList,position);

                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int postion, long l) {

                deleteOption(postion);
                return true;
            }
        });


    }



    public interface createDataParsed{

        void onDataPass(String name,String path);

        void fullSongList(ArrayList<SongsList> songsList,int position);

        int getPosition();

        String queryText();

    }

    public SongAdapter onQueryTextChange(){
        String text = createDataParsed.queryText();
        for (SongsList songs:songsList){
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)){
                newList.add(songs);
            }
        }

        return new SongAdapter(getContext(),newList);
    }


    private void deleteOption(int postion){

        if (postion != createDataParsed.getPosition()){
            showDialog(songsList.get(postion).getPath(),postion);
        }else {
            Toast.makeText(getContext(), "You Can't Delete the Current Song", Toast.LENGTH_SHORT).show();
        }

    }

    private void showDialog(final String index, final int position ){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialogCustom);
        builder.setTitle(getString(R.string.delete))
                .setTitle(getString(R.string.delete))
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        favoritesOperations.removeSong(index);
                        createDataParsed.fullSongList(songsList,position);
                        setContent();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();



    }


}
