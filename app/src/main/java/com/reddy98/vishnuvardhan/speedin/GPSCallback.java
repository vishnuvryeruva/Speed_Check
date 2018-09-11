package com.reddy98.vishnuvardhan.speedin;

import android.location.Location;

    public interface GPSCallback
    {
        public abstract void onGPSUpdate(Location location);
    }
