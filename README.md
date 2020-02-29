# Android SPV wallets by [Guarda](https://guarda.co)
All Guarda single-currency wallets are available in open-source. 
The wallets are designed to store, manage, transfer and receive cryptocurrency. The applications also include an option to purchase coins with a banking card and top up wallets via the built-in Exchange service.

Guarda does not store any personal information, wallet data or private keys of our clients. The private key is stored in the mobile device’s secure memory and deletes itself if the user logs out from the wallet. In order to secure the funds, a backup of the private key or the json backup feature are created. A pin code access is also available.

![Guarda Android SPV wallets](guarda_mobile_apps.png?raw=true "Guarda Android SPV wallets")

## How to install
Read the detailed instruction [here](https://guarda.freshdesk.com/support/solutions/articles/36000095874-how-to-install-a-guarda-open-source-android-wallet).
## About
Cryptocurrency wallets support:
- ZEC Shielded (Z-addresses, mainnet) - [/APKs/zec-shielded-mainnet.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/zec-shielded-mainnet.apk)

[![Available on Google Play](https://guarda.co/assets/images/home-poster-play.png)](https://play.google.com/store/apps/details?id=guarda.shielded)
- ZEC Shielded (Z-addresses, testnet) - [/APKs/zec-shielded-testnet.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/zec-shielded-testnet.apk)
- BTC (Bitcoin) - [/APKs/btc-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/btc-release.apk)
- ETH (Ethereum) - [/APKs/eth-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/eth-release.apk)
- BCH (Bitcoin Cash) - [/APKs/bch-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/bch-release.apk)
- LTC (Litecoin) - [/APKs/ltc-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/ltc-release.apk)
- IOTA (Iota) - [/APKs/iota-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/iota-release.apk)
- ETC (Ethereum Classic) - [/APKs/etc-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/etc-release.apk)
- BTG (Bitcoin Gold) - [/APKs/btg-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/btg-release.apk)
- QTUM (Qtum) - [/APKs/qtum-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/qtum-release.apk)
- BTS (BitShares) - [/APKs/bts-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/bts-release.apk)
- DGB (DigiByte) - [/APKs/dgb-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/dgb-release.apk)
- KMD (Komodo) - [/APKs/kmd-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/kmd-release.apk)
- CLO (Callisto) - [/APKs/clo-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/clo-release.apk)
- DCT (Decent) - [/APKs/dct-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/dct-release.apk)
- SBTC (Super Bitcoin) - [/APKs/sbtc-release.apk](https://github.com/guardaco/guarda-android-wallets/blob/master/APKs/sbtc-release.apk)

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
