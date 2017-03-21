/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
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

#ifndef FLIGHTDEMO_IMAGEPROCESSOR_H
#define FLIGHTDEMO_IMAGEPROCESSOR_H
#endif //FLIGHTDEMO_IMAGEPROCESSOR_H

#include "Mutex.h"
#include "Condition.h"
#include "IPBase.h"
#include "IPPreprocess.h"
#include "IPFrame.h"
#include "IPDetectorLine.h"
#include "IPDetectorCurve.h"

using namespace android;

class ImageProcessor : virtual public IPPreprocess, virtual public IPFrame {
private:
	jobject mWeakThiz;
	jclass mClazz;
	volatile bool mIsRunning;
	DetectParam_t mParam;
	IPDetectorLine mLineDetector;
	IPDetectorCurve mCurveDetector;

	mutable Mutex mMutex;
	Condition mSync;
	pthread_t processor_thread;
	// 処理スレッドの実行関数
	static void *processor_thread_func(void *vptr_args);
	void do_process(JNIEnv *env);
	int callJavaCallback(JNIEnv *env, DetectRec_t &detect_result, cv::Mat &result, const long &last_queued_time_ms, const DetectParam_t &param);
protected:
public:
	ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz);
	virtual ~ImageProcessor();
	void release(JNIEnv *env);
	int start(const int &width, const int &height);	// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	int stop();		// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	inline const bool isRunning() const { return mIsRunning; };
	void setResultFrameType(const int &result_frame_type);
	inline const int getResultFrameType() const { return mParam.mResultFrameType; };
	int setAreaLimit(const float &min, const float &max);
	int setAspectLimit(const float &min);
	int setAreaErrLimit(const float &limit1, const float &limit2);
	int setFillInnerContour(const bool &fill);
	inline const int getFillInnerContour() const  { return mParam.mFillInnerContour; };
};