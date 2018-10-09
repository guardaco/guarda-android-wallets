package com.guarda.ethereum.models.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by samosudovd on 26/02/2018.
 */

public interface Const {
    String FRESHCHAT_APP_ID = "78b70e5b-301b-401f-b2be-3d8ca542453a";
    String FRESHCHAT_APP_KEY = "eb6b67fc-8fec-4d0f-b1b5-a8d19d543d04";

    int SANDBOX_COINIFY_PARTNER_ID = 37;
    int PROD_COINIFY_PARTNER_ID = 59;

    long CHANGELLY_TIMEOUT = 2 * 60 * 1000;

    Map<String, String> COIN_TO_RETURN_ADDRESS = new HashMap<String, String>() {{
        put("ZRX", "");
        put("ANT", "");
        put("REP", "");
        put("BNT", "");
        put("BAT", "");
        put("BTC", "1Kt5hf6gqm9BaoewxczqcPnboH6TzB1knZ");
        put("BCH", "qzpcrzfpnqnklrth6wj45ecyldmtjs63hsdslsaj74");
        put("BTG", "GL2C9dUT3B3kXqj9G4zGGoGbtgSehMt1fB");
        put("BLK", "");
        put("CVC", "");
        put("CLAM", "xDgTowuXcicKEoCRUFtQDHwEjq7ptiTWrR");
        put("DASH", "XkrbFpeqrmxkwU8ZmkdMBWYx9Q3upi8hn3");
        put("DCR", "");
        put("DGB", "DAupzZQKFKEjeXMqnsfUt7QQqriK4AVwKj");
        put("DNT", "");
        put("DOGE", "DMgobcsxqPCAptmkrpkk5YydFktd2rDsEt");
        put("EDG", "");
        put("EOS", "");
        put("ETH", "0x910d816bb0b4a2629e77ccb2c0b5fca8ff5eaf22");
        put("ETC", "0x6e9aeace921e166e046a4fd3f185d2a442b2ce60");
        put("FCT", "");
        put("1ST", "");
        put("GAME", "GdKPudjUJkHnuKJ5cRpNtsi3VaQxZ1ofrD");
        put("GNO", "");
        put("GNT", "");
        put("RLC", "");
        put("KMD", "");
        put("LBC", "bDr8AsH944TB69y9bZRkWA1ZXrwiLbJSFR");
        put("LTC", "LaMFN4tm2aFaXmWKXNGH4CKTnaUoPcziRy");
        put("GUP", "");
        put("MONA", "M84UBkE9th4FbiXwMVZcXEe9gf4VEkPe9n");
        put("NEO", "");
        put("NMC", "NCexPDzCx7h8qzmhdkXKFVd5w92yNVFvom");
        put("NMR", "");
        put("NVC", "4JMzyZKKH5sJs7PUqucLTHaZLEdg3NUmSG");
        put("OMG", "");
        put("POT", "PQUzYkzVWRozMfsLAxzA1RXHR5wKXZi6LV");
        put("PPC", "PWYLKJZrjry9NrCjMg8p7PwBbiYR5fVVJN");
        put("RCN", "");
        put("RDD", "RdDsVbW3vMVRJ74QMjMyRCveBUP9nqX3kR");
        put("SALT", "");
        put("SC", "");
        put("START", "");
        put("SNT", "");
        put("STORJ", "");
        put("SWT", "");
        put("VOX", "VRor9B16Y8UCaVSMVG5sa2oQRkEW69sinx");
        put("VTC", "Vpg5DLCn19aTcTQJxuErEXfv2aTRxzxeJU");
        put("TRST", "");
        put("USNBT", "B88K3eAoJs4DXHLPM5NjAFVFJiLuyAXT6s");
        put("WINGS", "");
        put("WINGS", "t1R2sgsitXLpxm59CBqvAn7pxftUCGAKexD");
    }};
}
