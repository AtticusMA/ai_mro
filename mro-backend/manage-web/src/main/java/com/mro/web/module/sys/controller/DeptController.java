package com.mro.web.module.sys.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.system.request.CreateDeptCommand;
import com.mro.common.dubbo.system.response.DeptDTO;
import com.mro.common.dubbo.system.response.DeptTreeDTO;
import com.mro.common.dubbo.system.request.UpdateDeptCommand;
import com.mro.web.module.sys.app.DeptAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/dept")
@Validated
public class DeptController {

    @Autowired
    private DeptAppService deptAppService;

    @GetMapping("/tree")
    public R<List<DeptTreeDTO>> getDeptTree() {
        return R.ok(deptAppService.getDeptTree());
    }

    @GetMapping("/{id}")
    public R<DeptDTO> getDeptById(@PathVariable Long id) {
        return R.ok(deptAppService.getDeptById(id));
    }

    @PostMapping("/")
    public R<Void> createDept(@Valid @RequestBody CreateDeptCommand cmd) {
        deptAppService.createDept(cmd);
        return R.ok();
    }

    @PutMapping("/")
    public R<Void> updateDept(@Valid @RequestBody UpdateDeptCommand cmd) {
        deptAppService.updateDept(cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteDept(@PathVariable Long id) {
        deptAppService.deleteDept(id);
        return R.ok();
    }
}
