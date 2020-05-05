package com.christina.assignment2;
/**Page used to [erform the  actual translation with use of the API
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Translate extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static String selectedString;
    int selectedID;
    SQLiteDatabase phraseDatabase;
    ListView listViewPhrases;
    EditText editTxt;
    ArrayList<String> arrayList = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayAdapter arrayAdapter;
    static String selectedStringlang;
    boolean[] selectedArrayLang;
    Spinner listViewLanguages;
    SQLiteDatabase database;
    ArrayList<String> arrayListLang = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayAdapter arrayAdapterLang;
    static TextView textView;
    private TextToSpeech textService;
    private StreamPlayer player = new StreamPlayer();
    private ProgressBar pb;
    private ProgressBar pbPro;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
        }
        setContentView(R.layout.activity_translate);
        listViewPhrases = findViewById(R.id.listViewPhrases2);
        pb = findViewById(R.id.pb);
        pbPro = findViewById(R.id.pbPronounce);
        try{
        showPhrasesFromDatabase();
        showLanguagesFromDatabase();
        }catch (SQLiteException e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("The libray is empty, Please add Phrases and Languages");
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println("The libray is empty, Please add Phrases and Languages");
        }
    }

    //method to retrieve and show the phrases on listview
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
                listViewPhrases = findViewById(R.id.listViewPhrases2);
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

    //method to retrieve and show the languages on the dropdown
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

        listViewLanguages = (Spinner) findViewById(R.id.listViewLangauges2);
        listViewLanguages.setOnItemSelectedListener(this);

        arrayAdapterLang = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayListLang);
        listViewLanguages.setAdapter(arrayAdapterLang);
        selectedArrayLang = new boolean[arrayListLang.size()];
        listViewLanguages.setAdapter(arrayAdapterLang);

    }

    //call api to perform the translation of selected words
    public void clickPerformTranslation(View view) {
        try {
            selectedStringlang = listViewLanguages.getSelectedItem().toString();
            listViewPhrases = findViewById(R.id.listViewPhrases2);
            textView = findViewById(R.id.textViewDisplay);
            pb.setProgress(0);
            pb.setVisibility(View.VISIBLE);
            new Translate.SimpleAsyncTask(listViewPhrases).execute();
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("Select a language and a phrase, before attempting translate ");
            alert.setPositiveButton("OK",null);
            alert.show();
        }
    }

    public void clickPerformPronounce(View view) {
        try {
            String pronounce = textView.getText().toString();
            pbPro.setProgress(0);
            pbPro.setVisibility(View.VISIBLE);
            textService = initTextToSpeechService();
            new SynthesisTask().execute(pronounce);
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("Perform a translation, before attempting pronounce ");
            alert.setPositiveButton("OK",null);
            alert.show();
        }
    }

    //to show selction in dropdown
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class SimpleAsyncTask extends AsyncTask<String,Integer,String> {
        private WeakReference<ListView> mListView;
        SimpleAsyncTask(ListView tv) {
            mListView = new WeakReference<>(tv);
        }
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            for (int i = 1; i <= 5; i++) {
                try {
                    IamAuthenticator authenticator = new IamAuthenticator("4lPnqDiG9I4BvwjrTQArBzsNvpp6Qz-P8ldYyL0fbgYe");
                    LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01", authenticator);
                    languageTranslator.setServiceUrl("https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/5e1d1f14-8b19-4a7e-800d-3d007ec9a544");


                    String SQL = "SELECT ab FROM languagesTable WHERE(language =?);";
                    String modelAb = null;
                    Cursor cursorLanguage = database.rawQuery(SQL, new String[]{selectedStringlang});

                    if (cursorLanguage.moveToFirst()) {
                        do {
                            modelAb = cursorLanguage.getString(0);
                        } while (cursorLanguage.moveToNext());
                    }
                    cursorLanguage.close();
                    TranslateOptions translateOptions = new TranslateOptions.Builder()
                            .addText(selectedString)
                            .modelId("en-" + modelAb)
                            .build();

                    TranslationResult result = languageTranslator.translate(translateOptions)
                            .execute().getResult();
                    String jsonFullString = result.toString();
                    JSONObject data = new JSONObject(jsonFullString);
                    JSONArray all_items = data.getJSONArray("translations");
                    // extract all items titles
                    for (int x = 0; x < all_items.length(); x++) {
                        JSONObject langs = all_items.getJSONObject(x);
                        String translation = langs.getString("translation").trim();
                        System.out.println(translation);
                        return translation;
                    }
                } catch (Exception jex) {
                    jex.printStackTrace();
                }
            }
            return "";
        }

        /* Runs on the UI thread receiving the value sent by
       publishProgress()
        run in the background thread */

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[0] * 20);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.setText(s);
            pb.setVisibility(View.INVISIBLE);
            // Change text to whatever value returned from
        }
    }

    //method to invoke pronounciatioin through the api.
    private TextToSpeech initTextToSpeechService() {
        Authenticator authenticator = new
                IamAuthenticator(getString(R.string.text_speech_apikey));
        TextToSpeech service = new TextToSpeech(authenticator);
        service.setServiceUrl(getString(R.string.text_speech_url));
        return service;
    }
    private class SynthesisTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            for (int i = 1; i <= 5; i++) {
                    SynthesizeOptions synthesizeOptions = new
                            SynthesizeOptions.Builder()
                            .text(params[0])
                            .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
                            .accept(HttpMediaType.AUDIO_WAV)
                            .build();
                    player.playStream(textService.synthesize(synthesizeOptions).execute()
                            .getResult());
                return "Did synthesize";
            }
            return "Did synthesize";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pbPro.setProgress(values[0] * 20);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbPro.setVisibility(View.INVISIBLE);
        }
    }

    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }
}
