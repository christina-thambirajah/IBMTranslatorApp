package com.christina.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PhraseAdapter  extends ArrayAdapter<Phrase> {
    Context mCtx;
    int listLayoutRes;
    List<Phrase> phraseList;
    SQLiteDatabase phraseDatabse;

    public PhraseAdapter(Context mCtx, int listLayoutRes, List<Phrase> phraseList, SQLiteDatabase phraseDatabase) {
        super(mCtx, listLayoutRes, phraseList);

        this.mCtx = mCtx;
        this.listLayoutRes = listLayoutRes;
        this.phraseList = phraseList;
        this.phraseDatabse = phraseDatabase;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(listLayoutRes, null);

        final Phrase phrase = phraseList.get(position);


        TextView textViewContent = view.findViewById(R.id.textViewPhrase);
        textViewContent.setText(phrase.getContent());

        return view;
    }
}
