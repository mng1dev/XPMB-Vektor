package com.raddstudios.xpmb;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class XPMBServices extends Service {

	public final class MediaPlayerControl implements MediaPlayer.OnPreparedListener,
			MediaPlayer.OnErrorListener {
		private MediaPlayer mMediaPlayer = null;
		public static final int STATE_NOT_INITIALIZED = -1, STATE_PLAYING = 0, STATE_STOPPED = 1,
				STATE_PAUSED = 2;

		private int intPlayerStatus = STATE_NOT_INITIALIZED;

		public MediaPlayerControl() {
			mMediaPlayer = new MediaPlayer();
		}
		
		public void initialize(){
			mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			mMediaPlayer.setOnPreparedListener(this);			
		}

		public void setOnCompletionListener(OnCompletionListener listener) {
			mMediaPlayer.setOnCompletionListener(listener);
		}

		public void play() {
			if (intPlayerStatus == STATE_PAUSED || intPlayerStatus == STATE_STOPPED) {
				mMediaPlayer.start();
				intPlayerStatus = STATE_PLAYING;
			}
		}

		public void pause() {
			if (intPlayerStatus == STATE_PLAYING) {
				mMediaPlayer.pause();
				intPlayerStatus = STATE_PAUSED;
			}
		}

		public void stop() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				pause();
				mMediaPlayer.seekTo(0);
				intPlayerStatus = STATE_STOPPED;
			}
		}

		public void seekTo(int msec) {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				mMediaPlayer.seekTo(msec);
			}
		}

		public int getCurrentPosition() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				return mMediaPlayer.getCurrentPosition();
			}
			return 0;
		}

		public int getDuration() {
			if (intPlayerStatus != STATE_NOT_INITIALIZED) {
				return mMediaPlayer.getDuration();
			}
			return 0;
		}

		public int getPlayerStatus() {
			return intPlayerStatus;
		}

		public void setMediaSource(String url) {
			intPlayerStatus = STATE_NOT_INITIALIZED;
			try {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(url);
				mMediaPlayer.prepareAsync();
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		public void release() {
			mMediaPlayer.release();
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mp.reset();
			intPlayerStatus = STATE_NOT_INITIALIZED;
			return false;
		}

		@Override
		public void onPrepared(MediaPlayer player) {
			player.start();
			intPlayerStatus = STATE_PLAYING;
		}
	}

	public final class ObjectCollections {
		private Hashtable<String, Hashtable<String, Object>> mCollection = null;
		private Hashtable<String, ArrayList<Object>> mList = null;

		public ObjectCollections() {
			mCollection = new Hashtable<String, Hashtable<String, Object>>();
			mList = new Hashtable<String, ArrayList<Object>>();
		}

		public void createList(String name) {
			mList.put(name, new ArrayList<Object>());
		}

		public void createCollection(String name) {
			mCollection.put(name, new Hashtable<String, Object>());
		}

		public void removeList(String name) {
			ArrayList<Object> cArray = mList.get(name);
			if (cArray != null) {
				cArray.clear();
			}
			mList.remove(name);
		}

		public void removeCollection(String name) {
			Hashtable<String, Object> cHash = mCollection.get(name);
			if (cHash != null) {
				cHash.clear();
			}
			mCollection.remove(name);
		}

		public ArrayList<?> getList(String name) {
			return mList.get(name);
		}

		public Hashtable<String, ?> getCollection(String name) {
			return mCollection.get(name);
		}

		public void putObject(String collection, String key, Object value) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				cHash.put(key, value);
			}
		}

		public Object getObject(String collection, String key) {
			return getObject(collection, key, null);
		}

		public Object getObject(String collection, String key, Object defValue) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				if (cHash.get(key) != null) {
					return cHash.get(key);
				} else {
					return defValue;
				}
			}
			return defValue;
		}

		public Object removeObject(String collection, String key) {
			Hashtable<String, Object> cHash = mCollection.get(collection);
			if (cHash != null) {
				return cHash.remove(key);
			}
			return null;
		}
	}

	MediaPlayerControl mpMedia = new MediaPlayerControl();
	ObjectCollections ocCollections = new ObjectCollections();

	public class LocalBinder extends Binder {
		public XPMBServices getService() {
			return XPMBServices.this;
		}
	}
	
	@Override
	public void onCreate(){
		mpMedia.initialize();
	}

	public MediaPlayerControl getMediaPlayerService() {
		return mpMedia;
	}

	public ObjectCollections getObjectCollectionsService() {
		return ocCollections;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new LocalBinder();
	}
}
