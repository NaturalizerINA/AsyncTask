package com.mukminullah;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;

    private CoordinatorLayout mCLayout;
    private Button mButtonDo;
    private ProgressDialog mProgressDialog;
    private LinearLayout mLLayout;

    private AsyncTask mMyTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the application context
        mContext = getApplicationContext();
        mActivity = MainActivity.this;

        // Get the widget reference from XML layout
        mCLayout = findViewById(R.id.coordinator_layout);
        mButtonDo = findViewById(R.id.btn_do);
        mLLayout = findViewById(R.id.ll);

        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Progress dialog title
        mProgressDialog.setTitle("AsyncTask");
        // Progress dialog message
        mProgressDialog.setMessage("Please wait, we are downloading your image files...");
        mProgressDialog.setCancelable(true);

        // Set a progress dialog dismiss listener
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // Cancel the AsyncTask
                mMyTask.cancel(false);
            }
        });

        // Specify some url to download images
        final URL url1 = stringToURL("http://www.freeimageslive.com/galleries/transtech/informationtechnology/pics/beige_keyboard.jpg");
        final URL url2 = stringToURL("http://www.freeimageslive.com/galleries/transtech/informationtechnology/pics/computer_blank_screen.jpg");
        final URL url3 = stringToURL("http://www.freeimageslive.com/galleries/transtech/informationtechnology/pics/computer_memory_dimm.jpg");
        final URL url4 = stringToURL("http://www.freeimageslive.com/galleries/transtech/informationtechnology/pics/computer_memory.jpg");
        final URL url5 = stringToURL("http://www.freeimageslive.com/galleries/transtech/informationtechnology/pics/ethernet_router.jpg");

        // Initialize a new click listener for positive button widget
        mButtonDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Execute the async task
                mMyTask = new DownloadTask()
                        .execute(
                                url1,
                                url2,
                                url3,
                                url4,
                                url5
                        );
            }
        });
    }

    private class DownloadTask extends AsyncTask<URL,Integer,List<Bitmap>>{
        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }

        // Do the task in background/non UI thread
        protected List<Bitmap> doInBackground(URL...urls){
            int count = urls.length;
            //URL url = urls[0];
            HttpURLConnection connection = null;
            List<Bitmap> bitmaps = new ArrayList<>();

            // Loop through the urls
            for(int i=0;i<count;i++){
                URL currentURL = urls[i];
                // So download the image from this url
                try{
                    // Initialize a new http url connection
                    connection = (HttpURLConnection) currentURL.openConnection();

                    // Connect the http url connection
                    connection.connect();

                    // Get the input stream from http url connection
                    InputStream inputStream = connection.getInputStream();

                    // Initialize a new BufferedInputStream from InputStream
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    // Convert BufferedInputStream to Bitmap object
                    Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                    // Add the bitmap to list
                    bitmaps.add(bmp);

                    // Publish the async task progress
                    // Added 1, because index start from 0
                    publishProgress((int) (((i+1) / (float) count) * 100));
                    if(isCancelled()){
                        break;
                    }

                }catch(IOException e){
                    e.printStackTrace();
                }finally{
                    // Disconnect the http url connection
                    connection.disconnect();
                }
            }
            // Return bitmap list
            return bitmaps;
        }

        // On progress update
        protected void onProgressUpdate(Integer... progress){
            // Update the progress bar
            mProgressDialog.setProgress(progress[0]);
        }

        // On AsyncTask cancelled
        protected void onCancelled(){
            Toast.makeText(mContext, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

        // When all async task done
        protected void onPostExecute(List<Bitmap> result){
            // Hide the progress dialog
            mProgressDialog.dismiss();

            // Remove all views from linear layout
            mLLayout.removeAllViews();

            // Loop through the bitmap list
            for(int i=0;i<result.size();i++){
                Bitmap bitmap = result.get(i);
                // Save the bitmap to internal storage
                Uri imageInternalUri = saveImageToInternalStorage(bitmap,i);
                // Display the bitmap from memory
                addNewImageViewToLayout(bitmap);
                // Display bitmap from internal storage
                addNewImageViewToLayout(imageInternalUri);
            }
        }
    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    // Custom method to save a bitmap into internal storage
    protected Uri saveImageToInternalStorage(Bitmap bitmap, int index){
        // Initialize ContextWrapper
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, "UniqueFileName"+ index+".jpg");

        try{
            // Initialize a new OutputStream
            OutputStream stream = null;

            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Return the saved image Uri
        return savedImageURI;
    }

    // Custom method to add a new image view using bitmap
    protected void addNewImageViewToLayout(Bitmap bitmap){
        // Initialize a new ImageView widget
        ImageView iv = new ImageView(getApplicationContext());

        // Set an image for ImageView
        iv.setImageBitmap(bitmap);

        // Create layout parameters for ImageView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500);

        // Add layout parameters to ImageView
        iv.setLayoutParams(lp);

        // Finally, add the ImageView to layout
        mLLayout.addView(iv);
    }

    // Custom method to add a new image view using uri
    protected void addNewImageViewToLayout(Uri uri){
        // Initialize a new ImageView widget
        ImageView iv = new ImageView(getApplicationContext());

        // Set an image for ImageView
        iv.setImageURI(uri);

        // Create layout parameters for ImageView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300);

        // Add layout parameters to ImageView
        iv.setLayoutParams(lp);

        // Finally, add the ImageView to layout
        mLLayout.addView(iv);
    }
}
