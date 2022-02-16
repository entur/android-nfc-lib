package no.entur.android.nfc;

import android.util.Log;

/**
 *
 * Generic service with onResume / onPause with enable / disable.
 *
 */

public abstract class AbstractActivitySupport {

	private static final String TAG = AbstractActivitySupport.class.getName();

	protected boolean enabled;
	protected boolean started = false;
	protected boolean resumed = false;

	public void onResume() {
		synchronized (this) {
			this.resumed = true;
			if (canStart()) {
				start();
			}
		}
	}

	public void onPause() {
		synchronized (this) {
			this.resumed = false;
			if (canStop()) {
				stop();
			}
		}
	}

	protected void stop() {
		if (started) {
			started = false;
			Log.d(TAG, "Stop " + getClass().getName());

			stopImpl();
		} else {
			Log.d(TAG, getClass().getName() + " already stopped");
		}
	}

	protected abstract void stopImpl();

	protected void start() {
		if (!started) {
			started = true;
			Log.d(TAG, "Start " + getClass().getName());

			startImpl();
		} else {
			Log.d(TAG, getClass().getName() + " already started");
		}
	}

	protected abstract void startImpl();

	public void refresh() {
		synchronized (this) {
			if (started) {
				Log.d(TAG, "Refresh " + getClass().getName());

				// TODO also check canStop() and canStart()?
				refreshImpl();
			} else {
				Log.d(TAG, "Unable to refresh " + getClass().getName() + ", not started.");
			}
		}
	}

	protected void refreshImpl() {
		stopImpl();
		startImpl();
	}

	protected void setEnabled(boolean enabled) {
		synchronized (this) {
			if (this.enabled && !enabled) {
				Log.d(TAG, "Disable " + getClass().getName());
				this.enabled = enabled;
				if (canStop()) {
					stop();
				}

			} else if (!this.enabled && enabled) {
				Log.d(TAG, "Enable " + getClass().getName());
				this.enabled = enabled;
				if (canStart()) {
					start();
				}
			}
		}
	}

	public void reevaluate() {
		synchronized (this) {
			if (!started) {
				if (canStart()) {
					start();
				}
			} else {
				if (canStop()) {
					stop();
				}
			}
		}
	}

	protected boolean canStart() {
		return resumed && enabled;
	}

	protected boolean canStop() {
		return !resumed || !enabled;
	}

	public void enable() {
		setEnabled(true);
	}

	public void disable() {
		setEnabled(false);
	}

	public boolean isEnabled() {
		return enabled;
	}

}
