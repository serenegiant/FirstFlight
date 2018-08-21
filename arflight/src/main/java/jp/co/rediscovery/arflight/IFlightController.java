package jp.co.rediscovery.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2018, saki t_saki@serenegiant.com
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

import jp.co.rediscovery.arflight.attribute.AttributeFloat;
import jp.co.rediscovery.arflight.attribute.AttributeMotor;
import com.serenegiant.math.Vector;

/** 飛行可能デバイス(ぶっちゃけドローン)用コントローラーの追加メソッド定義用のインターフェース */
public interface IFlightController extends IDeviceController {
	// フリップアクションの種類
	public static final int FLIP_FRONT = 0;
	public static final int FLIP_BACK = 1;
	public static final int FLIP_RIGHT = 2;
	public static final int FLIP_LEFT = 3;

	// アニメーション動作の種類
	public static final int ANIM_NON = -1;
	public static final int ANIM_HEADLIGHTS_FLASH = 0;
	public static final int ANIM_HEADLIGHTS_BLINK = 1;
	public static final int ANIM_HEADLIGHTS_OSCILLATION = 2;
	public static final int ANIM_SPIN = 3;
	public static final int ANIM_TAP = 4;
	public static final int ANIM_SLOW_SHAKE = 5;
	public static final int ANIM_METRONOME = 6;
	public static final int ANIM_ONDULATION = 7;
	public static final int ANIM_SPIN_JUMP = 8;
	public static final int ANIM_SPIN_TO_POSTURE = 9;
	public static final int ANIM_SPIRAL = 10;
	public static final int ANIM_SLALOM = 11;
	public static final int ANIM_BOOST = 12;

	// センサーの種類
	/** 慣性測定(ジャイロ/加速度) */
	public static final int SENSOR_IMU = 0;
	/** 高度計(気圧計) */
	public static final int SENSOR_BAROMETER = 1;
	/** 高度計(超音波) */
	public static final int SENSOR_ULTRASOUND = 2;
	/** GPS */
	public static final int SENSOR_GPS = 3;
	/** 磁気センサー(コンパス/姿勢) */
	public static final int SENSOR_MAGNETOMETER= 4;
	/** 垂直カメラ(対地速度検出) */
	public static final int SENSOR_VERTICAL_CAMERA = 5;

	public static final int STATE_MASK_CONNECTION = 0x00ff;
	public static final int STATE_MASK_FLYING = 0xff00;


	/**
	 * 飛行中かどうかを取得
	 * @return
	 */
	public boolean isFlying();

	/**
	 * 磁気センサーのキャリブレーションが必要かどうか
	 * @return
	 */
	public boolean needCalibration();

	/**
	 * 静止画撮影状態を取得
	 * @return
	 */
	public int getStillCaptureState();

	/**
	 * 動画撮影状態を取得
	 * @return
	 */
	public int getVideoRecordingState();

	/**
	 * 現在のマスストレージIDを取得
	 * @return
	 */
	public int getMassStorageId();

	/**
	 * 現在のマスストレージ名を取得
	 * @return
	 */
	public String getMassStorageName();

	/**
	 * 起こったことがないからよくわからんけどデバイス(多分マイコンとかESC)が過熱した時にその異常を解除するコマンドみたい
	 * @return
	 */
	public boolean sendOverHeatSwitchOff();

	/**
	 * 起こったことがないからよくわからんけどデバイス(多分マイコンとかESC)が過熱した時の冷却をさせるためのコマンドみたい
	 * @return
	 */
	public boolean sendOverHeatVentilate();

	/**
	 * 操縦モードかどうかをセット
	 * スカイコントローラーの場合には更にsendCoPilotingSetPilotingSourceちゅうのもある
	 * @param piloting
	 * @return
	 */
	public boolean sendControllerIsPiloting(final boolean piloting);

	/**
	 * 離陸指示
	 * @return
	 */
	public boolean requestTakeoff();

	/**
	 * 着陸指示
	 * @return
	 */
	public boolean requestLanding();

	/**
	 * 非常停止指示
	 * @return
	 */
	public boolean requestEmergencyStop();

	/**
	 * フラットトリム実行(姿勢センサー調整)
	 * @return
	 */
	public boolean requestFlatTrim();

	/**
	 * キャリブレーションを実行(磁気センサー調整)
	 * @param start true: 開始要求, false: 停止要求
	 * @return
	 */
	public boolean startCalibration(final boolean start);

	/**
	 * 最大高度を設定
	 * @param altitude [m]
	 * @return
	 */
	public boolean setMaxAltitude(final float altitude);

	/**
	 * 最大高度設定値[m]を返す
	 * @return
	 */
	public AttributeFloat getMaxAltitude();

	/**
	 * 最大傾斜設定
	 * @param tilt
	 * @return
	 */
	public boolean setMaxTilt(final float tilt);

	/**
	 * 最大傾斜角[度]を取得する
	 * @return
	 */
	public AttributeFloat getMaxTilt();

	/**
	 * 最大上昇/下降速度を設定
	 * @param speed m/s
	 * @return
	 */
	public boolean setMaxVerticalSpeed(final float speed);

	/**
	 * 最大上昇/下降速度[m/秒]を取得する
	 * @return
	 */
	public AttributeFloat getMaxVerticalSpeed();

	/**
	 * 最大回転速度
	 * @param speed [度/秒]
	 * @return
	 */
	public boolean setMaxRotationSpeed(final float speed);

	/**
	 * 最大回転速度[度/秒]を取得する
	 * @return
	 */
	public AttributeFloat getMaxRotationSpeed();

	/**
	 * デバイス姿勢を取得可能かどうか
	 * @return
	 */
	public boolean canGetAttitude();

	/**
	 * デバイス姿勢を取得する(ラジアン)
	 * zが高度ではなくyawなので注意
	 * @return Vector(x=roll, y=pitch, z=yaw)
	 */
	public Vector getAttitude();

	/**
	 * 高度[m]を取得
	 * @return
	 */
	public float getAltitude();
	/**
	 * モーターの個数を返す
	 * @return
	 */
	public int getMotorNums();

	/**
	 * モーター設定を取得する
	 * @param index
	 * @return
	 */
	public AttributeMotor getMotor(final int index);

	/**
	 * モーターの自動カット機能が有効かどうかを取得する
	 * 安全のためには自動カット機能を有効にしといたほうがええと思う
	 * @return
	 */
	public boolean isCutoffMode();

	/**
	 * モーターの自動カット機能を有効にするかどうかを設定
	 * 安全のためには自動カット機能を有効にしといたほうがええと思う
	 * @param enabled
	 * @return
	 */
	public boolean sendCutOutMode(final boolean enabled);

	/**
	 * 自動離陸モードが有効かどうかを取得する
	 * @return
	 */
	public boolean isAutoTakeOffModeEnabled();

	/**
	 * 自動離陸モードを設定
	 * @param enable
	 * @return
	 */
	public boolean sendAutoTakeOffMode(final boolean enable);

	/**
	 * ガード(ハル)を装着しているかどうかを取得する
	 * @return
	 */
	public boolean hasGuard();

	/**
	 * ガード(ハル)を装着しているかどうかを設定
	 * @param has_guard
	 * @return
	 */
	public boolean setHasGuard(final boolean has_guard);

	/**
	 * デバイスを移動させるかどうか
	 * @param flag 1:移動, 0:セットのみ
	 */
	public void setFlag(final int flag);

	/**
	 * 高度を上下させる
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setGaz(final float gaz);

	/**
	 * 左右に傾ける。
	 * @param roll 負:左, 正:右, -100〜+100
	 */
	public void setRoll(final float roll);

	/**
	 * 左右に傾ける
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param move, true:移動, false:設定変更のみ
	 */
	public void setRoll(final float roll, boolean move);

	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setPitch(final float pitch);

	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param move, true:移動, false:設定変更のみ
	 */
	public void setPitch(final float pitch, boolean move);

	/**
	 * 機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setYaw(final float yaw);

	/**
	 * 北磁極に対する角度を設定・・・デバイス側で実装されてない
	 * @param heading -360〜360度
	 */
	public void setHeading(final float heading);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 * @param flag roll/pitchが移動を意味する時1, 設定変更のみの時は0
	 */
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, final int flag);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setMove(final float roll, final float pitch);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setMove(final float roll, final float pitch, final float gaz);

	/**
	 * 指定した方向にフリップ実行
	 * @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * @return
	 */
	public boolean requestAnimationsFlip(final int direction);

	/**
	 * 自動で指定した角度回転させる
	 * ローリングスパイダー(ミニドローン)はデバイス側で処理するので回転速度設定に関係なく同じ時間で処理できるが
	 * Bebopはデバイス側に相当する処理がなくアプリ内で角度と回転速度設定から時間を計算して送信＆待機するので処理時間が変わる。
	 * @param degree -180〜180度
	 * @return
	 */
	public boolean requestAnimationsCap(final int degree);

	/**
	 * 自動で指定した角度回転させる。こっちは時間待ちの待機をsyncで指定したオブジェクトの#waitを使って行う
	 * @param degree -180〜180度
	 * @param sync
	 * @return
	 */
	public boolean requestAnimationsCap(final int degree, final Object sync);

	/**
	 * 静止画撮影要求
	 * @param mass_storage_id
	 * @return
	 */
	public boolean requestTakePicture(final int mass_storage_id);

	/**
	 * 静止画撮影要求
	 * @return
	 */
	public boolean requestTakePicture();

	/**
	 * LEDの明るさをセット
	 * @param left [0,255], 範囲外は256の剰余を適用
	 * @param right [0,255], 範囲外は256の剰余を適用
	 * @return
	 */
	public boolean setHeadlightsIntensity(final int left, final int right);
}
