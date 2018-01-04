package com.example.hl4350hb.inspirationapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 *  Custom CursorAdapter for handling interactions with ListView and database.
 */

public class NoteCursorAdapter extends CursorAdapter {

    NoteChangedListener noteChangedListener;

    // Column indices.
    private static final int ID_COL = 0;
    private static final int NOTE_COL = 1;
    private static final int IMG_COL = 2;
    private static final int DATE_COL = 3;
    private static final int HASH_COL = 4;
    // DateFormatter for Date field.
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("M-dd-yyyy hh:mm:ss");



    // Constructor.
    public NoteCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        // Sets Listener.
        if (context instanceof NoteChangedListener) {
            this.noteChangedListener = (NoteChangedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NoteChangedListener");
        }
    }

    @Override
    // Inflates view.
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // Sets up widgets.
        TextView notetxt = (TextView) view.findViewById(R.id.txt);
        TextView datetxt = (TextView) view.findViewById(R.id.datetxt);
        ImageView img = (ImageView) view.findViewById(R.id.img);

        // Fills widgets with values.
        final String note = cursor.getString(NOTE_COL);
        notetxt.setText(note);
        final String imageid = cursor.getString(IMG_COL);
        img.setImageURI(Uri.fromFile(new File(imageid)));
        long datetaken = cursor.getLong(DATE_COL);
        datetxt.setText(dateFormatter.format(datetaken));

        final int rowId = cursor.getInt(ID_COL);

        // Sets click events to the widgets, although the ImageView's event
        // always seems to overrule the TextView's event.
        notetxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteChangedListener.notifyNoteChanged(rowId, note, 1);
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteChangedListener.notifyNoteChanged(rowId, imageid, 2);
            }
        });
    }

    // Interface for returning values of clicked item to MainActivity.
    interface NoteChangedListener {
        void notifyNoteChanged(int rowId, String text, int which);
    }
}
