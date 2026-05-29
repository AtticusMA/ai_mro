package com.mro.web.module.sys.app;

import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.DeptDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private DeptDubboService deptDubboService;

    public List<DeptTreeDTO> getDeptTree() {
        return deptDubboService.getDeptTree();
    }

    public DeptDTO getDeptById(Long id) {
        return deptDubboService.getDeptById(id);
    }

    public void createDept(CreateDeptCommand cmd) {
        deptDubboService.createDept(cmd);
    }

    public void updateDept(UpdateDeptCommand cmd) {
        deptDubboService.updateDept(cmd);
    }

    public void deleteDept(Long id) {
        deptDubboService.deleteDept(id);
    }
}
