package com.guarda.zcash.sapling.api;

import com.guarda.ethereum.GuardaApp;
import com.guarda.zcash.crypto.Utils;
import com.guarda.zcash.sapling.db.DbManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.inject.Inject;

import autodagger.AutoInjector;
import cash.z.wallet.sdk.rpc.CompactFormats;
import cash.z.wallet.sdk.rpc.CompactTxStreamerGrpc;
import cash.z.wallet.sdk.rpc.Service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import timber.log.Timber;

@AutoInjector(GuardaApp.class)
public class ProtoApi {

    public long pageNum = 551912; //height
    private ManagedChannel channel;

    @Inject
    DbManager dbManager;

    public ProtoApi() {
        GuardaApp.getAppComponent().inject(this);
//        String host = "zec-lightwallet.guarda.co"; //9067 //mainnet
        String host = "34.68.177.238"; //9067 //testnet
        int port = 9067;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    public Boolean gB(long fBlock, long tBlock) {
        Timber.d("ProtoApi getBlocks started");
        Iterator<CompactFormats.CompactBlock> l;
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
                Timber.d("checkBlocks block height=%d", cb.getHeight());
                dbManager.addBlockWithTxs(cb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("protoApi.checkBlocks(items); from=" + fBlock + " to=" + tBlock + " e=" + e.getMessage());
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

    public void getTestTxByBlockHeight(long blockHeight) {
        Service.RawTransaction latest;
        try {
            CompactTxStreamerGrpc.CompactTxStreamerBlockingStub stub = CompactTxStreamerGrpc.newBlockingStub(channel);
            Service.BlockID blockID = Service.BlockID.newBuilder().setHeight(blockHeight).build();
            Service.TxFilter empty = Service.TxFilter.newBuilder().setBlock(blockID).build();

            latest = stub.getTransaction(empty);
            Timber.d("getTestTx data=%s", Utils.bytesToHex(latest.toByteArray()));
        } catch (Exception e) {
            Timber.e("getTestTx e=%s", e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
        }
    }

}
