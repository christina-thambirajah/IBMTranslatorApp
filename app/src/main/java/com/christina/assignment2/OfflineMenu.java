package com.christina.assignment2;
/**Page used to retrieve the translated word from database when offline
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class OfflineMenu extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    SQLiteDatabase phraseDatabase;
    ArrayList<String> phraseList = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayAdapter arrayAdapter;
    boolean[] selectedArrayLang;
    Spinner listViewLanguages;
    SQLiteDatabase database;
    ArrayList<String> arrayListLang = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayAdapter arrayAdapterLang;
    static String selectedStringlang;
    ListView listViewDisplayOffline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_offline_menu);
        listViewDisplayOffline = findViewById(R.id.listViewDisplayOffline);
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        try{
        showLanguagesFromDatabase();
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("The libray is empty, Please add Phrases and Languages");
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println(e);
        }
    }
    private void showLanguagesFromDatabase() {

        database = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        arrayListLang.clear();
        Cursor cursorPhrases = database.rawQuery("SELECT * FROM languagesTable WHERE(selected = 'true')", null);
        if (cursorPhrases.moveToFirst()) {
            do {
                arrayListLang.add(cursorPhrases.getString(1));
            } while (cursorPhrases.moveToNext());
        }
        cursorPhrases.close();

        listViewLanguages = (Spinner) findViewById(R.id.listViewLangaugesOffline);
        listViewLanguages.setOnItemSelectedListener(this);

        arrayAdapterLang = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayListLang);
        listViewLanguages.setAdapter(arrayAdapterLang);
        selectedArrayLang = new boolean[arrayListLang.size()];
        listViewLanguages.setAdapter(arrayAdapterLang);

    }

    //offline translation values retreived from database where the values are stored.
    public void clickPerformOfflineTranslation(View view) {
        phraseList.clear();
        try {
            selectedStringlang = listViewLanguages.getSelectedItem().toString();
            String SQL = "SELECT * FROM offline_" + selectedStringlang;
            Cursor cursorLanguage = phraseDatabase.rawQuery(SQL,new String[]{});
            if (cursorLanguage.moveToFirst()) {
                do {
                    phraseList.add(new String(
                            cursorLanguage.getString(1)+"\t\t:\t\t\t"+
                            cursorLanguage.getString(2)
                    ));
                } while (cursorLanguage.moveToNext());
            }
            cursorLanguage.close();

            //creating the adapter object
            arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, phraseList);

            //adding the adapter to listview
            listViewDisplayOffline.setAdapter(arrayAdapter);
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("Language has not been selected." );
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println(e);
        }
    }

    //dropdown menu methods.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }

}