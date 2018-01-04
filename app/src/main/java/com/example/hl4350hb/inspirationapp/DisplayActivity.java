package com.example.hl4350hb.inspirationapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 *  Activity for displaying various types of information.
 */

public class DisplayActivity extends AppCompatActivity {

    // Variables for widgets.
    EditText mDisplayNoteEntry;
    ImageView mDisplayNewPicture;
    ListView mDisplayTable;
    Button mSubmitButton;

    // Static key string for intents.
    protected static final String EXTRA_FROM_DISPLAYER = "extra from the displayer";

    // Private variables for Activity.
    private int whichOption;
    private int currRowId;
    private ArrayList<PictureEntry> results = new ArrayList<PictureEntry>();
    private SearchArrayAdapter listAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayer);

        // Sets up widgets.
        mDisplayNoteEntry = (EditText) findViewById(R.id.display_noteEntry);
        mDisplayNewPicture = (ImageView) findViewById(R.id.display_newPicture);
        mDisplayTable = (ListView) findViewById(R.id.display_table);
        mSubmitButton = (Button) findViewById(R.id.submit_button);

        // Submit button click event.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hides widgets and determines if the Note editing option was active.
                boolean noteChanged = hideOption();
                Bundle bundle = new Bundle();
                if (noteChanged) {
                    String newNote = mDisplayNoteEntry.getText().toString();
                    bundle.putInt(MainActivity.ID_KEY, currRowId);
                    bundle.putString(MainActivity.NOTE_KEY, newNote);
                }
                returnIntent(bundle);
            }
        });

        // Grab the attached intent and break it down to its components.
        Intent intent = getIntent();
        // Determine which option was chosen.
        int whichOption = intent.getIntExtra(MainActivity.OPT_KEY, 0);
        if (whichOption == 3) {
            // Update global variable to passed array if option 3.
            results = intent.getParcelableArrayListExtra(MainActivity.SRCH_KEY);
        }
        String text = intent.getStringExtra(MainActivity.TEXT_KEY);
        int rowId = intent.getIntExtra(MainActivity.ID_KEY, 0);

        // Run method to display option.
        displayOption(rowId, text, whichOption);
    }



    // Method for managing which widgets and components are used.
    private void displayOption(int rowId, String text, int which) {
        // Sets global variable to the passed option to be used later.
        whichOption = which;
        // Determines which actions to take.
        switch (which) {
            case 1:     // Edit Note
                mDisplayNoteEntry.setVisibility(View.VISIBLE);
                mDisplayNoteEntry.setText(text);
                currRowId = rowId;
                break;
            case 2:     // Fullscreen Image
                mDisplayNewPicture.setVisibility(View.VISIBLE);
                mDisplayNewPicture.setImageURI(Uri.fromFile(new File(text)));
                break;
            case 3:     // Search Results
                mDisplayTable.setVisibility(View.VISIBLE);
                setupSearchResults();
                break;
            default:
                break;
        }
    }

    // Method for managing cleanup after user is done with Activity.
    private boolean hideOption() {
        switch (whichOption) {
            case 1:     // Edit Note
                mDisplayNoteEntry.setVisibility(View.GONE);
                return true;
            case 2:     // Fullscreen Image
                mDisplayNewPicture.setVisibility(View.GONE);
                break;
            case 3:     // Search Results
                mDisplayTable.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        return false;
    }

    // Method for returning Intent back to MainActivity.
    private void returnIntent(Bundle bundle) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FROM_DISPLAYER, bundle);
        setResult(RESULT_OK, intent);
        finish();
    }


    // Method for setting up the ListView to display passed search results.
    private void setupSearchResults() {
        // Instantiates array adapter object and sets it to the list.
        listAdapter = new SearchArrayAdapter(this, R.layout.list_item, results);
        mDisplayTable.setAdapter(listAdapter);
        mDisplayTable.deferNotifyDataSetChanged();
    }
}
