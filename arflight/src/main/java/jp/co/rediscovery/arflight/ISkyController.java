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

import java.util.List;

/** スカイコントローラー用の追加メソッド定義用のインターフェース */
public interface ISkyController extends IDeviceController {

	/**
	 * スカイコントローラーの設定をリセット
	 * onSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * SSIDもリセットされる
	 */
	public boolean resetSettings();

	/**
	 * スカイコントローラーのSSIDを設定
	 * onAccessPointSSIDChangedUpdateとonSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * 次回電源投入時に有効になる
	 * @param ssid 設定するSSID 使用可能な文字数はたぶん32文字, 半角英数+α
	 * @return
	 */
	public boolean setSkyControllerSSID(final String ssid);

	/**
	 * スカイコントローラーが検出しているWiFiアクセスポイント一覧を要求
	 * 周囲に存在するWiFiの状態を確認するぐらいにしか役に立たない
	 */
	public boolean requestWifiList();

	/**
	 * スカイコントローラーが現在接続しているWiFiネットワークとの接続状態を
	 * これを呼ぶとARCommandSkyControllerWifiStateConnexionChangedListenerと
	 * ARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 */
	public boolean requestCurrentWiFi();

	/**
	 * スカイコントローラーからSSIDで指定したWiFiネットワークに接続する
	 * @param bssid
	 * @param ssid
	 * @param passphrase
	 * @return
	 */
	public boolean connectToWiFi(final String bssid, final String ssid, final String passphrase);

	/**
	 * SSIDで指定したWiFiネットワークとのスカイコントローラー上の接続設定を消去, たぶん切断される
	 * @param ssid
	 * @return
	 */
	public boolean requestForgetWiFi(final String ssid);

	/**
	 * スカイコントローラーが検出しているデバイス一覧を要求
	 */
	public boolean requestDeviceList();

	/**
	 * スカイコントローラーが検出しているデバイスの数を取得
	 * @return
	 */
	public int getDeviceNum();

	/**
	 * ISkyControllerを実装するクラスが保持している、スカイコントローラーが検出しているデバイス一覧を取得
	 * コピーを返すので呼び出し以降の接続状態の変更は反映されない
	 * SDK側で#requestDeviceListが正しく動いてないようなので
	 * @return
	 */
	public List<DeviceInfo> getDeviceList();

	/**
	 * スカイコントローラーが現在接続しているデバイスとの接続状態を要求する
	 * これを呼ぶとARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * 接続しているデバイスがなくてもARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * (ARCommandSkyControllerWifiStateConnexionChangedListenerは来ない)
	 * XXX ARSDK3.8.3のNewAPIだと結果が返ってこない
	 */
	public boolean requestCurrentDevice();

	/**
	 * ISkyControllerを実装するクラスが保持している、スカイコントローラーが現在接続しているデバイスを取得
	 * コピーを返すので呼び出し以降の接続状態の変更は反映されない
	 * @return null 接続されていない
	 */
	public DeviceInfo getCurrentDevice();

	/**
	 * 指定したデバイス名を持つデバイスへ接続する
	 * @param deviceName
	 * @return true 接続できなかった
	 */
	public boolean connectToDevice(final String deviceName);

	/**
	 * 指定したデバイスへ接続する
	 * @param info
	 * @return true 接続できなかった
	 */
	public boolean connectToDevice(final DeviceInfo info);

	/**
	 * 接続中のデバイスから切断する
	 */
	public void disconnectFrom();

	/**
	 * 操縦に使用する入力方法を選択
	 * ARCommandSkyControllerCoPilotingStatePilotingSourceListenerのコールバックメソッドが呼ばれる。なんでやねん
	 * @param _source 0: スカイコントローラーを使用する, 1: タブレット/スマホを使用する
	 */
	public boolean setCoPilotingSource(final int _source);

	/**
	 * 操縦に使用する入力方法を取得b
	 * @return 0: スカイコントローラーを使用する, 1: タブレット/スマホを使用する
	 */
	public int getCoPilotingSource();

	/**
	 * カメラのpan/tiltをリセットする
	 * FIXME デバイス	のカメラなんかな? これを呼んでも何のコールバックもこない. スカイコントローラー自体のアプリ用なのかも
	 */
	public boolean resetCameraOrientation();

	/**
	 * スカイコントローラーのボタン・ジョイスティック等の一覧を要求する
	 */
	public boolean requestGamepadControls();

	/** 現在のボタン割当設定を要求 */
	public boolean requestCurrentButtonMappings();

	/** 使用可能なボタン割当設定を要求 */
	public boolean requestAvailableButtonMappings();

	/**
	 * ボタンの割当設定
	 * @param key_id 物理ボタンID
	 * @param mapping_uid ボタン機能ID
	 * @return
	 */
	public boolean setButtonMapping(final int key_id, final String mapping_uid);

	/**
	 * ボタン割り付け設定をデフォルトにリセットする
	 * @return
	 */
	public boolean resetButtonMapping();

	/** 現在のジョイスティック割当設定を要求 */
	public boolean requestCurrentAxisMappings();

	/** 使用可能なジョイスティック割当設定を要求 */
	public boolean requestAvailableAxisMappings();

	/**
	 * ジョイスティックの割当を変更する
	 * @param axis_id ジョイスティックの物理ID
	 * @param mapping_uid ジョイスティックの機能ID
	 * @return
	 */
	public boolean setAxisMapping(final int axis_id, final String mapping_uid);

	/**
	 * ジョイスティックの割当をデフォルトにリセットする
	 * なぜかonSkyControllerAxisMappingsStateCurrentAxisMappingsUpdateと
	 * onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdateが2ペア分送られてくる
	 * もしかすると1回目は変更前で2回目が変更後なのかも
	 * @return
	 */
	public boolean resetAxisMapping();

	/** ジョイスティックの入力フィルター設定を要求 */
	public boolean requestCurrentAxisFilters();

	/** ジョイスティックの入力フィルターのプリセット設定を要求 */
	public boolean requestPresetAxisFilters();

	/**
	 * ジョイスティックの入力フィルター設定
	 * @param axis_id 物理ジョイスティックID
	 * @param filter_uid_or_builder フィルターID
	 * @return
	 */
	public boolean setAxisFilter(final int axis_id, final String filter_uid_or_builder);

	/**
	 * ジョイスティックの入力フィルターをデフォルトにリセットする
	 * @return
	 */
	public boolean resetAxisFilter();

	/**
	 * 磁気センサーのキャリブレーション品質更新通知の有効/無効を切り替える
	 * @param enable
	 * @return
	 */
	public boolean setMagnetoCalibrationQualityUpdates(final boolean enable);

	/**
	 * なんじゃ?
	 * 何のコールバックも返ってこない
	 * FIXME スカイコントローラー自体のアプリ用なのかも
	 * @return
	 */
	public boolean requestButtonEventsSettings();

	/**
	 * スカイコントローラー自体のGPS座標が特定されたかどうか
	 * @return
	 */
	public boolean isGPSFixedSkyController();

	/**
	 * スカイコントローラー自体のバッテリー残量を取得
	 * @return
	 */
	public int getBatterySkyController();
}
