package com.nbsoft.tvofall.volley;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;


public class RequestBodyWithUploadProgress extends RequestBody {
    private static final int SEGMENT_SIZE = 4096;//8192
    private static final String DISPOSITION_LENGTH = "Content-Disposition: form-data; name=ImageData ; filename=";
    public interface OkHttpProgressListener {
        void transferred(long num);
    }
    private final File file;
    private final OkHttpProgressListener listener;
    private final String contentType;

    public RequestBodyWithUploadProgress(File file, String contentType, OkHttpProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                this.listener.transferred(total);
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
}
