package com.unagit.parkedcar;

/**
 * Created by a264889 on 28.01.2018.
 */

public final class Constants {

    static class Tabs {
        /**
         * Number of tabs to show
         */
        static final int TABS_COUNT = 3;
        /**
         * Positions of tabs in TabLayout
         */
        static final int MAP_TAB = 0;
        static final int PHOTOS_TAB = 1;
        static final int BLUETOOTH_TAB = 2;
        /**
         * Icons for tabs in TabLayout
         */
        static int MAP_TAB_ICON = android.R.drawable.ic_menu_mylocation;
        static int PHOTOS_TAB_ICON = android.R.drawable.ic_menu_camera;
        static int BLUETOOTH_TAB_ICON = android.R.drawable.stat_sys_data_bluetooth;
    }

    /**
     * IDs for different requests within the app (permission requests, notification IDs etc.)
     */
    static class Requests {
        static final int ENABLE_BLUETOOTH_ACTIVITY_REQUEST = 10;
        static final String NOTIFICATIONS_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".testnotifications";
        static final int NOTIFICATION_ID = 100;
    }

    static class ParkActions {
        static final int PARK_CAR = 0;
        static final int CLEAR_PARKING_LOCATION= 1;
    }

    static class Notifications {
        static final String NOTIFICATION_TITLE = "Parked Car";
        static final String NOTIFICATION_TEXT = "Your Car has been marked on the map.";
        static final String NOTIFICATION_ACTION_TITLE_SHOW = "Show Location";
        static final String NOTIFICATION_ACTION_TITLE_DIRECTIONS = "Get Directions";
        static final String NOTIFICATION_ACTION_TITLE_CLEAR = "Clear";
        static final String ACTION_SHOW_ON_MAP = "Show";
        static final String ACTION_DIRECTIONS = "Directions";
        static final String ACTION_CLEAR = "Clear";
    }

    static class GoogleMaps {
        static final String GOOGLE_MAPS_QUERY_URL = "https://www.google.com/maps/search/?api=1&query=";
    }
}