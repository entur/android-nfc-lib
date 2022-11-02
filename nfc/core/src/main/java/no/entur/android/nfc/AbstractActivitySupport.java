package no.entur.android.nfc;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Generic service with onResume / onPause with enable / disable.
 *
 */

public abstract class AbstractActivitySupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractActivitySupport.class);

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
			LOGGER.debug("Stop " + getClass().getName());

			stopImpl();
		} else {
			LOGGER.debug(getClass().getName() + " already stopped");
		}
	}

	protected abstract void stopImpl();

	protected void start() {
		if (!started) {
			started = true;
			LOGGER.debug("Start " + getClass().getName());

			startImpl();
		} else {
			LOGGER.debug(getClass().getName() + " already started");
		}
	}

	protected abstract void startImpl();

	public void refresh() {
		synchronized (this) {
			if (started) {
				LOGGER.debug("Refresh " + getClass().getName());

				// TODO also check canStop() and canStart()?
				refreshImpl();
			} else {
				LOGGER.debug("Unable to refresh " + getClass().getName() + ", not started.");
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
				LOGGER.debug("Disable " + getClass().getName());
				this.enabled = enabled;
				if (canStop()) {
					stop();
				}

			} else if (!this.enabled && enabled) {
				LOGGER.debug("Enable " + getClass().getName());
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
