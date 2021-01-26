package com.d33kshant.reader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ReaderActivity extends AppCompatActivity {

    TextView title, pageInfo;
    String path;

    Toolbar toolbar;
    ViewPager2 viewPager;
    PageAdapter adapter;

    int pageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        toolbar = findViewById(R.id.toolbarReader);
        title = findViewById(R.id.title_reader);
        viewPager = findViewById(R.id.viewPager);
        pageInfo = findViewById(R.id.pageInfo);

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        if(!path.isEmpty() || path != null){
            title.setText(new File(path).getName());
            loadFile();
            try {
                pageCount = getPageCount(new File(path));
            }catch (Exception e){
                pageCount = 0;
            }
        }
    }

    void loadFile(){
        adapter = new PageAdapter(this, path);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pageInfo.setText((position+1) + "/" + pageCount);
            }
        });
        viewPager.setCurrentItem(0,false);
    }



    private int getPageCount(File pdfFile) throws IOException {
        int count = 0;
        try {
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            count = pdfRenderer.getPageCount();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.goToPage:
                goToPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToPage() {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint("Between 1 to "+pageCount);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(editText)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pageNo = Integer.parseInt(editText.getText().toString());
                        if (pageNo > 0 && pageNo <= pageCount){
                            viewPager.setCurrentItem(pageNo-1, false);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }
}