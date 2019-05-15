# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

#cmake_minimum_required(VERSION 3.10.2)
cmake_minimum_required(VERSION 3.6)

# For Android Studio we will force generated toolchain
#if(NATIVELIB_ANDROID_STUDIO)
#    set(gen_toolchain "${CMAKE_CURRENT_BINARY_DIR}/generated/toolchain.cmake")
#    file(TO_CMAKE_PATH "${CMAKE_TOOLCHAIN_FILE}" CMAKE_TOOLCHAIN_FILE_CMAKE_PATH)
#    configure_file(
#            "${CMAKE_CURRENT_LIST_DIR}/cmake/template/toolchain.cmake.in"
#            "${gen_toolchain}"
#            @ONLY
#    )
#    set(CMAKE_TOOLCHAIN_FILE "${gen_toolchain}" CACHE PATH "" FORCE)
#endif()


#cmake_minimum_required(VERSION 3.0)
# * https://github.com/hunter-packages/gate
# * https://github.com/ruslo/hunter

#include("cmake/HunterGate.cmake")
#HunterGate(
#        URL "https://github.com/ruslo/hunter/archive/v0.23.94.tar.gz"
#        SHA1 "3f4c104df81f1c4a627f4b0ea9f3306de1b0b54b"
#)

project(CMakeRustSample)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")

enable_language(Rust)
include(CMakeCargo)

include_directories("include")
add_subdirectory(test-lib)

#add_executable(helloworld src/main/cpp/main.cpp)
add_library(native-lib SHARED cpp/main.cpp cpp/uint256.cpp cpp/utilstrencodings.cpp)
find_library(log-lib log)

# If CMAKE_DEBUG_POSTFIX is emtpy Hunter will initialize with 'd'.
# We have to explicitly set DEBUG_POSTFIX to simplify library
# load from Java code.
#set_target_properties(native-lib PROPERTIES DEBUG_POSTFIX "")

#hunter_add_package(Boost COMPONENTS system filesystem)
#find_package(Boost CONFIG REQUIRED system filesystem)
SET(BOOST_ROOT /usr/local/Cellar/boost/boost169)
SET(Boost_INCLUDE_DIR /usr/local/Cellar/boost/boost169)
SET(BOOST_LIBRARYDIR /usr/local/Cellar/boost/boost169/lib)
set(Boost_USE_STATIC_LIBS   ON)
find_package(Boost 1.69.0 REQUIRED)
include_directories(${BOOST_ROOT})

target_link_libraries(
        native-lib
        test-lib
        ${log-lib}
        android
        ${Boost_LIBRARYDIR}
#        Boost::filesystem
#        Boost::system # Should be last
        # Boost 1.66.0, Linux, Clang with -stdlib=libstdc++:
        # * https://travis-ci.org/ingenue/hunter/jobs/318140416#L2674
)
