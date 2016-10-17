package jp.co.rediscovery.arflight;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_STREAM_CODEC_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arsal.ARNativeData;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.serenegiant.glutils.RendererHolder;
import com.serenegiant.utils.FpsCounter;

/** ライブ映像処理用のヘルパークラス */
public class VideoStream {
	private static final String TAG = VideoStream.class.getSimpleName();

	private static final String VIDEO_MIME_TYPE = "video/avc";
	private static final int VIDEO_INPUT_TIMEOUT_US = 33000;
	private static final long VIDEO_OUTPUT_TIMEOUT_US = 20000;
	public static final int VIDEO_WIDTH = 640;
	public static final int VIDEO_HEIGHT = 368;

	public static final int VIDEO_WIDTH_HALF = VIDEO_WIDTH >>> 1;
	public static final int VIDEO_HEIGHT_HALF = VIDEO_HEIGHT >>> 1;

	private final Object mSync = new Object();
	private volatile boolean isDecoderRunning;

	private final RendererHolder mRendererHolder;
	private final DecodeTask mDecodeTask;
	private final FpsCounter mFps = new FpsCounter();

	public VideoStream() {
		mDecodeTask = new DecodeTask();
		new Thread(mDecodeTask, "VideoStream#decodeTask").start();
		mRendererHolder = new RendererHolder(VIDEO_WIDTH, VIDEO_HEIGHT, null);
		synchronized (mSync) {
			for ( ; !isDecoderRunning ; ) {
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
					break;
				}
			}
		}
	}

	/**
	 * 関連するリソースをすべて破棄する
	 */
	public void release() {
		synchronized (mSync) {
			isDecoderRunning = false;
			mSync.notifyAll();
		}
		mRendererHolder.release();
	}

//--------------------------------------------------------------------------------
	/**
	 * 映像書き込み用Surfaceを追加する
	 * @param id
	 * @param surface
	 */
	public void addSurface(final int id, final Surface surface) {
		mRendererHolder.addSurface(id, surface, false);
	}

	/**
	 * 映像書き込み用Surfaceを取り除く
	 * @param id
	 */
	public void removeSurface(final int id) {
		mRendererHolder.removeSurface(id);
	}

//--------------------------------------------------------------------------------
	/**
	 * ライブ映像用のデコーダーの初期化用のデータが取得できた時
	 * @param codec
	 */
	public void configureDecoder(final ARControllerCodec codec) {
		ByteBuffer sps = null, pps = null;
		if (codec.getType() == ARCONTROLLER_STREAM_CODEC_TYPE_ENUM.ARCONTROLLER_STREAM_CODEC_TYPE_H264) {
			final ARControllerCodec.H264 codecH264 = codec.getAsH264();

			sps = ByteBuffer.wrap(codecH264.getSps().getByteData());
			pps = ByteBuffer.wrap(codecH264.getPps().getByteData());
		} else {
			Log.w(TAG, "unexpected codec type:" + codec.getType());
		}
		if (sps != null) {
			onSpsPpsReady(sps, pps);
			mDecodeTask.waitForIFrame = false;
		} else {
			Log.w(TAG, "sps is null");
		}
	}

	/**
	 * 映像フレームデータを受信した時の処理
	 * @param frame
	 */
	public void onReceiveFrame(final com.parrot.arsdk.arcontroller.ARFrame frame) {
		// デコーダーへキューイングする
		mDecodeTask.queueFrame(frame, frame.isIFrame());
	}

	/**
	 * 一定時間内に映像フレームデータを受信できなかった時
	 */
	public void onFrameTimeout() {
		// 今のところLogCatにメッセージを出すだけで特に何もしない
		Log.w(TAG, "onFrameTimeout");
	}

	protected ByteBuffer[] onSpsPpsReady(final ByteBuffer sps, final ByteBuffer pps) {
		mDecodeTask.initMediaCodec();
		mDecodeTask.configureMediaCodec(sps, pps, mRendererHolder.getSurface()/*mRendererTask.getSurface()*/);
		return mDecodeTask.inputBuffers;
	}

//--------------------------------------------------------------------------------

	/**
	 * フレームレートを更新
	 * @return
	 */
	public VideoStream updateFps() {
		mFps.update();
		return this;
	}

	/**
	 * 前回の#getFps呼び出しから現在までのフレームレートを取得
	 * @return
	 */
	public float getFps() {
		return mFps.getFps();
	}

	/**
	 * 測定開始からのフレームレートを取得
	 * @return
	 */
	public float getTotalFps() {
		return mFps.getTotalFps();
	}

//--------------------------------------------------------------------------------

	/** 受信したh.264映像をデコードして描画タスクにキューイングするタスク */
	private final class DecodeTask implements Runnable {
		private MediaCodec mediaCodec;
		/** デコーダーが初期化出来たかどうか */
		private volatile boolean isCodecConfigured;
		/** IFrame待機中フラグ */
		private boolean waitForIFrame = true;
		private ByteBuffer [] inputBuffers;

		public DecodeTask() {
			isCodecConfigured = false;
			waitForIFrame = true;
		}

		/**
		 * デバイスから受け取った映像データを非同期でデコードするためにキューに入れる
		 * @param frame
		 * @param isIFrame
		 */
		@SuppressWarnings("deprecation")
		public void queueFrame(final ARNativeData frame, final boolean isIFrame) {
			if ((mediaCodec != null)) {
				if (!isCodecConfigured && isIFrame) {
					final ByteBuffer csdBuffer = getCSD(frame, true/*isIFrame*/);
					if (csdBuffer != null) {
						configureMediaCodec(csdBuffer, mRendererHolder.getSurface()/*mRendererTask.getSurface()*/);
					} else {
						Log.w(TAG, "CSDを取得できなかった");
					}
				}
				if (isCodecConfigured && (!waitForIFrame || isIFrame)) {
					waitForIFrame = false;

					// ここに来るのはIフレームかIフレームから連続してPフレームを受信している時
					int index = -1;

					for (int i = 0; isDecoderRunning && (index < 0) && (i < 30)  ; i++) {
						try {
							index = mediaCodec.dequeueInputBuffer(VIDEO_INPUT_TIMEOUT_US);
						} catch (final IllegalStateException e) {
							Log.e(TAG, "Error while dequeue input buffer");
						}
					}
					if (index >= 0) {
						try {
							final ByteBuffer b = inputBuffers[index];
							final int sz = frame.getDataSize();
							b.clear();
							b.put(frame.getByteData(), 0, sz);
							int flag = 0;
							if (isIFrame) {
								flag |= MediaCodec.BUFFER_FLAG_SYNC_FRAME; //  | MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
							}
							mediaCodec.queueInputBuffer(index, 0, sz, 0, flag);
						} catch (final IllegalStateException e) {
							Log.w(TAG, "Error while queue input buffer");
						}
					} else if (isDecoderRunning) {
						waitForIFrame = true;
					}
				}
			} else {
				Log.w(TAG, "MediaCodecが生成されていない");
			}
		}

		@Override
		public void run() {
			// デコーダーを初期化
			initMediaCodec();
			synchronized (mSync) {
				isDecoderRunning = true;
				mSync.notifyAll();
			}
			// デコーダーの初期化完了待ちループ
			for ( ; isDecoderRunning && !isCodecConfigured ; ) {
				try {
					Thread.sleep(VIDEO_OUTPUT_TIMEOUT_US / 1000);
				} catch (final InterruptedException e) {
					break;
				}
			}
			if (isDecoderRunning && isCodecConfigured) {
				// 正常に初期化出来た時
				final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
				int outIndex;
				for ( ; isDecoderRunning; ) {
					// MediaCodecでデコードした映像フレームを取り出してSurfaceへ反映させるためのループ
					try {
						outIndex = mediaCodec.dequeueOutputBuffer(info, VIDEO_OUTPUT_TIMEOUT_US);
						if (outIndex >= 0) {
							// これを呼び出すとSurfaceへの書き込み要求が発行される.
							// これより未来のどこかわからへん時に実際の描画が行われる
							mediaCodec.releaseOutputBuffer(outIndex, true/*render*/);
							mFps.count();
						}
					} catch (final IllegalStateException e) {
						Log.e(TAG, "Error while dequeue output buffer (outIndex)");
					}
				}
			}
			synchronized (mSync) {
				isDecoderRunning = false;
				mSync.notifyAll();
			}
			// デコーダーを破棄
			releaseMediaCodec();
		}

		/**
		 * デコーダー用のMediaCodecを生成
		 */
		private void initMediaCodec() {
			if (mediaCodec == null) {
				try {
					mediaCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE);
				} catch (final IOException e) {
					Log.w(TAG, e);
				}
			}
		}

		/**
		 * デコーダー用にMediaCodecを初期化
		 * @param csdBuffer
		 * @param surface
		 */
		@SuppressWarnings("deprecation")
		private void configureMediaCodec(final ByteBuffer csdBuffer, final Surface surface) {
			final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
			format.setByteBuffer("csd-0", csdBuffer);

			mediaCodec.configure(format, surface, null, 0);
			mediaCodec.start();

			inputBuffers = mediaCodec.getInputBuffers();
			isCodecConfigured = true;
		}

		/**
		 * デコーダー用にMediaCodecを初期化
		 * @param sps
		 * @param pps
		 * @param surface
		 */
		@SuppressWarnings("deprecation")
		private void configureMediaCodec(final ByteBuffer sps, final ByteBuffer pps, final Surface surface) {
			final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
			format.setByteBuffer("csd-0", sps);
			format.setByteBuffer("csd-1", pps);

			mediaCodec.configure(format, surface, null, 0);
			mediaCodec.start();

			inputBuffers = mediaCodec.getInputBuffers();
			isCodecConfigured = true;
		}

		/**
		 * デコーダーを破棄
		 */
		private void releaseMediaCodec() {
			if ((mediaCodec != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
				try {
					if (isCodecConfigured) {
						mediaCodec.stop();
					}
				} catch (final IllegalStateException e) {
					Log.w(TAG, e);
				}
				isCodecConfigured = false;
				mediaCodec.release();
				mediaCodec = null;
			}
		}

		/**
		 * MediaCodecのh.264デコーダーの初期化用にSPS, PPSを映像データから取り出す
		 * @param frame
		 * @param isIFrame
		 * @return
		 */
		private ByteBuffer getCSD(final ARNativeData frame, final boolean isIFrame) {
			if (isIFrame) {
				final byte[] data = frame.getByteData();
				int spsSize;
				int searchIndex;
				// we'll need to search the "00 00 00 01" pattern to find each header size
				// Search start at index 4 to avoid finding the SPS "00 00 00 01" tag
				for (searchIndex = 4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
					if (0 == data[searchIndex  ] &&
							0 == data[searchIndex+1] &&
							0 == data[searchIndex+2] &&
							1 == data[searchIndex+3])
					{
						break;  // SPS header found
					}
				}
				spsSize = searchIndex;

				// Search start at index 4 to avoid finding the PPS "00 00 00 01" tag
				for (searchIndex = spsSize + 4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
					if (0 == data[searchIndex  ] &&
							0 == data[searchIndex+1] &&
							0 == data[searchIndex+2] &&
							1 == data[searchIndex+3]) {
						break;  // frame header found
					}
				}
				int csdSize = searchIndex;

				final ByteBuffer result = ByteBuffer.allocateDirect(csdSize);
				result.clear();
				result.put(data, 0, csdSize);	// 要素を1つずつループしてputするのでちょっと遅い
//				final byte[] csdInfo = new byte[csdSize];
//				System.arraycopy(data, 0, csdInfo, 0, csdSize);
//				return ByteBuffer.wrap(csdInfo);
				return result;
			}
			return null;
		}
	}

}
