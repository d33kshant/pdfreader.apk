package com.d33kshant.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {

    File file;
    Context parent;

    public PageAdapter(Context context, String path) {
        file = new File(path);
        parent = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView page;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            page = itemView.findViewById(R.id.pageView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bitmap;
        try {
            ParcelFileDescriptor input = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(input);
            PdfRenderer.Page page = renderer.openPage(position);
            bitmap = Bitmap.createBitmap(page.getWidth(),page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            holder.page.setImageBitmap(bitmap);
            page.close();
        } catch (Exception e) {
            Toast.makeText(parent, "Filed to load pdf.\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        try {
            return getPageCount(file);
        } catch (IOException e) {
            Toast.makeText(parent, "Filed to load pdf.\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0;
        }
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
