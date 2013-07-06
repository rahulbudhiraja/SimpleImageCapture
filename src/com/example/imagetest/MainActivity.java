package com.example.imagetest;

import java.io.File;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {

	
	ImageButton shutterButton;
	Bitmap mImageBitmap;
	ImageView mImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_main);
		
		shutterButton=(ImageButton) findViewById(R.id.shutterButton);
		mImageView=(ImageView) findViewById(R.id.imageView1);
		
		shutterButton.setOnClickListener((android.view.View.OnClickListener) new MyOnClickListener());
		    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	private void handleSmallCameraPhoto(Intent intent) {
	    Bundle extras = intent.getExtras();
	    mImageBitmap = (Bitmap) extras.get("data");
	    mImageView.setImageBitmap(mImageBitmap);
	    mImageBitmap.recycle();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    Log.d("Request code:"+requestCode,"tea");
	    
	    switch(requestCode){
	    case 1337:
	        if(resultCode==RESULT_OK)
	        {
	           
	        	//Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
	         //  mImageView.setImageBitmap(thumbnail);
    	File imgFile = new  File((String) data.getExtras().get("left_URI"));
    	Log.d("left_uri","url="+imgFile);
    	if(imgFile.exists())
    	{
	    	Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		    mImageView.setImageBitmap(myBitmap);

	        	}
	        }
	    }
	}

	class MyOnClickListener implements View.OnClickListener{
		
//	     public boolean onClick(View v, MotionEvent event) 
//	     {
//			    if(event.getAction()==(MotionEvent.ACTION_DOWN))
//			    {
//			    	
////			   	Intent camera_intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
////			    startActivityForResult(camera_intent, 1337);
//			    Intent camera_intent=new Intent(getBaseContext(),CustomClickActivity.class);
//			    startActivityForResult(camera_intent,1337);
//			    }
//				return false;
//			    	
//	     }
//
//		@Override
//		public void onClick(DialogInterface dialog, int which) {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			    Intent camera_intent=new Intent(getBaseContext(),CustomClickActivity.class);
			    startActivityForResult(camera_intent,1337);
		
		}
	     };

}
