package com.christina.assignment2;
/**Page used to add values to the library database
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddPhrases extends AppCompatActivity {
    EditText editTxt;
    SQLiteDatabase phraseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_add_phrases);
        editTxt = (EditText) findViewById(R.id.editTxtPhrase);

        //creating a database
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        createPhrasesTable();


    }

    //method creating table of phrases if exsists
    private void createPhrasesTable() {
        phraseDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS phrases (\n" +
                        "    id INTEGER NOT NULL CONSTRAINT phrases_pk PRIMARY KEY AUTOINCREMENT,\n" +
                        "    content varchar(200) NOT NULL\n" +
                        ");"
        );
    }

    //method adding phrases to the table in the database
    public void clickSaveBtn(View view) {
        String content = editTxt.getText().toString().trim();

        String insertSQL = "INSERT INTO phrases \n" +
                "(content)\n" +
                "VALUES \n" +
                "(?);";

        phraseDatabase.execSQL(insertSQL, new String[]{content});

        Toast.makeText(this, "Phrase Added Successfully", Toast.LENGTH_SHORT).show();

    }

    //clearinf the textfeild
    public void clickClearBtn(View view) {
        editTxt.setText(" ");
    }

    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }
}
