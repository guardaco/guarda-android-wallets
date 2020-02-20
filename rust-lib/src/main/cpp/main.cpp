#include <string.h>
#include <jni.h>
#include <string>


extern "C" {

JNIEXPORT jstring JNICALL Java_work_samosudov_rust_1lib_RustAPI_testString( JNIEnv * env, jobject obj )
{
    std::string hello = "Hello from C++ Format! GCC version: {}.{}";
    return env->NewStringUTF(hello.c_str());
}


}