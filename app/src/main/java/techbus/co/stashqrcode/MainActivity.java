package techbus.co.stashqrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    ImageView qrCodeImageview;
    String QRcode;
    public final static int WIDTH=500;
    static String responseString="000";
    static String timestamp;
    String emailtext;
    static RadioGroup radioGenderGroup;
    static RadioButton radioGenderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button save = (Button) findViewById(R.id.saveButton);
        final EditText email=(EditText)findViewById(R.id.emailTextBox);
        final ProgressBar pb=(ProgressBar)findViewById(R.id.circleLoad);
        radioGenderGroup = (RadioGroup) findViewById(R.id.radioGroup);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                emailtext=email.getText().toString();
                if(TextUtils.isEmpty(emailtext))
                {

                    email.setError("Please Enter Email");
                    return;

                }
                else
                {
                    if (isEmailValid(emailtext.trim())) {


                        int selectedId=radioGenderGroup.getCheckedRadioButtonId();
                        radioGenderButton=(RadioButton)findViewById(selectedId);

                        String gender=radioGenderButton.getText().toString();
                        timestamp = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
                        String[] stringArray = {emailtext, gender, timestamp};
                        System.out.println("email " + emailtext+"gender "+gender+"timestamp "+timestamp);
                        System.out.println("time stamp= " + timestamp);
                        new MyAsyncTask().execute(stringArray);
                    }
                    else
                    {
                        email.setError("Please Enter Valid Email");
                    }
                }

            }
        });
        getID();

        Thread t = new Thread(new Runnable() {
            public void run() {

// this is the msg which will be encode in QRcode
                    timestamp = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
                    QRcode = "http://stashedphotos.stashcity.com/latest.html" + "#AppGenKey=" + timestamp;
                    try {
                        synchronized (this) {
                            wait(5000);
// runOnUiThread method used to do UI task in main thread.
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Bitmap bitmap = null;

                                        bitmap = encodeAsBitmap(QRcode);
                                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                        qrCodeImageview.setImageBitmap(bitmap);

                                    } catch (WriterException e) {
                                        e.printStackTrace();
                                    } // end of catch block

                                } // end of run method
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }





            }
        });
        t.start();

    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void getID() {
        qrCodeImageview=(ImageView) findViewById(R.id.img_qr_code_image);
    }

    // this is method call from on create and return bitmap image of QRCode.

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;

    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {


        ProgressBar pb = (ProgressBar)findViewById(R.id.circleLoad);
        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            postData(params[0],params[1],params[2]);
            return null;
        }

        protected void onPostExecute(Double result){
            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Email Saved", Toast.LENGTH_LONG).show();
            System.out.println(responseString);
        }
        protected void onProgressUpdate(Integer... progress){
            pb.setProgress(progress[0]);
        }

        public void postData(String email,String Gender,String AppGenKey) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://sp-as.herokuapp.com/api/users");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("Email", email));
                nameValuePairs.add(new BasicNameValuePair("Gender", Gender));
                nameValuePairs.add(new BasicNameValuePair("AppGenKey", AppGenKey));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
               // responseString=response.toString();
                if (response.getStatusLine().getStatusCode() == 200)
                {
                    HttpEntity entity = response.getEntity();
                    responseString = EntityUtils.toString(entity);
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }

    }
}
