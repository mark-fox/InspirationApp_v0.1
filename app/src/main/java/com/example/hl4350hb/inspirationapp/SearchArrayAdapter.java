package com.example.hl4350hb.inspirationapp;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 *  Custom ArrayAdapter for displaying search results in DisplayActivity.
 */

public class SearchArrayAdapter extends ArrayAdapter<PictureEntry> {

    Activity mActivity;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("M-dd-yyyy hh:mm:ss");

    // Constructor.
    public SearchArrayAdapter(Context context, int resource, ArrayList<PictureEntry> results) {
        super(context, resource, results);
        this.mActivity = (Activity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            // Inflates view.
            LayoutInflater inflater = mActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }
        // Retrieves object.
        PictureEntry pictureEntry = getItem(position);

        // Sets up widgets.
        ImageView picView = (ImageView) rowView.findViewById(R.id.img);
        TextView textView = (TextView) rowView.findViewById(R.id.txt);
        TextView dateView = (TextView) rowView.findViewById(R.id.datetxt);

        // Assigns values to widgets.
        picView.setImageURI(Uri.fromFile(new File(pictureEntry.getImageId())));
        textView.setText(pictureEntry.getNote());
        dateView.setText(dateFormatter.format(pictureEntry.getPicTime()));

        return rowView;
    }
}
