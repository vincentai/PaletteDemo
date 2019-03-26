package com.smzdm.palettedemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PICTURE = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;
    private static final String IMAGE_UNSPECIFIED = "image/*";

    private View root;
    private Toolbar toolbar;
    private ImageView icon;
    private TextView tv_title;
    private Button btn_vibrant, btn_vibrant_dark, btn_vibrant_light, btn_muted, btn_muted_dark, btn_muted_light;

    private Palette palette;

    private int[] colors = {0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        FloatingActionButton fab = findViewById(R.id.fab);
        root = findViewById(R.id.root);
        icon = findViewById(R.id.icon);
        tv_title = findViewById(R.id.tv_title);
        btn_vibrant = findViewById(R.id.btn_vibrant);
        btn_vibrant_dark = findViewById(R.id.btn_vibrant_dark);
        btn_vibrant_light = findViewById(R.id.btn_vibrant_light);
        btn_muted = findViewById(R.id.btn_muted);
        btn_muted_dark = findViewById(R.id.btn_muted_dark);
        btn_muted_light = findViewById(R.id.btn_muted_light);

        fab.setOnClickListener(this);
        btn_vibrant.setOnClickListener(this);
        btn_vibrant_dark.setOnClickListener(this);
        btn_vibrant_light.setOnClickListener(this);
        btn_muted.setOnClickListener(this);
        btn_muted_dark.setOnClickListener(this);
        btn_muted_light.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (hasPermission()) {
                    pickPicture();
                }
                break;
            default:
                if (palette == null) {
                    break;
                }
                addBackgroundColor(getSwatch(palette, v));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICTURE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Bitmap bitmap = getSmallBitmap(this, data.getData());
                        if (bitmap != null) {
                            generatePalette(bitmap);
                        }
                    }
                }
                break;
        }
    }

    private void pickPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        startActivityForResult(intent, REQUEST_CODE_PICTURE);
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickPicture();
            }
        }
    }

    public static Bitmap getSmallBitmap(Context context, Uri uri) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        try {
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri), null, newOpts);
            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            int hh = 120;
            int ww = 120;
            int be = 1;
            if (w > h && w > ww) {
                be = newOpts.outWidth / ww;
            } else if (w < h && h > hh) {
                be = newOpts.outHeight / hh;
            }
            newOpts.inSampleSize = be;
            bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri), null, newOpts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void generatePalette(Bitmap bitmap) {
        icon.setImageBitmap(bitmap);
        Palette.Builder builder = Palette.from(bitmap);
        builder.generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                MainActivity.this.palette = palette;
                setButtonColor();
                colors[0] = 0;
                colors[1] = 0;
                addBackgroundColor(getSwatch(palette, btn_muted_light));
                addBackgroundColor(getSwatch(palette, btn_muted));
                addBackgroundColor(getSwatch(palette, btn_muted_dark));
            }
        });
    }

    private Palette.Swatch getSwatch(Palette palette, View button) {
        Palette.Swatch swatch;
        switch (button.getId()) {
            case R.id.btn_vibrant:
                swatch = palette.getVibrantSwatch();
                break;
            case R.id.btn_vibrant_dark:
                swatch = palette.getDarkVibrantSwatch();
                break;
            case R.id.btn_vibrant_light:
                swatch = palette.getLightVibrantSwatch();
                break;
            case R.id.btn_muted_dark:
                swatch = palette.getDarkMutedSwatch();
                break;
            case R.id.btn_muted_light:
                swatch = palette.getLightMutedSwatch();
                break;
            case R.id.btn_muted:
            default:
                swatch = palette.getMutedSwatch();
                break;
        }
        return swatch;
    }

    private void addBackgroundColor(Palette.Swatch swatch) {
        if (swatch == null) {
            return;
        }
        if (colors[1] == 0) {
            colors[1] = swatch.getRgb();
        } else if (colors[0] == 0) {
            colors[0] = swatch.getRgb();
            setRootBackground(swatch);
        }
    }

    private void setRootBackground(Palette.Swatch swatch) {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        root.setBackground(bg);
        root.post(new Runnable() {
            @Override
            public void run() {
                colors[0] = 0;
                colors[1] = 0;
            }
        });

        tv_title.setTextColor(swatch.getTitleTextColor());
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            DrawableCompat.setTint(drawable, swatch.getTitleTextColor());
        }
    }

    private void setButtonColor() {
        int buttonDefaultColor = fetchDefaultButtonColor();
        Palette.Swatch swatch = palette.getVibrantSwatch();
        setButtonColor(btn_vibrant, swatch, buttonDefaultColor);
        swatch = palette.getDarkVibrantSwatch();
        setButtonColor(btn_vibrant_dark, swatch, buttonDefaultColor);
        swatch = palette.getLightVibrantSwatch();
        setButtonColor(btn_vibrant_light, swatch, buttonDefaultColor);
        swatch = palette.getMutedSwatch();
        setButtonColor(btn_muted, swatch, buttonDefaultColor);
        swatch = palette.getDarkMutedSwatch();
        setButtonColor(btn_muted_dark, swatch, buttonDefaultColor);
        swatch = palette.getLightMutedSwatch();
        setButtonColor(btn_muted_light, swatch, buttonDefaultColor);
    }

    private void setButtonColor(Button button, Palette.Swatch swatch, int buttonDefaultColor) {
        if (swatch != null) {
            button.setBackgroundColor(swatch.getRgb());
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            button.setBackgroundColor(buttonDefaultColor);
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);
        }
    }

    private int fetchDefaultButtonColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorButtonNormal});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

}
