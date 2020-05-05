package com.christina.assignment2;
/**Page used to edit phrases in the database according to ID
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EditPhrases extends AppCompatActivity {

    String selectedString;
    int selectedID;
    SQLiteDatabase phraseDatabase;
    ListView listViewPhrases;
    EditText editTxt;
    ArrayList<String> arrayList = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_edit_phrases);
        listViewPhrases = (ListView) findViewById(R.id.listViewPhrases);
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        try {
            showPhrasesFromDatabase();
        }catch (Exception e){
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Error");
                alert.setMessage("The libray is empty, Please add Phrases and Languages" );
                alert.setPositiveButton("OK",null);
                alert.show();
            System.out.println(e);
        }
    }
    //show saved phrases in listview
    private void showPhrasesFromDatabase() {
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        arrayList.clear();
        Cursor cursorPhrases = phraseDatabase.rawQuery("SELECT * FROM phrases", null);
        if (cursorPhrases.moveToFirst()) {
            do {
                arrayList.add(cursorPhrases.getString(1));
            } while (cursorPhrases.moveToNext());
        }
        cursorPhrases.close();


        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_single_choice, arrayList);
        listViewPhrases.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewPhrases.setAdapter(arrayAdapter);

        listViewPhrases.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView arg0, View view, int position,
                                    long itemId) {

                editTxt = findViewById(R.id.editPhraseTxt);
                CheckedTextView textView;
                for (int i = 0; i < listViewPhrases.getCount(); i++) {
                    textView = (CheckedTextView) listViewPhrases.getChildAt(i);
                    if(listViewPhrases.isItemChecked(i)==true){
                        selectedID = i;
                    }

                    if (textView != null) {
                        textView.setTextColor(Color.BLACK);
                    }

                }
                listViewPhrases.invalidate();
                textView = (CheckedTextView) view;
                if (textView != null) {
                    textView.setTextColor(Color.BLUE);
                    selectedString = textView.getText().toString();
                }

            }
        });

    }

   //Save the edited phrase to database
    public void clickSaveEditBtn(View view) {
        editTxt = findViewById(R.id.editPhraseTxt);

        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        String content = editTxt.getText().toString().trim();
        String id = Integer.toString(selectedID+1);

        String sql = "UPDATE phrases \n" +
                        "SET content = ? \n" +
                        "WHERE id = ?;\n";

        phraseDatabase.execSQL(sql, new String[]{content,id});
                Toast.makeText(this, "Phrase Updated", Toast.LENGTH_SHORT).show();


        showPhrasesFromDatabase();
    }

    //get phrase and display on textFeild
    public void clickEditBtn(View view) {
        editTxt = findViewById(R.id.editPhraseTxt);
        editTxt.setText(selectedString);
    }
    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }

}
