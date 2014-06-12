package com.ndnxr.bambi; 
import com.ndnxr.bambi.R;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public class EnergyMeterFragment extends Fragment {
 
	private View energyChartView;
	private EnergyChart energyChart;
	private int screen_width;
	private int screen_height;
	private LinearLayout rootViewLayout ;
	private TextView textView;
	private int energySave;
	private long totalDataBytes;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	G.Log("onCreateView() !!");
    	
    	// Set basic root view layout
        View rootView = inflater.inflate(R.layout.fragment_energy_meter, container, false);
        rootViewLayout = (LinearLayout) rootView;
        
        // Get screen size for display pie chart
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        
        // Update energySaveRaio and total data byte from Bambi
        totalDataBytes = ((Bambi)this.getActivity()).totalDataBytes;
        energySave = ((Bambi) this.getActivity()).energySavePercent;
        recreateRootView();
        
        /*
        // Get energy value from Bambi to create pie chart
        createEnergySaveChart(((Bambi) this.getActivity()).energySavePercent);
        
        // Get total byte value from Bambi to write textView      
        textView = new TextView(this.getActivity());
        totalDataBytes = ((Bambi)this.getActivity()).totalDataBytes;
        textView.setText(String.format("Bambi Uploaded %d bytes of Data in total",totalDataBytes));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        
        // Add views to root view
        rootViewLayout.addView(energyChartView,screen_width, screen_height/2);
        rootViewLayout.addView(textView);
        */
       
      
        return rootView;
    }
    
    /**
     * create energy save percentage in pie chart
     * @return 
     */
    public void createEnergySaveChart(int energysave){
    	G.Log("drawEnergySaveChart called "+energysave);
    	// Create energy chart
        energyChart = new EnergyChart();
        // Create energyChartView with energy save ratio
     	energyChartView = energyChart.setView(this.getActivity(), energysave);
    }
    
   
    /**
     * recreate the entire root view, called when update occurs
     */
    public void recreateRootView(){
        	
    	// Create energy chart
        EnergyChart updateEnergyChart = new EnergyChart();
        
        // Create energyChartView with energy save ratio
     	View updateEnergyChartView = updateEnergyChart.setView(this.getActivity(), energySave);
     	
     	// Create textView with totalDataBytes
    	TextView updateTextView = new TextView(this.getActivity());
    	updateTextView.setText(String.format("Bambi Uploaded %d bytes of Data in total",totalDataBytes) );
    	updateTextView.setGravity(Gravity.CENTER_HORIZONTAL);
    	// Clear out of date views element
    	rootViewLayout.removeAllViews();
     	
    	// Add views to root view
    	rootViewLayout.addView(updateEnergyChartView,screen_width, screen_height/2);
    	rootViewLayout.addView(updateTextView);
    }
    
    
    /**
     * Update text view to display total Bytes number
     * @param totalBytes
     */
    public void updateTotalBytes(long totalBytes){
    	totalDataBytes = totalBytes;
    	recreateRootView();
    }
    
    public void updateEnergySaveChart(int energysave){
    	energySave = energysave;
    	recreateRootView();    	
    }
}
