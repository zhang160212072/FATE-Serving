package com.webank.ai.fate.serving.guest.provider;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.rpc.core.*;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  主要兼容host为1.2.x版本的接口
 *
 **/
@FateService(name ="singleInference",  preChain= {
        "guestSingleParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
      },postChain = {
        "defaultPostProcess"
})
@Service
@Deprecated
public class OldVersionInferenceProvider extends AbstractServingServiceProvider<InferenceRequest,ReturnResult>{

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Value("${inference.single.timeout:3000}")
    long timeout;

    @Override
    public ReturnResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {
        Model  model = ((ServingServerContext)context).getModel();
        Preconditions.checkArgument(model!=null);
        ModelProcessor modelProcessor = model.getModelProcessor();
        InferenceRequest inferenceRequest = (InferenceRequest) inboundPackage.getBody();
        ListenableFuture<Proxy.Packet> future = federatedRpcInvoker.async(context, inferenceRequest, Dict.FEDERATED_INFERENCE);
        ReturnResult returnResult = modelProcessor.guestInference(context, inferenceRequest, future,timeout);
        return returnResult;
    }

//    private BatchInferenceRequest convertToBatchInferenceRequest(Context context, InferenceRequest inferenceRequest) {
//        BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
//        batchInferenceRequest.setServiceId(context.getServiceId());
//        batchInferenceRequest.setApplyId(context.getApplyId());
//        batchInferenceRequest.setCaseId(inferenceRequest.getCaseid());
//        batchInferenceRequest.setSeqNo(inferenceRequest.getSeqno());
//        List<BatchInferenceRequest.SingleInferenceData> dataList = new ArrayList<>();
//        BatchInferenceRequest.SingleInferenceData data = new BatchInferenceRequest.SingleInferenceData();
//        data.setIndex(0);
//        data.setFeatureData(inferenceRequest.getFeatureData());
//        data.setSendToRemoteFeatureData(inferenceRequest.getSendToRemoteFeatureData());
//        dataList.add(data);
//        batchInferenceRequest.setDataList(dataList);
//        return batchInferenceRequest;
//    }
//
//    private HostFederatedParams buildHostFederatedParams(Context context, InferenceRequest inferenceRequest) {
//        Model model = context.getModel();
//        HostFederatedParams hostFederatedParams = new HostFederatedParams();
//        hostFederatedParams.setCaseId(inferenceRequest.getCaseid());
//        hostFederatedParams.setSeqNo(inferenceRequest.getSeqno());
//        if (inferenceRequest.getSendToRemoteFeatureData() != null && hostFederatedParams.getFeatureIdMap() != null) {
//            hostFederatedParams.getFeatureIdMap().putAll(inferenceRequest.getSendToRemoteFeatureData());
//        }
//        if (inferenceRequest.getFeatureData() != null && hostFederatedParams.getFeatureIdMap() != null) {
//            hostFederatedParams.getFeatureIdMap().putAll(inferenceRequest.getFeatureData());
//        }
//        hostFederatedParams.setLocal(new FederatedParty(model.getFederationModel().getRole(), model.getFederationModel().getPartId()));
//        hostFederatedParams.setPartnerLocal(new FederatedParty(model.getRole(), model.getPartId()));
//        FederatedRoles federatedRoles = new FederatedRoles();
//        List<String> guestPartyIds = new ArrayList<>();
//        guestPartyIds.add(model.getPartId());
//        federatedRoles.setRole(model.getRole(), guestPartyIds);
//        List<String> hostPartyIds = new ArrayList<>();
//        hostPartyIds.add(model.getFederationModel().getPartId());
//        federatedRoles.setRole(model.getFederationModel().getRole(), hostPartyIds);
//        hostFederatedParams.setRole(federatedRoles);
//        hostFederatedParams.setPartnerModelInfo(new ModelInfo(model.getTableName(), model.getNamespace()));
//        return hostFederatedParams;
//    }

//
//    @Override
//    protected ReturnResult transformErrorMap(Context context, Map data) {
//        return null;
//    }
}