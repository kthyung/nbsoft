package com.nbsoft.tvofall.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class JsonBodyRequest extends Request<NetworkResponse> {
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;

    public JsonBodyRequest(int method, String url,
                           Response.Listener<NetworkResponse> listener,
                           Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public String getBodyContentType() {
        return "application/json;charset=UTF-8";
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    private void buildJsonPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        //dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        //dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"meta-data\"" + lineEnd);
        //dataOutputStream.writeBytes("Content-type: application/json; charset=UTF-8" + lineEnd);
        //dataOutputStream.writeBytes(lineEnd);
        //dataOutputStream.writeBytes(parameterValue + lineEnd);
        //dataOutputStream.writeChars(parameterValue + lineEnd);
        dataOutputStream.write((parameterValue + lineEnd).getBytes());
    }

    private void jsonParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildJsonPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                jsonParse(dos, params, getParamsEncoding());
            }
            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
