package com.meadowhawk.cah;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.cast.CastDevice;

//TODO: 
/*
 * 
 * */
/**
 * The fun starts here! Getting a connection to a ChromeCast is the primary objective here, after that Start the GameActivity.
 * @author leeclarke
 */
public class MainActivity extends Activity {
    private TextView mConnectedTextView;
    private DeviceSelectionDialog mDialog;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConnectedTextView = (TextView) findViewById(R.id.connected_device);
        setConnectedDeviceTextView(getResources().getString(R.string.no_device));
        setupButtons();
        mDialog = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


    /** 
     * Dismisses and removes the DeviceSelectionDialog object on application stop. 
     */
    @Override
    protected void onStop() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onStop();
    }
    
    /** 
     * Performs onClick setup on game buttons. 
     */
    private void setupButtons() {
        findViewById(R.id.start).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(true);
            }
        });

        findViewById(R.id.connect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDevice();
            }
        });
    }
    
    /** 
     * Starts the GameActivity that handles the TicTacToe game. startWithPlayer1 is not used. 
     */
    private void startGame(boolean startWithPlayer1) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }

    /** 
     * Creates a new DeviceSelectionDialog with an attached listener, which listens for device 
     * selection, sets the device in the top-level Application, and enables the game start button. 
     */
    private void selectDevice() {
        mDialog = new DeviceSelectionDialog(this);
        mDialog.setListener(new DeviceSelectionDialog.DeviceSelectionListener() {

            @Override
            public void onSelected(DeviceSelectionDialog dialog) {
                mDialog = null;
                CastDevice device = dialog.selectedDevice();
                if (device != null) {
                    setConnectedDeviceTextView(dialog.selectedDevice().getFriendlyName());
                    CastCAHApplication.getInstance().setDevice(device);
                    findViewById(R.id.start).setEnabled(true);
                } else {
                    setConnectedDeviceTextView(
                            MainActivity.this.getResources().getString(R.string.no_device));
                    findViewById(R.id.start).setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DeviceSelectionDialog dialog) {
                mDialog = null;
            }
        });
        try {
            mDialog.show();
        } catch (IllegalStateException e) {
            showErrorDialog(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** 
     * Builds and displays the error dialog when an error is caught. 
     */
    private void showErrorDialog(String errorString) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
    
    /** 
     * Updates a label with the passed name of the currently connected device. 
     */
    private void setConnectedDeviceTextView(String deviceName) {
        mConnectedTextView.setText(
                String.format(getResources().getString(R.string.connected_to_text), deviceName));
    }
}
