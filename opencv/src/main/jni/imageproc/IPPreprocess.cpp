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

#include "IPPreprocess.h"

IPPreprocess::IPPreprocess()
{
	ENTER();

	EXIT();
}

IPPreprocess::~IPPreprocess() {
	ENTER();

	EXIT();
}

/** 映像の前処理 */
/*protected*/
int IPPreprocess::pre_process(cv::Mat &frame, cv::Mat &src, cv::Mat &result, const DetectParam_t &param) {

	ENTER();

	int res = 0;
	// 輪郭抽出結果(最外形輪郭)
	std::vector<std::vector< cv::Point>> outlines;	// これも上位から渡さないかんかなぁ

	try {
//		// グレースケールに変換(RGBA->Y)
		cv::cvtColor(frame, src, cv::COLOR_RGBA2GRAY, 1);
		// 輪郭内の塗りつぶし(色抽出してなければ全面塗りつぶされる)
		if (param.mFillInnerContour) {
			findContours(src, outlines, cv::RETR_EXTERNAL);
			// 見つかった輪郭を塗りつぶす
			cv::drawContours(src, outlines, -1, COLOR_WHITE, cv::FILLED);
		}
		outlines.clear();

		// 表示用にカラー画像に戻す
		if (param.needs_result) {
			if (param.show_src) {
				result = frame;
			} else {
				cv::cvtColor(src, result, cv::COLOR_GRAY2RGBA);
			}
		}

	} catch (cv::Exception e) {
		LOGE("pre_process failed:%s", e.msg.c_str());
		res = -1;
	}

    RETURN(res, int);
}

// 最大輪郭数
#define MAX_CONTOURS 100

/** 輪郭線を検出 */
/*protected*/
int IPPreprocess::findPossibleContours(cv::Mat &src, cv::Mat &result,
	std::vector<std::vector< cv::Point>> &contours,	// 輪郭データ
	std::vector<DetectRec_t> &approxes,	// 近似輪郭データ
	const DetectParam_t &param) {

	ENTER();

	std::stringstream ss;
	DetectRec_t possible;
	std::vector<cv::Vec4i> hierarchy;
	std::vector< cv::Point > convex, approx;		// 近似輪郭
	cv::Point2f vertices[4];
	const float areaErrLimit2Min = 1.0f / param.mAreaErrLimit2;

	// 輪郭を求める
	findContours(src, contours, hierarchy, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);
//	findContours(src, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE);
	// 検出した輪郭の数分ループする
	int idx = -1, cnt = 0;
	for (auto contour = contours.begin(); contour != contours.end(); contour++) {
		idx++;
		if (hierarchy[idx][3] != -1) continue;	// 一番外側じゃない時
		// 凸包図形にする
		convex.clear();
		approx.clear();
		cv::convexHull(*contour, convex);
		const size_t num_vertex = convex.size();
		if (LIKELY(num_vertex < 4)) continue;	// 3角形はスキップ
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = cv::minAreaRect(convex);
		// 常に横長として幅と高さを取得
		const float w = fmax(area_rect.size.width, area_rect.size.height);	// 最小矩形の幅=長軸長さ
		const float h = fmin(area_rect.size.width, area_rect.size.height);	// 最小矩形の高さ=短軸長さ
		const float a = w * h;	// 最小矩形の面積
		// 外周線または最小矩形が小さすぎるか大きすぎるのはスキップ
		if (((w > 620) && (h > 350)) || (a < param.mAreaLimitMin) || (a > param.mAreaLimitMax)) continue;
		if (param.show_detects) {
			cv::drawContours(result, contours, idx, COLOR_YELLOW);	// 輪郭
		}
		// 凸包図形の面積を計算
		const float area_convex = (float)cv::contourArea(convex);
		// 面積が小さすぎるのと大きすぎるのはスキップ
		if ((area_convex < param.mAreaLimitMin) || (area_convex > param.mAreaLimitMax)) continue;
		if (param.show_detects) {
			cv::polylines(result, convex, true, COLOR_GREEN);	// 凸包
		}
		// 輪郭近似精度(元の輪郭と近似曲線との最大距離)を計算
		const double epsilon = param.mApproxType == APPROX_RELATIVE
			? param.mApproxFactor * cv::arcLength(*contour, true)	// 周長に対する比
			: param.mApproxFactor;								// 絶対値
		// 輪郭を近似する
		cv::approxPolyDP(*contour, approx, epsilon, true);	// 閉曲線にする
		// 最小矩形の面積が凸包図形面積より指定値以上大きければスキップ=凹凸が激しい
		if (a / area_convex > param.mAreaErrLimit1) {
			// 輪郭の面積を計算
			const float area = (float)cv::contourArea(approx);
			if (param.show_detects) {
				clear_stringstream(ss);
				ss << std::setw(5) << (int)a << ':' << std::setw(5) << (int)area;
				cv::putText(result, ss.str(), area_rect.center, cv::FONT_HERSHEY_SIMPLEX, 0.5f, COLOR_GREEN);
			}
			const float rate = a / area;
			if ((rate < areaErrLimit2Min) || (rate > param.mAreaErrLimit2))
				continue;
		}
		if (param.show_detects) {
			cv::polylines(result, approx, true, COLOR_YELLOW, 2);
		}
		if (UNLIKELY(++cnt > MAX_CONTOURS)) break;
		if (param.show_detects) {
			cv::polylines(result, approx, true, COLOR_GREEN, 2);
		}

		possible.clear();
		possible.type = TYPE_NON;
		possible.moments = cv::moments(approx);
		if (possible.moments.m00 != 0.0f) {
			possible.center.x = possible.moments.m10 / possible.moments.m00;
			possible.center.y = possible.moments.m01 / possible.moments.m00;
		} else {
			possible.center.x = possible.center.y = 0.0f;
		}
		possible = approx; // *contour;
		possible.area_rect = area_rect;	// 最小矩形
		possible.ellipse.center.x = possible.ellipse.center.y = possible.ex = possible.ey = 0.0f;
		possible.area_rate = w * h / area_convex;		// 凸包図形面積に対する最小矩形の面積比
		possible.area = area_convex;					// 凸包図形の面積
		possible.aspect = h != 0.0f ? w / h : 0.0f;		// 最小矩形のアスペクト比
		possible.length = w;				// 最小矩形の長軸長さ
		possible.width = h;					// 最小矩形の短軸長さ
		possible.analogous = 0.0f;
		possible.curvature = possible.ex = possible.ey = 0.0f;
		approxes.push_back(possible);
	}

	RETURN(0, int);
}
