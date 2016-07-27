//
// Created by saki on 16/03/31.
//

#if 1	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
//	#define USE_LOGALL
	#define USE_LOGD
	#undef LOG_NDEBUG
	#undef NDEBUG
#endif

#include "utilbase.h"
#include "IPDetector.h"

IPDetector::IPDetector() {
	ENTER();

	EXIT();
}

IPDetector::~IPDetector() {
	ENTER();

	EXIT();
}

void IPDetector::resize(const int &width, const int &height) {
	if ((mWidth != width) || (mHeight != height)) {
		mWidth = width;
		mHeight = height;
	}
}
