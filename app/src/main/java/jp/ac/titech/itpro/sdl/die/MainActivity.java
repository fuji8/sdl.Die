package jp.ac.titech.itpro.sdl.die;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, SensorEventListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private GLSurfaceView glView;
    private SimpleRenderer renderer;

    private Cube cube;
    private Pyramid pyramid;


    // sensor
    private SensorManager manager;
    private Sensor gyroscope;

    private long previousSensorChangedTime = -1;
    private float thetaX = 0;
    private float thetaY = 0;
    private float thetaZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        glView = findViewById(R.id.gl_view);
        SeekBar seekBarX = findViewById(R.id.seekbar_x);
        SeekBar seekBarY = findViewById(R.id.seekbar_y);
        SeekBar seekBarZ = findViewById(R.id.seekbar_z);
        seekBarX.setMax(360);
        seekBarY.setMax(360);
        seekBarZ.setMax(360);

        seekBarX.setEnabled(false);
        seekBarY.setEnabled(false);
        seekBarZ.setEnabled(false);


        renderer = new SimpleRenderer();
        cube = new Cube();
        pyramid = new Pyramid();
        renderer.setObj(cube);
        glView.setRenderer(renderer);

        // sensor
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null) {
            Toast.makeText(this, R.string.toast_no_gyroscope, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        glView.onResume();
        manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        glView.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
        case R.id.menu_cube:
            renderer.setObj(cube);
            break;
        case R.id.menu_pyramid:
            renderer.setObj(pyramid);
            break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
        case R.id.seekbar_x:
            renderer.rotateObjX(progress);
            break;
        case R.id.seekbar_y:
            renderer.rotateObjY(progress);
            break;
        case R.id.seekbar_z:
            renderer.rotateObjZ(progress);
            break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float omegaX = event.values[0];  // z-axis angular velocity (rad/sec)
        float omegaY = event.values[1];  // z-axis angular velocity (rad/sec)
        float omegaZ = event.values[2];  // z-axis angular velocity (rad/sec)


        long changedTime = System.nanoTime();
        // 最初の時は無視
        if (previousSensorChangedTime != -1) {
            thetaX += (changedTime - previousSensorChangedTime) / 1e9  * omegaX; // 平均速度として近似
            thetaY += (changedTime - previousSensorChangedTime) / 1e9  * omegaY; // 平均速度として近似
            thetaZ += (changedTime - previousSensorChangedTime) / 1e9  * omegaZ; // 平均速度として近似
        }

        previousSensorChangedTime = changedTime;

        renderer.rotateObjX(thetaX * 180 / 3.14f);
        renderer.rotateObjY(thetaY * 180 / 3.14f);
        renderer.rotateObjZ(thetaZ * 180 / 3.14f);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: accuracy=" + accuracy);
    }
}