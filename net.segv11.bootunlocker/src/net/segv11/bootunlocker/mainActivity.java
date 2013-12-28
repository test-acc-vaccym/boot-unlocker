package net.segv11.bootunlocker;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
Copyright 2013 James Mason

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

public class mainActivity extends Activity {

	/** For logging */
	private static final String TAG = "net.segv11.mainActivity";
	private bootLoader theBootLoader = null;
	private static final Boolean dontCare = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /** Called at the start of the visible lifetime */
    @Override
    public void onStart() {
    	super.onStart();
		Log.v(TAG, "handling onStart ");
    	Boolean setState = false;
    	Boolean desiredState = dontCare;
    	Boolean setTamperFlag = false;
    	Boolean desiredTamperFlag = dontCare;
    	theBootLoader = bootLoader.makeBootLoader();
		
    	TextView versionID = (TextView) findViewById(R.id.versionID);
		TextView modelID = (TextView) findViewById(R.id.modelID);
		TextView deviceID = (TextView) findViewById(R.id.deviceID);
    	TextView bootloaderID = (TextView) findViewById(R.id.bootloaderID);
    	try {
			versionID.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
			modelID.setText(android.os.Build.MODEL);
			deviceID.setText(android.os.Build.DEVICE);
	    	bootloaderID.setText(android.os.Build.BOOTLOADER);
	    } catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 	
    	new AsyncBootLoader().execute(setState, desiredState, setTamperFlag, desiredTamperFlag);
   }

    /** Called from UI to unlock the bootloader */
    public void doUnlockBootloader(View v) {
    	Boolean setState = true;
    	Boolean desiredState = false;
    	Boolean setTamperFlag = false;
    	Boolean desiredTamperFlag = dontCare;
    	new AsyncBootLoader().execute(setState, desiredState, setTamperFlag, desiredTamperFlag);
    }
    
    /** Called from UI to lock the bootloader */
    public void doLockBootloader(View v) {
    	Boolean setState = true;
    	Boolean desiredState = true;
    	Boolean setTamperFlag = false;
    	Boolean desiredTamperFlag = dontCare;
    	new AsyncBootLoader().execute(setState, desiredState, setTamperFlag, desiredTamperFlag);
    }

    /** Called from UI to clear tamper flag */
    public void doClearTamper(View v) {
    	Boolean setState = false;
    	Boolean desiredState = dontCare;
    	Boolean setTamperFlag = true;
    	Boolean desiredTamperFlag = false;
    	new AsyncBootLoader().execute(setState, desiredState, setTamperFlag, desiredTamperFlag);
    }
    
    /** Called from UI to set tamper flag */
    public void doSetTamper(View v) {
    	Boolean setState = false;
    	Boolean desiredState = dontCare;
    	Boolean setTamperFlag = true;
    	Boolean desiredTamperFlag = true;
    	new AsyncBootLoader().execute(setState, desiredState, setTamperFlag, desiredTamperFlag);
    }

  

    private class AsyncBootLoader extends AsyncTask<Boolean, Void, Integer> {
    	/* Ideas for the future:
    	 * 		Can we receive a broadcast intent when someone ELSE tweaks the param partition?
    	 * 		Do we want a progress indicator in case su takes a long time?
    	 *		Set text colors for lock status?
    	 */ 

    	@Override
    	protected Integer doInBackground(Boolean...booleans) {
    		Boolean setState = booleans[0];
    		Boolean desiredState = booleans[1];
    		Boolean setTamperFlag = booleans[2];
    		Boolean desiredTamperFlag = booleans[3];

    		// If this device is incompatible, return immediately.
    		if (theBootLoader == null) {
    			return Integer.valueOf(bootLoader.BL_UNSUPPORTED_DEVICE);
    		}

    		// If we need to change the bootloader state, then do so.
    		if (setState) {
    	    	try {
    	    		theBootLoader.setLockState(desiredState);
    			} catch (IOException e) {
    				if (desiredState) {
    					Log.e(TAG, "Caught IOException locking: " + e);
    				} else {
    					Log.e(TAG, "Caught IOException unlocking: " + e);
    				}
    			}
    	    	
    	    	// Since we changed the bootloader lock state, we will pause for
    	    	// it to take effect.  This is OK because we are on a background
    	    	// thread.
    	    	try {
					Thread.sleep(bootLoader.delayAfterChange);
				} catch (InterruptedException e) {
					// Should not happen; if it does, we just keep going.
					e.printStackTrace();
				}
    		}
 
      		if (theBootLoader.hasTamperFlag() && setTamperFlag) {
    	    	try {
    	    		theBootLoader.setTamperFlag(desiredTamperFlag);
    			} catch (IOException e) {
    				if (desiredTamperFlag) {
    					Log.e(TAG, "Caught IOException setting tamper flag: " + e);
    				} else {
    					Log.e(TAG, "Caught IOException clearing tamper flag: " + e);
    				}
    			}
    	    	
    	    	// Since we changed the bootloader tamper flag, we will pause for
    	    	// it to take effect.  This is OK because we are on a background
    	    	// thread.
    	    	try {
					Thread.sleep(bootLoader.delayAfterChange);
				} catch (InterruptedException e) {
					// Should not happen; if it does, we just keep going.
					e.printStackTrace();
				}
    		}
 
    		
    		// Now query the bootloader lock state and tamper flag.
			return Integer.valueOf(theBootLoader.getBootLoaderState());
    	}

    	
    	/*
    	 *  We don't override onProgresUpdate, because we're not
    	 *  taking that long
    	 */
    	@Override
    	protected void onPostExecute(Integer resultObj) {
    		int result = resultObj.intValue();
  			TextView bootLoaderStatusText = (TextView) findViewById(R.id.bootLoaderStatusText);
  			Button lockButton = (Button) findViewById(R.id.lockButton);
  			Button unlockButton = (Button) findViewById(R.id.unlockButton);
  			TextView extendedStatus = (TextView) findViewById(R.id.extendedStatus);
  			LinearLayout tamperLL= (LinearLayout) findViewById(R.id.tamperLayout);
 
			if (result == bootLoader.BL_UNLOCKED || result == bootLoader.BL_TAMPERED_UNLOCKED) {
				bootLoaderStatusText.setText(R.string.stat_unlocked);
  				lockButton.setEnabled(true);
  				unlockButton.setEnabled(true);
  				extendedStatus.setText("");
			} else if (result == bootLoader.BL_LOCKED || result == bootLoader.BL_TAMPERED_LOCKED) {
				bootLoaderStatusText.setText(R.string.stat_locked);
  				lockButton.setEnabled(true);
  				unlockButton.setEnabled(true);
  				extendedStatus.setText("");
			} else if (result == bootLoader.BL_UNSUPPORTED_DEVICE){
				bootLoaderStatusText.setText(R.string.stat_unknown_device);
  				lockButton.setEnabled(false);
  				unlockButton.setEnabled(false);
  				Resources res = getResources();
  				extendedStatus.setText(String.format(
  						res.getString(R.string.extra_unknown_device),
  						android.os.Build.DEVICE));
			} else {
				bootLoaderStatusText.setText(R.string.stat_no_root);
  				lockButton.setEnabled(false);
  				unlockButton.setEnabled(false);
  				extendedStatus.setText("");
			}
			
			if (theBootLoader.hasTamperFlag()) {				
				if (result == bootLoader.BL_LOCKED || result == bootLoader.BL_UNLOCKED) {
					bootLoaderStatusText.setText(R.string.stat_not_tampered);
					// change Set/Clear buttons
				} else if (result == bootLoader.BL_LOCKED || result == bootLoader.BL_UNLOCKED) {
					bootLoaderStatusText.setText(R.string.stat_tampered);
					// change Set/Clear buttons
				}
			} else {
				// change Set/Clear buttons
				//tamperLL.setVisibility(android:visibility:gone)
			}
  			
    	}
    }
    
   
}
  