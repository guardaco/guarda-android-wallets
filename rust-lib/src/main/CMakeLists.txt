# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.6)

project(CMakeRustLib)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")

enable_language(Rust)
include(CMakeCargo)

#include_directories("include")
#add_subdirectory(zapling-lib)

add_library(native-lib SHARED cpp/main.cpp)
find_library(log-lib log)

# Boost location may vary per machine and it may help to download and install the required version
#set(BOOST_ROOT /usr/local/Cellar/boost/boost169)
#set(Boost_INCLUDE_DIR ${BOOST_ROOT})
#set(BOOST_LIBRARYDIR ${BOOST_ROOT}/libs)
#set(Boost_USE_STATIC_LIBS   ON)
#find_package(Boost 1.69.0 REQUIRED)
#include_directories(${BOOST_ROOT})

target_link_libraries(
        native-lib
#        zapling-lib
        ${log-lib}
        android
#        ${Boost_LIBRARYDIR}
)
