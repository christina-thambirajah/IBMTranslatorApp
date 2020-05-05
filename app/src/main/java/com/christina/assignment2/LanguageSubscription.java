package com.christina.assignment2;
/**Page used to allow user to subscribe to languages
 * Last updated on 16/04/2020
 * Author - S C Thambirajah
 * */

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.IdentifiableLanguages;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
public class LanguageSubscription extends AppCompatActivity {
    String selectedStringlang;
    int selectedIDlang;
    boolean[] selectedArrayLang;
    private ListView listViewLanguageslang;
    SQLiteDatabase database;
    ArrayList<String> arrayListLang = new ArrayList<>(); // list of the strings that should appear in ListView
    ArrayList<String> selectedListLang = new ArrayList<>();
    ArrayAdapter arrayAdapterLang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_language_subscription);
        database = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        createLanguagesTable();

        listViewLanguageslang = findViewById(R.id.listViewLanguages);
        try{
        showLanguagesFromDatabase();
        }catch (Exception e){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Error");
            alert.setMessage("The libaray is Empty, Please connect to the internet");
            alert.setPositiveButton("OK",null);
            alert.show();
            System.out.println(e);
        }
        if(arrayListLang.isEmpty()) {
            new SimpleAsyncTask(listViewLanguageslang).execute();

        }

    }

    private void createLanguagesTable() {
        
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS languagesTable (\n" +
                        "    id INTEGER NOT NULL CONSTRAINT languages_pk PRIMARY KEY AUTOINCREMENT,\n" +
                        "    language varchar(200) NOT NULL,\n" +
                        "    ab varchar(10) NOT NULL,\n" +
                        "    selected boolean);"
        );
    }

        public class SimpleAsyncTask extends AsyncTask<String, Void, String> {
            private WeakReference<ListView> mListView;
            SimpleAsyncTask(ListView tv) {
                mListView = new WeakReference<>(tv);
            }
            @Override
            protected String doInBackground(String... strings) {
                try {
                    IamAuthenticator authenticator = new IamAuthenticator("4lPnqDiG9I4BvwjrTQArBzsNvpp6Qz-P8ldYyL0fbgYe");
                    LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01", authenticator);
                    languageTranslator.setServiceUrl("https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/5e1d1f14-8b19-4a7e-800d-3d007ec9a544");

                    IdentifiableLanguages languages = languageTranslator.listIdentifiableLanguages().execute().getResult();
                    String jsonFullString = languages.toString();
                    JSONObject data = new JSONObject(jsonFullString);
                    JSONArray all_items = data.getJSONArray("languages");
                    // extract all items titles
                    String insertSQL = "";
                    for (int i = 0; i < all_items.length(); i++) {
                        JSONObject langs = all_items.getJSONObject(i);
                        String titles = langs.getString("name").trim();
                        String abbreviation = langs.getString("language").trim();

                            insertSQL = "INSERT INTO languagesTable \n" +
                                    "(language,ab)\n" +
                                    "VALUES \n" +
                                    "(?,?);";

                            database.execSQL(insertSQL, new String[]{titles,abbreviation});
                    }
            return insertSQL;
            } catch (Exception jex) {
                    jex.printStackTrace();
            }
        return "";
        }
        protected void onPostExecute(String result) {
            System.out.println(result);
            showLanguagesFromDatabase();
        }
    }
    //display list of available languages
    private void showLanguagesFromDatabase() {
        database = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        arrayListLang.clear();
        Cursor cursorPhrases = database.rawQuery("SELECT * FROM languagesTable", null);
        if (cursorPhrases.moveToFirst()) {
            do {
                arrayListLang.add(cursorPhrases.getString(1));
                selectedListLang.add(cursorPhrases.getString(3));
            } while (cursorPhrases.moveToNext());
        }
        cursorPhrases.close();
        arrayAdapterLang = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, arrayListLang);
        listViewLanguageslang.setAdapter(arrayAdapterLang);
        ListView lv = listViewLanguageslang;
        for (int i = 0; i < arrayAdapterLang.getCount(); i++) {
            lv.setItemChecked(i, true);
        }

        selectedArrayLang = new boolean[arrayListLang.size()];
        listViewLanguageslang.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listViewLanguageslang.setAdapter(arrayAdapterLang);

        for (int i = 0; i < arrayAdapterLang.getCount(); i++) {
            if(Boolean.parseBoolean(selectedListLang.get(i))){
                listViewLanguageslang.setItemChecked(i, true);
            }
            if(!(Boolean.parseBoolean(selectedListLang.get(i)))){
                listViewLanguageslang.setItemChecked(i, false);
            }
        }

        listViewLanguageslang.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView arg0, View view, int position,
                                    long itemId) {
                CheckedTextView textView;
                for (int i = 0; i < listViewLanguageslang.getCount(); i++) {
                    textView = (CheckedTextView) listViewLanguageslang.getChildAt(i);
                    if(listViewLanguageslang.isItemChecked(i)==true){
                        selectedIDlang = i;
                        selectedArrayLang[i]=true;
                    }
                    if (textView != null) {
                        if(listViewLanguageslang.isItemChecked(i)==false) {
                            textView.setTextColor(Color.BLACK);
                            selectedArrayLang[i] =false;
                        }
                    }

                }
                listViewLanguageslang.invalidate();
                textView = (CheckedTextView) view;
                if (textView != null) {
                    textView.setTextColor(Color.BLUE);
                    selectedStringlang = textView.getText().toString();
                }

            }
        });

    }
    //update the database according to whether subscribed or unsubscribed
    public void clickUpdateBtn(View view) {
        try {
            database = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

            for (int x = 0; x < arrayListLang.size(); x++) {
                //boolean select = selectedArrayLang.get(x);
                String sql = "UPDATE languagesTable \n" +
                        "SET selected = ? \n" +
                        "WHERE id = ?;\n";

                database.execSQL(sql, new String[]{Boolean.toString(selectedArrayLang[x]), Integer.toString(x + 1)});
            }
            Toast.makeText(this, "Languages Updated", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Error");
                alert.setMessage("Unnable to make update, select a phrase for updating.");
                alert.setPositiveButton("OK",null);
                alert.show();
                System.out.println(e);
            }
    }
    public void clickBacktoMenu(View view) {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(myIntent);
    }

}
