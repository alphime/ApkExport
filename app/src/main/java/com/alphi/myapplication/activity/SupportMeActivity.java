package com.alphi.myapplication.activity;

import static com.alphi.myapplication.Utils.BitmapUtil.drawableToBitmap150;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.alphi.myapplication.R;
import com.alphi.myapplication.Utils.ExtractFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * A simple {@link android.app.Activity} subclass.
 * create an instance of this fragment.
 */
public class SupportMeActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_support);
        ActionBar supportActionBar = getSupportActionBar();
        // 设置返回按钮
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        Button btn = findViewById(R.id.save_support_pic);
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                Drawable drawable = getResources().getDrawable(R.drawable.support1, null);
                File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + ExtractFile.savePath + "/save_support");
                if (dir.exists()) {
                    if (dir.isFile()) {
                        dir.delete();
                        dir.mkdirs();
                    }
                } else {
                    dir.mkdirs();
                }
                try {
                    File file = new File(dir, "alipay.jpg");
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bitmap = drawableToBitmap150(drawable);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    // 刷新媒体库
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    Toast.makeText(SupportMeActivity.this, "保存成功！感谢您的支持！\n路径：sdcard/" + ExtractFile.savePath + "/save_support", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // toolbar上级按钮 id:android.R.id.home
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}