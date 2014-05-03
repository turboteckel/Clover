package org.floens.chan.ui.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.floens.chan.ChanApplication;
import org.floens.chan.R;
import org.floens.chan.utils.IOUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

public class ImagePickActivity extends Activity {
    private static final int IMAGE_RESULT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        try {
            startActivityForResult(intent, IMAGE_RESULT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.image_open_failed, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();

        if (requestCode == IMAGE_RESULT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();

                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String name = "image";
                if (nameIndex > -1) {
                    name = returnCursor.getString(nameIndex);
                }

                returnCursor.close();

                final String finalName = name;

                ChanApplication.getReplyManager()._onPickedFileLoading();

                // Async load the stream into "pickedFileCache", an file in the cache root
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final File cacheFile = new File(getCacheDir() + File.separator + "pickedFileCache");

                            if (cacheFile.exists()) {
                                cacheFile.delete();
                            }

                            InputStream is = getContentResolver().openInputStream(uri);
                            FileOutputStream fos = new FileOutputStream(cacheFile);

                            IOUtils.copy(is, fos);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ChanApplication.getReplyManager()._onPickedFile(finalName, cacheFile);
                                }

                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
}
