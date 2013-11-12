package com.scottlogic.accelerometervisualisation;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;

import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.SeriesStyle.FillStyle;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;

public class MainActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ArrayList<SimpleDataAdapter<Double, Float>> dataAdapters;
	private ShinobiChart shinobiChart;
	private long startTimestamp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		if (savedInstanceState == null) {
			ChartFragment chartFragment =
	                (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);

			shinobiChart = chartFragment.getShinobiChart();
			shinobiChart.setTitle("Accelerometer Visualisation");
			
			NumberAxis xAxis = new NumberAxis();
			xAxis.setTitle("Elapsed time (s)");
			shinobiChart.setXAxis(xAxis);
			NumberAxis yAxis = new NumberAxis();
			yAxis.setTitle("Acceleration (m/s^2)");
			shinobiChart.setYAxis(yAxis);
			
			dataAdapters = new ArrayList<SimpleDataAdapter<Double, Float>>();
			
			for (int i=0; i<3; i++)
			{
				dataAdapters.add(new SimpleDataAdapter<Double, Float>());
				LineSeries lineSeries = new LineSeries();
				String title = i==0 ? "x" : (i==1 ? "y" : "z");
		        lineSeries.setTitle(title);
		        lineSeries.getStyle().setFillStyle(FillStyle.GRADIENT);
		        lineSeries.setDataAdapter(dataAdapters.get(i));
		        shinobiChart.addSeries(lineSeries);
			}	        
	        
	        shinobiChart.redrawChart();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Do something if accuracy changes
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (startTimestamp == 0) {
			startTimestamp = event.timestamp;
		}
		double timestamp = (event.timestamp - startTimestamp)/1000000000;
		for(int i=0; i<3; i++) {
			DataPoint<Double,Float> xDataPoint = new DataPoint<Double,Float>(timestamp, event.values[i]);
			dataAdapters.get(i).add(xDataPoint);
		}
		shinobiChart.redrawChart();
	}
}

