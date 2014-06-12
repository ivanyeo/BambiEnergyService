package com.ndnxr.bambi; 
import com.ndnxr.bambi.R;

import android.animation.ValueAnimator;
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
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	// Initialization
        final View rootView = inflater.inflate(R.layout.fragment_energy_meter, container, false);
        rootViewLayout = (LinearLayout) rootView;
        
        
        // Create energy chart
        energyChart = new EnergyChart();
        energyChartView = energyChart.setEmptyView(this.getActivity());
        
        // Get screen size
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        
        // Add energy chart to root view
        G.Log(String.format("Height is: %d", screen_height) );
        rootViewLayout.addView(energyChartView, screen_width, screen_height/2);
        
        
        // Add text info to root view
        textView = new TextView(this.getActivity());
        textView.setText("Bambi Saved Your Battary!");
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

        rootViewLayout.addView(textView);
           
        return rootView;
    }
    
    /**
     * Update energy save percentage in pie chart
     * @return 
     */
    public void updateEnergySave(int energysave){
    	rootViewLayout.removeView(energyChartView);
    	energyChartView = energyChart.setView(this.getActivity(), energysave);
    	updateRootView();
    	//rootViewLayout.requestLayout();    	
    }
    
    /**
     * Update text view to display total Bytes number
     * @param totalBytes
     */
    public void updateTotalBytes(long totalBytes){
    	textView.setText(String.format("Bambi Uploaded %d bytes of Data in total",totalBytes) );
    	updateRootView();		
    }
    
    /**
     * Update every widget
     */
    public void updateRootView(){
    	rootViewLayout.removeView(energyChartView);
    	rootViewLayout.removeView(textView);
    	rootViewLayout.addView(energyChartView,screen_width, screen_height/2);
    	rootViewLayout.addView(textView);
    }
}
