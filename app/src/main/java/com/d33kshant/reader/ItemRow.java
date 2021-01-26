package com.d33kshant.reader;

import android.graphics.Bitmap;

public class ItemRow {
    String Tittle, Info;
    Bitmap Bitmap;

    public ItemRow(String tittle, String info, Bitmap bitmap) {
        Tittle = tittle;
        Info = info;
        Bitmap = bitmap;
    }

    public String getTittle() {
        return Tittle;
    }

    public String getInfo() {
        return Info;
    }

    public Bitmap getBitmap() {
        return Bitmap;
    }
}
