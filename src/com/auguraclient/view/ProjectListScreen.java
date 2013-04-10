package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.util.SoundEngine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProjectListScreen extends Activity
{

	private TextView tab_your_card=null;
	private TextView tab_promotions=null;
	private TextView tab_instructions=null;

	private LinearLayout yourcard_layout=null;
	private LinearLayout promotions_layout=null;
	private LinearLayout instructions_layout=null;
	private LinearLayout cliente_button=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setfullScreen();
  	    setContentView(R.layout.projectlist);

  	    overridePendingTransition(R.anim.fadein, R.anim.fadeout);



  	  tab_your_card=(TextView)findViewById(R.id.bottom_tab_add_project);
	  yourcard_layout=(LinearLayout)findViewById(R.id.tab_add_project_layout);
	  yourcard_layout.setOnClickListener(yourcard_listner);


    }




    OnClickListener yourcard_listner=new OnClickListener()
    {
 		public void onClick(View v)
 		{
 			SoundEngine.sharedEngine().playEffect(getApplicationContext(),R.raw.button);
 		}
     };




    public void setfullScreen()
 	{
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 	}
}
