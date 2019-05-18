package com.guarda.zcash.sapling.api;

import android.util.Log;

import com.guarda.zcash.sapling.db.AppDb;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.tree.SaplingMerkleTree;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.inject.Inject;

import cash.z.wallet.sdk.rpc.CompactFormats;
import cash.z.wallet.sdk.rpc.CompactTxStreamerGrpc;
import cash.z.wallet.sdk.rpc.Service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import timber.log.Timber;

public class ProtoApi {

//    public long pageNum = 252500;
//    public long pageNum = 282732;
//    public long pageNum = 280000;
//    public long pageNum = 478034;
//    public long pageNum = 451320;
//    public long pageNum = 464563;
//    public long pageNum = 421059;
    public long pageNum = 437489;
    private ManagedChannel channel = null;

    @Inject
    DbManager dbManager;

    public ProtoApi() {
//        String host = "lightwalletd.z.cash";
        String host = "10.88.66.3";
//        int port = 9067;
        int port = 7878;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    public Boolean gB(long fBlock, long tBlock) {
        Timber.d("ProtoApi getBlocks started");
        Iterator<CompactFormats.CompactBlock> l = null;
        try {
            CompactTxStreamerGrpc.CompactTxStreamerBlockingStub stub = CompactTxStreamerGrpc.newBlockingStub(channel);

            Service.BlockID from = Service.BlockID.newBuilder().setHeight(fBlock).build();
            Service.BlockID to = Service.BlockID.newBuilder().setHeight(tBlock).build();
            Service.BlockRange br = Service.BlockRange.newBuilder().setStart(from).setEnd(to).build();

            l = stub.getBlockRange(br);
        } catch (Exception e) {
            Timber.e("gB from=" + fBlock + " to=" + tBlock + " e=" + e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            return false;
        }
        pageNum = tBlock + 1;

        Timber.d("ProtoApi getBlocks done");

        CompactFormats.CompactBlock cb = null;
        try {
            while (l.hasNext()) {
                cb = l.next();
                if (cb.getVtxCount() == 0) continue;
                Timber.d("checkBlocks block height=" + cb.getHeight());
                dbManager.addBlockWithTxs(cb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("protoApi.checkBlocks(items); from=" + fBlock + " to=" + tBlock + " e=" + e.getMessage());
            // sometimes we get UNKNOWN: context deadline exceeded from server
            // https://github.com/zcash-hackworks/lightwalletd/blob/51614ecd2bff7595114c30a70eaf3c7488af12dd/frontend/service.go#L107
            // workaround: recall previous block, so we set last block number
            if (cb != null)
                pageNum = cb.getHeight();

            return false;
        }

        return true;
    }

    public long getLastBlock() {
        Service.BlockID latest;
        try {
            CompactTxStreamerGrpc.CompactTxStreamerBlockingStub stub = CompactTxStreamerGrpc.newBlockingStub(channel);
            Service.ChainSpec empty = Service.ChainSpec.newBuilder().build();

            latest = stub.getLatestBlock(empty);
            return latest.getHeight();
        } catch (Exception e) {
            Timber.e("getLastBlock e=%s", e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            return 0;
        }
    }

}
