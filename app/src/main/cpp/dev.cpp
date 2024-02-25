// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("dev")
//      }
//    }

# include <jni.h>
# include <string>
# include <android/log.h>
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_tamhoang_ldpro4_ui_main_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
//    std::string url = "ldpro=fun";
    std::string url = "103=154=244=98:8000";

    return env->NewStringUTF(url.c_str());
}