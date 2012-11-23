package com.android.phone;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class PreciseCallStateBroadcaster extends Handler {

	public static final String ACTION_CALL_STATE_CHANGED = "com.android.phone.PRECISE_CALL_STATE_CHANGED";
	public static final String EXTRA_TIMESTAMP = "timestamp";
	public static final String EXTRA_PHONE_STATE = "phone-state";
	public static final String EXTRA_CALL_STATE = "call-state";
	public static final String EXTRA_CALL_DURATION = "call-state";

	private static final String TAG = PreciseCallStateBroadcaster.class.getCanonicalName();
	private static final int PHONE_STATE_CHANGED = 1;
	private Context mCtx;
	private CallManager mCM;

	PreciseCallStateBroadcaster(Context mContext, CallManager mCallManager) {
		Log.v(TAG, "Registering");
		mCtx = mContext;
		mCM = mCallManager;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);
	}

	void dispose() {
		Log.v(TAG, "Unregistering");
		mCM.unregisterForPreciseCallStateChanged(this);
	}

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PHONE_STATE_CHANGED:
            	long mTimestamp = SystemClock.elapsedRealtime();
            	long callDuration = -1;
            	Call.State cstate = Call.State.IDLE;
        		Log.v(TAG, "Received PHONE_STATE_CHANGED");
                Phone.State state = mCM.getState();
                Log.v(TAG, "onPhoneStateChanged: state = " + state);
                if (state == Phone.State.OFFHOOK) {
                    Log.v(TAG, "onPhoneStateChanged: OFF HOOK");

                    Phone fgPhone = mCM.getFgPhone();
                    Call call = PhoneUtils.getCurrentCall(fgPhone);
                    Connection c = PhoneUtils.getConnection(fgPhone, call);
                    PhoneUtils.dumpCallState(fgPhone);
                    cstate = call.getState();
                    callDuration = c.getDurationMillis();
                }
                Intent phoneStateEvent = new Intent(ACTION_CALL_STATE_CHANGED);
                phoneStateEvent.putExtra(EXTRA_TIMESTAMP, mTimestamp);
                phoneStateEvent.putExtra(EXTRA_PHONE_STATE, state);
                phoneStateEvent.putExtra(EXTRA_CALL_STATE, cstate);
                phoneStateEvent.putExtra(EXTRA_CALL_DURATION, callDuration);
                mCtx.sendBroadcast(phoneStateEvent);
                break;

            default:
                // super.handleMessage(msg);
        }
    }
}
