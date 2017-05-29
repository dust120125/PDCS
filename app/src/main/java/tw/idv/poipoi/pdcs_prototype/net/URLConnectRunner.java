package tw.idv.poipoi.pdcs_prototype.net;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by DuST on 2017/5/25.
 */

public class URLConnectRunner {

    public URLConnectRunner(String url, final String post, final Charset charset, final Callback callback) {
        AsyncTask<String, Void, String> urlTask = new AsyncTask<String, Void, String>() {

            Callback cb;

            @Override
            protected void onPreExecute() {
                this.cb = callback;
            }

            @Override
            protected void onPostExecute(String s) {
                if (cb != null)
                    cb.runCallback(s);
            }

            @Override
            protected String doInBackground(String... params) {
                String result = null;
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    if (post != null && !post.isEmpty()){
                        con.setRequestMethod("POST");
                        con.setDoOutput(true);
                        DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                        dos.writeBytes(post);
                        dos.flush();
                        dos.close();
                    }

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), charset));
                    StringWriter sw = new StringWriter();
                    String tmp;
                    while ((tmp = br.readLine()) != null) {
                        sw.write(tmp);
                    }
                    result = sw.toString();

                    //Log.d("URL result", result);
                    //Remove UTF-8 BOM header
                    if (result.charAt(0) == 65279) result = result.substring(1);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                return result;
            }
        };
        urlTask.execute(url);
    }
}
