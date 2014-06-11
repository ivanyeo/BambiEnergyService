package com.ndnxr.bambi; 
import com.ndnxr.bambi.R;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public class EnergyMeterFragment extends Fragment {
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	// Initialization
        final View rootView = inflater.inflate(R.layout.fragment_energy_meter, container, false);
        LinearLayout rootViewLayout = (LinearLayout) rootView;
        
        // Create energy chart
        EnergyChart energyChart = new EnergyChart();
        View energyChartView = energyChart.getView(this.getActivity());
        
        // Get screen size
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        
        // Add energy chart to root view
        G.Log(String.format("Height is: %d", height) );
        rootViewLayout.addView(energyChartView, width, height/2);
        
        
        // Add text info to root view
        TextView textView = new TextView(this.getActivity());
        textView.setText("This is my text!");
        rootViewLayout.addView(textView);
           
        return rootView;
    }
}
