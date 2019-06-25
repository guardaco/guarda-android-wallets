#ifndef BITCOIN_MAIN_H
#define BITCOIN_MAIN_H

#include "zapling_lib.h"
#include "uint256.h"
#include "serialize.h"
#include "version.h"
#include "streams.h"
#include "zcash/IncrementalMerkleTree.hpp"
#include "primitives/transaction.h"

#include <jni.h>
#include <string>
#include <iostream>

#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <android/sharedmem.h>
#include <sys/mman.h>
#include <sstream>
#include <array>
#include <iomanip>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

auto ctx = librustzcash_sapling_proving_ctx_init();

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_initModel(
        JNIEnv *env,
        jobject /* this */,
        jobject _assetManager,
        jstring _outPath,
        jstring _spPath) {
    // Get the file descriptor of the the model data file.
    AAssetManager *assetManager = AAssetManager_fromJava(env, _assetManager);
    const char *outName = env->GetStringUTFChars(_outPath, NULL);
    const char *spendName = env->GetStringUTFChars(_spPath, NULL);
    AAsset *assetOut = AAssetManager_open(assetManager, outName, AASSET_MODE_BUFFER);
    AAsset *assetSp = AAssetManager_open(assetManager, spendName, AASSET_MODE_BUFFER);

    if(assetOut == nullptr || assetSp == nullptr) {
        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "Failed to open the asset. out or spend params");
        std::string hello = "assetOut == nullptr || assetSp == nullptr";
        return env->NewStringUTF(hello.c_str());
    }

    env->ReleaseStringUTFChars(_outPath, outName);
    env->ReleaseStringUTFChars(_spPath, spendName);
    // Get the file length
    off_t fileLengthOut = AAsset_getLength(assetOut);
    off_t fileLengthSp = AAsset_getLength(assetSp);

    // Allocate memory to read your file
    char* fileContentOut = new char[fileLengthOut+1];
    char* fileContentSp = new char[fileLengthSp+1];

    // Read your file
    AAsset_read(assetOut, fileContentOut, fileLengthOut);
    AAsset_read(assetSp, fileContentSp, fileLengthSp);

    librustzcash_init_zksnark_params(fileContentOut,
                                     "657e3d38dbb5cb5e7dd2970e8b03d69b4787dd907285b5a7f0790dcc8072f60bf593b32cc2d1c030e00ff5ae64bf84c5c3beb84ddc841d48264b4a171744d028",
                                     fileContentSp,
                                     "8270785a1a0d0bc77196f000ee6d221c9c9894f55307bd9357c3f0105d31ca63991ab91324160d8f53e2bbd3c2633a6eb8bdf5205d822e7f3f73edac51b2b70c");

    // For safety you can add a 0 terminating character at the end of your file ...
    fileContentOut[fileLengthOut] = '\0';
    fileContentSp[fileLengthSp] = '\0';

    std::ostringstream o;
    o << fileLengthSp;

    std::string hello = "Hello from initModel()=";
    hello += o.str();

    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_genr(
        JNIEnv *env,
        jobject /* this */) {
    uint256 r;
    librustzcash_sapling_generate_r(r.begin());

    std::string strHex = r.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_epk(
        JNIEnv *env,
        jobject,
        jstring d,
        jstring esk) {

    uint256 res;
    // D
    std::string cpstr = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstr.length(); i += 2) {
        std::string byteString = cpstr.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }

    unsigned char* resD = new unsigned char[bytesD.size()];
    std::copy(bytesD.begin(), bytesD.end(), resD);
    // ESK
    std::string cpstrEsk = env->GetStringUTFChars(esk, NULL);
    uint256 eskunt;
    eskunt.SetHex(cpstrEsk);

    if(!librustzcash_sapling_ka_derivepublic(resD, eskunt.begin(), res.begin())) {
        std::string hello = "librustzcash_sapling_ka_derivepublic=false";
        return env->NewStringUTF(hello.c_str());
    }

    std::string strHex = res.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_test(
        JNIEnv *env,
        jobject,
        jbyteArray d) {

    // D to unsigned char array
    int lend = env->GetArrayLength (d);
    unsigned char* dbuf = new unsigned char[lend];
    env->GetByteArrayRegion (d, 0, lend, reinterpret_cast<jbyte*>(dbuf));

    std::stringstream ss;
    ss << std::hex;
    for (int i = 0; i < 11; ++i)
        ss << std::setw(2) << std::setfill('0') << (int)dbuf[i];

    std::string cpstr = ss.str();

    std::vector<char> bytes;

    for (unsigned int i = 0; i < cpstr.length(); i += 2) {
        std::string byteString = cpstr.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytes.push_back(byte);
    }

    unsigned char* res = new unsigned char[lend];
    std::copy(bytes.begin(), bytes.end(), res);
    jbyteArray array = env->NewByteArray (lend);
    env->SetByteArrayRegion (array, 0, lend, reinterpret_cast<jbyte*>(res));

    return array;
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_cm(
        JNIEnv *env,
        jobject,
        jstring d,
        jstring pkd,
        jstring v,
        jstring r) {

    // D to unsigned char array
    std::string cpstr = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstr.length(); i += 2) {
        std::string byteString = cpstr.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }
    unsigned char* resD = new unsigned char[11];
    std::copy(bytesD.begin(), bytesD.end(), resD);
    // PKD to uint256
    std::string cpstrPkd = env->GetStringUTFChars(pkd, NULL);
    uint256 pkdunt;
    pkdunt.SetHex(cpstrPkd);

    // VALUE to uint64_t
    uint64_t value;
    std::string cpstrV = env->GetStringUTFChars(v, NULL);
    std::istringstream issV(cpstrV);
    issV >> value;
    // R to uint256
    std::string cpstrR = env->GetStringUTFChars(r, NULL);
    uint256 runt;
    runt.SetHex(cpstrR);

    uint256 res;

    librustzcash_sapling_compute_cm(resD, pkdunt.begin(), value, runt.begin(), res.begin());

    std::string strHex = res.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_greeting(
        JNIEnv *env,
        jobject,
        jstring esk,
        jstring d,
        jstring pkd,
        jstring r,
        jstring v) {

    std::array<unsigned char, 192> zkproof;
    uint256 cv;

    // ESK
    std::string cpstrEsk = env->GetStringUTFChars(esk, NULL);
    std::vector<char> bytesEsk;
    for (unsigned int i = 0; i < cpstrEsk.length(); i += 2) {
        std::string byteString = cpstrEsk.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesEsk.push_back(byte);
    }
    unsigned char* resEsk = new unsigned char[32];
    std::copy(bytesEsk.begin(), bytesEsk.end(), resEsk);
    uint256 eskunt;
    eskunt.SetHex(cpstrEsk);
    // D to unsigned char array
    std::string cpstr = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstr.length(); i += 2) {
        std::string byteString = cpstr.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }
    unsigned char* resD = new unsigned char[11];
    std::copy(bytesD.begin(), bytesD.end(), resD);
    // PKD to unsigned char array
    std::string cpstrPkd = env->GetStringUTFChars(pkd, NULL);
    std::vector<char> bytesPkd;
    for (unsigned int i = 0; i < cpstrPkd.length(); i += 2) {
        std::string byteString = cpstrPkd.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesPkd.push_back(byte);
    }
    unsigned char* resPkd = new unsigned char[32];
    std::copy(bytesPkd.begin(), bytesPkd.end(), resPkd);
    uint256 pkdunt;
    pkdunt.SetHex(cpstrPkd);
    // R to unsigned char array
    std::string cpstrR = env->GetStringUTFChars(r, NULL);
    std::vector<char> bytesR;
    for (unsigned int i = 0; i < cpstrR.length(); i += 2) {
        std::string byteString = cpstrR.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesR.push_back(byte);
    }
    unsigned char* resR = new unsigned char[32];
    std::copy(bytesR.begin(), bytesR.end(), resR);
    uint256 runt;
    runt.SetHex(cpstrR);
    // VALUE to uint64_t
    uint64_t value;
    std::string cpstrV = env->GetStringUTFChars(v, NULL);
    std::istringstream issV(cpstrV);
    issV >> value;

    if (!librustzcash_sapling_output_proof(ctx, eskunt.begin(), resD, pkdunt.begin(), runt.begin(), value, cv.begin(), zkproof.begin())) {
        librustzcash_sapling_proving_ctx_free(ctx);
        return env->NewByteArray(2);
    };

    unsigned char* ztest = new unsigned char[192];
    std::copy(zkproof.begin(), zkproof.end(), ztest);
    unsigned char* cvtest = new unsigned char[32];
    std::copy(cv.begin(), cv.end(), cvtest);
    jbyteArray array = env->NewByteArray(192 + 32);
    env->SetByteArrayRegion (array, 0, 192, reinterpret_cast<jbyte*>(ztest));
    env->SetByteArrayRegion (array, 192, 32, reinterpret_cast<jbyte*>(cvtest));
    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_bsig(
        JNIEnv *env,
        jobject,
        jstring v,
        jstring dataToBeSigned) {

    std::array<unsigned char, 64> bindingSig;

    // v string to int64_t
    std::string cpstrV = env->GetStringUTFChars(v, NULL);
    int64_t value = std::stoi(cpstrV);

    // dataToBeSigned to unsigned char array
    std::string cpstrData = env->GetStringUTFChars(dataToBeSigned, NULL);
    std::vector<char> bytesData;
    for (unsigned int i = 0; i < cpstrData.length(); i += 2) {
        std::string byteString = cpstrData.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesData.push_back(byte);
    }
    unsigned char* resData = new unsigned char[32];
    std::copy(bytesData.begin(), bytesData.end(), resData);

    if (!librustzcash_sapling_binding_sig(ctx, value, resData, bindingSig.begin())) {
        librustzcash_sapling_proving_ctx_free(ctx);
        std::string hello = "librustzcash_sapling_binding_sig=false";
        env->NewByteArray(2);
    };

    // bindingSig to hex (code from uint256.cpp method GetHex())
    char psz[sizeof(bindingSig) * 2 + 1];
    for (unsigned int i = 0; i < sizeof(bindingSig); i++)
        sprintf(psz + i * 2, "%02x", bindingSig[sizeof(bindingSig) - i - 1]);

    std::string hello = std::string(psz, psz + sizeof(bindingSig) * 2);


    unsigned char* bsigtest = new unsigned char[64];
    std::copy(bindingSig.begin(), bindingSig.end(), bsigtest);

    jbyteArray array = env->NewByteArray(64);
    env->SetByteArrayRegion (array, 0, 64, reinterpret_cast<jbyte*>(bsigtest));
    return array;
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_kagree(
        JNIEnv *env,
        jobject,
        jstring epk,
        jstring ivk) {

    // EPK to uint256
    std::string cpstrEpk = env->GetStringUTFChars(epk, NULL);
    uint256 epkunt;
    epkunt.SetHex(cpstrEpk);
    // IVK to uint256
    std::string cpstrIvk = env->GetStringUTFChars(ivk, NULL);
    uint256 ivkunt;
    ivkunt.SetHex(cpstrIvk);

    uint256 dhsecret;

    if (!librustzcash_sapling_ka_agree(epkunt.begin(), ivkunt.begin(), dhsecret.begin())) {
        env->NewByteArray(3);
    }

    std::string strHex = dhsecret.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_ivkToPdk(
        JNIEnv *env,
        jobject,
        jstring ivk,
        jstring d) {

    // IVK to uint256
    std::string cpstrIvk = env->GetStringUTFChars(ivk, NULL);
    uint256 ivkunt;
    ivkunt.SetHex(cpstrIvk);

    // D to unsigned char array
    std::string cpstr = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstr.length(); i += 2) {
        std::string byteString = cpstr.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }
    unsigned char* resD = new unsigned char[11];
    std::copy(bytesD.begin(), bytesD.end(), resD);

    uint256 pk_d;

    if (!librustzcash_ivk_to_pkd(ivkunt.begin(), resD, pk_d.begin())) {
        std::string hello = "librustzcash_ivk_to_pkd=false";
        return env->NewStringUTF(hello.c_str());
    }

    std::string strHex = pk_d.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_checkout(
        JNIEnv *env,
        jobject,
        jstring cv,
        jstring cm,
        jstring ephemeralKey,
        jstring zkproof) {

    auto verctx = librustzcash_sapling_verification_ctx_init();

    // cv to unsigned char array
    std::string cpstrCv = env->GetStringUTFChars(cv, NULL);
    std::vector<char> bytesCv;
    for (unsigned int i = 0; i < cpstrCv.length(); i += 2) {
        std::string byteString = cpstrCv.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesCv.push_back(byte);
    }
    unsigned char* resCv = new unsigned char[bytesCv.size()];
    std::copy(bytesCv.begin(), bytesCv.end(), resCv);
    // cm to unsigned char array
    std::string cpstrCm = env->GetStringUTFChars(cm, NULL);
    std::vector<char> bytesCm;
    for (unsigned int i = 0; i < cpstrCm.length(); i += 2) {
        std::string byteString = cpstrCm.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesCm.push_back(byte);
    }
    unsigned char* resCm = new unsigned char[bytesCm.size()];
    std::copy(bytesCm.begin(), bytesCm.end(), resCm);
    // ephemeralKey to unsigned char array
    std::string cpstrEk = env->GetStringUTFChars(ephemeralKey, NULL);
    std::vector<char> bytesEk;
    for (unsigned int i = 0; i < cpstrEk.length(); i += 2) {
        std::string byteString = cpstrEk.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesEk.push_back(byte);
    }
    unsigned char* resEk = new unsigned char[bytesEk.size()];
    std::copy(bytesEk.begin(), bytesEk.end(), resEk);
    // zkproof to unsigned char array
    std::string cpstrZk = env->GetStringUTFChars(zkproof, NULL);
    std::vector<char> bytesZk;
    for (unsigned int i = 0; i < cpstrZk.length(); i += 2) {
        std::string byteString = cpstrZk.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesZk.push_back(byte);
    }
    unsigned char* resZk = new unsigned char[bytesZk.size()];
    std::copy(bytesZk.begin(), bytesZk.end(), resZk);

    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "cpstrZk=[%02x]...[%02x] size=[%lu]", resZk[0], resZk[bytesZk.size()], bytesZk.size());

    uint256 cvunt = uint256S(cpstrCv);

    std::uint32_t hello = librustzcash_sapling_check_output(verctx, resCv, resCm, resEk, resZk);
    librustzcash_sapling_verification_ctx_free(verctx);
    std::string s = std::to_string(hello);

    return env->NewStringUTF(s.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_merkelHash(
        JNIEnv *env,
        jobject,
        jint depth,
        jstring l,
        jstring r) {

    size_t d = depth;

    // L to uint256
    std::string cpstrL = env->GetStringUTFChars(l, NULL);
    uint256 lunt;
    lunt.SetHex(cpstrL);

    // R to uint256
    std::string cpstrR = env->GetStringUTFChars(r, NULL);
    uint256 runt;
    runt.SetHex(cpstrR);

    uint256 res;

    librustzcash_merkle_hash(d, lunt.begin(), runt.begin(), res.begin());

    std::string strHex = res.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_uncommitted(
        JNIEnv *env,
        jobject) {

    uint256 res;

    librustzcash_tree_uncommitted(res.begin());

    std::string strHex = res.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_computeNf(
        JNIEnv *env,
        jobject,
        jstring d,
        jstring pkd,
        jstring value,
        jstring r,
        jstring ak,
        jstring nk,
        jstring position) {

    // D to unsigned char array
    std::string cpstrD = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstrD.length(); i += 2) {
        std::string byteString = cpstrD.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }
    unsigned char* resD = new unsigned char[11];
    std::copy(bytesD.begin(), bytesD.end(), resD);

    // PKD to uint256
    std::string cpstrPkd = env->GetStringUTFChars(pkd, NULL);
    uint256 pkdunt;
    pkdunt.SetHex(cpstrPkd);

    // VALUE to uint64_t
    uint64_t vuint;
    std::string cpstrV = env->GetStringUTFChars(value, NULL);
    std::istringstream issV(cpstrV);
    issV >> vuint;

    // R to uint256
    std::string cpstrR = env->GetStringUTFChars(r, NULL);
    uint256 runt;
    runt.SetHex(cpstrR);

    // AK to uint256
    std::string cpstrAk = env->GetStringUTFChars(ak, NULL);
    uint256 akunt;
    akunt.SetHex(cpstrAk);

    // NK to uint256
    std::string cpstrNk = env->GetStringUTFChars(nk, NULL);
    uint256 nkunt;
    nkunt.SetHex(cpstrNk);

    // POSITION to uint64_t
    uint64_t posuint;
    std::string cpstrP = env->GetStringUTFChars(position, NULL);
    std::istringstream issP(cpstrP);
    issP >> posuint;

    uint256 result;

    if (!librustzcash_sapling_compute_nf(
            resD,
            pkdunt.begin(),
            vuint,
            runt.begin(),
            akunt.begin(),
            nkunt.begin(),
            posuint,
            result.begin()
    ))
    {
        std::string hello = "librustzcash_sapling_compute_nf=false";
        return env->NewStringUTF(hello.c_str());
    }

    std::string strHex = result.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_compactDecrypt(
        JNIEnv *env,
        jobject,
        jstring key,
        jstring ciphertext) {

    // Key to uint256
    std::string cpstrKey = env->GetStringUTFChars(key, NULL);
    uint256 keyunt;
    keyunt.SetHex(cpstrKey);

    // ciphertext to unsigned char array
    std::string cpstrCph = env->GetStringUTFChars(ciphertext, NULL);
    std::vector<char> bytesCph;
    for (unsigned int i = 0; i < cpstrCph.length(); i += 2) {
        std::string byteString = cpstrCph.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesCph.push_back(byte);
    }
    unsigned char* resCph = new unsigned char[bytesCph.size()];
    std::copy(bytesCph.begin(), bytesCph.end(), resCph);


    std::array<unsigned char, 52> result;

    if (!try_sapling_compact_note_decryption(
            keyunt.begin(),
            resCph,
            result.begin()
    ))
    {
        env->NewByteArray(3);
    }

    unsigned char* restest = new unsigned char[52];
    std::copy(result.begin(), result.end(), restest);

    jbyteArray array = env->NewByteArray(52);
    env->SetByteArrayRegion(array, 0, 52, reinterpret_cast<jbyte*>(restest));

    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_encryptNp(
        JNIEnv *env,
        jobject,
        jstring key,
        jstring ciphertext) {

    // Key to uint256
    std::string cpstrKey = env->GetStringUTFChars(key, NULL);
    uint256 keyunt;
    keyunt.SetHex(cpstrKey);

    // ciphertext to unsigned char array
    std::string cpstrCph = env->GetStringUTFChars(ciphertext, NULL);
    std::vector<char> bytesCph;
    for (unsigned int i = 0; i < cpstrCph.length(); i += 2) {
        std::string byteString = cpstrCph.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesCph.push_back(byte);
    }
    unsigned char* resCph = new unsigned char[bytesCph.size()];
    std::copy(bytesCph.begin(), bytesCph.end(), resCph);

    std::array<unsigned char, 580> result;

    encrypt_note_plaintext(keyunt.begin(), resCph, result.begin());

    unsigned char* restest = new unsigned char[580];
    std::copy(result.begin(), result.end(), restest);

    jbyteArray array = env->NewByteArray(580);
    env->SetByteArrayRegion(array, 0, 580, reinterpret_cast<jbyte*>(restest));

    return array;
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_proveContextInit(
        JNIEnv *env,
        jobject) {

    ctx = librustzcash_sapling_proving_ctx_init();

    std::string res = "librustzcash_sapling_proving_ctx_init";
    return env->NewStringUTF(res.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_spendProof(
        JNIEnv *env,
        jobject,
        jstring ak,
        jstring nsk,
        jstring d,
        jstring r,
        jstring alpha,
        jstring value,
        jstring anchor,
        jobjectArray authPathsArr,
        jbooleanArray indexesArr) {

    uint256 cv;
    uint256 rk;
    std::array<unsigned char, 192> zkproof;
    uint256 nf;

    // Ak to uint256
    std::string cpstrAk = env->GetStringUTFChars(ak, NULL);
    uint256 akunt;
    akunt.SetHex(cpstrAk);
    // Nsk to uint256
    std::string cpstrNsk = env->GetStringUTFChars(nsk, NULL);
    uint256 nskunt;
    nskunt.SetHex(cpstrNsk);
    // D to unsigned char array
    std::string cpstrD = env->GetStringUTFChars(d, NULL);
    std::vector<char> bytesD;
    for (unsigned int i = 0; i < cpstrD.length(); i += 2) {
        std::string byteString = cpstrD.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesD.push_back(byte);
    }
    unsigned char* resD = new unsigned char[11];
    std::copy(bytesD.begin(), bytesD.end(), resD);
    // R to uint256
    std::string cpstrR = env->GetStringUTFChars(r, NULL);
    uint256 runt;
    runt.SetHex(cpstrR);
    // Alpha to uint256
    std::string cpstrAlpha = env->GetStringUTFChars(alpha, NULL);
    uint256 alphaunt;
    alphaunt.SetHex(cpstrAlpha);
    // VALUE to uint64_t
    uint64_t vuint;
    std::string cpstrV = env->GetStringUTFChars(value, NULL);
    std::istringstream issV(cpstrV);
    issV >> vuint;
    // Anchor to uint256
    std::string cpstrAnchor = env->GetStringUTFChars(anchor, NULL);
    uint256 anchorunt;
    anchorunt.SetHex(cpstrAnchor);

    // Witness
    // 1. converting authPathsArr to std::vector
    std::vector<std::vector<bool>> authentication_path;

    int len1 = env -> GetArrayLength(authPathsArr);
    jbooleanArray dim=  (jbooleanArray)env->GetObjectArrayElement(authPathsArr, 0);
    int len2 = env -> GetArrayLength(dim);

    for(int i=0; i<len1; ++i){
        jbooleanArray oneDim= (jbooleanArray)env->GetObjectArrayElement(authPathsArr, i);
        jboolean *element=env->GetBooleanArrayElements(oneDim, 0);
        std::vector<bool> oneAuthVector;
        for(int j=0; j<len2; ++j) {
            oneAuthVector.push_back(element[j]);
        }
        authentication_path.push_back(oneAuthVector);
    }
    // 2. converting indexesArr to std::vector
    std::vector<bool> index;

    int len3 = env -> GetArrayLength(indexesArr);
    jboolean *element=env->GetBooleanArrayElements(indexesArr, 0);
    for(int i=0; i<len3; ++i){
        index.push_back(element[i]);
    }
    //3. serialize witness
    MerklePath merklePath = MerklePath(authentication_path, index);
    CDataStream ss(SER_NETWORK, PROTOCOL_VERSION);
    ss << merklePath;
    std::vector<unsigned char> witness(ss.begin(), ss.end());


    if (!librustzcash_sapling_spend_proof(
            ctx,
            akunt.begin(),
            nskunt.begin(),
            resD,
            runt.begin(),
            alphaunt.begin(),
            vuint,
            anchorunt.begin(),
            witness.data(),
            cv.begin(),
            rk.begin(),
            zkproof.begin(),
            nf.begin())) {
        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "librustzcash_sapling_spend_proof false");
        librustzcash_sapling_proving_ctx_free(ctx);
        return env->NewByteArray(4);
    };


    unsigned char* cvArr = new unsigned char[32];
    std::copy(cv.begin(), cv.end(), cvArr);
    unsigned char* rkArr = new unsigned char[32];
    std::copy(rk.begin(), rk.end(), rkArr);
    unsigned char* zArr = new unsigned char[192];
    std::copy(zkproof.begin(), zkproof.end(), zArr);
    unsigned char* nfArr = new unsigned char[32];
    std::copy(nf.begin(), nf.end(), nfArr);

    jbyteArray array = env->NewByteArray(32 + 32 + 192 + 32);
    env->SetByteArrayRegion (array, 0, 32, reinterpret_cast<jbyte*>(cvArr));
    env->SetByteArrayRegion (array, 32, 32, reinterpret_cast<jbyte*>(rkArr));
    env->SetByteArrayRegion (array, 64, 192, reinterpret_cast<jbyte*>(zArr));
    env->SetByteArrayRegion (array, 256, 32, reinterpret_cast<jbyte*>(nfArr));
    return array;
}

JNIEXPORT jbyte JNICALL
Java_com_guarda_zcash_RustAPI_toByteMerklePath(
        JNIEnv *env,
        jobject,
        jbyte pathByte,
        jboolean authPathBool,
        jint p) {

    unsigned char pb = static_cast<unsigned char>(pathByte);
    pb |= authPathBool << (7-(p % 8));

    signed char res = static_cast<signed char>(pb);
    return res;
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_vectorToInt(
        JNIEnv *env,
        jobject,
        jobjectArray authPathsArr,
        jbooleanArray indexesArr) {

    std::vector<std::vector<bool>> authentication_path;

    int len1 = env -> GetArrayLength(authPathsArr);
    jbooleanArray dim=  (jbooleanArray)env->GetObjectArrayElement(authPathsArr, 0);
    int len2 = env -> GetArrayLength(dim);

    for(int i=0; i<len1; ++i){
        jbooleanArray oneDim= (jbooleanArray)env->GetObjectArrayElement(authPathsArr, i);
        jboolean *element=env->GetBooleanArrayElements(oneDim, 0);
        //allocate localArray[i] using len2
        std::vector<bool> oneAuthVector;
        for(int j=0; j<len2; ++j) {
            oneAuthVector.push_back(element[j]);
        }
        authentication_path.push_back(oneAuthVector);
    }

    // converting indexesArr to std::vector
    std::vector<bool> index;

    int len3 = env -> GetArrayLength(indexesArr);
    jboolean *element=env->GetBooleanArrayElements(indexesArr, 0);
    for(int i=0; i<len3; ++i){
        index.push_back(element[i]);
    }

    MerklePath merklePath = MerklePath(authentication_path, index);
    CDataStream ss(SER_NETWORK, PROTOCOL_VERSION);
    ss << merklePath;
    std::vector<unsigned char> witness(ss.begin(), ss.end());

    unsigned char* wtnarr = new unsigned char[141];
    std::copy(witness.begin(), witness.end(), wtnarr);

    jbyteArray array = env->NewByteArray(141);
    env->SetByteArrayRegion(array, 0, 141, reinterpret_cast<jbyte*>(wtnarr));

    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_spendSig(
        JNIEnv *env,
        jobject,
        jstring ask,
        jstring alpha,
        jstring dataToBeSigned) {

    std::array<unsigned char, 64> spendAuthSig;

    // Ask to uint256
    std::string cpstrAsk = env->GetStringUTFChars(ask, NULL);
    uint256 askunt;
    askunt.SetHex(cpstrAsk);
    // Alpha to uint256
    std::string cpstrAlpha = env->GetStringUTFChars(alpha, NULL);
    uint256 alphaunt;
    alphaunt.SetHex(cpstrAlpha);

    // dataToBeSigned to unsigned char array
    std::string cpstrData = env->GetStringUTFChars(dataToBeSigned, NULL);
    std::vector<char> bytesData;
    for (unsigned int i = 0; i < cpstrData.length(); i += 2) {
        std::string byteString = cpstrData.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesData.push_back(byte);
    }
    unsigned char* resData = new unsigned char[32];
    std::copy(bytesData.begin(), bytesData.end(), resData);

    if (!librustzcash_sapling_spend_sig(
            askunt.begin(),
            alphaunt.begin(),
            resData,
            spendAuthSig.begin())) {
        librustzcash_sapling_proving_ctx_free(ctx);
        std::string hello = "librustzcash_sapling_spend_sig=false";
        env->NewByteArray(5);
    };

    // spendAuthSig to hex (code from uint256.cpp method GetHex())
    char psz[sizeof(spendAuthSig) * 2 + 1];
    for (unsigned int i = 0; i < sizeof(spendAuthSig); i++)
        sprintf(psz + i * 2, "%02x", spendAuthSig[sizeof(spendAuthSig) - i - 1]);

    std::string hello = std::string(psz, psz + sizeof(spendAuthSig) * 2);


    unsigned char* spsigtest = new unsigned char[64];
    std::copy(spendAuthSig.begin(), spendAuthSig.end(), spsigtest);

    jbyteArray array = env->NewByteArray(64);
    env->SetByteArrayRegion (array, 0, 64, reinterpret_cast<jbyte*>(spsigtest));
    return array;
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_testVerify(
        JNIEnv *env,
        jobject,
        jstring sp,
        jstring sigHash,
        jstring bsig) {

    std::string spStr = env->GetStringUTFChars(sp, NULL);

    SpendDescription spend;
    CDataStream ss(ParseHex(spStr), SER_NETWORK, PROTOCOL_VERSION);
    ss >> spend;

    std::string sigStr = env->GetStringUTFChars(sigHash, NULL);
    uint256 dataToBeSigned = uint256S(sigStr);

    auto ctx = librustzcash_sapling_verification_ctx_init();

    if (!librustzcash_sapling_check_spend(
            ctx,
            spend.cv.begin(),
            spend.anchor.begin(),
            spend.nullifier.begin(),
            spend.rk.begin(),
            spend.zkproof.begin(),
            spend.spendAuthSig.begin(),
            dataToBeSigned.begin()))
    {
        librustzcash_sapling_verification_ctx_free(ctx);
        std::string hello = "librustzcash_sapling_check_spend=false";
        return env->NewStringUTF(hello.c_str());
    };

    // bsig hex to unsigned char array
    std::string cpstrData = env->GetStringUTFChars(bsig, NULL);
    std::vector<char> bytesData;
    for (unsigned int i = 0; i < cpstrData.length(); i += 2) {
        std::string byteString = cpstrData.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesData.push_back(byte);
    }
    unsigned char* resBsig = new unsigned char[64];
    std::copy(bytesData.begin(), bytesData.end(), resBsig);

    // valueBalance
    int64_t value = 54321;

    if (!librustzcash_sapling_final_check(
            ctx,
            value,
            resBsig,
            dataToBeSigned.begin()))
    {
        librustzcash_sapling_verification_ctx_free(ctx);
        std::string hello = "librustzcash_sapling_final_check=false";
        return env->NewStringUTF(hello.c_str());
    }

    librustzcash_sapling_verification_ctx_free(ctx);

    std::string res = "testVerify completed";
    return env->NewStringUTF(res.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_guarda_zcash_RustAPI_testUint256(
        JNIEnv *env,
        jobject,
        jstring str) {

    // PKD to uint256
    std::string cpstr = env->GetStringUTFChars(str, NULL);
    uint256 res;
    res.SetHex(cpstr);

    std::string strHex = res.GetHex();
    return env->NewStringUTF(strHex.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_guarda_zcash_RustAPI_testToArr(
        JNIEnv *env,
        jobject,
        jstring str) {

    std::string cpstrCph = env->GetStringUTFChars(str, NULL);
    std::vector<char> bytesCph;
    for (unsigned int i = 0; i < cpstrCph.length(); i += 2) {
        std::string byteString = cpstrCph.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), NULL, 16);
        bytesCph.push_back(byte);
    }
    unsigned char* resCph = new unsigned char[bytesCph.size()];
    std::copy(bytesCph.begin(), bytesCph.end(), resCph);

    jbyteArray array = env->NewByteArray(52);
    env->SetByteArrayRegion (array, 0, 52, reinterpret_cast<jbyte*>(resCph));
    return array;
}

#ifdef __cplusplus
}
#endif

#endif // BITCOIN_MAIN_H