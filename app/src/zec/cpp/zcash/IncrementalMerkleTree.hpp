#ifndef ZC_INCREMENTALMERKLETREE_H_
#define ZC_INCREMENTALMERKLETREE_H_

//#include <array>
//#include <deque>
//#include <boost/optional.hpp>
//#include <boost/static_assert.hpp>

//#include "../uint256.h"
//#include "../serialize.h"

//#include "Zcash.h"
//#include "util.h"

class MerklePath {
public:
    std::vector<std::vector<bool>> authentication_path;
    std::vector<bool> index;

    ADD_SERIALIZE_METHODS;

    template <typename Stream, typename Operation>
    inline void SerializationOp(Stream& s, Operation ser_action) {
        std::vector<std::vector<unsigned char>> pathBytes;
        std::uint64_t indexInt;
        if (ser_action.ForRead()) {
            READWRITE(pathBytes);
            READWRITE(indexInt);
            MerklePath &us = *(const_cast<MerklePath*>(this));
            for (size_t i = 0; i < pathBytes.size(); i++) {
                us.authentication_path.push_back(convertBytesVectorToVector(pathBytes[i]));
                us.index.push_back((indexInt >> ((pathBytes.size() - 1) - i)) & 1);
            }
        } else {
            assert(authentication_path.size() == index.size());
            pathBytes.resize(authentication_path.size());
            for (size_t i = 0; i < authentication_path.size(); i++) {
                pathBytes[i].resize((authentication_path[i].size()+7)/8);
                for (unsigned int p = 0; p < authentication_path[i].size(); p++) {
                    pathBytes[i][p / 8] |= authentication_path[i][p] << (7-(p % 8));
                }
            }
            indexInt = convertVectorToInt(index);
            READWRITE(pathBytes);
            READWRITE(indexInt);
        }
    }

    MerklePath() { }

    MerklePath(std::vector<std::vector<bool>> authentication_path, std::vector<bool> index)
    : authentication_path(authentication_path), index(index) { }

    // Convert bytes into boolean vector. (MSB to LSB)
    std::vector<bool> convertBytesVectorToVector(const std::vector<unsigned char>& bytes) {
        std::vector<bool> ret;
        ret.resize(bytes.size() * 8);

        unsigned char c;
        for (size_t i = 0; i < bytes.size(); i++) {
            c = bytes.at(i);
            for (size_t j = 0; j < 8; j++) {
                ret.at((i*8)+j) = (c >> (7-j)) & 1;
            }
        }

        return ret;
    }

    // Convert boolean vector (big endian) to integer
    uint64_t convertVectorToInt(const std::vector<bool>& v) {
        if (v.size() > 64) {
            throw std::length_error ("boolean vector can't be larger than 64 bits");
        }

        uint64_t result = 0;
        for (size_t i=0; i<v.size();i++) {
            if (v.at(i)) {
                result |= (uint64_t)1 << ((v.size() - 1) - i);
            }
        }

        return result;
    }
};

#endif /* ZC_INCREMENTALMERKLETREE_H_ */
