package com.guarda.zcash

import androidx.test.core.app.ApplicationProvider
import com.guarda.ethereum.sapling.tree.SaplingMerkleTree
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import work.samosudov.rustlib.RustAPI
import java.util.*

class RustAPITest {

    companion object {
        val note = byteArrayOf(1, 49, -56, 16, 72, -122, 103, 46, 68, 17, 99, 117, -96, 105, 117, 0, 0, 0, 0, 0, 76, -96, 22, 106, 90, -59, 124, 1, -55, 48, 96, 10, -60, 49, -63, 1, 15, -41, -72, -75, -104, -57, 42, 47, 3, 54, -24, -65, -62, -52, 77, 14)
        val key = byteArrayOf(69, 121, -20, 127, -124, -3, 114, -88, 7, -102, 47, 22, -12, -31, -92, -93, -41, 62, 32, -6, -80, -24, 115, 22, 115, -83, -91, -43, 98, 103, -54, -15)
    }

    @Before
    fun initNativeLibrary() {
        try {
            RustAPI.init(ApplicationProvider.getApplicationContext())
        } catch (e: Exception) {
            println("RustAPI.init e=${e.message}")
        }
    }

    @Test
    fun testKdf() {
        val res2 = RustAPI.kdfSapling(ByteArray(32), ByteArray(32))
        println("testKdf res2 = ${Arrays.toString(res2)}")
        // res1 = [64, 80, -35, 4, 81, 3, -32, 1, 98, -119, 50, 67, -101, 58, 116, 98, -71, 88, -76, 119, -72, 117, 26, -113, 126, 68, 18, 76, -31, -121, -114, 15]
    }

    @Test
    fun testPrfOck() {
        val res2 = RustAPI.prfOck(ByteArray(32), ByteArray(32), ByteArray(32), ByteArray(32))
        println("testPrfOck res2 = ${Arrays.toString(res2)}")
//        testPrfOck res1 = [-128, 53, 125, -116, -71, -93, 54, -120, 9, -106, -115, 123, -42, -98, -78, -79, -70, -57, -13, -37, -30, -80, 89, -24, -60, 100, 120, 62, 79, -103, -107, -99]
    }

    @Test
    fun encryptNp() {
        val res1 = RustAPI.encryptNp(key, note + ByteArray(512))
        println("res1 = ${Arrays.toString(res1)}")
    }

    @Test
    fun testConvertAddress() {
        val expected = byteArrayOf(-68, -62, 86, 122, -85, -75, 57, 69, -45, 77, -87, 75, 103, 10, -92, -49, -51, -104, 53, 83, 112, -120, 102, 1, 93, 43, -47, 41, 120, 106, 100, 100, -31, -20, 53, -21, -89, -30, 46, 80, -78, -110, 63, -47, -4, -17, 78, -58, 121, -16, 24, -114, 38, 69, -81, -46, 56, -65, -66, 55, 102, 75, 90, 120, -103, 37, 109, 78, 35, 90, -25, 60, -31, -71, -74, -97, -74, 89, 47, -105, 103, 85, -101, 63, 85, -72, -66, -10, -112, -122, -85, 70, -86, -60, -31, 69, 11, 77, 124, -8, -16, 23, 115, 3, -27, -98, 94, 94, -66, -28, -114, -93, -64, 22, 54, 70, -125, 59, -66, 56, 103, -7, 94, 48, 61, -79, -98, 58, -75, 16, -94, -4, -10, -66, -22, -76, -73, 60, -72, 73, -83, 115, -74, 7, 78, -92, 39, 1, -111, -24, -67, 125, -50, 67, -68, 108, -72, -89, 14, 118, 100, -54, -126, -62, -53, 45, -85, -9, 101, 9, -116, 31, 30, -107, 1, -98, -10, 85, -52, 62, -53, 107, -8, -82, 100, -89, -91, -3, -39, 76, 59, -127, -38, -125, -5, -9, -39, 102, 10, 43, -93, 59, 57, -2, 69, -37, -33, -105, 42, 41, -79, 51, -30, 22, 127, 30, -86, -15, 59, 7, 85, -98, 82, 110, -37, -90, 39, 20, -101, -126, -127, -98, 118, 113, -41, -27, 3, -66, 93, -34, -118, -27, 80, -84, -118, -100, 7, 6, 107, 55, 39, -34, -128, -118, -99, -93, 40, -66, -84, -66, 52, 103, 90, -90, -45, 9, -84, 121, -98, -109, -46, 117, 97, 58, 20, -16, 85, 77, 97, 49, 68, -23, -82, -5, 73, 22, 65, -87, -50, 12, -64, -99, -75, 103, -52, -11, 71, 7, -80, -77, -46, -63, 106, -31, -53, -33, 83, -58, -48, 79, 55, 21, -79, -75, 13, -34, 101, -18, 40, -71, 85, 62, 2, -73, 21, -23, 126, 63, -8, -87, -24, -74, -72, -72, 41, 80, 87, -102, -24, -34, -76, -37, -21, -24, 43, 81, -50, 1, 41, 74, 62, 117, -19, -34, 65, 69, 68, 92, -59, -107, -83, -10, 66, -93, 107, 98, -119, -60, 92, -33, -33, 83, 26, 39, -51, 122, -43, -69, -31, -43, -19, -60, 5, 97, 111, 115, 57, 100, 1, 26, -61, -114, 74, -89, 12, 20, 36, -13, -92, -47, -124, 11, 91, -89, -120, 107, -126, 9, 48, -115, -9, -111, 90, -3, 116, 32, -114, -47, 47, 26, 105, -18, -58, 97, 23, 24, 33, 89, 80, 118, 114, 36, 107, -119, -94, -118, 2, 68, -54, -16, -124, 127, -74, 8, -54, -54, -99, -55, -3, -87, -26, -87, -109, 91, -38, -72, -33, -37, -39, 122, 79, 76, -79, 118, 41, 50, 93, 60, -7, -44, -53, -34, 76, -74, -50, -2, 6, 57, 0, -14, 109, 55, 99, 87, 105, -108, 81, -38, 49, -39, 99, -83, 45, -75, 31, -21, 6, -30, 96, 96, 39, -74, 30, 53, 67, -40, 81, -114, 44, -62, 93, 71, 46, -90, 117, -65, -52, -85, 8, -30, -117, 75, 91, -68, -98, 21, 98, -18, 110, 103, 97, -109, 28, -109, 20, 81, 55, -38, 107, 117, -127, -63, -114, 57, -1, 53, 120, 71, 25, -73, -38, 57, -7, -110, 25, 83, -42, -13, -89, 17, 127, -68, 50, -50, -113, -83, 68, 75, 64, -7, -89, 60, 11, 65, -126, 41, 63, -81, 95, 0)
        val res = RustAPI.checkConvertAddr("ztestsapling12rc54wchrdvp0tlw096c96fa7uu0546e90jfh74k40lgrsjl7z6a9vf5cgr7fxkf08pdj28d0h2")

        println("res=${Arrays.toString(res)}")
        assert(res.contentEquals(expected))
    }

    @Test
    fun root() {
        val treeOnHeight880000main = "0101fd38eb0e85b3db6b0d45d61c9f47cae61ed7825d61caf33254662f5d12f06100120001e55f4b8f204a727959afede5044cae6b5b2486ffa5af5127a844a9a644ce8731019b3907b294b1e8b75ae59c9c359ca4cb1c56334074baea34c76c6555f1f9c20c000180f6205c75c9bf538f9a5a135a9aaf52cf4349484af4ea888ad116e3312116200127c4f4c5235428ef2f2bba0b50dcc997f5ff3f1280c4232c45410391e01b0817000001c0bbfea92a974909670d22a36e58a5161fcc878f593e0bf5927210c7655dc2720104e65868ae3a4ac40cbe908477b57d0988ae22e935b9767ca87f37d96c18514c0104a146b26bc71de0dde7b7c617272dce6d00b0700dabd1813a1cf14c4c63624f0001dac3bfe63198c1675d1660edb9854c9189cba02b3d510605c9fe518e79fcbb500001166bb2e71749ab956e549119ce9099df3dbb053409ff89d0d86a17d5b02d015d0000011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight890000main = "0144a6a550c31c8804ea65b0a44c1dffb9a6f22bfdba8482090d6adb4797b1703d01986a4a8fd8fdc40be2f10fe8d8287d4386ea6f6d6eb1174ada233874d390773c12013e74d3cf7dbcdcc0e81d31e8e19feb9be9b87075342de699d4215eeee065bd7000000001b8199c8da17fc8c9f86ab4c5896c6b2d1ce34499203c5940ca194a4edededa5000016f0b14fc490c5bb5eb77d59bf82345f0da4988df051b9b03be23ed2d9e42254e0001d622d9e5d20fb58c5ad14a6f5b7a7abc6a5c3511bcef438be480c9caf8a2a72a00010eb6f9b986b46f428c4bd697d8ba0158185ff27dcc1b1c9f2e503352034a360a000001d3ddf137881180d7c5fd73d188c4346291168bde688643855eb3cd5f680f9c0001166bb2e71749ab956e549119ce9099df3dbb053409ff89d0d86a17d5b02d015d0000011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight900000main = "0162267b975eb64d5e602e9bfa63c56b1326e8eab6fa971847f2d4f8f27d26f90101cc33a70cde4e4e5f5347034f44b503e8660c7380e596961ccab15d7a64044371120140d08a6c2958e7283c3e95053eeb3e5cf502d5857a961e107c8b37ae00dcb30700019f7e8cf60fb1b31d852d6786fe74078ff1cae30fd982ed2ee11176f96103622301d3f9f17d782359f08d500309ded4ddc1488c86906f3bd742aa95b8de0f189524000001b64361426e35478691ef23c34cf977afab4962c0d7d1998b08ccac635149524201a73426649efdd3a6bda5f8a253a4823f8558dc16d8042ba071f13c0575a3955d0181e81df06e97ade7989e0ce8790f75f1d34e6cfeb4347205c4aaa7adf90a180e0001ecd87912a28cfbe4f34bf57bad9827e4013d8d4c8e85c48bfdd06c35eade9920010d393867f4f2bebf9f603bf827dd015aaf3a46dfa8b68d86cdce07aa4fbb97240001d3ddf137881180d7c5fd73d188c4346291168bde688643855eb3cd5f680f9c0001166bb2e71749ab956e549119ce9099df3dbb053409ff89d0d86a17d5b02d015d0000011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight915000main = "018032a417e482233d0f7b514ef9cdcc561712a1c140b0f1dbf2019648aca3bf3900120190a448936550d025fb97989caa0a1ee5ce38299feb4016ad28e5abfaeba76e0b0000000001b3890c3b8b48fc8780ebf7fc3ac87f3cd8a3c773ab384bea0241f0d1e804ba680001774061907b95272e0de7514a2e983ee9ee6cef53d30a16743c1738f508ec741f000173182028e0ae6bad062e6436bc8ba057e13807d9cfb0196b5211f7859be2275501ee9cc1a73505042d7c904b8548a6ff5026c2918a954d191723e2f6fe1b83a60b0133604c7020edaefee31d4a419a4067ccd09d369a43fe4c032eeb7081774ed53901390f92d515d7c479d7c9807949f237c50bc77a732f0853884f12d01b72a5e75401d3ddf137881180d7c5fd73d188c4346291168bde688643855eb3cd5f680f9c0001166bb2e71749ab956e549119ce9099df3dbb053409ff89d0d86a17d5b02d015d0000011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight930000main = "012ee9cbe55d438e5fdf9fa2df34a989514d038fc50d5913303221392dc3b33257014c2cabd5f58b23406cbcde4034d5b62c3023f30ea9a0ea5ffafb66fb7e4f000e120116683075450cb081d897a78d86224019865ce7e3ead189e74286be307f43ac5c00000001b9da9a7344e04ad7be9cfbc57c89e840703264f5692060a5ea4f7c2e201f5e42000001a3db46e41c1c7e1026ee8b49b9a148f53b00a9c728f72b996c2dc8fafbe4cb2c0001bc597d37e5f726d22ddbe544885571ac89f5fc1746c3e7e198d0bbe85b77196a011439e56aa582ec419f2a0004222be95ea1762876ea33912c5830ab4e8bdc02660163a8ebc9ee4b289becb230ef62829a8c8b4820fcd11409f0b4c849190155a65f00000001f1c57245fff8dbc2d3efe5a0953eafdedeb06e18a3ad4f1e4042ee76623f803200011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight940000main = "01dd59466cdc00ee8ceb305d15c25ef32aa2d96b7e4536e071c1d14e67aee9ee0b001200000001defc97883745ef01fbca65d4e5c0f8e22b7714b704a1c36fe8a851fdbfe3a1160178cf44f19dda025a44490dd37b96b83758d04cfa3b26476e129173e7c70cb103000167b3485f4aefed9426fc301de1138fedf6949fc806c54acc2b86d897cb6a2041000000000103e2fcbb87cd5fb954d55d094943fbac487ecda1384a8fa7dbf61097a9755e54014104c9c36ff1a2e7eda524840463de4e2c02f10412a33dd754564d76f458c525000001f1c57245fff8dbc2d3efe5a0953eafdedeb06e18a3ad4f1e4042ee76623f803200011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight950000main = "018b0ff026a6c3d70e5592d7b42fa829ec41c5a86faa2d12cbbcdf81e039cbee5301f4decddef8e3183dac4de8d1b9aa3918d7df9293aae3c1931f99a253149fb65312019066138ebf0e6cfefbe9cd7876aada7d3f27834d604f827b56b7a56e1a7b551d0000018d080dcdf356c41f5a86d2062ca9e617965faae9b9a7ea678d3799f526a353130001a5ba52f3a03ce38d7d48afc3d2c9e6d65e90dba09b53ad3d3a5921267375de5d016b84dd3e9242073d7069f797bc81a4ccbe0d3847a3b3b4d1376d9e6d61446605015d52a3c83f77ec5c8e823143e74569c0040e1cb58d2a170ce907ae010ba29c670001be5b81f3b1b8a035bd0894392b0b78bdae4c57f206d3875b933de545f65bd93b000000018e1d474609c9c09894638a0ab3e656aadccaf7ddf12bcc6b6ece44a4cc79e1140001f1c57245fff8dbc2d3efe5a0953eafdedeb06e18a3ad4f1e4042ee76623f803200011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"
        val treeOnHeight975000main = "01c53be5d35790982aedce43627d98bf86ea040ca63022cc0ad3046cf8a5493e4500120000015ab117209e3077bb3f8e3664f4e8a9614201dbebe3c5347a745898c44cae9b1a000000011c51e9b2425e3e9aab3b693199455a1e0dd45584f9f0bc3c6b9b088b5244cc0f0000000001e286f4540652505262d06a4fa2c0eddff6e0cff3883492a6164cd2d7fe3d531501733a18e135d6b0a0e61a0fbd88445b858a1646dd2026e871fcafb820f3cc235a000110b2bfd32a99e0b982a41a6dbaebf783bdb9d6af795f5f20056ce7317d15ce1101f1c57245fff8dbc2d3efe5a0953eafdedeb06e18a3ad4f1e4042ee76623f803200011323ddf890bfd7b94fc609b0d191982cb426b8bf4d900d04709a8b9cb1a27625"

        val root880000main = "8bc94d39cb99196e8316bf3a496df8fac9a8b7eae23e12d0dbfb296c7d36561e"
        val root890000main = "2aa5b9d84077c9ed1a1842a676b242df46540c1f0c3fb700355f1528ad10d01e"
        val root900000main = "160d96aed74401e0e4bc9d330bf6367e838613b5ae25a1926e4dfa8a8183ae66"
        val root915000main = "9f5ebb121597366a08b766e305392ccf7413b7d2dad614d95af59e932c15c173"
        val root930000main = "b78159ced943591f467b83eb2a10841033115df8adf811819d021484836a0a5f"
        val root940000main = "21ea7737fe7aaecfaf23cf69c19e5dd347869ed0e6f5ed688ff773e6faa03d04"
        val root950000main = "9bfbba8398edd44bff79c76dc430b60e2a98d928bf0edfbbc9fa2427735c8f6a"
        val root975000main = "36b7213b2187b7388c62ebe8b3899ee836d9cfb07a232d039627417bb676a348"

        val saplingTree880000 = SaplingMerkleTree(treeOnHeight880000main)
        assertEquals(saplingTree880000.root(), root880000main)

        val saplingTree890000 = SaplingMerkleTree(treeOnHeight890000main)
        assertEquals(saplingTree890000.root(), root890000main)

        val saplingTree900000 = SaplingMerkleTree(treeOnHeight900000main)
        assertEquals(saplingTree900000.root(), root900000main)

        val saplingTree915000 = SaplingMerkleTree(treeOnHeight915000main)
        println("sapling tree saplingTree915000 root=${saplingTree915000.root()}")
        assertEquals(saplingTree915000.root(), root915000main)

        val saplingTree930000 = SaplingMerkleTree(treeOnHeight930000main)
        println("sapling tree saplingTree930000 root=${saplingTree930000.root()}")
        assertEquals(saplingTree930000.root(), root930000main)

        val saplingTree940000 = SaplingMerkleTree(treeOnHeight940000main)
        println("sapling tree saplingTree940000 root=${saplingTree940000.root()}")
        assertEquals(saplingTree940000.root(), root940000main)

        val saplingTree950000 = SaplingMerkleTree(treeOnHeight950000main)
        println("sapling tree saplingTree950000 root=${saplingTree950000.root()}")
        assertEquals(saplingTree950000.root(), root950000main)

        val saplingTree975000 = SaplingMerkleTree(treeOnHeight975000main)
        println("sapling tree saplingTree975000 root=${saplingTree975000.root()}")
        assertEquals(saplingTree975000.root(), root975000main)
    }
}