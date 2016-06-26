package com.angelhack.giveaway.giveaway;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonTake;
    private Button buttonExp;

    private ImageView imageView;

    private EditText editTextName;

    private EditText editTextQuant;

    private EditText editTextExpry;

    private DatePickerDialog expryDatePickerDialog;

    private SimpleDateFormat dateFormatter;

    private Bitmap bitmap;

    private Bitmap exBitmap;

    private int PICK_IMAGE_REQUEST = 1;

    private String UPLOAD_URL ="http://angel-hack-gilln.c9users.io/giveaway";
    private static final int CAMERA_REQUEST = 1888;
    private static final int EXPIRY_REQUEST = 2000;

    private String KEY_IMAGE = "image";
    private String KEY_ID = "vendorId";
    private String KEY_QUANTITY = "quantity";

    String vendorName = "";

    String quantity = "";

    String expiryDate = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonTake = (Button) findViewById(R.id.buttonTake);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonExp = (Button) findViewById(R.id.buttonExp);

        editTextName = (EditText) findViewById(R.id.editText);

        editTextQuant = (EditText) findViewById(R.id.editQuan);

        editTextExpry = (EditText) findViewById(R.id.editExpry);
        editTextExpry.setInputType(InputType.TYPE_NULL);

        imageView  = (ImageView) findViewById(R.id.imageView);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonTake.setOnClickListener(this);
        buttonExp.setOnClickListener(this);
        setDateTimeField();
        editTextName.setText(i.getStringExtra("user"));
    }

    private void setDateTimeField() {
        editTextExpry.setOnClickListener(this);

        Date today = new Date();

        editTextExpry.setText(dateFormatter.format(today));

        Calendar newCalendar = Calendar.getInstance();

        expryDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editTextExpry.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getStringImage(Bitmap bmp){
        if(bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
        }
        return "";
    }

    private void uploadImage(){

        //Getting Image Name
        vendorName = editTextName.getText().toString().trim();

        //Getting Image Name
        quantity = editTextQuant.getText().toString().trim();
        //Getting Image Name

        expiryDate = editTextExpry.getText().toString().trim();
        new UploadToServer().execute();
        //Showing the progress dialog
    }

    private void captureImage(int requestCode) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, requestCode);
    }

    public class UploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("image", getStringImage(bitmap)));
            nameValuePairs.add(new BasicNameValuePair("expiryImage", getStringImage(exBitmap)));
            nameValuePairs.add(new BasicNameValuePair("vendorId", "123"));
            nameValuePairs.add(new BasicNameValuePair("quantity", quantity));
            nameValuePairs.add(new BasicNameValuePair("expiryDate", expiryDate));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(UPLOAD_URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());
                Log.d("log_tag", "Attempt to send request: " + st);

            } catch (Exception e) {
                Log.d("log_tag", "Error in http connection " + e.toString());
                return "Failed";
            }
            return "Success";

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
            if("Failed".equals(result)) {
                Toast.makeText(getApplicationContext(), "Unable to connect to server. Try again later!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Thanks for contribution! :)", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        } else if (requestCode == EXPIRY_REQUEST && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            exBitmap = (Bitmap) data.getExtras().get("data");
        }
    }

    @Override
    public void onClick(View v) {

        if(v == buttonChoose){
            showFileChooser();
        }

        if(v == buttonTake) {
            captureImage(CAMERA_REQUEST);
        }

        if(v == buttonUpload){
            uploadImage();
        }

        if(v == editTextExpry) {
            expryDatePickerDialog.show();
        }

        if(v == buttonExp) {
            captureImage(EXPIRY_REQUEST);
        }
    }
}
