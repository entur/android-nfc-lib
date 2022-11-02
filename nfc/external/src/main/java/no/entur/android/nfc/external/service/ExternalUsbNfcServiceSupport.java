package no.entur.android.nfc.external.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;

/**
 *
 * Support for scanning for USB devices, and if supported, opening a connection and initializing a reader.
 *
 * TODO technically we could support multiple readers and/or usb units using this helper.
 *
 */

public class ExternalUsbNfcServiceSupport {

	private static final String TAG = ExternalUsbNfcServiceSupport.class.getName();

	public interface ReaderAdapter<T> {
		void closeReader(UsbDevice device);

		T openReader(UsbDevice param);

		boolean isReaderSupported(UsbDevice device);
	}

	private static class Scanner extends Handler {

		private static final long USB_RESCAN_INTERVAL_STANDARD = 1000;
		private static final long USB_RESCAN_INTERVAL_READER_DETECTED = 10000;

		private WeakReference<ExternalUsbNfcServiceSupport> activityReference;

		public Scanner(ExternalUsbNfcServiceSupport support) {
			this.activityReference = new WeakReference<ExternalUsbNfcServiceSupport>(support);
		}

		void resume() {
			synchronized (this) {
				if (!hasMessages(0)) {
					sendEmptyMessage(0);
				}
			}
		}

		void resumeDelayed() {
			synchronized (this) {
				if (!hasMessages(0)) {
					sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_STANDARD);
				}
			}
		}

		void pause() {
			synchronized (this) {
				removeMessages(0);
			}
		}

		@Override
		public void handleMessage(Message message) {
			// Log.v(TAG, "Handle message");

			ExternalUsbNfcServiceSupport activity = activityReference.get();
			if (activity != null) {
				if (activity.detectUSBDevices()) {
					Log.v(TAG, "Detected USB devices");
					sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_READER_DETECTED);

				} else {
					sendEmptyMessageDelayed(0, USB_RESCAN_INTERVAL_STANDARD);
				}
			}
		}
	}

	private final BroadcastReceiver usbDevicePermissionReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (ACTION_USB_PERMISSION.equals(action)) {

				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

				if (device != null) {
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

						Log.d(TAG, "Open reader: " + device.getDeviceName());

						synchronized (ExternalUsbNfcServiceSupport.this) {
							openDevices.add(device.getDeviceId());
						}

						new OpenReaderTask().execute(device);
					} else {
						Log.d(TAG, "Permission denied for device " + device.getDeviceName() + " / " + device.getDeviceId() + ", resume scanning.");

						synchronized (ExternalUsbNfcServiceSupport.this) {
							refusedPermissionDevices.add(device.getDeviceId());
						}

						readerScanner.resume();
					}
				} else {
					Log.d(TAG, "Did not find any device");
				}

			}
		}
	};

	private final BroadcastReceiver usbDeviceDetachedReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

				Log.d(TAG, "Usb device detached");

				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

				if (device != null) {
					synchronized (ExternalUsbNfcServiceSupport.this) {
						if (openDevices.remove(device.getDeviceId())) {
							// Close reader
							Log.d(TAG, "Closing reader " + device.getDeviceId());

							new CloseTask().execute();
						}
					}
				}
			}
		}
	};

	private class OpenReaderTask extends AsyncTask<UsbDevice, Void, Exception> {

		@Override
		protected Exception doInBackground(UsbDevice... params) {

			Exception result = null;

			Object reader = null;
			try {
				synchronized (ExternalUsbNfcServiceSupport.this) {
					requestPermissionDevices.remove(params[0].getDeviceId());
				}

				String name = params[0].getDeviceName();

				Log.d(TAG, "Opening reader " + name + "...");

				reader = readerAdapter.openReader(params[0]);
				if (reader != null) {
					Log.d(TAG, "Opened reader " + name);
					setNfcReaderStatus(ExternalNfcReaderCallback.READER_STATUS_OK, null);
				} else {
					Log.d(TAG, "Unable to open reader " + name);
					setNfcReaderStatus(ExternalNfcReaderCallback.READER_STATUS_ERROR, null);
				}
			} catch (Exception e) {
				Log.w(TAG, "Problem opening reader " + params[0].getDeviceName(), e);

				synchronized (ExternalUsbNfcServiceSupport.this) {
					if (e instanceof IllegalArgumentException && e.getMessage().contains("Cannot claim interface.")) {
						Log.d(TAG, "Fail USB open, attemp to connect " + params[0].getDeviceId() + " again after a delay");

						try {
							ExternalUsbNfcServiceSupport.this.wait(1000);
						} catch (InterruptedException e1) {
							// Restore interrupted state...
							Thread.currentThread().interrupt();
						}
					}
				}

				result = e;

				int status;
				if (e instanceof IllegalArgumentException && e.getMessage().contains("Cannot claim interface.")) {
					status = ExternalNfcReaderCallback.READER_STATUS_ERROR_UNABLE_TO_CLAIM_USB_INTERFACE;
				} else {
					status = ExternalNfcReaderCallback.READER_STATUS_ERROR;
				}

				setNfcReaderStatus(status, result.toString());
			}

			if (reader != null) {
				startReceivingUsbDeviceDetachBroadcasts();

				listener.onReaderOpen(reader, ExternalNfcReaderCallback.READER_STATUS_OK);
			} else {
				listener.onReaderClosed(nfcReaderStatusCode, nfcReaderStatusMessage);

				synchronized (ExternalUsbNfcServiceSupport.this) {
					openDevices.remove(params[0].getDeviceId());
				}
			}
			scanForDevices();

			return result;
		}
	}

	private class CloseTask extends AsyncTask<Void, Void, Exception> {

		@Override
		protected Exception doInBackground(Void... params) {

			Exception result = null;

			try {
				UsbDevice device = ExternalUsbNfcServiceSupport.this.connectedDevice;
				if (device != null) {
					synchronized (ExternalUsbNfcServiceSupport.this) {
						openDevices.remove(device.getDeviceId());
					}
					readerAdapter.closeReader(device);
				}

			} catch (Exception e) {
				result = e;
			} finally {
				stopReceivingUsbDeviceDetachBroadcasts();

				setNfcReaderStatus(ExternalNfcReaderCallback.READER_STATUS_USB_DEVICE_DISCONNECTED, null);

				listener.onReaderClosed(ExternalNfcReaderCallback.READER_STATUS_USB_DEVICE_DISCONNECTED, null);
			}
			return result;
		}

		@Override
		protected void onPostExecute(Exception result) {
			scanForDevices();
		}

	}

	private static final String ACTION_USB_PERMISSION = ExternalUsbNfcServiceSupport.class.getPackage() + ".USB_PERMISSION";

	protected static final String[] stateStrings = { "Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

	protected final Service service;

	protected UsbManager usbManager;
	protected UsbDevice connectedDevice;
	protected PendingIntent permissionIntent;

	protected Scanner readerScanner;
	protected boolean scanningForReader = false;

	protected boolean recievingDetachBroadcasts = false;

	protected boolean detectReader = false;

	protected Set<Integer> refusedPermissionDevices = new HashSet<Integer>();
	protected Set<Integer> requestPermissionDevices = new HashSet<Integer>();
	protected Set<Integer> openDevices = new HashSet<Integer>();

	/** current status */
	private int nfcReaderStatusCode;
	private String nfcReaderStatusMessage;

	private final ExternalNfcReaderStatusListener listener;
	private final ReaderAdapter readerAdapter;

	public ExternalUsbNfcServiceSupport(Service service, ExternalNfcReaderStatusListener listener, ReaderAdapter readerAdapter) {
		this.service = service;
		this.listener = listener;
		this.readerAdapter = readerAdapter;
	}

	public void onCreate() {
		// Get USB manager
		usbManager = (UsbManager) service.getSystemService(Context.USB_SERVICE);

		int flag = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			flag = PendingIntent.FLAG_MUTABLE;
		}

		// Register receiver for USB permission
		permissionIntent = PendingIntent.getBroadcast(service, 0, new Intent(ACTION_USB_PERMISSION), flag);

		readerScanner = new Scanner(this);
	}

	protected boolean detectUSBDevices() {
		Log.d(TAG, "Detecing USB devices..");

		for (UsbDevice device : usbManager.getDeviceList().values()) {
			if (readerAdapter.isReaderSupported(device)) {
				// askingForPermission = true;

				synchronized (this) {
					return detectUSBDevice(device);
				}
			} else {
				Log.d(TAG, "Reader not supported: " + device.getDeviceName());
			}
		}

		return false;

	}

	private boolean detectUSBDevice(UsbDevice device) {
		Integer deviceId = device.getDeviceId();

		if (openDevices.contains(deviceId)) {
			Log.d(TAG, "Device " + deviceId + " is already open");
		} else {
			if (usbManager.hasPermission(device)) {
				Log.d(TAG, "Already has permission for reader: " + device.getDeviceName());

				openDevices.add(deviceId);

				new OpenReaderTask().execute(device);

				return true;
			} else {
				if (!requestPermissionDevices.contains(deviceId)) {
					requestPermissionDevices.add(deviceId);

					usbManager.requestPermission(device, permissionIntent);

					Log.d(TAG, "Detected ACR reader..");

					return true;
				} else {
					Log.d(TAG, "Do not ask for permission for previous device " + device.getDeviceName() + " / " + device.getDeviceId());
				}
			}
		}
		return false;
	}

	protected boolean isDetectUSBDevice() {
		return detectReader;
	}

	private void startReceivingPermissionBroadcasts(boolean delay) {
		synchronized (this) {
			if (!scanningForReader) {
				Log.d(TAG, "Start scanning for reader");

				scanningForReader = true;

				// register receiver
				IntentFilter filter = new IntentFilter();
				filter.addAction(ACTION_USB_PERMISSION);
				service.registerReceiver(usbDevicePermissionReceiver, filter);

				if (!delay) {
					readerScanner.resume();
				} else {
					readerScanner.resumeDelayed();
				}
			}
		}
	}

	private void stopReceivingPermissionBroadcasts() {
		synchronized (this) {
			if (scanningForReader) {
				Log.d(TAG, "Stop scanning for reader");

				scanningForReader = false;

				readerScanner.pause();

				try {
					service.unregisterReceiver(usbDevicePermissionReceiver);
				} catch (IllegalArgumentException e) {
					// ignore
				}
			}
		}
	}

	public void scanForDevices() {
		synchronized (this) {
			if (openDevices.isEmpty()) {
				startDetectingReader();
			} else {
				stopDetectingReader();
			}
		}
	}

	protected void startDetectingReader() {
		synchronized (this) {
			if (!detectReader) {
				Log.d(TAG, "Start / resume detecting readers");

				detectReader = true;

				startReceivingPermissionBroadcasts(false);
			}
		}
	}

	protected void stopDetectingReader() {
		synchronized (this) {
			if (detectReader) {
				Log.d(TAG, "Stop / pause detecting readers");

				detectReader = false;

				stopReceivingPermissionBroadcasts();
			}
		}
	}

	private void stopReceivingUsbDeviceDetachBroadcasts() {
		synchronized (this) {
			if (recievingDetachBroadcasts) {
				Log.d(TAG, "Stop recieving USB device detach broadcasts");

				recievingDetachBroadcasts = false;

				// Unregister receiver
				try {
					service.unregisterReceiver(usbDeviceDetachedReceiver);
				} catch (IllegalArgumentException e) {
					// ignore
				}
			}
		}
	}

	private void startReceivingUsbDeviceDetachBroadcasts() {
		synchronized (this) {
			if (!recievingDetachBroadcasts) {
				Log.d(TAG, "Start recieving USB device detach broadcasts");

				recievingDetachBroadcasts = true;

				// register receiver
				IntentFilter filter = new IntentFilter();
				filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
				service.registerReceiver(usbDeviceDetachedReceiver, filter);
			}
		}
	}

	public void onDestroy() {
		detectReader = false;

		stopReceivingPermissionBroadcasts();

		stopReceivingUsbDeviceDetachBroadcasts();
	}

	public void setNfcReaderStatus(int nfcReaderStatusCode, String nfcReaderStatusMessage) {
		synchronized (ExternalUsbNfcServiceSupport.this) {
			this.nfcReaderStatusCode = nfcReaderStatusCode;
			this.nfcReaderStatusMessage = nfcReaderStatusMessage;
		}
	}

}
