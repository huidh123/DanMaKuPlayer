package org.libsdl.app;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


/**
 * SDL Activity
 */
public class SDLActivity {
	private static final String TAG = "SDL";

	// Keep track of the paused state
	public static boolean mIsPaused, mIsSurfaceReady, mHasFocus;
	public static boolean mExitCalledFromJava;

	// Main components
	protected static SDLActivity mSingleton;
	protected static SDLSurface mSurface;
	protected static View mTextEdit;
	protected static ViewGroup mLayout;
	private static Context mContext;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	protected static Thread mSDLThread;

	// Audio
	protected static AudioTrack mAudioTrack;

	// Load the .so
	static {
		System.loadLibrary("SDL2");
		System.loadLibrary("avutil-52");  
        System.loadLibrary("avcodec-55");  
        System.loadLibrary("swscale-2");  
        System.loadLibrary("swresample-0");  
        System.loadLibrary("avformat-55"); 
        System.loadLibrary("avfilter-4");
        System.loadLibrary("avdevice-55");
		System.loadLibrary("SDLVedio");
	}

	public static void initialize() {
		// The static nature of the singleton and Android quirkyness force us to
		// initialize everything here
		// Otherwise, when exiting the app and returning to it, these variables
		// *keep* their pre exit values
		mSingleton = null;
		mSurface = null;
		mTextEdit = null;
		mLayout = null;
		mSDLThread = null;
		mAudioTrack = null;
		mExitCalledFromJava = false;
		mIsPaused = false;
		mIsSurfaceReady = false;
		mHasFocus = true;
	}

	public SDLActivity(Context context) {
		SDLActivity.mContext = context;
		mSingleton = this;
		SDLActivity.initialize();
		mSurface = new SDLSurface(context);
	}

	// Events
	protected void onPause() {
		Log.i("SDL", "onPause()");
		SDLActivity.handlePause();
	}

	protected void onResume() {
		Log.i("SDL", "onResume()");
		SDLActivity.handleResume();
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		Log.i("SDL", "onWindowFocusChanged(): " + hasFocus);

		SDLActivity.mHasFocus = hasFocus;
		if (hasFocus) {
			SDLActivity.handleResume();
		}
	}

	public void onLowMemory() {
		Log.i("SDL", "onLowMemory()");
		SDLActivity.nativeLowMemory();
	}

	protected void onDestroy() {
		Log.i("SDL", "onDestroy()");
		// Send a quit message to the application
		SDLActivity.mExitCalledFromJava = true;
		SDLActivity.nativeQuit();

		// Now wait for the SDL thread to quit
		if (SDLActivity.mSDLThread != null) {
			try {
				SDLActivity.mSDLThread.join();
			} catch (Exception e) {
				Log.i("SDL", "Problem stopping thread: " + e);
			}
			SDLActivity.mSDLThread = null;

			Log.i("SDL", "Finished waiting for SDL thread");
		}

		// Reset everything in case the user re opens the app
		SDLActivity.initialize();
	}

	/**
	 * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed is the
	 * first to be called, mIsSurfaceReady should still be set to 'true' during
	 * the call to onPause (in a usual scenario).
	 */
	public static void handlePause() {
		if (!SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady) {
			SDLActivity.mIsPaused = true;
			SDLActivity.nativePause();
		}
	}

	/**
	 * Called by onResume or surfaceCreated. An actual resume should be done
	 * only when the surface is ready. Note: Some Android variants may send
	 * multiple surfaceChanged events, so we don't need to resume every time we
	 * get one of those events, only if it comes after surfaceDestroyed
	 */
	public static void handleResume() {
		if (SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady
				&& SDLActivity.mHasFocus) {
			SDLActivity.mIsPaused = false;
			SDLActivity.nativeResume();
		}
	}

	/* The native thread has finished */
	public static void handleNativeExit() {
		SDLActivity.mSDLThread = null;
		// ((Activity)mContext).finish();
	}

	// Messages from the SDLMain thread
	static final int COMMAND_CHANGE_TITLE = 1;
	static final int COMMAND_UNUSED = 2;
	static final int COMMAND_TEXTEDIT_HIDE = 3;

	protected static final int COMMAND_USER = 0x8000;

	/**
	 * This method is called by SDL if SDL did not handle a message itself. This
	 * happens if a received message contains an unsupported command. Method can
	 * be overwritten to handle Messages in a different class.
	 * 
	 * @param command
	 *            the command of the message.
	 * @param param
	 *            the parameter of the message. May be null.
	 * @return if the message was handled in overridden method.
	 */
	protected boolean onUnhandledMessage(int command, Object param) {
		return false;
	}

	/**
	 * A Handler class for Messages from native SDL applications. It uses
	 * current Activities as target (e.g. for the title). static to prevent
	 * implicit references to enclosing object.
	 */
	protected static class SDLCommandHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Context context = mContext;
			if (context == null) {
				Log.e(TAG, "error handling message, getContext() returned null");
				return;
			}
			switch (msg.arg1) {
			case COMMAND_CHANGE_TITLE:
				if (context instanceof Activity) {
					((Activity) context).setTitle((String) msg.obj);
				} else {
					Log.e(TAG,
							"error handling message, getContext() returned no Activity");
				}
				break;
			case COMMAND_TEXTEDIT_HIDE:
				if (mTextEdit != null) {
					mTextEdit.setVisibility(View.GONE);

					InputMethodManager imm = (InputMethodManager) context
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mTextEdit.getWindowToken(), 0);
				}
				break;

			default:
			}
		}
	}

	// Handler for the messages
	Handler commandHandler = new SDLCommandHandler();

	// Send a message from the SDLMain thread
	boolean sendCommand(int command, Object data) {
		Message msg = commandHandler.obtainMessage();
		msg.arg1 = command;
		msg.obj = data;
		return commandHandler.sendMessage(msg);
	}

	// C functions we call
	public static native void nativeInit();

	public static native void nativeLowMemory();

	public static native void nativeQuit();

	public static native void nativePause();

	public static native void nativeResume();

	public static native void onNativeResize(int x, int y, int format);

	public static native int onNativePadDown(int device_id, int keycode);

	public static native int onNativePadUp(int device_id, int keycode);

	public static native void onNativeJoy(int device_id, int axis, float value);

	public static native void onNativeHat(int device_id, int hat_id, int x,
			int y);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeKeyboardFocusLost();

	public static native void onNativeTouch(int touchDevId,
			int pointerFingerId, int action, float x, float y, float p);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void onNativeSurfaceChanged();

	public static native void onNativeSurfaceDestroyed();

	public static native void nativeFlipBuffers();

	public static native int nativeAddJoystick(int device_id, String name,
			int is_accelerometer, int nbuttons, int naxes, int nhats, int nballs);

	public static native int nativeRemoveJoystick(int device_id);

	public static void flipBuffers() {
		SDLActivity.nativeFlipBuffers();
	}

	public static boolean setActivityTitle(String title) {
		// Called from SDLMain() thread and can't directly affect the view
		if (mSingleton == null) {
			return false;
		}
		return mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	public static boolean sendMessage(int command, int param) {
		return mSingleton.sendCommand(command, Integer.valueOf(param));
	}

	public static Context getContext() {
		return mContext;
	}

	/**
	 * @return result of getSystemService(name) but executed on UI thread.
	 */
	public Object getSystemServiceFromUiThread(final String name) {
		final Object lock = new Object();
		final Object[] results = new Object[2]; // array for writable variables
		synchronized (lock) {
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					synchronized (lock) {
						results[0] = ((Activity) mContext)
								.getSystemService(name);
						results[1] = Boolean.TRUE;
						lock.notify();
					}
				}
			});
			if (results[1] == null) {
				try {
					lock.wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		return results[0];
	}

	public static Surface getNativeSurface() {
		return SDLActivity.mSurface.getNativeSurface();
	}

	// Audio
	public static int audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.i("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono")
				+ " " + (is16Bit ? "16-bit" : "8-bit") + " "
				+ (sampleRate / 1000f) + "kHz, " + desiredFrames
				+ " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(
				desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig,
						audioFormat) + frameSize - 1)
						/ frameSize);

		if (mAudioTrack == null) {
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					channelConfig, audioFormat, desiredFrames * frameSize,
					AudioTrack.MODE_STREAM);

			// Instantiating AudioTrack can "succeed" without an exception and
			// the track may still be invalid
			// Ref:
			// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
			// Ref:
			// http://developer.android.com/reference/android/media/AudioTrack.html#getState()

			if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
				Log.e("SDL", "Failed during initialization of Audio Track");
				mAudioTrack = null;
				return -1;
			}

			mAudioTrack.play();
		}

		Log.i("SDL",
				"SDL audio: got "
						+ ((mAudioTrack.getChannelCount() >= 2) ? "stereo"
								: "mono")
						+ " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
								: "8-bit") + " "
						+ (mAudioTrack.getSampleRate() / 1000f) + "kHz, "
						+ desiredFrames + " frames buffer");

		return 0;
	}

	public static void audioWriteShortBuffer(short[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("SDL", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioWriteByteBuffer(byte[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("SDL", "SDL audio: error return from write(byte)");
				return;
			}
		}
	}

	public static void audioQuit() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}

	// Input

	/**
	 * @return an array which may be empty but is never null.
	 */
	public static int[] inputGetInputDeviceIds(int sources) {
		int[] ids = InputDevice.getDeviceIds();
		int[] filtered = new int[ids.length];
		int used = 0;
		for (int i = 0; i < ids.length; ++i) {
			InputDevice device = InputDevice.getDevice(ids[i]);
			if ((device != null) && ((device.getSources() & sources) != 0)) {
				filtered[used++] = device.getId();
			}
		}
		return Arrays.copyOf(filtered, used);
	}

	// Joystick glue code, just a series of stubs that redirect to the
	// SDLJoystickHandler instance
	public static boolean handleJoystickMotionEvent(MotionEvent event) {
		return false;
	}

	public static void pollInputDevices() {
	}

	public View getSurfaceView() {
		// TODO Auto-generated method stub
		return mSurface;
	}

}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {
	@Override
	public void run() {
		// Runs SDL_main()
		SDLActivity.nativeInit();

		Log.i("SDL", "SDL thread terminated");
	}
}

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback {

	// Sensors
	protected static Display mDisplay;

	// Keep track of the surface size to normalize touch events
	protected static float mWidth, mHeight;

	// Startup
	public SDLSurface(Context context) {
		super(context);
		getHolder().addCallback(this);

		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();

		mDisplay = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		// Some arbitrary defaults to avoid a potential division by zero
		mWidth = 1.0f;
		mHeight = 1.0f;
	}

	public Surface getNativeSurface() {
		return getHolder().getSurface();
	}

	// Called when we have a valid drawing surface
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("SDL", "surfaceCreated()");
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}

	// Called when we lose the surface
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("SDL", "surfaceDestroyed()");
		// Call this *before* setting mIsSurfaceReady to 'false'
		SDLActivity.handlePause();
		SDLActivity.mIsSurfaceReady = false;
		SDLActivity.onNativeSurfaceDestroyed();
	}

	// Called when the surface is resized
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i("SDL", "surfaceChanged()");

		int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
		case PixelFormat.A_8:
			Log.i("SDL", "pixel format A_8");
			break;
		case PixelFormat.LA_88:
			Log.i("SDL", "pixel format LA_88");
			break;
		case PixelFormat.L_8:
			Log.i("SDL", "pixel format L_8");
			break;
		case PixelFormat.RGBA_4444:
			Log.i("SDL", "pixel format RGBA_4444");
			sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
			break;
		case PixelFormat.RGBA_5551:
			Log.i("SDL", "pixel format RGBA_5551");
			sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
			break;
		case PixelFormat.RGBA_8888:
			Log.i("SDL", "pixel format RGBA_8888");
			sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
			break;
		case PixelFormat.RGBX_8888:
			Log.i("SDL", "pixel format RGBX_8888");
			sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
			break;
		case PixelFormat.RGB_332:
			Log.i("SDL", "pixel format RGB_332");
			sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
			break;
		case PixelFormat.RGB_565:
			Log.i("SDL", "pixel format RGB_565");
			sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
			break;
		case PixelFormat.RGB_888:
			Log.i("SDL", "pixel format RGB_888");
			// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
			sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
			break;
		default:
			Log.i("SDL", "pixel format unknown " + format);
			break;
		}

		mWidth = width;
		mHeight = height;
		SDLActivity.onNativeResize(width, height, sdlFormat);
		Log.i("SDL", "Window size:" + width + "x" + height);

		// Set mIsSurfaceReady to 'true' *before* making a call to handleResume
		SDLActivity.mIsSurfaceReady = true;
		SDLActivity.onNativeSurfaceChanged();

		if (SDLActivity.mSDLThread == null) {
			// This is the entry point to the C app.
			// Start up the C app thread and enable sensor input for the first
			// time

			SDLActivity.mSDLThread = new Thread(new SDLMain(), "SDLThread");
			SDLActivity.mSDLThread.start();

			// Set up a listener thread to catch when the native thread ends
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						SDLActivity.mSDLThread.join();
					} catch (Exception e) {
					} finally {
						// Native thread has finished
						if (!SDLActivity.mExitCalledFromJava) {
							SDLActivity.handleNativeExit();
						}
					}
				}
			}).start();
		}
	}

	// unused
	@Override
	public void onDraw(Canvas canvas) {
	}

}
