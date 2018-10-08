package com.nbsoft.sample.volley;

import com.nbsoft.sample.Define;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NukeSSLCerts {
    protected static final String TAG = NukeSSLCerts.class.getSimpleName();

    public static void nuke() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
        	        /* Create a new array with room for an additional trusted certificate. */
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession arg1) {
                    //Log.d("NukeSSLCerts", "hostname  : " + hostname);
                    if(hostname.equalsIgnoreCase(Define.SERVER_IP)
                            ||hostname.indexOf(Define.GSTATIC_COM) != -1
                            ||hostname.indexOf(Define.GOOGLE_COM) != -1
                            ||hostname.indexOf(Define.FACEBOOK_COM) != -1
                            ||hostname.indexOf(Define.FACEBOOK_NET) != -1
                            ||hostname.indexOf(Define.FBCDN_NET) != -1
                            ||hostname.indexOf(Define.FB_COM) != -1) {
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            // pass
        }
    }
}
