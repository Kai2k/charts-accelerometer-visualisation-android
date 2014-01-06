/***
 * 
 * @author aclarke
 * 
 * Copyright 2013 Scott Logic
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.scottlogic.accelerometervisualisation;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataAdapter;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.Legend;
import com.shinobicontrols.charts.LegendStyle;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.SeriesStyle.FillStyle;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;

public class AccelerometerActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ArrayList<SimpleDataAdapter<Double, Float>> dataAdapters;
	private ShinobiChart shinobiChart;
	private long startTimestamp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accelerometer);
		
		// Get hold of the sensor manager and accelerometer sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// Get the a reference to the ShinobiChart from the ChartFragment
        ChartFragment chartFragment =
                (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        shinobiChart = chartFragment.getShinobiChart();
        
        // Only set the chart up the first time the Activity is created
        if (savedInstanceState == null) {
			// Uncomment this line to set the license key if you're using a trial version
            //shinobiChart.setLicenseKey("<license_key_here>");
			
            // Set the chart title
            shinobiChart.setTitle("Accelerometer Visualisation");
            
            // Create the axes, set their titles, and add them to the chart
			NumberAxis xAxis = new NumberAxis();
			xAxis.setTitle("Elapsed time (s)");
			shinobiChart.setXAxis(xAxis);
			NumberAxis yAxis = new NumberAxis();
			yAxis.setTitle("Acceleration (m/s^2)");
			shinobiChart.setYAxis(yAxis);
			
			// Create arrays to hold the data adapters, and the axis titles
			dataAdapters = new ArrayList<SimpleDataAdapter<Double, Float>>();
			String[] seriesTitles = {"x-axis", "y-axis", "z-axis"};
			
			// Create a data adapter and a line series to represent each of the x, y and z axes
			for (int i=0; i<3; i++)
			{
				// Create a data adapter
				dataAdapters.add(new SimpleDataAdapter<Double, Float>());
				
				// Create a line series
				LineSeries lineSeries = new LineSeries();
				// Set its title
				lineSeries.setTitle(seriesTitles[i]);
		        // Style it to have a gradient fill beneath the line
				lineSeries.getStyle().setFillStyle(FillStyle.GRADIENT);
				// Set its data adapter
		        lineSeries.setDataAdapter(dataAdapters.get(i));
		        
		        // Add the series to the chart
		        shinobiChart.addSeries(lineSeries);
			}	        
	        
			// Get the legend from the chart
            Legend legend = shinobiChart.getLegend();
            // Make the legend visible
            legend.setVisibility(View.VISIBLE);
            legend.setPosition(Legend.Position.BOTTOM_CENTER);
            
            // Change the legend's style
            LegendStyle legendStyle = legend.getStyle();
            legendStyle.setPadding(10.0f);
            legendStyle.setSymbolLabelGap(2.0f);
            legendStyle.setSymbolWidth(15.0f);
            legendStyle.setTextSize(13.0f);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// Register ourselves as an event listener for accelerometer events
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		// Unregister the event listener for accelerometer events
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Don't do anything
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// If this is the first event, save its timestamp to use as the base value
		if (startTimestamp == 0) {
			startTimestamp = event.timestamp;
		}
		
		// Calculate the elapsed time in seconds (from the nanosecond timestamps)
		double elapsedTime = (event.timestamp - startTimestamp)/1000000000.0;
		
		// event.values for an accelerometer event contains 3 values, one for each of the x, y and z axes
		for(int i=0; i<3; i++) {
			// Check we've got a data adapter before attempting to add a point
			if (dataAdapters != null && dataAdapters.get(i) != null) {
				// Create a data point for the value, and add it to the relevant DataAdapter
				DataPoint<Double,Float> dataPoint = new DataPoint<Double,Float>(elapsedTime, event.values[i]);
				DataAdapter<Double,Float> dataAdapter = dataAdapters.get(i);
				dataAdapter.add(dataPoint);
				
				// Remove any data points from more than 20s ago
				DataPoint<Double,Float> oldPoint = (DataPoint<Double,Float>) dataAdapter.get(0);
				while (oldPoint.getX() < elapsedTime - 20)
				{
					dataAdapter.remove(0);
					oldPoint = (DataPoint<Double,Float>) dataAdapter.get(0);
				}
			}
		}
		
		// Finally, redraw the chart to display the new values
		shinobiChart.redrawChart();
	}
}

