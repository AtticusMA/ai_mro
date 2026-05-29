package com.mro.web.module.sys.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.DictDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private DictDubboService dictDubboService;

    public PageResult<DictDTO> listDicts(DictQueryParam param) {
        return dictDubboService.listDicts(param);
    }

    public List<DictItemDTO> getDictByGroup(String dictGroup) {
        return dictDubboService.getDictByGroup(dictGroup);
    }

    public void createDict(CreateDictCommand cmd) {
        dictDubboService.createDict(cmd);
    }

    public void updateDict(UpdateDictCommand cmd) {
        dictDubboService.updateDict(cmd);
    }

    public void deleteDict(Long id) {
        dictDubboService.deleteDict(id);
    }
}
