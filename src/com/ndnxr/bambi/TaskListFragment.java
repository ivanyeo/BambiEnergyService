package com.ndnxr.bambi; 


import com.ndnxr.bambi.R;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	   	
        View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);
        rootViewLayout = (LinearLayout)rootView;
        
        DrawTable(this.getActivity());
        
    	//View rootView = new View(mActivity);
    	//View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);
      		
        return rootView;
        
    }
    
    public void DrawTable(Context context){
    	
    	// Create horizontal linear layout as a table row
    	LinearLayout linearLayout = new LinearLayout(context);
    	linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    	linearLayout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) );
    	  	
    	// Add text view to each linear layout
    	String[] info = {"Type","Add Time","Deadline","Description"};
    	
    	for (String word:info){
    		text = new TextView(context);
        	text.setText(word);
            text.setLayoutParams( new android.widget.LinearLayout.LayoutParams(
            		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setBackgroundColor(Color.parseColor("#dcdcdc"));
            linearLayout.addView(text);//, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	}
          
    	rootViewLayout.addView(linearLayout);//, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	
    	
    }
}
