# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.6)

project(CMakeRustSample)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")

enable_language(Rust)
include(CMakeCargo)

include_directories("include")
add_subdirectory(zapling-lib)

add_library(native-lib SHARED cpp/main.cpp cpp/uint256.cpp cpp/utilstrencodings.cpp)
find_library(log-lib log)

SET(BOOST_ROOT /usr/local/Cellar/boost/boost169)
SET(Boost_INCLUDE_DIR /usr/local/Cellar/boost/boost169)
SET(BOOST_LIBRARYDIR /usr/local/Cellar/boost/boost169/lib)
set(Boost_USE_STATIC_LIBS   ON)
find_package(Boost 1.69.0 REQUIRED)
include_directories(${BOOST_ROOT})

target_link_libraries(
        native-lib
        zapling-lib
        ${log-lib}
        android
        ${Boost_LIBRARYDIR}
)
