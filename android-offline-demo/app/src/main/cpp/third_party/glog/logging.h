// Minimal glog compatibility layer for Android
// Maps glog macros to Android logging

#ifndef GLOG_LOGGING_H_
#define GLOG_LOGGING_H_

#include <sstream>
#include <string>

#ifdef __ANDROID__
#include <android/log.h>
#include <cstdlib>

// Define log levels
#define LOG_TAG "FunASR"

// Map glog LOG() macros to Android log
#define LOG(severity) AndroidLog##severity()

// Helper classes for streaming
class AndroidLogInfo {
public:
    AndroidLogInfo() {}
    ~AndroidLogInfo() {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%s", stream_.str().c_str());
    }
    template<typename T>
    AndroidLogInfo& operator<<(const T& value) {
        stream_ << value;
        return *this;
    }
private:
    std::ostringstream stream_;
};

class AndroidLogWarning {
public:
    AndroidLogWarning() {}
    ~AndroidLogWarning() {
        __android_log_print(ANDROID_LOG_WARN, LOG_TAG, "%s", stream_.str().c_str());
    }
    template<typename T>
    AndroidLogWarning& operator<<(const T& value) {
        stream_ << value;
        return *this;
    }
private:
    std::ostringstream stream_;
};

class AndroidLogError {
public:
    AndroidLogError() {}
    ~AndroidLogError() {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", stream_.str().c_str());
    }
    template<typename T>
    AndroidLogError& operator<<(const T& value) {
        stream_ << value;
        return *this;
    }
private:
    std::ostringstream stream_;
};

class AndroidLogFatal {
public:
    AndroidLogFatal() {}
    ~AndroidLogFatal() {
        __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "%s", stream_.str().c_str());
        abort();  // Fatal errors should terminate
    }
    template<typename T>
    AndroidLogFatal& operator<<(const T& value) {
        stream_ << value;
        return *this;
    }
private:
    std::ostringstream stream_;
};

// CHECK macros
#define CHECK(condition) \
    if (!(condition)) LOG(FATAL) << "Check failed: " #condition " "

#define CHECK_EQ(val1, val2) CHECK((val1) == (val2))
#define CHECK_NE(val1, val2) CHECK((val1) != (val2))
#define CHECK_LE(val1, val2) CHECK((val1) <= (val2))
#define CHECK_LT(val1, val2) CHECK((val1) < (val2))
#define CHECK_GE(val1, val2) CHECK((val1) >= (val2))
#define CHECK_GT(val1, val2) CHECK((val1) > (val2))

#else
// Non-Android platforms - stub implementation
#include <iostream>
#define LOG(severity) std::cerr
#define CHECK(condition) if (!(condition)) std::cerr << "Check failed: " #condition
#endif

#endif  // GLOG_LOGGING_H_
