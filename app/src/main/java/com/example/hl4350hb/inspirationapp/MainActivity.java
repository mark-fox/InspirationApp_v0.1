package com.example.hl4350hb.inspirationapp;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NoteCursorAdapter.NoteChangedListener {
//     ############################################################

//       ##################     GLOBALS     ######################

//     ############################################################

    // Logging tag to identify custom messages.
    private static final String TAG = "thisIsMyTag";
    // Key value used to maintain image during rotations.
    private static final String IMAGE_FILEPATH_KEY = "aKeyForFilepath";

    // Static keys for passing values between Activities.
    protected static final String NOTE_KEY = "note key";
    protected static final String PIC_KEY = "picture time";
    protected static final String SRCH_KEY = "search";
    protected static final String OPT_KEY = "which option";
    protected static final String TEXT_KEY = "text for option";
    protected static final String ID_KEY = "everyone has an id";
    protected static final String HASH_KEY = "hashtag selfie";


    // Creates global references to widgets.
    Button mPicButton;
    ImageView mNewPicture;
    ListView mListView;
    EditText mNoteEntry;
    EditText mHashtagEntry;
    Button mSaveButton;
    Button mSearchButton;


    // Identifier code for the camera returning a result.
    private static final int CAMERA_ACCESS_REQUEST_CODE = 0;
    // Identifier code for returning results.
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1001;
    // Identifier code for returning from DisplayActivity.
    private static final int DISPLAYER_REQUEST_CODE = 202;


    // Will contain the location of the file on device.
    private String mImagePath;
    // Holds the image in variable.
    private Bitmap mImage;

    // Database variable global holders.
    DatabaseManager dbManager;
    NoteCursorAdapter cursorListAdapter;

    // Variable to hold the current time stamp while naming and
    // saving new pictures.
    private long currTime;

    // Global boolean flag used to prevent ListView click
    // events from simultaneously activating.
    private boolean longClick = false;




//     ############################################################

//       ##############     onCreate Magic     #################

//     ############################################################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loads image when rotated so it is not completely lost.
        if (savedInstanceState != null) {
            mImagePath = savedInstanceState.getString(IMAGE_FILEPATH_KEY);
        }

        // Defines widget variables for global use.
        mPicButton = (Button) findViewById(R.id.picButton);
        mNewPicture = (ImageView) findViewById(R.id.newPicture);
        mNoteEntry = (EditText) findViewById(R.id.noteEntry);
        mListView = (ListView) findViewById(R.id.picList);
        mHashtagEntry = (EditText) findViewById(R.id.hashTagEntry);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mSearchButton = (Button) findViewById(R.id.search_route_button);


        // Creates database and its components.
        dbManager = new DatabaseManager(this);
        Cursor cursor = dbManager.getAllPics();


        // FOR TESTING - Shows all the database entries while debugging.
        String data = DatabaseUtils.dumpCursorToString(cursor);


        // Instantiates CursorAdapter and is assigned to the ListView.
        cursorListAdapter = new NoteCursorAdapter(this, cursor, true);
        mListView.setAdapter(cursorListAdapter);


        // Function to add the listeners to widgets.
        addListeners();


        // Forces widgets to stay where they are when the keyboard appears.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        // Puts the focus in the EditText to start typing right away.
        mNoteEntry.setFocusableInTouchMode(true);
        mNoteEntry.requestFocus();
    }





//     ############################################################

//       #############     OTHER OVERRIDES     #################

//     ############################################################

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Save captured image to device and database.
            if (requestCode == CAMERA_ACCESS_REQUEST_CODE) {
                saveImage();
            }
            // Receive new value from DisplayActivity.
            else if (requestCode == DISPLAYER_REQUEST_CODE) {
                Bundle bundle = data.getBundleExtra(DisplayActivity.EXTRA_FROM_DISPLAYER);
                // Both keys will be present if coming from DisplayActivity.
                if (bundle.containsKey(NOTE_KEY) && bundle.containsKey(ID_KEY)) {
                    int rowId = bundle.getInt(ID_KEY, 0);
                    String newNote = bundle.getString(NOTE_KEY);
                    // Update entry in database and then refresh CursorAdapter.
                    dbManager.updateNote(rowId, newNote);
                    cursorListAdapter.changeCursor(dbManager.getAllPics());
                    mNoteEntry.setVisibility(View.GONE);
                    mHashtagEntry.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    mNewPicture.setImageResource(android.R.color.transparent);
                    mSaveButton.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Fits picture to ImageView widget.
        if (hasFocus && mImagePath != null) {
            scalePicture();
            mNewPicture.setImageBitmap(mImage);
            // Makes widgets appear again.
            mHashtagEntry.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.VISIBLE);
            mNoteEntry.setVisibility(View.VISIBLE);
            mNoteEntry.requestFocus();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putString(IMAGE_FILEPATH_KEY, mImagePath);
    }


    @Override   // Callback for adding an image to the device's MediaStore.
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == SAVE_IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Save image
                MediaStore.Images.Media.insertImage(getContentResolver(), mImage, "InspirationApp", "Photo take by InspirationApp");
            } else {
                Toast.makeText(this, "All pictures will be saved..NOT!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override   // Return function from CursorAdapter about clicked item.
    // Starts DisplayActivity.
    public void notifyNoteChanged(int rowId, String text, int which) {
        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra(ID_KEY, rowId);
        intent.putExtra(TEXT_KEY, text);
        intent.putExtra(OPT_KEY, which);
        startActivityForResult(intent, DISPLAYER_REQUEST_CODE);
    }






//     ############################################################

//        ############     CUSTOM FUNCTIONS     ################

//     ############################################################

    private void takePhoto() {
        // Creates a new Intent object.
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if the current device has a camera before continuing.
        if (pictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(MainActivity.this, "A camera is needed to take pictures", Toast.LENGTH_LONG).show();
        } else {
            currTime = new Date().getTime();
            // Designate the filename for picture using the current time.
            String imageFilename = "inspiration_from_" + currTime; // new Date().getTime();

            // Designate temporary location of new file.
            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = null;
            Uri imageFileUri = null;

            // Tries to save image.
            try {
                // Creates temporary file using name created above and stores in
                // designated storage location.
                imageFile = File.createTempFile(imageFilename, ".jpg", storageDirectory);
                // Save the image's location to be used later.
                mImagePath = imageFile.getAbsolutePath();

                // Defines the image location and how to access it for the camera.
                imageFileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.hl4350hb.inspirationapp", imageFile);
            } catch (IOException err) {
                // Can't go any further at this point.
                Log.e(TAG, "ERROR: There was a problem creating file for storing on device", err);
                return;
            }
            // Adds the Uri to the Intent so it can be transported in app.
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);

            // Request the device's camera starts.
            startActivityForResult(pictureIntent, CAMERA_ACCESS_REQUEST_CODE);
        }
    }


    private void scalePicture() {
        // Figures out the picture's dimensions.
        int imageViewHeight = mNewPicture.getHeight();
        int imageViewWidth = mNewPicture.getWidth();

        // Checks whether either value is 0 and stops progress.
        if (imageViewHeight == 0 || imageViewWidth == 0) {
            Log.w(TAG, "ERROR: The picture size is not scalable");
            return;
        }

        // Creates BitmapFactory object to store picture as pixels.
        BitmapFactory.Options bOptions = new BitmapFactory.Options();
        bOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mImagePath, bOptions);

        // Retrieves dimensions.
        int pictureHeight = bOptions.outHeight;
        int pictureWidth = bOptions.outWidth;

        // Determines the scaling factor.
        int scaleFactor = Math.min(pictureHeight / imageViewHeight, pictureWidth / imageViewWidth);

        // Decodes the picture into a new image file while also scaling its size.
        bOptions.inJustDecodeBounds = false;
        bOptions.inSampleSize = scaleFactor;

        mImage = BitmapFactory.decodeFile(mImagePath, bOptions);
    }


    private void saveImage() {
        // Adds image to MediaStore so it can be accessed by the gallery app and others.

        // Check if app has correct permissions before continuing.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MediaStore.Images.Media.insertImage(getContentResolver(), mImage, "InspirationApp", "Image taken by InspirationApp");
        } else {
            // Prompts user to accept the permission request.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_IMAGE_PERMISSION_REQUEST_CODE);
        }
    }




    // Collective method for attaching multiple listeners to widgets onCreate.
    private void addListeners() {
        // Defines click event for Picture button.
        mPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                mListView.setVisibility(View.GONE);
            }
        });


        // ListView's single-click event.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // This event is used as a workaround for attaching an event to TextView.
            // The same method is called.
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (longClick) {
                    longClick = false;
                } else {
                    String query = dbManager.findNote((int) id);
                    notifyNoteChanged((int) id, query, 1);
                }
            }
        });


        // ListView's long-click event.
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            // Method used for deleting entries from the ListView as well as the database.
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id) {
                // Sets global boolean flag.
                longClick = true;
                // Builds dialog box to confirm deletion prior to action.
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Delete forever???");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete item from database and updates the CursorAdapter.
                        dbManager.deleteNote((int)id);
                        cursorListAdapter.changeCursor(dbManager.getAllPics());
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                // Displays dialog box.
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });


        // Save button click event.
        // Button appears after taking a new picture.
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieves input values.
                String newNote = mNoteEntry.getText().toString();
                String hashtags = mHashtagEntry.getText().toString();

                // Add picture and its accompanying data to database.
                dbManager.addNote(newNote, mImagePath, currTime, hashtags);
                // Refreshes ListView with new full database list.
                cursorListAdapter.changeCursor(dbManager.getAllPics());

                // Clears EditTexts and hides/shows certain widgets.
                mNoteEntry.getText().clear();
                mNoteEntry.setVisibility(View.GONE);
                mHashtagEntry.getText().clear();
                mHashtagEntry.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mNewPicture.setImageResource(android.R.color.transparent);
                mSaveButton.setVisibility(View.GONE);

                // Displays message letting user know their picture was saved.
                Toast.makeText(MainActivity.this, "Pic saved!", Toast.LENGTH_SHORT).show();
            }
        });


        // Search button click event.
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creates new dialog object.
                final Dialog dialog = new Dialog(MainActivity.this);
                // Sets dialog objects' values.
                dialog.setContentView(R.layout.dialog_search);
                dialog.setTitle("Search Through Pictures");

                // Sets size of window; otherwise, window is cut off.
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                dialog.getWindow().setLayout((6*width)/6, height/3);

                // Sets up custom dialog's widgets.
                final EditText searchText = (EditText) dialog.findViewById(R.id.dialog_edittext);
                final RadioButton noteRadio = (RadioButton) dialog.findViewById(R.id.dialog_notes_radio);
                final RadioButton hashRadio = (RadioButton) dialog.findViewById(R.id.dialog_hash_radio);
                Button okButton = (Button) dialog.findViewById(R.id.dialog_search_button);
                Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel_button);

                // OK button click event.
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Grabs string value from widget.
                        String search = searchText.getText().toString();
                        // Verifies something is entered in EditText and a radio button is selected.
                        if (!search.equals("") && (noteRadio.isChecked() || hashRadio.isChecked())) {
                            // Determines which radio button was selected to indicate
                            // which database column to search in.
                            int field;
                            if (noteRadio.isChecked()) {
                                field = 1;
                            } else {
                                field = 2;
                            }
                            // Queries database and receives arraylist in return.
                            ArrayList<PictureEntry> results = dbManager.findNote(search, field);

                            // Creates new Intent and starts DisplayActivity.
                            Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                            intent.putParcelableArrayListExtra(SRCH_KEY, results);
                            intent.putExtra(OPT_KEY, 3);
                            startActivityForResult(intent, DISPLAYER_REQUEST_CODE);
                            // Closes dialog box.
                            dialog.dismiss();
                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                // Displays dialog box.
                dialog.show();
            }
        });
    }
}





// References:
    // picture list setup - https://www.learn2crack.com/2013/10/android-custom-listview-images-text-example.html
    // adjust nothing when keyboard active - https://stackoverflow.com/questions/4207880/android-how-do-i-prevent-the-soft-keyboard-from-pushing-my-view-up
    // ENTER key event - https://stackoverflow.com/questions/8233586/android-execute-function-after-pressing-enter-for-edittext
    // loading picture from string - https://stackoverflow.com/questions/3004713/get-content-uri-from-file-path-in-android
    // "hiding" imageview - https://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
    // date format - http://www.java2s.com/Tutorial/Java/0120__Development/Convertstringdatetolongvalue.htm
    // limiting character limit - https://stackoverflow.com/questions/9149846/can-i-limit-textviews-number-of-characters
    // viewing database content in debugger - https://stackoverflow.com/questions/4235996/viewing-an-android-database-cursor
    // single row query - https://stackoverflow.com/questions/12473194/get-a-single-row-from-table
    // custom auto-scale ImageView - https://www.ryadel.com/en/android-proportionally-stretch-imageview-fit-whole-screen-width-maintaining-aspect-ratio/
    // align widget on bottom - https://stackoverflow.com/questions/25159572/how-to-display-widget-at-bottom-of-screen-android
    // dialog box setup - https://examples.javacodegeeks.com/android/core/ui/dialog/android-custom-dialog-example/
    // query with LIKE - https://stackoverflow.com/questions/16416827/android-sqlite-select-from-table-where-name-like-key-using-prepared-statemen
    // convert class to parce - http://www.parcelabler.com/
    // prevent onitemclick overriding long click - https://stackoverflow.com/questions/6183874/android-detect-end-of-long-press
    // resizing dialog box window - https://stackoverflow.com/questions/19133822/custom-dialog-too-small

