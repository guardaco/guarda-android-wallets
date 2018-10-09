package com.guarda.ethereum.utils;

import android.content.Context;
import android.util.Log;

import com.guarda.ethereum.models.items.ContractUnspentOutput;
import com.guarda.ethereum.utils.sha3.Keccak;
import com.guarda.ethereum.utils.sha3.Parameters;

import org.bitcoinj.core.Address;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import java.util.ArrayList;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContractBuilder {

    private String hashPattern = "0000000000000000000000000000000000000000000000000000000000000000"; //64b

    private final int radix = 16;
    private final String TYPE_INT = "int";
    private final String TYPE_STRING = "string";
    private final String TYPE_ADDRESS = "address";
    private final String TYPE_BOOL = "bool";

    private final String ARRAY_PARAMETER_CHECK_PATTERN = ".*?\\d+\\[\\d*\\]";
    private final String ARRAY_PARAMETER_TYPE = "(.*?\\d+)\\[(\\d*)\\]";

    final int OP_PUSHDATA_1 = 1;
    final int OP_PUSHDATA_4 = 0x04;
    final int OP_PUSHDATA_8 = 8;
    final int OP_EXEC = 193;
    final int OP_EXEC_ASSIGN = 194;
    final int OP_EXEC_SPEND = 195;


    public ContractBuilder() {
    }


    private boolean parameterIsArray(ContractMethodParameter contractMethodParameter) {
        Pattern p = Pattern.compile(ARRAY_PARAMETER_CHECK_PATTERN);
        Matcher m = p.matcher(contractMethodParameter.getType());
        return m.matches();
    }



    public String createAbiMethodParams(final String _methodName, final List<ContractMethodParameter> contractMethodParameterList) {

        String methodName = _methodName;
        String parameters = "";
        String abiParams = "";
        if (contractMethodParameterList != null && contractMethodParameterList.size() != 0) {
            for (ContractMethodParameter parameter : contractMethodParameterList) {
                abiParams += convertParameter(parameter);
                parameters = parameters + parameter.getType() + ",";
            }
            methodName = methodName + "(" + parameters.substring(0, parameters.length() - 1) + ")";
        } else {
            methodName = methodName + "()";
        }
        Keccak keccak = new Keccak();
        String hashMethod = keccak.getHash(Hex.toHexString((methodName).getBytes()), Parameters.KECCAK_256).substring(0, 8);
        abiParams = hashMethod + abiParams;
        return abiParams;

    }


    long paramsCount;

    private String convertParameter(ContractMethodParameter parameter) {
        String _value = parameter.getValue();
        if (!parameterIsArray(parameter)) {
            if (parameter.getType().contains(TYPE_INT)) {
                return appendNumericPattern(convertToByteCode(new BigInteger(_value)));
            } else if (parameter.getType().contains(TYPE_STRING)) {
                return getStringOffset(parameter);
            } else if (parameter.getType().contains(TYPE_ADDRESS) && _value.length() == 34) {
                byte[] decode = Base58.decode(_value);
                String toHexString = Hex.toHexString(decode);
                String substring = toHexString.substring(2, 42);
                return appendAddressPattern(substring);
            } else if (parameter.getType().contains(TYPE_ADDRESS)) {
                return getStringOffset(parameter);
            } else if (parameter.getType().contains(TYPE_BOOL)) {
                return appendBoolean(_value);
            }
        } else {
            return getStringOffset(parameter);
        }
        return "";
    }

    private String appendBoolean(String parameter) {
        return Boolean.valueOf(parameter) ? appendNumericPattern("1") : appendNumericPattern("0");
    }

    private String convertToByteCode(long _value) {
        return Long.toString(_value, radix);
    }

    private String convertToByteCode(BigInteger _value) {
        return _value.toString(radix);
    }

    long currStringOffset = 0;

    private String getStringOffset(ContractMethodParameter parameter) {
        long currOffset = ((paramsCount + currStringOffset) * 32);
        currStringOffset = getStringHash(parameter.getValue()).length() / hashPattern.length() + 1;
        return appendNumericPattern(convertToByteCode(currOffset));
    }

    private String getStringHash(String _value) {
        if (_value.length() <= hashPattern.length()) {
            return formNotFullString(_value);
        } else {
            int ost = _value.length() % hashPattern.length();
            return _value + hashPattern.substring(0, hashPattern.length() - ost);
        }
    }

    private String appendAddressPattern(String _value) {
        return hashPattern.substring(_value.length()) + _value;
    }

    private String formNotFullString(String _value) {
        return _value + hashPattern.substring(_value.length());
    }

    private String appendNumericPattern(String _value) {
        return hashPattern.substring(0, hashPattern.length() - _value.length()) + _value;
    }

    public Script createMethodScript(String abiParams, int gasLimitInt, int gasPriceInt, String _contractAddress) throws RuntimeException {
        byte[] version = Hex.decode("04000000");
        byte[] arrayGasLimit = org.spongycastle.util.Arrays.reverse((new BigInteger(String.valueOf(gasLimitInt))).toByteArray());
        byte[] gasLimit = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(arrayGasLimit, 0, gasLimit, 0, arrayGasLimit.length);
        byte[] arrayGasPrice = org.spongycastle.util.Arrays.reverse((new BigInteger(String.valueOf(gasPriceInt))).toByteArray());
        byte[] gasPrice = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(arrayGasPrice, 0, gasPrice, 0, arrayGasPrice.length);
        byte[] data = Hex.decode(abiParams);
        byte[] contractAddress = Hex.decode(_contractAddress);
        byte[] program;
        ScriptChunk versionChunk = new ScriptChunk(OP_PUSHDATA_4, version);
        ScriptChunk gasLimitChunk = new ScriptChunk(OP_PUSHDATA_8, gasLimit);
        ScriptChunk gasPriceChunk = new ScriptChunk(OP_PUSHDATA_8, gasPrice);
        ScriptChunk dataChunk = new ScriptChunk(ScriptOpCodes.OP_PUSHDATA2, data);
        ScriptChunk contactAddressChunk = new ScriptChunk(ScriptOpCodes.OP_PUSHDATA2, contractAddress);
        ScriptChunk opExecChunk = new ScriptChunk(OP_EXEC_ASSIGN, null);
        List<ScriptChunk> chunkList = new ArrayList<>();
        chunkList.add(versionChunk);
        chunkList.add(gasLimitChunk);
        chunkList.add(gasPriceChunk);
        chunkList.add(dataChunk);
        chunkList.add(contactAddressChunk);
        chunkList.add(opExecChunk);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (ScriptChunk chunk : chunkList) {
                chunk.write(bos);
            }
            program = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Script(program);
    }

    public String createTransactionHash(Script script, List<ContractUnspentOutput> unspentOutputs,
                                        int gasLimit, int gasPrice, BigDecimal feePerKb,
                                        String feeString, Context context,
                                        NetworkParameters netParams, Address ownAddress, Wallet wallet,
                                        String description, DeterministicKey ownDeterKey)  throws Exception{

        Transaction transaction = new Transaction(netParams);

        /*add description*/
        if (!android.text.TextUtils.isEmpty(description)){
            try {
                byte[] descriptionsBytes = description.getBytes("UTF-8");

                /*OP_RETURN with  message limit is 80 bytes*/
                if (descriptionsBytes.length <= 70) {
                    transaction.addOutput(Coin.ZERO,
                            ScriptBuilder.createOpReturnScript(descriptionsBytes));
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        BigDecimal fee = new BigDecimal(feeString);
        BigDecimal gasFee = (new BigDecimal(gasLimit)).multiply(new BigDecimal(gasPrice)).divide(new BigDecimal(100000000), MathContext.DECIMAL128);
        BigDecimal totalAmount = fee.add(gasFee);
        BigDecimal amountFromOutput = new BigDecimal("0.0");
        BigDecimal overFlow = new BigDecimal("0.0");
        BigDecimal bitcoin = new BigDecimal(100000000);

        transaction.addOutput(Coin.ZERO, script);

        for (ContractUnspentOutput unspentOutput : unspentOutputs) {
            overFlow = overFlow.add(unspentOutput.getAmount());
            if (overFlow.doubleValue() >= totalAmount.doubleValue()) {
                break;
            }
        }
        if (overFlow.doubleValue() < totalAmount.doubleValue()) {
            throw new RuntimeException("You have insufficient funds for this transaction");
        }
        BigDecimal delivery = overFlow.subtract(totalAmount);

        if (delivery.doubleValue() != 0.0) {
            transaction.addOutput(Coin.valueOf((long) (delivery.multiply(bitcoin).doubleValue())), ownAddress);
        }

        for (ContractUnspentOutput unspentOutput : unspentOutputs) {
            //for (ECKey ecKey : wallet.getIssuedReceiveKeys()) {
            {
                ECKey ecKey = ownDeterKey;
                if (ecKey.toAddress(netParams).toString().equals(unspentOutput.getAddress())) {
                    Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(unspentOutput.getTxHash()));
                    TransactionOutPoint outPoint = new TransactionOutPoint(netParams, unspentOutput.getVout(), sha256Hash);
                    Script script2 = new Script(Utils.parseAsHexOrBase58(unspentOutput.getTxoutScriptPubKey()));
                    transaction.addSignedInput(outPoint, script2, ecKey, Transaction.SigHash.ALL, true);
                    amountFromOutput = amountFromOutput.add(unspentOutput.getAmount());
                    break;
                }
            }
            if (amountFromOutput.doubleValue() >= totalAmount.doubleValue()) {
                break;
            }
        }
        transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
        transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);
        byte[] bytes = transaction.unsafeBitcoinSerialize();
        int txSizeInkB = (int) Math.ceil(bytes.length / 1024.);
        BigDecimal minimumFee = (feePerKb.multiply(new BigDecimal(txSizeInkB)));
        if (minimumFee.doubleValue() > fee.doubleValue()) {
            throw new RuntimeException("insufficient funds");
        }
        return Hex.toHexString(bytes);
    }

}

