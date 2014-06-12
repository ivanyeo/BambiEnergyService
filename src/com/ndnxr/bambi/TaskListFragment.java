package com.ndnxr.bambi; 


import java.util.ArrayList;

import com.ndnxr.bambi.R;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
 
public class TaskListFragment extends Fragment {
	
	public TableLayout tableLayout;
	private TextView text;
	private LinearLayout rootViewLayout;
	private int screen_width ;
    private int screen_height ;
    private int tableRowNumberPerPage;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	   	
        View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);
        rootViewLayout = (LinearLayout)rootView;
            
        initialTable(this.getActivity());
        
        // Get Screen Size to display relative layout for table
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        tableRowNumberPerPage = 10;
           		
        return rootView;
        
    }
    /**
     * Draw table with task list information
     * @param context
     * @param replyList
     */   
    public void drawTable(Context context, ArrayList<Task> replyList ){
	
    	// Clear old table view
    	rootViewLayout.removeAllViews();
    	
    	// Initialize table title
    	initialTable(context);
    	
    	for (Task t : replyList) {		
			G.Log("DrawTable: " + t.getType());
			
			// Create horizontal linear layout as a table row
	    	LinearLayout linearLayout = new LinearLayout(context);
	    	linearLayout.setOrientation(LinearLayout.HORIZONTAL);
	    	linearLayout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, screen_height/tableRowNumberPerPage) );
	    	  	
	    	// Extract text information from Task
	    	String[] info = new String[3];
	    	info[0] = "" + t.getType(); // Task type
	    	if(t.getUrgency() == BambiLib.URGENCY.NORMAL){
	    		info[1] = "-";  // No deadline for normal task
	    	}
	    	else{
	    		info[1] = "" + t.getDeadline(); //Deadline for schedule task	
	    	}
	    	info[2] = ((Email) t.getPayload()).getSubject(); // Get e-mail subject as description
	    	
	    	// Add information to row
	    	for (String word:info){
	    		text = new TextView(context);
	        	text.setText(word);
	            text.setLayoutParams( new android.widget.LinearLayout.LayoutParams(
	            		linearLayout.getWidth()/info.length, LinearLayout.LayoutParams.FILL_PARENT, 1f));
	            text.setGravity(Gravity.CENTER);
	            text.setBackgroundColor(Color.parseColor("#dcdcdc"));
	            linearLayout.addView(text);
	    	}
	          
	    	rootViewLayout.addView(linearLayout);			
		}
    }
    
    /**
     * Initialize table with title row
     * @param context
     */   
    public void initialTable(Context context){
    	
    	// Create horizontal linear layout as a table row
    	LinearLayout linearLayout = new LinearLayout(context);
    	linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    	linearLayout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) );
    	  	
    	// Add text view to each linear layout
    	String[] info = {"Type","Deadline","Description"};
    	
    	for (String word:info){
    		text = new TextView(context);
        	text.setText(word);
            text.setLayoutParams( new android.widget.LinearLayout.LayoutParams(
            		linearLayout.getWidth()/info.length, LayoutParams.WRAP_CONTENT, 1f));
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setBackgroundColor(Color.parseColor("#ff8400"));
            text.setTypeface(null,Typeface.BOLD);
            linearLayout.addView(text);
    	}
    	        
    	rootViewLayout.addView(linearLayout);   	
    }
}
