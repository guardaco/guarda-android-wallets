#include <cstdint>
#include <iostream>
#include <test_lib.h>

//extern "C" {
//uint32_t add(uint32_t lhs, uint32_t rhs);
//}

int main() {
    std::cout << "1300 + 14 == " << 1300 << '\n';
    print_hello();
    return 0;
}


//#include <jni.h>
//#include <string>
//
//extern "C" JNIEXPORT jstring JNICALL
//Java_co_guarda_ndkrusttest_MainActivity_stringFromJNI(
//        JNIEnv *env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}
