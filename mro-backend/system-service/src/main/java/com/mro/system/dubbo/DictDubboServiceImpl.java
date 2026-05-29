package com.mro.system.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateDictCommand;
import com.mro.common.dubbo.system.response.DictDTO;
import com.mro.common.dubbo.system.response.DictItemDTO;
import com.mro.common.dubbo.system.request.DictQueryParam;
import com.mro.common.dubbo.system.request.UpdateDictCommand;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.DictDubboService;
import com.mro.system.service.DictService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class DictDubboServiceImpl implements DictDubboService {

    @Autowired
    private DictService dictService;

    @Override
    public PageResult<DictDTO> listDicts(DictQueryParam param) {
        return dictService.listDicts(param);
    }

    @Override
    public List<DictItemDTO> getDictByGroup(String dictGroup) {
        return dictService.getDictByGroup(dictGroup);
    }

    @Override
    public Long createDict(CreateDictCommand cmd) {
        Long operatorId = getOperatorId();
        return dictService.createDict(cmd, operatorId);
    }

    @Override
    public void updateDict(UpdateDictCommand cmd) {
        Long operatorId = getOperatorId();
        dictService.updateDict(cmd, operatorId);
    }

    @Override
    public void deleteDict(Long id) {
        Long operatorId = getOperatorId();
        dictService.deleteDict(id, operatorId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Long getOperatorId() {
        String v = RpcContext.getServiceContext().getAttachment("userId");
        return v != null ? Long.parseLong(v) : 1L;
    }
}
