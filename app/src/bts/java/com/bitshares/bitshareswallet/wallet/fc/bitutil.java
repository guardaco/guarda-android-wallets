package com.bitshares.bitshareswallet.wallet.fc;


public class bitutil {
    public static long endian_reverse_u64( long x )
    {
        return (((x >> 0x38) & 0xFF)        )
                | (((x >> 0x30) & 0xFF) << 0x08)
                | (((x >> 0x28) & 0xFF) << 0x10)
                | (((x >> 0x20) & 0xFF) << 0x18)
                | (((x >> 0x18) & 0xFF) << 0x20)
                | (((x >> 0x10) & 0xFF) << 0x28)
                | (((x >> 0x08) & 0xFF) << 0x30)
                | (((x        ) & 0xFF) << 0x38)
                ;
    }

    public static int endian_reverse_u32( int x )
    {
        return (((x >> 0x18) & 0xFF)        )
                | (((x >> 0x10) & 0xFF) << 0x08)
                | (((x >> 0x08) & 0xFF) << 0x10)
                | (((x        ) & 0xFF) << 0x18)
                ;
    }
}
