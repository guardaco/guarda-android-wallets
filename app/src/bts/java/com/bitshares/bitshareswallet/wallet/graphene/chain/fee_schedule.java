package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.asset;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

import static com.bitshares.bitshareswallet.wallet.graphene.chain.config.GRAPHENE_100_PERCENT;

public class fee_schedule {
    public static class fee_parameters {
        int nOperationId;
        Object objectFeeParametersType;
    };

    public static final int MAX_FEE_STABILIZATION_ITERATION = 4;

    private List<fee_parameters> parameters;
    private int scale = GRAPHENE_100_PERCENT; ///< fee * scale / GRAPHENE_100_PERCENT

    public static class fee_parameters_deserializer implements JsonDeserializer<fee_parameters> {
        @Override
        public fee_parameters deserialize(JsonElement json,
                                          Type typeOfT,
                                          JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            int nOperationId = jsonArray.get(0).getAsInt();
            fee_parameters parameters = new fee_parameters();

            parameters.nOperationId = nOperationId;
            Type typeFee = operations.operations_map.getOperationFeeObjectById(nOperationId);
            JsonElement jsonElement = jsonArray.get(1);
            if (typeFee != null) {
                parameters.objectFeeParametersType = context.deserialize(jsonElement, typeFee);
            } else {
                parameters.objectFeeParametersType = context.deserialize(jsonElement, Object.class);
            }

            return parameters;
        }
    }

    public asset calculate_fee(operations.operation_type operationType, price core_exchange_rate) {
        fee_parameters targetParam = null;
        for (fee_parameters param : parameters) {
            if (param.nOperationId == operationType.nOperationType) {
                targetParam = param;
                break;
            }
        }

        operations.base_operation operationBase = (operations.base_operation)operationType.operationContent;
        long lFee = operationBase.calculate_fee(targetParam.objectFeeParametersType);
        BigInteger bigFee = BigInteger.valueOf(lFee);
        BigInteger bigScale = BigInteger.valueOf(scale);
        BigInteger bigScaled = bigFee.multiply(bigScale);
        BigInteger bigDefault = BigInteger.valueOf(GRAPHENE_100_PERCENT);
        // // TODO: 07/09/2017 需要确保 FC_ASSERT( scaled <= GRAPHENE_MAX_SHARE_SUPPLY );

        long lResult = bigScaled.divide(bigDefault).longValue();

        asset assetResult = new asset(lResult, new object_id<asset_object>(0, asset_object.class)).multipy(core_exchange_rate);

        while (assetResult.multipy(core_exchange_rate).amount < lResult) {
            assetResult.amount++;
        }

        return assetResult;

    }

    public asset set_fee(operations.operation_type operationType, price core_exchange_rate) {
        asset assetFee = calculate_fee(operationType, core_exchange_rate);

        asset assetFeeMax = assetFee;
        operations.base_operation operationBase = (operations.base_operation)operationType.operationContent;
        for (int i = 0; i < MAX_FEE_STABILIZATION_ITERATION; ++i) {
            operationBase.set_fee(assetFee);
            asset assetNewFee = calculate_fee(operationType, core_exchange_rate);
            if (assetFee.amount == assetNewFee.amount) {
                break;
            }
            assetFee = assetNewFee;

            if (assetFeeMax.amount < assetNewFee.amount) {
                assetFeeMax = assetNewFee;
            }
        }
        return assetFeeMax;
    }
}
