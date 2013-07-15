package com.example.imagetest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Style;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.graphics.PorterDuff;

import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.opencv.core.Mat;
import org.opencv.highgui.*;

import com.twotoasters.android.horizontalimagescroller.image.ImageToLoad;
import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadDrawableResource;
import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadSD;
import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadUrl;
import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScroller;
import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScrollerAdapter;


public class MainActivity extends Activity {

	
	ImageButton shutterButton;
	Bitmap mImageBitmap;
	ImageView mImageView;
	private Mat mRgba;
	private Mat finalImage;
	String TAG="SimpleImageCapture";
	private File imgFile;
	private Bitmap myBitmap;
	boolean isImageClicked=false;
	static int loading_progress=0;
	ProgressBar mProgress;
	private Handler mHandler = new Handler();
	ProgressDialog progress;
	float converted_xcoord,converted_ycoord;
	File leftimgFile;
	
	// Horizontal Image Scroll Stuff ..
	
	private List<HorizontalImageScroller> _horizontalImageScrollers;
	HorizontalImageScroller scroller;
	
	 ArrayList<ImageToLoad> images = new ArrayList<ImageToLoad>();
	// String key for persisting the scrollX position in Bundle objects 
	private static final String KEY_SCROLL_XES = "scrollXes";
	
	Bitmap imageViewBitmap;
	
	String []imageFilters={"sepia", "stark","sunnyside","cool", "worn","grayscale"};
	
	List<String> fileList = new ArrayList<String>();
	 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_main);
		
//		 mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		
		shutterButton=(ImageButton) findViewById(R.id.shutterButton);
		mImageView=(ImageView) findViewById(R.id.imageView1);
		System.loadLibrary("disp_img");
		shutterButton.setOnClickListener((android.view.View.OnClickListener) new MyOnClickListener());
		mImageView.setOnTouchListener(new TouchListener());
		
		progress= new ProgressDialog(this);
		
		scroller = (HorizontalImageScroller) findViewById(R.id.Scroller1);
		//scroller.setVisibility(View.INVISIBLE);
		scroller.setAdapter(new HorizontalImageScrollerAdapter(this,images));
		
		// This is a dummy image that I have to add to the horizontal scroller at the start,Seems like a bug .
		images.add(new ImageToLoadDrawableResource(R.drawable.ic_launcher));
		scroller.setImageSize(100); 
		scroller.setVisibility(View.INVISIBLE);
		
		 //Log.d("Size","size"+ String.valueOf(this.getWindowManager().getDefaultDisplay().getWidth()-mImageView.getHeight()));
        
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath()+"/Tesseract/");
        dir.mkdirs();
        
        scroller.setOnItemClickListener(onItemClickListener);
       
        
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    Log.d("Request code:"+requestCode,"tea");
	    
	    switch(requestCode){
	    case 1337:
	        if(resultCode==RESULT_OK)
	        {
	        	loading_progress=0;
	        	// @Jay : Change this to part to full_URI
		    	imgFile = new  File((String) data.getExtras().get("full_URI"));
		    	leftimgFile = new File((String) data.getExtras().get("left_URI"));
		    	Log.d("full_URI","url="+imgFile);
		    	
		    	isImageClicked=true;
		    	
		    	if(imgFile.exists())
		    	{
			    	myBitmap = BitmapFactory.decodeFile(leftimgFile.getAbsolutePath());
			    	imageViewBitmap = Bitmap.createScaledBitmap(myBitmap, mImageView.getWidth(),mImageView.getHeight(), true);
				    mImageView.setImageBitmap(imageViewBitmap);
				    scroller.setVisibility(View.VISIBLE);
				      
				    applyImageFilters();
		
			    }
	        }
	    }
	}
	 // Applying Instagram-like Filters 
    
    public void applyImageFilters()
    {
    	// Load Image .
    	        	
    	
    	long timestamp = System.currentTimeMillis();
    	
    	if(imgFile.exists())
    	{
    		// Resize the converted Bitmap..
	    	
	    	// Resize
	    	Bitmap resizedbitmap = Bitmap.createScaledBitmap(myBitmap, 200,100, true);
	    	Bitmap backupbitmap=resizedbitmap;
	    	// Apply Different Filters and save 
	    	Paint paint = new Paint();
			ColorMatrix cm = new ColorMatrix();
			
			
			 Canvas canvas = new Canvas(resizedbitmap);
			 canvas.drawBitmap(resizedbitmap, 0, 0, new Paint());
			
			// Make an instagram version of the filters ..
			 for (int i = 0; i < imageFilters.length; i++) 
			 {
				
				 findFilterandSave(imageFilters[i],resizedbitmap,backupbitmap,canvas,cm,paint);
				 
			//	 resizedbitmap=backupbitmap;
//				 Paint transpaint = new Paint();
//				 transpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); 
//
//				 Rect rect=new Rect(0,0,canvas.getWidth(),canvas.getHeight());
//				 canvas.drawRect(rect,transpaint);
				 resizedbitmap = Bitmap.createScaledBitmap(myBitmap, 200,100, true);
				 canvas = new Canvas(resizedbitmap);
				 canvas.drawBitmap(resizedbitmap, 0, 0, new Paint());
				 

			 }
			 
			 // Load the Instagrammed Pictures into the Horizontal Image Scroller ..
			 
			 // Getting the list of files in the Tesseract Directory
		        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tesseract/");
		        
		        LoadFiles(root);
		        scroller.invalidate();
		    //  scroller.setVisibility(0);
		     // scroller.bringToFront();
//		        scroller.postInvalidate();
//		        scroller.refreshDrawableState();

			 //
			 
			 
			
			   
	    }
    	
    	Toast.makeText(getApplicationContext(), "Time taken for applying Filters"+String.valueOf(System.currentTimeMillis()-timestamp), Toast.LENGTH_SHORT).show();
    	
    	
    }
    
    public void findFilterandSave(String filterName,Bitmap canvas_bitmap,Bitmap backup_bitmap,Canvas canvas,ColorMatrix cm,Paint paint)
    {
    	canvas.drawBitmap(canvas_bitmap, 0, 0, new Paint());
    	
    	if (filterName.equalsIgnoreCase("stark")) {

			Paint spaint = new Paint();
			ColorMatrix scm = new ColorMatrix();

			scm.setSaturation(0);
			final float m[] = scm.getArray();
			final float c = 1;
			scm.set(new float[] { m[0] * c, m[1] * c, m[2] * c, m[3] * c, m[4] * c + 15, m[5] * c, m[6] * c,
					m[7] * c, m[8] * c, m[9] * c + 8, m[10] * c, m[11] * c, m[12] * c, m[13] * c, m[14] * c + 10,
					m[15], m[16], m[17], m[18], m[19] });

			spaint.setColorFilter(new ColorMatrixColorFilter(scm));
			Matrix smatrix = new Matrix();
			canvas.drawBitmap(canvas_bitmap, smatrix, spaint);

			cm.set(new float[] { 1, 0, 0, 0, -90, 0, 1, 0, 0, -90, 0, 0, 1, 0, -90, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("sunnyside")) {

			cm.set(new float[] { 1, 0, 0, 0, 10, 0, 1, 0, 0, 10, 0, 0, 1, 0, -60, 0, 0, 0, 1, 0 });
		}
		else if (filterName.equalsIgnoreCase("worn")) {

			cm.set(new float[] { 1, 0, 0, 0, -60, 0, 1, 0, 0, -60, 0, 0, 1, 0, -90, 0, 0, 0, 1, 0 });
		}
		else if (filterName.equalsIgnoreCase("grayscale")) {

			float[] matrix = new float[] { 0.3f, 0.59f, 0.11f, 0, 0, 0.3f, 0.59f, 0.11f, 0, 0, 0.3f, 0.59f, 0.11f,
					0, 0, 0, 0, 0, 1, 0, };

			cm.set(matrix);

		}
		else if (filterName.equalsIgnoreCase("cool")) {

			cm.set(new float[] { 1, 0, 0, 0, 10, 0, 1, 0, 0, 10, 0, 0, 1, 0, 60, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter0")) {

			cm.set(new float[] { 1, 0, 0, 0, 30, 0, 1, 0, 0, 10, 0, 0, 1, 0, 20, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter1")) {

			cm.set(new float[] { 1, 0, 0, 0, -33, 0, 1, 0, 0, -8, 0, 0, 1, 0, 56, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter2")) {

			cm.set(new float[] { 1, 0, 0, 0, -42, 0, 1, 0, 0, -5, 0, 0, 1, 0, -71, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter3")) {

			cm.set(new float[] { 1, 0, 0, 0, -68, 0, 1, 0, 0, -52, 0, 0, 1, 0, -15, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter4")) {

			cm.set(new float[] { 1, 0, 0, 0, -24, 0, 1, 0, 0, 48, 0, 0, 1, 0, 59, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter5")) {

			cm.set(new float[] { 1, 0, 0, 0, 83, 0, 1, 0, 0, 45, 0, 0, 1, 0, 8, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter6")) {

			cm.set(new float[] { 1, 0, 0, 0, 80, 0, 1, 0, 0, 65, 0, 0, 1, 0, 81, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter7")) {

			cm.set(new float[] { 1, 0, 0, 0, -44, 0, 1, 0, 0, 38, 0, 0, 1, 0, 46, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter8")) {

			cm.set(new float[] { 1, 0, 0, 0, 84, 0, 1, 0, 0, 63, 0, 0, 1, 0, 73, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("random")) {

			//pick an integer between -90 and 90 apply
			int min = -90;
			int max = 90;
			Random rand = new Random();

			int five = rand.nextInt(max - min + 1) + min;
			int ten = rand.nextInt(max - min + 1) + min;
			int fifteen = rand.nextInt(max - min + 1) + min;

			Log.d(TAG, "five " + five);
			Log.d(TAG, "ten " + ten);
			Log.d(TAG, "fifteen " + fifteen);

			cm.set(new float[] { 1, 0, 0, 0, five, 0, 1, 0, 0, ten, 0, 0, 1, 0, fifteen, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("sepia")) {

			float[] sepMat = { 0.3930000066757202f, 0.7689999938011169f, 0.1889999955892563f, 0, 0,
					0.3490000069141388f, 0.6859999895095825f, 0.1679999977350235f, 0, 0, 0.2720000147819519f,
					0.5339999794960022f, 0.1309999972581863f, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 };
			cm.set(sepMat);
		}
    	
    	
    	paint.setColorFilter(new ColorMatrixColorFilter(cm));
		Matrix matrix = new Matrix();
		canvas.drawBitmap(canvas_bitmap, matrix, paint);
		paint=new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(20);
		canvas.drawText(filterName, (float) (100-5*filterName.length()), 80, paint); 
		
		/* code... */

		String fileName = Environment.getExternalStorageDirectory() + "/Tesseract/"+filterName+".png";
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(fileName);
			/* Write bitmap to file using JPEG or PNG and 80% quality hint for JPEG. */
			canvas_bitmap.compress(CompressFormat.PNG, 100, stream);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d("Filter name","path "+filterName);    	
    }
    
//    public int returnPhoneWidth()
//    {
//    Display display = getWindowManager().getDefaultDisplay();
//    DisplayMetrics outMetrics = new DisplayMetrics ();
//    display.getMetrics(outMetrics);
//
//    float density  = getResources().getDisplayMetrics().density;
//    float dpHeight = outMetrics.heightPixels / density;
//    float dpWidth  = outMetrics.widthPixels / density; 
//    
//    return 	;
//    
//    }
    
    void LoadFiles(File f)
    {
        
        
// Alternative way of loading the files by listing all the filters in the folder .
//        File[] files = f.listFiles();
//        fileList.clear();
//        
//        for (File file : files){
//        fileList.add(file.getPath());  
//        Log.d("File path:","Path="+file.getPath());
//       
//        images.add(new ImageToLoadSD(file.getPath()));
//        }
        
        for (int i=0;i<imageFilters.length;i++)
        	images.add(new ImageToLoadSD(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tesseract/"+imageFilters[i]+".png"));
        
        images.remove(0); // Removing the first image we inserted ..Nasty hack but cant help it !
	
     
       
      }   
    
    
    OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			//Toast.makeText(MainActivity.this, "Position "+position, Toast.LENGTH_SHORT).show();
			
			applyFiltertoView(imageFilters[position]);
			
		}
	};
    
    
    public void applyFiltertoView(String filterName)
    {
    	
    	Toast.makeText(MainActivity.this, filterName, Toast.LENGTH_SHORT).show();
    	
    	Bitmap modifiedBitmap=Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth(),myBitmap.getHeight(), true);
    	
    	
    	Canvas imageViewCanvas=new Canvas(modifiedBitmap);
    	imageViewCanvas.drawBitmap(modifiedBitmap, 0, 0, new Paint());
    	
    	ColorMatrix cm = new ColorMatrix();
    	
    	if (filterName.equalsIgnoreCase("stark")) {

			Paint spaint = new Paint();
			ColorMatrix scm = new ColorMatrix();

			scm.setSaturation(0);
			final float m[] = scm.getArray();
			final float c = 1;
			scm.set(new float[] { m[0] * c, m[1] * c, m[2] * c, m[3] * c, m[4] * c + 15, m[5] * c, m[6] * c,
					m[7] * c, m[8] * c, m[9] * c + 8, m[10] * c, m[11] * c, m[12] * c, m[13] * c, m[14] * c + 10,
					m[15], m[16], m[17], m[18], m[19] });

			spaint.setColorFilter(new ColorMatrixColorFilter(scm));
			Matrix smatrix = new Matrix();
			imageViewCanvas.drawBitmap(modifiedBitmap, smatrix, spaint);

			cm.set(new float[] { 1, 0, 0, 0, -90, 0, 1, 0, 0, -90, 0, 0, 1, 0, -90, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("sunnyside")) {

			cm.set(new float[] { 1, 0, 0, 0, 10, 0, 1, 0, 0, 10, 0, 0, 1, 0, -60, 0, 0, 0, 1, 0 });
		}
		else if (filterName.equalsIgnoreCase("worn")) {

			cm.set(new float[] { 1, 0, 0, 0, -60, 0, 1, 0, 0, -60, 0, 0, 1, 0, -90, 0, 0, 0, 1, 0 });
		}
		else if (filterName.equalsIgnoreCase("grayscale")) {

			float[] matrix = new float[] { 0.3f, 0.59f, 0.11f, 0, 0, 0.3f, 0.59f, 0.11f, 0, 0, 0.3f, 0.59f, 0.11f,
					0, 0, 0, 0, 0, 1, 0, };

			cm.set(matrix);

		}
		else if (filterName.equalsIgnoreCase("cool")) {

			cm.set(new float[] { 1, 0, 0, 0, 10, 0, 1, 0, 0, 10, 0, 0, 1, 0, 60, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter0")) {

			cm.set(new float[] { 1, 0, 0, 0, 30, 0, 1, 0, 0, 10, 0, 0, 1, 0, 20, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter1")) {

			cm.set(new float[] { 1, 0, 0, 0, -33, 0, 1, 0, 0, -8, 0, 0, 1, 0, 56, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter2")) {

			cm.set(new float[] { 1, 0, 0, 0, -42, 0, 1, 0, 0, -5, 0, 0, 1, 0, -71, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter3")) {

			cm.set(new float[] { 1, 0, 0, 0, -68, 0, 1, 0, 0, -52, 0, 0, 1, 0, -15, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter4")) {

			cm.set(new float[] { 1, 0, 0, 0, -24, 0, 1, 0, 0, 48, 0, 0, 1, 0, 59, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter5")) {

			cm.set(new float[] { 1, 0, 0, 0, 83, 0, 1, 0, 0, 45, 0, 0, 1, 0, 8, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter6")) {

			cm.set(new float[] { 1, 0, 0, 0, 80, 0, 1, 0, 0, 65, 0, 0, 1, 0, 81, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter7")) {

			cm.set(new float[] { 1, 0, 0, 0, -44, 0, 1, 0, 0, 38, 0, 0, 1, 0, 46, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("filter8")) {

			cm.set(new float[] { 1, 0, 0, 0, 84, 0, 1, 0, 0, 63, 0, 0, 1, 0, 73, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("random")) {

			//pick an integer between -90 and 90 apply
			int min = -90;
			int max = 90;
			Random rand = new Random();

			int five = rand.nextInt(max - min + 1) + min;
			int ten = rand.nextInt(max - min + 1) + min;
			int fifteen = rand.nextInt(max - min + 1) + min;

			Log.d(TAG, "five " + five);
			Log.d(TAG, "ten " + ten);
			Log.d(TAG, "fifteen " + fifteen);

			cm.set(new float[] { 1, 0, 0, 0, five, 0, 1, 0, 0, ten, 0, 0, 1, 0, fifteen, 0, 0, 0, 1, 0 });

		}
		else if (filterName.equalsIgnoreCase("sepia")) {

			float[] sepMat = { 0.3930000066757202f, 0.7689999938011169f, 0.1889999955892563f, 0, 0,
					0.3490000069141388f, 0.6859999895095825f, 0.1679999977350235f, 0, 0, 0.2720000147819519f,
					0.5339999794960022f, 0.1309999972581863f, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 };
			cm.set(sepMat);
		}
    	
    	Paint paint =new Paint();
    	
    	paint.setColorFilter(new ColorMatrixColorFilter(cm));
		Matrix matrix = new Matrix();
		
		imageViewCanvas.drawBitmap(modifiedBitmap, matrix, paint);
		mImageView.setImageBitmap(modifiedBitmap);
	
    	
    }
    
    
    
    
	class MyOnClickListener implements View.OnClickListener{

		public void onClick(View v) {
			
			    Intent camera_intent=new Intent(getBaseContext(),CustomClickActivity.class);
			    startActivityForResult(camera_intent,1337);
		
		}
	     };
	     
	     
	     class TouchListener implements View.OnTouchListener{

	 		@Override
	 		public boolean onTouch(View v, MotionEvent event) {

	 			if(event.getAction() == MotionEvent.ACTION_DOWN&&isImageClicked) 
	 			{
	 				
	 				// Image has been clicked already,so u should not be able to touch the ImageView again ..
	 				isImageClicked=false;
	 				
	 				Log.d(TAG,"X ="+(event.getRawX()-mImageView.getLeft())+"  Y= "+(event.getRawY()-mImageView.getTop())); // For landscape orientation,i.e max val of x is 800 and y max value is 480 ..

	 				
	 				
	 				// Pass these to the JNI function
	 				converted_xcoord=(event.getRawX()-mImageView.getLeft());
	 				converted_ycoord=(event.getRawY()-mImageView.getTop());
	 				
	 				
	 				converted_xcoord=(converted_xcoord/mImageView.getWidth())*640;
	 				converted_ycoord=(converted_ycoord/mImageView.getHeight())*720;
	 				// 
	 				
	 				
	 				Log.d(TAG, "converted");
	 				mRgba = new Mat();
			    	finalImage = new Mat();
				    	
			    	Log.d(TAG,"Initialized Mat");
			    	Log.d(TAG,"progress"+loading_progress);
			    	
			    	mRgba = Highgui.imread(imgFile.getAbsolutePath());
			    
			    	Log.d(TAG, "Image loaded");
			    	
			    	//mRgba = Highgui.imread(filename);
			    	
			    	
				    new ComputeDisparity().execute("");
				   
	 			}
	 			else 
	 			{
	 				
	 				Toast.makeText(getBaseContext(), "Please click an image", Toast.LENGTH_SHORT).show();
	 			
	 			}
	 			return false;
	 		}

	 	  }

    public native void getDisparity(long matAddrRgba, long matAddrfinalImage, int ji1, int ji2);

    private class ComputeDisparity extends AsyncTask<String, Void, String> {
    	
        @Override
        protected String doInBackground(String... params) {
        	getDisparity(mRgba.getNativeObjAddr(), finalImage.getNativeObjAddr(), (int)converted_xcoord, (int)converted_ycoord);

              return "";
        }      

        @Override
        protected void onPostExecute(String result) {
           
             progress.dismiss();
             
             String colVal = String.valueOf(finalImage.cols());
	    	 Log.d("Cols", colVal);
	    	 Highgui.imwrite(imgFile.getAbsolutePath(), finalImage);
	    	 myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		     mImageView.setImageBitmap(myBitmap);
		    
		       Log.d("done","done");
             
             // txt.setText(result);
             
              //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
        	   progress.setTitle("Processing Image");
               progress.setMessage("Please wait while we process your image ...");
               progress.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        

        
        
        
        
        
        
  }   
}

