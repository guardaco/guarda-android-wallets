# Android SPV wallets by [Guarda](https://guarda.co)
## How to install
Here is detailed instruction [link](https://guarda.freshdesk.com/support/solutions/articles/36000095874-how-to-install-a-guarda-open-source-android-wallet).
## About
Cryptocurrency wallets support:
- BTC (Bitcoin) - [/APKs/btc-release-v0.32-code-32.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/btc-release-v0.32-code-32.apk)
- ETH (Ethereum) - [/APKs/eth-release-v0.62-code-74.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/eth-release-v0.62-code-74.apk)
- BCH (Bitcoin Cash) - [/APKs/bch-release-v0.19-code-19.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/bch-release-v0.19-code-19.apk)
- LTC (Litecoin) - [/APKs/ltc-release-v0.17-code-17.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/ltc-release-v0.17-code-17.apk)
- IOTA (Iota) - [/APKs/iota-release-v0.19-code-19.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/iota-release-v0.19-code-19.apk)
- ETC (Ethereum Classic) - [/APKs/etc-release-v0.59-code-46.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/etc-release-v0.59-code-46.apk)
- ZEC (Zcash) - [/APKs/zec-release-v0.18-code-18.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/zec-release-v0.18-code-18.apk)
- BTG (Bitcoin Gold) - [/APKs/btg-release-v0.36-code-36.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/btg-release-v0.36-code-36.apk)
- QTUM (Qtum) - [/APKs/qtum-release-v0.20-code-20.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/qtum-release-v0.20-code-20.apk)
- BTS (BitShares) - [/APKs/bts-release-v0.13-code-13.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/bts-release-v0.13-code-13.apk)
- DGB (DigiByte) - [/APKs/dgb-release-v0.8-code-8.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/dgb-release-v0.8-code-8.apk)
- KMD (Komodo) - [/APKs/kmd-release-v0.17-code-17.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/kmd-release-v0.17-code-17.apk)
- CLO (Callisto) - [/APKs/clo-release-v0.23-code-23.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/clo-release-v0.23-code-23.apk)
- DCT (Decent) - [/APKs/dct-release-v0.25-code-25.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/dct-release-v0.25-code-25.apk)
- SBTC (Super Bitcoin) - [/APKs/sbtc-release-v0.23-code-23.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/sbtc-release-v0.23-code-23.apk)

## Do not see your coin?
See below (pull-request section)

## Installation
Just download a last version of APK-file from a link in About section and install on your device.
Click the link and then ```Download``` button or right click on the link and choose ```Save link as...```.

Or build it yourself:
1. Download and install Java 7 or up (https://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. Download and Install the latest Android studio (https://developer.android.com/studio/)
3. Clone or download the project
4. Clean\Rebuild\Build if needed
5. Open Build Variants and choose a wallet. For example “btcDebug” (all variants described in ```app/build.gradle```).
6. For build on your device (or emulator) tap Run.
7. For build APK-file select menu Build -> Build APK(s)

## Pull-requests
If you would like to contribute code you can do so through GitHub by forking the repository and sending a pull request.

If you want to add your own coin you need to discover of this files:
```app/src/btc/java/com/guarda/ethereum/managers/WalletManager.java``` - for BTC-like
```app/src/eth/java/com/guarda/ethereum/managers/WalletManager.java``` - for ETH-like

In other cases you need to follow the existing API of ```WalletManager.java``` and implement methods for creating and restore wallet, generating a public address and creating a transaction (or something else specific for your coin).

If you have any question create an [issue](https://github.com/guardaco/guarda-android-wallets/issues/new).

## License
Library are licensed under the [GPL-3.0 License](https://github.com/guardaco/guarda-android-wallets/blob/master/LICENSE).

Enjoy! Guarda Team hopes you will like using our wallets as much as we liked creating them.
