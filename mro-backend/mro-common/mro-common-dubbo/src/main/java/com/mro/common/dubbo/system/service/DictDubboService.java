package com.mro.common.dubbo.system.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateDictCommand;
import com.mro.common.dubbo.system.request.UpdateDictCommand;
import com.mro.common.dubbo.system.request.DictQueryParam;
import com.mro.common.dubbo.system.response.DictDTO;
import com.mro.common.dubbo.system.response.DictItemDTO;

import java.util.List;

public interface DictDubboService {

    PageResult<DictDTO> listDicts(DictQueryParam param);

    List<DictItemDTO> getDictByGroup(String dictGroup);

    Long createDict(CreateDictCommand cmd);

    void updateDict(UpdateDictCommand cmd);

    void deleteDict(Long id);
}
