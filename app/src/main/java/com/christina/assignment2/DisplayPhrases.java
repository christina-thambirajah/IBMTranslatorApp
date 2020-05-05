package com.christina.assignment2;
/**Page used to display phrases stores in database
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DisplayPhrases extends AppCompatActivity {
    SQLiteDatabase phraseDatabase;
    PhraseAdapter adapter;
    ListView listViewPhrases;
    List<Phrase> phraseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_display_phrases);
        listViewPhrases = (ListView) findViewById(R.id.listViewPhrases);
        phraseList = new ArrayList<>();
        //this method will display the phrases in the list
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        try{
        showPhrasesFromDatabase();
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("The libray is empty, Please add Phrases and Languages before atempting display" );
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println(e);
        }
    }

    //show phrases from table in listView
    private void showPhrasesFromDatabase() {
        //we used rawQuery(sql, selectionargs) for fetching all the phrases
        Cursor cursorPhrases = phraseDatabase.rawQuery("SELECT * FROM phrases ORDER BY content ASC", null);
        if (cursorPhrases.moveToFirst()) {
            do {
                phraseList.add(new Phrase(
                        cursorPhrases.getInt(0),
                        cursorPhrases.getString(1)
                ));
            } while (cursorPhrases.moveToNext());
        }
        cursorPhrases.close();

        //creating the adapter object
        adapter = new PhraseAdapter(this, R.layout.list_layout_phrases, phraseList, phraseDatabase);

        //adding the adapter to listview
        listViewPhrases.setAdapter(adapter);

    }
    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }
}
