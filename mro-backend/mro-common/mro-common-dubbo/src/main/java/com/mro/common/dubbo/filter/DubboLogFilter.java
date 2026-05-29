package com.mro.common.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Slf4j
@Activate(group = {CONSUMER, PROVIDER})
public class DubboLogFilter implements Filter {

    //private static final Logger log = LoggerFactory.getLogger(DubboLogFilter.class);
    private static final String ATT_REQUEST_ID = "requestId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String iface = invoker.getInterface().getSimpleName();
        String method = invocation.getMethodName();
        boolean isConsumer = RpcContext.getServiceContext().isConsumerSide();
        long start = System.currentTimeMillis();

        if (isConsumer) {
            // Consumer 端：将 MDC requestId 传递给 Provider
            String requestId = MDC.get(ATT_REQUEST_ID);
            if (requestId != null) {
                RpcContext.getClientAttachment().setAttachment(ATT_REQUEST_ID, requestId);
            }
            log.debug("DUBBO-OUT {}.{}", iface, method);
        } else {
            // Provider 端：从 Attachment 读取 requestId，放入 MDC
            String requestId = RpcContext.getServerAttachment().getAttachment(ATT_REQUEST_ID);
            if (requestId != null) {
                MDC.put(ATT_REQUEST_ID, requestId);
            }
            log.debug("DUBBO-IN  {}.{}", iface, method);
        }

        try {
            Result result = invoker.invoke(invocation);
            long cost = System.currentTimeMillis() - start;

            if (result.hasException()) {
                log.warn("DUBBO-ERR {}.{} cost={}ms ex={}",
                        iface, method, cost, result.getException().getMessage());
            } else {
                log.debug("DUBBO-OUT {}.{} cost={}ms", iface, method, cost);
            }
            return result;
        } catch (RpcException e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("DUBBO-ERR {}.{} cost={}ms ex={}", iface, method, cost, e.getMessage());
            throw e;
        } finally {
            if (!isConsumer) {
                MDC.remove(ATT_REQUEST_ID);
            }
        }
    }
}
