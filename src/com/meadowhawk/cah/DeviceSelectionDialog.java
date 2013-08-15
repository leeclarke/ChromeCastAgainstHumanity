/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.meadowhawk.cah;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.cast.CastDevice;
import com.google.cast.CastDeviceAdapter;
import com.google.cast.DeviceManager;

/**
 * A class to manage the CastDevice selection dialog.
 */
public class DeviceSelectionDialog {
    /**
     * Interface which provides methods to listen for device selection.
     */
    public interface DeviceSelectionListener {
        /**
         * Called if a device is selected.
         *
         * @param dialog the selection dialog where the device is selected
         */
        public void onSelected(DeviceSelectionDialog dialog);

        /**
         * Called if the selection is cancelled.
         *
         * @param dialog the dialog that was cancelled
         */
        public void onCancelled(DeviceSelectionDialog dialog);
    }

    private AlertDialog mDialog;
    private Context mContext;
    private DeviceManager mDeviceManager;
    private CastDeviceAdapter mDeviceAdapter;
    private CastDevice mDevice;
    private DeviceSelectionListener mListener;

    /**
     * Creates a new DeviceSelectionDialog with the current context passed and initializes the 
     * object to listen for Cast device selection.
     */
    public DeviceSelectionDialog(Context context) {
        mContext = context;
        mDeviceAdapter = new CastDeviceAdapter(mContext);
        mDeviceManager = new DeviceManager(CastCAHApplication.getInstance().getCastContext());
        mDeviceManager.addListener(new DeviceManager.Listener() {
            @Override
            public void onScanStateChanged(int state) {
                if (state == DeviceManager.SCAN_SUSPENDED_NETWORK_ERROR) {
                    new AlertDialog.Builder(mContext)
                            .setMessage(R.string.scan_failed_network_error)
                            .setPositiveButton(R.string.ok, null)
                            .create()
                            .show();
                }
            }

            @Override
            public void onDeviceOnline(CastDevice device) {
                mDeviceAdapter.add(device);
            }

            @Override
            public void onDeviceOffline(CastDevice device) {
                mDeviceAdapter.remove(device);
            }
        });
    }

    /**
     * Initializes and displays the dialog being used for device selection.
     * 
     * @throws IllegalStateException if the dialog is already being displayed
     */
    public void show() throws IllegalStateException {
        if (mDialog != null) {
            throw new IllegalStateException("Can't show dialog more than once");
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.device_discover_title);
        DialogListener listener = new DialogListener();
        builder.setAdapter(mDeviceAdapter, listener);
        mDialog = builder.create();
        mDialog.setOnDismissListener(listener);
        mDialog.setOnCancelListener(listener);
        mDeviceManager.startScan();
        mDialog.show();
    }

    /**
     * Dismisses the dialog if it is being displayed.
     */
    public void dismiss() {
        mListener = null;
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * Returns the device selected from the dialog, or {@code null} if none is selected.
     */
    public CastDevice selectedDevice() {
        return mDevice;
    }

    public void setListener(DeviceSelectionListener listener) {
        mListener = listener;
    }

    /**
     * A Listener class attached to this object's displayed dialog, which handles dismiss, cancel,
     * and click events by modifying instance variables accordingly.
     */
    private class DialogListener implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
        @Override
        public void onDismiss(DialogInterface dialog) {
            mDeviceManager.stopScan();
            if (mListener != null) {
                if (mDevice != null) {
                    mListener.onSelected(DeviceSelectionDialog.this);
                } else {
                    mListener.onCancelled(DeviceSelectionDialog.this);
                }
            }
            mDialog = null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            mDevice = null;
        }

        @Override
        public void onClick(DialogInterface dialog, int index) {
            CastDevice selected = mDeviceAdapter.getItem(index);
            if (selected != null) {
                mDevice = selected;
            }
        }
    }
}
