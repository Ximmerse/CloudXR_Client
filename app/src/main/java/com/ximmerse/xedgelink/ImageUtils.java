package com.ximmerse.xedgelink;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {
    static Bitmap pngToBitmap(String file) throws IOException {
        Bitmap bmf = BitmapFactory.decodeFile(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File f = new File("/sdcard/1.bmp");
        FileOutputStream fos = new FileOutputStream(f);
        ByteBuffer buf = ByteBuffer.allocate(bmf.getByteCount());
        bmf.copyPixelsToBuffer(buf);
        fos.write(buf.array());
        fos.close();
        return bmf;
    }

    static int loadBitmapToOpenGl(Bitmap bm) {
        return 0;
    }
}
