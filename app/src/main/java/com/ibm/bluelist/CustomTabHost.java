package com.ibm.bluelist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.TabHost;

/**
 * Created by Michael on 4/21/16.
 */
public class CustomTabHost extends TabHost {
    private boolean locationFound = true;

    public CustomTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentTab(int currentTab) {
        if (locationFound) { // position of the tab that should not get selected
            super.setCurrentTab(currentTab);
        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Location loading...")
                    .setMessage("Wait for location to be found by Eventure.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
        // in my case I want to trigger something here but I don't want the button to get selected
    }

    public void setLocationFound(boolean b) {
        locationFound = b;
    }
}
