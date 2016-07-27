//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTOR_H
#define FLIGHTDEMO_IPDETECTOR_H

#include <stdlib.h>
#include <algorithm>
#include <iomanip>

#include "IPBase.h"

class IPDetector : virtual public IPBase {
private:
	int mWidth, mHeight;
protected:
	inline const int width() const { return mWidth; };
	inline const int height() const { return mHeight; };
public:
	IPDetector();
	virtual ~IPDetector();
	void resize(const int &width, const int &height);
	virtual int detect(cv::Mat &src, std::vector<DetectRec_t> &contours, std::vector<const DetectRec_t *> &possibles_work,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param) = 0;
};
#endif //FLIGHTDEMO_IPDETECTOR_H
