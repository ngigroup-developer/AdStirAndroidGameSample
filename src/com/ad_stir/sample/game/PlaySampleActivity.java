/*
   Copyright 2012 motionBEAT Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ad_stir.sample.game;

import com.ad_stir.AdstirTerminate;
import com.ad_stir.AdstirView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class PlaySampleActivity extends Activity implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int orientation = 0;
	private float sensorX = 0;
	private float sensorY = 0;
	private boolean pause = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.play);
		SurfaceView gameView = (SurfaceView) this.findViewById(R.id.game);
		gameView.getHolder().addCallback(game);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
	    orientation = wm.getDefaultDisplay().getOrientation();
	    Button pauseButton = (Button) this.findViewById(R.id.pause);
	    pauseButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				showDialog();
			}});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if(orientation ==  Surface.ROTATION_90){
				sensorX = event.values[SensorManager.DATA_Y];
				sensorY = event.values[SensorManager.DATA_X];
			}else if(orientation ==  Surface.ROTATION_180){
				sensorX = event.values[SensorManager.DATA_X];
				sensorY = -event.values[SensorManager.DATA_Y];
			}else if(orientation ==  Surface.ROTATION_270){
				sensorX = -event.values[SensorManager.DATA_Y];
				sensorY = -event.values[SensorManager.DATA_X];
			}else if(orientation ==  Surface.ROTATION_0){
				sensorX = -event.values[SensorManager.DATA_X];
				sensorY = event.values[SensorManager.DATA_Y];
			}
		}
	}

	SurfaceHolder.Callback game = new SurfaceHolder.Callback() {
		private boolean running = false;

		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			running = true;
			Thread thread = new Thread(new Runnable() {
				private static final long FPS = 20;
				private static final long INTERVAL = 1000 / FPS;
				private static final float FRICTION = 2.0f / FPS;
				private static final float SPEED = 4.0f;
				private static final float BALL = 10.0f;
				private float x = 0;
				private float y = 0;
				private float speedX = 0;
				private float speedY = 0;

				@Override
				public void run() {
					x = holder.getSurfaceFrame().centerX();
					y = holder.getSurfaceFrame().centerY();
					while (true) {
						try {
							Thread.sleep(INTERVAL);
						} catch (InterruptedException e) {
						}
						if(!running)return;
						if(pause)continue;
						
						speedX = speedX + ((sensorX - (FRICTION * speedX * Math.abs(speedX))) / FPS);
						speedY = speedY + ((sensorY - (FRICTION * speedY * Math.abs(speedY))) / FPS);
						x = x + (speedX * SPEED);
						y = y + (speedY * SPEED);
						if(x < 0){x = 0;speedX=-speedX;}
						if(y < 0){y = 0;speedY=-speedY;}
						if(holder.getSurfaceFrame().width() < x){x = holder.getSurfaceFrame().width();speedX=-speedX;}
						if(holder.getSurfaceFrame().height() < y){y = holder.getSurfaceFrame().height();speedY=-speedY;}

					    Paint red = new Paint();
					    red.setColor(Color.argb(255, 255, 0, 0));
					    red.setAntiAlias(true);
						
					    Canvas canvas = holder.lockCanvas();
					    if(canvas == null) return;
					    canvas.drawColor(Color.GRAY);
					    canvas.drawCircle(x, y, BALL, red);
					    
					    holder.unlockCanvasAndPost(canvas);

					}
				}
			});
			thread.start();
		}



		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			running = false;
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// onDestroy()にここから
		AdstirTerminate.init(this);
		// ここまでを追加
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showDialog() {
		AdstirView ad = new AdstirView(this, "MEDIA-ID", SPOT-NO);
		pause = true;
		new AlertDialog.Builder(this).setTitle("Pause").setCancelable(true).setView(ad)
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						pause = false;
					}
				})
				.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						PlaySampleActivity.this.finish();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						PlaySampleActivity.this.finish();
					}
				}).create().show();
	}

}
