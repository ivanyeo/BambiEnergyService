package com.ndnxr.bambi;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

public class EnergyChart {
    public View getView(Context context){
      // this is my data of performance; data is collected in array.
       int EnergySave = 90;
       int EnergyWifi = 100 - EnergySave;// [0] for Call, [1] for Meeting, [2] for Email
        CategorySeries series = new CategorySeries("pie"); // adding series to charts. //collect 3 value in array. therefore add three series.
            series.add("Save",EnergySave);            
            series.add("WiFi", EnergyWifi);
    
// add three colors for three series respectively            
            int []colors = new int[]{Color.parseColor("#98FB98"), Color.parseColor("#AB82FF")};
// set style for series
            final DefaultRenderer renderer = new DefaultRenderer();
            
            for(int color : colors){
                SimpleSeriesRenderer r = new SimpleSeriesRenderer();
                r.setColor(color);
                r.setDisplayBoundingPoints(true);
                r.setDisplayChartValuesDistance(-1);
                r.setDisplayChartValues(true);
                r.setChartValuesTextSize(15);
                renderer.addSeriesRenderer(r);
            }
            //renderer.isInScroll();
            //renderer.setZoomButtonsVisible(true);   //set zoom button in Graph
            //renderer.setApplyBackgroundColor(true);
            //renderer.setBackgroundColor(Color.BLACK); //set background color
            renderer.setChartTitle("Bambi Energy Meter");
            renderer.setChartTitleTextSize((float) 40);
            renderer.setShowLabels(true); 
            renderer.setShowLegend(false);
            renderer.setLabelsTextSize(30);
            renderer.setDisplayValues(true);
            
            final View v = ChartFactory.getPieChartView(context, series, renderer);
            final Activity parentActivity = (Activity) context;
            
            // Animate
            ValueAnimator anim = ValueAnimator.ofFloat(renderer.getStartAngle(), 180);
            anim.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //view.setBackgroundColor(animation.getAnimatedValue());
                    renderer.setStartAngle( (Float) animation.getAnimatedValue() );
                   // parentActivity.findViewById(R.id.chart_view).requestLayout();
                }
            });
            
            anim.setDuration(10000);
          
            anim.start();
       
            return v;
    }
}