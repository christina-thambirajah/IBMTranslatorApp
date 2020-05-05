package com.christina.assignment2;
/**This is the implementation for the assignment 2 of mobile development
 * This is an app that can help people learn many languages
 * this is the first page of entering the app containing 5 main buttons and a switch to offline feature.
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase phraseDatabase;
    ArrayList<String> selectedLanguages = new ArrayList<>();
    ArrayList<String> selectedPhrases = new ArrayList<>();
    static String languages;
    static String phrases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.activity_main);
    }
    //method to add phrases to library
    public void clickButtonAddPhrases(View view) {
        Intent myIntent = new Intent(getBaseContext(), AddPhrases.class);
        startActivity(myIntent);
    }

    //method to view phrases added to library
    public void clickButtonDisplayPhrases(View view) {
        Intent myIntent = new Intent(getBaseContext(), DisplayPhrases.class);
        startActivity(myIntent);
    }

    //Method to veiw phrases in library
    public void clickbuttonEditPhrases(View view) {
        Intent myIntent = new Intent(getBaseContext(),
                EditPhrases.class);
        startActivity(myIntent);
    }

    //method to view the page to subscribe to languages
    public void clickBbuttonLanguageSubscription(View view) {
        Intent myIntent = new Intent(getBaseContext(), LanguageSubscription.class);
        startActivity(myIntent);
    }

    //method to view translate page
    public void clickButtonTranslate(View view) {
        Intent myIntent = new Intent(getBaseContext(), Translate.class);
        startActivity(myIntent);
    }

    //save all translated words to tables to use offline
    public void clickUseOffline(View view) {
        Intent myIntent = new Intent(getBaseContext(), OfflineMenu.class);
        startActivity(myIntent);
        //creating a database
        phraseDatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        createOfflineTables();
    }

    //creating tables to store translated words
    private void createOfflineTables() {
        try {
            selectedLanguages.clear();
            Cursor cursorLanguages = phraseDatabase.rawQuery("SELECT * FROM languagesTable WHERE(selected = 'true')", null);
            if (cursorLanguages.moveToFirst()) {
                do {
                    selectedLanguages.add(cursorLanguages.getString(1));
                } while (cursorLanguages.moveToNext());
            }
            cursorLanguages.close();

            selectedPhrases.clear();
            Cursor cursorPhrases = phraseDatabase.rawQuery("SELECT * FROM phrases", null);
            if (cursorPhrases.moveToFirst()) {
                do {
                    selectedPhrases.add(cursorPhrases.getString(1));
                } while (cursorPhrases.moveToNext());
            }
            cursorPhrases.close();

            new SimpleAsyncTask().execute();
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("The libray is empty, Please add Phrases and Languages before trying offline" );
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println(e);
        }
    }

    private class SimpleAsyncTask extends AsyncTask<String, Void, CharSequence> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                for (int x = 0; x < selectedLanguages.size(); x++) {
                    languages = selectedLanguages.get(x);
                    phraseDatabase.execSQL(
                            "DROP TABLE IF EXISTS offline_" + languages + "\n" +
                                    ";"
                    );
                    phraseDatabase.execSQL(
                            "CREATE TABLE IF NOT EXISTS offline_" + languages + " (\n" +
                                    "    id INTEGER NOT NULL CONSTRAINT offline_pk PRIMARY KEY AUTOINCREMENT,\n" +
                                    "    phrase varchar(200) NOT NULL,\n" +
                                    "    translated varchar(200)\n" +
                                    ");"
                    );
                    for (int y = 0; y < selectedPhrases.size(); y++) {
                        phrases = selectedPhrases.get(y);
                        String insertSQL = "INSERT INTO offline_" + languages + " \n" +
                                "(phrase)\n" +
                                "VALUES \n" +
                                "(?);";
                        phraseDatabase.execSQL(insertSQL, new String[]{phrases});

                        IamAuthenticator authenticator = new IamAuthenticator("4lPnqDiG9I4BvwjrTQArBzsNvpp6Qz-P8ldYyL0fbgYe");
                        LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01", authenticator);
                        languageTranslator.setServiceUrl("https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/5e1d1f14-8b19-4a7e-800d-3d007ec9a544");


                        String SQL = "SELECT ab FROM languagesTable WHERE(language =?);";
                        String modelAb = null;
                        Cursor cursorLanguage = phraseDatabase.rawQuery(SQL, new String[]{languages});
                        if (cursorLanguage.moveToFirst()) {
                            do {
                                modelAb = cursorLanguage.getString(0);
                            } while (cursorLanguage.moveToNext());
                        }
                        cursorLanguage.close();
                        TranslateOptions translateOptions = new TranslateOptions.Builder()
                                .addText(phrases)
                                .modelId("en-" + modelAb)
                                .build();

                        TranslationResult result = languageTranslator.translate(translateOptions)
                                .execute().getResult();
                        String jsonFullString = result.toString();
                        JSONObject data = new JSONObject(jsonFullString);
                        JSONArray all_items = data.getJSONArray("translations");
                        // extract all items titles
                        for (int i = 0; i < all_items.length(); i++) {
                            JSONObject langs = all_items.getJSONObject(i);
                            String translation = langs.getString("translation").trim();
                            String insertNewSQL = "UPDATE offline_" + languages + " SET translated = (?) WHERE (phrase = (?));";
                            phraseDatabase.execSQL(insertNewSQL, new String[]{translation, phrases});
                        }
                    }
                    System.out.println("Completed table " + languages);
                }
            } catch (Exception jex) {
                jex.printStackTrace();
            }
            return "";
        }
        @Override
        protected void onPostExecute(CharSequence charSequence) {
        }
    }


}
