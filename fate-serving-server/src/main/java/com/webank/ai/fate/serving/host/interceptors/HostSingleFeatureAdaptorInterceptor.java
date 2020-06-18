package com.webank.ai.fate.serving.host.interceptors;

import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.adaptor.dataaccess.AbstractSingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.common.interceptors.AbstractInterceptor;
import com.webank.ai.fate.serving.core.adaptor.SingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.FeatureDataAdaptorException;
import com.webank.ai.fate.serving.core.exceptions.HostGetFeatureErrorException;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class HostSingleFeatureAdaptorInterceptor extends AbstractInterceptor<InferenceRequest, ReturnResult> implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(HostSingleFeatureAdaptorInterceptor.class);
    SingleFeatureDataAdaptor singleFeatureDataAdaptor = null;
    @Override
    public void doPreProcess(Context context, InboundPackage<InferenceRequest> inboundPackage, OutboundPackage<ReturnResult> outboundPackage) throws Exception {
        if (singleFeatureDataAdaptor == null) {
            throw new FeatureDataAdaptorException("adaptor not found");
        }
        InferenceRequest inferenceRequest = inboundPackage.getBody();
        ReturnResult singleFeatureDataAdaptorData = singleFeatureDataAdaptor.getData(context, inboundPackage.getBody().getSendToRemoteFeatureData());
        if (singleFeatureDataAdaptorData == null) {
            throw new HostGetFeatureErrorException("adaptor return null");
        }
        if (!StatusCode.SUCCESS.equals(singleFeatureDataAdaptorData.getRetcode())) {
            throw new HostGetFeatureErrorException("adaptor return code is invalid {}", singleFeatureDataAdaptorData.getRetcode());
        }
        inferenceRequest.getFeatureData().putAll(singleFeatureDataAdaptorData.getData());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String adaptorClass = MetaInfo.FEATURE_SINGLE_ADAPTOR;
        if (StringUtils.isNotEmpty(adaptorClass)) {
            logger.info("try to load adaptor {}", adaptorClass);
            singleFeatureDataAdaptor = (SingleFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
            ((AbstractSingleFeatureDataAdaptor) singleFeatureDataAdaptor).setEnvironment(environment);
            try {
                singleFeatureDataAdaptor.init();
            } catch (Exception e) {
                logger.error("single adaptor init error");
            }
            logger.info("single adaptor class is {}", adaptorClass);
        }
    }

}


