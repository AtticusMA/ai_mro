package com.mro.web.module.workcard.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.workcard.response.QualificationDTO;
import com.mro.web.module.workcard.app.QualificationAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationAppService qualificationAppService;

    @GetMapping
    public R<PageResult<QualificationDTO>> listQualifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(qualificationAppService.listQualifications(pageNum, pageSize));
    }

    @GetMapping("/match")
    public R<PageResult<QualificationDTO>> matchQualifications(
            @RequestParam(required = false) Long workcardId,
            @RequestParam(required = false) String aircraftType,
            @RequestParam(required = false) String cardType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(qualificationAppService.matchQualifications(
                workcardId, aircraftType, cardType, pageNum, pageSize));
    }
}
