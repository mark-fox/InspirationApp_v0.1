package com.example.hl4350hb.inspirationapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *  Object Class for holding values for individual images/notes.
 */

public class PictureEntry implements Parcelable{

    // Attributes.
    protected String note;
    protected String imageId;
    protected long picTime;
    protected String hashtags;


    // Constructor.
    public PictureEntry(String note, String imageId, long time, String hashtags) {
        this.note = note;
        this.imageId = imageId;
        this.picTime = time;
        this.hashtags = hashtags;
    }

    // Getters.
    public String getNote() {
        return note;
    }

    public String getImageId() {
        return imageId;
    }

    public long getPicTime() {
        return picTime;
    }

    public String getHashtags() { return hashtags; }



//
// PARCELABLE METHODS
//

    protected PictureEntry(Parcel in) {
        note = in.readString();
        imageId = in.readString();
        picTime = in.readLong();
        hashtags = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(note);
        dest.writeString(imageId);
        dest.writeLong(picTime);
        dest.writeString(hashtags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PictureEntry> CREATOR = new Parcelable.Creator<PictureEntry>() {
        @Override
        public PictureEntry createFromParcel(Parcel in) {
            return new PictureEntry(in);
        }

        @Override
        public PictureEntry[] newArray(int size) {
            return new PictureEntry[size];
        }
    };
}
