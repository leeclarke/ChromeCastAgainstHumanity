package com.meadowhawk.cah;

import android.app.Application;

import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.Logger;

/**
 * An application that provides getter and setter methods for passing Cast-related objects between 
 * multiple activities, as well as a way to access its currently running instance.
 */
public class CastCAHApplication extends Application {
    private static final String TAG = CastCAHApplication.class.getSimpleName();

    private static Logger sLog = new Logger(TAG);
    private static CastCAHApplication singleton;
    private CastContext mCastContext;
    private CastDevice mDevice;

    /**
     * Initializes the CastContext associated with this application's context, upon application
     * creation.
     */
    @Override
    public final void onCreate() {
        super.onCreate();
        singleton = this;

        try {
            mCastContext = new CastContext(getApplicationContext());
        } catch (IllegalArgumentException e) {
            sLog.e(e, "Unable to create CastContext");
        }
    }

    /**
     * Returns the instance of this class that is currently running.
     */
    public static CastCAHApplication getInstance() {
        return singleton;
    }

    /**
     * Returns the CastContext associated with this application's context.
     */
    public CastContext getCastContext() {
        return mCastContext;
    }

    /**
     * Returns the currently selected device, or null if no device is selected.
     */
    public CastDevice getDevice() {
        return mDevice;
    }

    /**
     * Sets the currently selected device.
     */
    public void setDevice(CastDevice device) {
        mDevice = device;
    }
}
