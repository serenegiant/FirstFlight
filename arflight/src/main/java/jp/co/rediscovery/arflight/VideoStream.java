package jp.co.rediscovery.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2016, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

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
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = VideoStream.class.getSimpleName();

	private static final String VIDEO_MIME_TYPE = "video/avc";	// h.264
	private static final int VIDEO_INPUT_TIMEOUT_US = 33000;
	private static final long VIDEO_OUTPUT_TIMEOUT_US = 20000;
	public static final int VIDEO_WIDTH = 640;
	public static final int VIDEO_HEIGHT = 368;

	public static final int VIDEO_WIDTH_HALF = VIDEO_WIDTH >>> 1;
	public static final int VIDEO_HEIGHT_HALF = VIDEO_HEIGHT >>> 1;

	private final Object mSync = new Object();
//	private volatile boolean isRendererRunning;
	private volatile boolean isDecoderRunning;

	private RendererHolder mRendererHolder;
//	private final RendererTask mRendererTask;
	private final DecodeTask mDecodeTask;
	private final FpsCounter mFps = new FpsCounter();

	public VideoStream() {
		mDecodeTask = new DecodeTask();
		new Thread(mDecodeTask, "VideoStream#decodeTask").start();
		mRendererHolder = new RendererHolder(VIDEO_WIDTH, VIDEO_HEIGHT, null);
//		mRendererTask = new RendererTask(this);
//		new Thread(mRendererTask, "VideoStream$rendererTask").start();
//		mRendererTask.waitReady();
		synchronized (mSync) {
			for ( ; /*!isRendererRunning ||*/ !isDecoderRunning ; ) {
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
			/*isRendererRunning =*/ isDecoderRunning = false;
			mSync.notifyAll();
		}
		mRendererHolder.release();
//		mRendererTask.release();
	}

//--------------------------------------------------------------------------------
	/**
	 * 映像書き込み用Surfaceを追加する
	 * @param id
	 * @param surface
	 */
	public void addSurface(final int id, final Surface surface) {
		mRendererHolder.addSurface(id, surface, false);
//		mRendererTask.addSurface(id, surface);
	}

	/**
	 * 映像書き込み用Surfaceを取り除く
	 * @param id
	 */
	public void removeSurface(final int id) {
		mRendererHolder.removeSurface(id);
//		mRendererTask.removeSurface(id);
	}

//--------------------------------------------------------------------------------
	/**
	 * ライブ映像用のデコーダーの初期化用のデータが取得できた時
	 * @param codec
	 */
	public void configureDecoder(final ARControllerCodec codec) {
		if (DEBUG) Log.v(TAG, "configureDecoder:");
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
			if (DEBUG) Log.v(TAG, "queueFrame:");
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
					// mSyncで待ったほうがええかも
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
							// これより未来のどっかで実際の描画が行われる
							mediaCodec.releaseOutputBuffer(outIndex, true/*render*/);
							mFps.count();
							if (DEBUG) Log.v(TAG, "releaseOutputBuffer:");
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

//	private static final int REQUEST_DRAW = 1;
//	private static final int REQUEST_UPDATE_SIZE = 2;
//	private static final int REQUEST_ADD_SURFACE = 3;
//	private static final int REQUEST_REMOVE_SURFACE = 4;
//
//	/** デコードした映像をOpenGL|ESでSurface全面に表示するためのタスク */
//	private static final class RendererTask extends EglTask {
//		/** 映像の分配描画先を保持&描画するためのホルダークラス */
//		private static final class RendererSurfaceRec {
//			private Object mSurface;
//			private EGLBase.IEglSurface mTargetSurface;
//			final float[] mMvpMatrix = new float[16];
//
//			public RendererSurfaceRec(final EGLBase egl, final Object surface) {
//				mSurface = surface;
//				mTargetSurface = egl.createFromSurface(surface);
//				Matrix.setIdentityM(mMvpMatrix, 0);
//			}
//
//			public void release() {
//				if (mTargetSurface != null) {
//					mTargetSurface.release();
//					mTargetSurface = null;
//				}
//				mSurface = null;
//			}
//		}
//
//		private final VideoStream mParent;
//		/** 受け取った映像の分配描画の排他制御用 */
//		private final Object mClientSync = new Object();
//		/** 分配描画先 */
//		private final SparseArray<RendererSurfaceRec> mClients = new SparseArray<RendererSurfaceRec>();
//
//		private GLDrawer2D mDrawer;
//		/** MediaCodecでデコードした映像を受け取るためのテクスチャのテクスチャ名(SurfaceTexture生成時/分配描画に使用) */
//		private int mTexId;
//		/** MediaCodecでデコードした映像を受け取るためのSurfaceTexture */
//		private SurfaceTexture mMasterTexture;
//		/** mMasterTextureのテクスチャ変換行列 */
//		final float[] mTexMatrix = new float[16];
//		/** MediaCodecでデコードした映像を受け取るためのSurfaceTextureから取得したSurface */
//		private Surface mMasterSurface;
//		/** 映像サイズ */
//		private int mVideoWidth, mVideoHeight;
//
//		/**
//		 * コンストラクタ
//		 * @param parent
//		 */
//		public RendererTask(final VideoStream parent) {
//			super(3, null, EglTask.EGL_FLAG_RECORDABLE);
//			mParent = parent;
//			mVideoWidth = VIDEO_WIDTH;
//			mVideoHeight = VIDEO_HEIGHT;
//		}
//
////		private float mTexWidth;
////		private float mTexHeight;
////		private float[] mTexOffset;
////		private int muTexOffsetLoc;			// テクスチャオフセット(カーネル行列用)
//		@Override
//		protected void onStart() {
//			if (DEBUG) Log.v(TAG, "onStart:");
//			mDrawer = new GLDrawer2D(true);
////			mDrawer.updateShader(ShaderConst.FRAGMENT_SHADER_EXT_FILT3x3);
////			setTexSize(mVideoWidth, mVideoHeight);
//
//			mTexId = GLHelper.initTex(ShaderConst.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
//			mMasterTexture = new SurfaceTexture(mTexId);
//			mMasterSurface = new Surface(mMasterTexture);
//			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
//			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
//			synchronized (mParent.mSync) {
//				mParent.isRendererRunning = true;
//				mParent.mSync.notifyAll();
//			}
//			mParent.mFps.reset();
//		}
//
////		private void setTexSize(final int width, final int height) {
////			mTexHeight = height;
////			mTexWidth = width;
////			final float rw = 1.0f / width;
////			final float rh = 1.0f / height;
////
////			// Don't need to create a new array here, but it's syntactically convenient.
////			mTexOffset = new float[] {
////				-rw, -rh,   0f, -rh,    rw, -rh,
////				-rw, 0f,    0f, 0f,     rw, 0f,
////				-rw, rh,    0f, rh,     rw, rh
////			};
////			muTexOffsetLoc = mDrawer.glGetUniformLocation("uTexOffset");
////			// テクセルオフセット
////			if ((muTexOffsetLoc >= 0) && (mTexOffset != null)) {
////				GLES20.glUniform2fv(muTexOffsetLoc, ShaderConst.KERNEL_SIZE3x3, mTexOffset, 0);
////			}
////		}
//
//		@Override
//		protected void onStop() {
//			if (DEBUG) Log.v(TAG, "onStop:");
//			synchronized (mParent.mSync) {
//				mParent.isRendererRunning = false;
//				mParent.mSync.notifyAll();
//			}
//			handleRemoveAll();
//			makeCurrent();
//			if (mDrawer != null) {
//				mDrawer.release();
//				mDrawer = null;
//			}
//			mMasterSurface = null;
//			if (mMasterTexture != null) {
//				mMasterTexture.release();
//				mMasterTexture = null;
//			}
//		}
//
//		@Override
//		protected Object processRequest(int request, int arg1, int arg2, Object obj) {
//			switch (request) {
//			case REQUEST_DRAW:
//				handleDraw();
//				break;
//			case REQUEST_UPDATE_SIZE:
//				handleResize(arg1, arg2);
//				break;
//			case REQUEST_ADD_SURFACE:
//				handleAddSurface(arg1, obj);
//				break;
//			case REQUEST_REMOVE_SURFACE:
//				handleRemoveSurface(arg1);
//				break;
//			}
//			return null;
//		}
//
//		/** 映像受け取り用Surfaceを取得 */
//		public Surface getSurface() {
//			return mMasterSurface;
//		}
//
//		/** 映像受け取り用SurfaceTextureを取得 */
//		public SurfaceTexture getSurfaceTexture() {
//			return mMasterTexture;
//		}
//
//		/**
//		 * 分配描画用のSurfaceを追加
//		 * @param id
//		 * @param surface Surface/SurfaceHolder/SurfaceTexture
//		 */
//		public void addSurface(final int id, final Object surface) {
//			if (DEBUG) Log.v(TAG, "addSurface:id=" + id + ",surface=" + surface);
//			synchronized (mClientSync) {
//				if ((surface != null) && (mClients.get(id) == null)) {
//					offer(REQUEST_ADD_SURFACE, id, surface);
//					try {
//						mClientSync.wait();
//					} catch (final InterruptedException e) {
//						// ignore
//					}
//				}
//			}
//		}
//
//		/***
//		 * 分配描画用のSurfaceを削除
//		 * @param id
//		 */
//		public void removeSurface(final int id) {
//			if (DEBUG) Log.v(TAG, "removeSurface:id=" + id);
//			synchronized (mClientSync) {
//				if (mClients.get(id) != null) {
//					offer(REQUEST_REMOVE_SURFACE, id);
//					try {
//						mClientSync.wait();
//					} catch (final InterruptedException e) {
//						// ignore
//					}
//				}
//			}
//		}
//
//		/***
//		 * 描画映像サイズを変更
//		 * @param width
//		 * @param height
//		 */
//		public void resize(final int width, final int height) {
//			if ((mVideoWidth != width) || (mVideoHeight != height)) {
//				offer(REQUEST_UPDATE_SIZE, width, height);
//			}
//		}
//
//		/**
//		 * 実際の描画処理(ワーカースレッド上で実行)
//		 */
//		private void handleDraw() {
//			if (DEBUG) Log.v(TAG, "handleDraw:");
//			mParent.mFps.count();
//			try {
//				makeCurrent();
//				mMasterTexture.updateTexImage();
//				mMasterTexture.getTransformMatrix(mTexMatrix);
//			} catch (final Exception e) {
//				Log.e(TAG, "draw:thread id =" + Thread.currentThread().getId(), e);
//				return;
//			}
//			// 各Surfaceへ描画する
//			synchronized (mClientSync) {
//				final int n = mClients.size();
//				RendererSurfaceRec client;
//				for (int i = 0; i < n; i++) {
//					client = mClients.valueAt(i);
//					if (client != null) {
//						client.mTargetSurface.makeCurrent();
////						mDrawer.setMvpMatrix(client.mMvpMatrix, 0);
//						mDrawer.draw(mTexId, mTexMatrix, 0);
//						client.mTargetSurface.swap();
//					}
//				}
//			}
//			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//			GLES20.glFlush();
////			if (DEBUG) Log.v(TAG, "handleDraw:終了");
//		}
//
//		/**
//		 * 分配描画用Surfaceを追加(ワーカースレッド上で実行)
//		 * @param id
//		 * @param surface
//		 */
//		private void handleAddSurface(final int id, final Object surface) {
//			if (DEBUG) Log.v(TAG, "handleAddSurface:");
//			checkSurface();
//			synchronized (mClientSync) {
//				RendererSurfaceRec client = mClients.get(id);
//				if (client == null) {
//					try {
//						client = new RendererSurfaceRec(getEgl(), surface);
//						mClients.append(id, client);
//					} catch (final Exception e) {
//						Log.w(TAG, "invalid surface: surface=" + surface);
//					}
//				} else {
//					Log.w(TAG, "surface is already added: id=" + id);
//				}
//				mClientSync.notifyAll();
//			}
//		}
//
//		/**
//		 * 分配描画用Surfaceを取り除く(ワーカースレッド上で実行)
//		 * @param id
//		 */
//		private void handleRemoveSurface(final int id) {
//			if (DEBUG) Log.v(TAG, "handleRemoveSurface:");
//			synchronized (mClientSync) {
//				final RendererSurfaceRec client = mClients.get(id);
//				if (client != null) {
//					mClients.remove(id);
//					client.release();
//				}
//				checkSurface();
//				mClientSync.notifyAll();
//			}
//		}
//
//		private void handleRemoveAll() {
//			synchronized (mClientSync) {
//				final int n = mClients.size();
//				RendererSurfaceRec client;
//				for (int i = 0; i < n; i++) {
//					client = mClients.valueAt(i);
//					if (client != null) {
//						makeCurrent();
//						client.release();
//					}
//				}
//				mClients.clear();
//			}
//		}
//
//		private void checkSurface() {
//			synchronized (mClientSync) {
//				final int n = mClients.size();
//				for (int i = 0; i < n; i++) {
//					final RendererSurfaceRec client = mClients.valueAt(i);
//					if (client != null && client.mSurface instanceof Surface) {
//						if (!((Surface)client.mSurface).isValid()) {
//							final int id = mClients.keyAt(i);
//							Log.d(TAG, "checkSurface:found invalid surface:id=" + id);
//							mClients.valueAt(i).release();
//							mClients.remove(id);
//						}
//					}
//				}
//			}
//		}
//
//		/**
//		 * 製造サイズ変更処理(ワーカースレッド上で実行)
//		 * @param width
//		 * @param height
//		 */
//		private void handleResize(final int width, final int height) {
//			mVideoWidth = width;
//			mVideoHeight = height;
//			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
//		}
//
//		/**
//		 * TextureSurfaceで映像を受け取った際のコールバックリスナー
//		 */
//		private final SurfaceTexture.OnFrameAvailableListener
//			mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
//
//			@Override
//			public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
//				offer(REQUEST_DRAW);
//			}
//		};
//	}

}
