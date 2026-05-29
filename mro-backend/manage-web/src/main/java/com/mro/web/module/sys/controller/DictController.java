package com.mro.web.module.sys.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.web.module.sys.app.DictAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/dict")
@Validated
public class DictController {

    @Autowired
    private DictAppService dictAppService;

    @GetMapping("/page")
    public R<PageResult<DictDTO>> listDicts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String dictGroup,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        DictQueryParam param = new DictQueryParam(pageNum, pageSize, dictGroup, keyword, status);
        return R.ok(dictAppService.listDicts(param));
    }

    @GetMapping("/group/{dictGroup}")
    public R<List<DictItemDTO>> getDictByGroup(@PathVariable String dictGroup) {
        return R.ok(dictAppService.getDictByGroup(dictGroup));
    }

    @PostMapping("/")
    public R<Void> createDict(@Valid @RequestBody CreateDictCommand cmd) {
        dictAppService.createDict(cmd);
        return R.ok();
    }

    @PutMapping("/")
    public R<Void> updateDict(@Valid @RequestBody UpdateDictCommand cmd) {
        dictAppService.updateDict(cmd);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteDict(@PathVariable Long id) {
        dictAppService.deleteDict(id);
        return R.ok();
    }
}
