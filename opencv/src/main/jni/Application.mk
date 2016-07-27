NDK_TOOLCHAIN_VERSION := 4.9
# OpenGL|ES3を使うため API>=18とする
APP_PLATFORM := android-18

# Cコンパイラオプション
APP_CFLAGS += -DHAVE_PTHREADS
APP_CFLAGS += -DNDEBUG					# LOG_ALLを無効にする・assertを無効にする場合
APP_CFLAGS += -DLOG_NDEBUG				# デバッグメッセージを出さないようにする時

# C++コンパイラオプション
APP_CPPFLAGS += -std=c++0x
APP_CPPFLAGS += -fexceptions			# 例外を有効にする
APP_CPPFLAGS += -frtti					# RTTI(実行時型情報)を有効にする

# 最適化設定
APP_CFLAGS += -DAVOID_TABLES
APP_CFLAGS += -O3 -fstrict-aliasing
APP_CFLAGS += -fprefetch-loop-arrays

# 警告を消す設定
APP_CFLAGS += -Wno-parentheses
APP_CFLAGS += -Wno-switch
APP_CFLAGS += -Wno-extern-c-compat
APP_CFLAGS += -Wno-empty-body
APP_CFLAGS += -Wno-deprecated-register
APP_CPPFLAGS += -Wreturn-type
APP_CPPFLAGS += -Wno-multichar

# 出力アーキテクチャ
APP_ABI := armeabi-v7a x86

# STLライブラリ GNU-STLじゃないとリンクできない
APP_STL := gnustl_shared

# 出力オプション
APP_OPTIM := release
