package com.d33kshant.reader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView title;
    Toolbar toolbar;

    RecyclerView recyclerView;
    MainAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<ItemRow> list;
    ArrayList<File> files;

    PdfRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title = findViewById(R.id.title_reader);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);

        list = new ArrayList<>();

        setSupportActionBar(toolbar);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        loadFiles();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {}
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {}
                }).check();
    }

    public void loadFiles(){
        files = searchFiles(Environment.getExternalStorageDirectory());
        listFies(files);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.discover:
                explore();
                return true;
            case R.id.about:
                about();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void listFies(ArrayList<File> files){
        for(int i = 0; i < files.size(); i++){
            Bitmap bitmap = Bitmap.createBitmap(100, 141, Bitmap.Config.ARGB_8888);
            String name = files.get(i).getName().replace(".pdf","");
            String info = "";
            try {
                ParcelFileDescriptor input = ParcelFileDescriptor.open(files.get(i), ParcelFileDescriptor.MODE_READ_ONLY);
                renderer = new PdfRenderer(input);
                PdfRenderer.Page page = renderer.openPage(0);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();
                int count = getPageCount(files.get(i));
                if (count > 1){
                    info += count + " Pages | ";
                }else {
                    info += count + " Page | ";
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            info += getSize(Long.parseLong(String.valueOf(files.get(i).length())));
            list.add(new ItemRow(name, info, bitmap));
        }
        adapter = new MainAdapter(list);
        adapter.setOnItemClickListener(new MainAdapter.onItemClickListener() {
            @Override
            public void onCardClicked(int position) {
                openFile(position);
            }

            @Override
            public void onDeleteClicked(int position) {
                deleteFile(position);
            }

            @Override
            public void onShareClicked(int position) {
                shareFile(position);
            }
        });
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void shareFile(int position){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", files.get(position));
        sharingIntent.setType("application/pdf");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share file"));
    }

    public void deleteFile(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete File")
                .setMessage("Are you sure you want to delete "+files.get(position).getName().replace(".pdf","")+" ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public void delete(int position){
        boolean deleted = files.get(position).delete();
        if (deleted){
            String name = files.get(position).getName();
            files.remove(position);
            list.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, name + " deleted.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to delete file.", Toast.LENGTH_SHORT).show();
        }
    }

    public void openFile(int position){
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        intent.putExtra("path", files.get(position).getPath());
        startActivity(intent);
    }

    public void explore(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pdfdrive.com/"));
        startActivity(browserIntent);
    }

    public void about(){

        SpannableString ss = new SpannableString("Simple PDF reader made by d33kshant\nSource code available on GitHub");
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://d33kshant.github.io/aboutme/"));
                startActivity(browserIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/d33kshant/pdfreader.apk"));
                startActivity(browserIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 26, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan2, 61, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView textView = new TextView(this);
        textView.setPadding(48,16,48,16);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About")
                .setView((View) textView)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public ArrayList<File> searchFiles(File mFile){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = mFile.listFiles();
        for(File file: files){
            if(file.isDirectory() && !file.isHidden()){
                arrayList.addAll(searchFiles(file));
            }else{
                if(file.getName().endsWith(".pdf")){
                    arrayList.add(file);
                }
            }
        }
        return arrayList;
    }

    public static String getSize(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    private int getPageCount(File pdfFile) throws IOException {
        int count = 0;
        try {

            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                count = pdfRenderer.getPageCount();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return count;
    }
}