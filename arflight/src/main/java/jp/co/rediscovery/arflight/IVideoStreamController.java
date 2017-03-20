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
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
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

/**
 * ライブ映像に対応したコントローラー用のメソッド定義用のインターフェース
 */
public interface IVideoStreamController {
	public static final int DEFAULT_VIDEO_FRAGMENT_SIZE = 1000;
	public static final int DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER = 128;
	public static final int VIDEO_RECEIVE_TIMEOUT_MS = 500;

	/**
	 * ライブ映像処理用のVideoStreamインスタンスをセット
	 * @param video_stream
	 */
	public void setVideoStream(final VideoStream video_stream);

	/**
	 * ライブ映像取得が有効かどうかを取得
	 * @return
	 */
	public boolean isVideoStreamingEnabled();

	/**
	 * ライブ映像取得の有効/無効を切り替え
	 * @param enable
	 * @return
	 */
	public boolean enableVideoStreaming(final boolean enable);
}
