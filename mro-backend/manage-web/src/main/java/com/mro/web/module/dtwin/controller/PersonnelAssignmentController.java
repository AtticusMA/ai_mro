package com.mro.web.module.dtwin.controller;

import com.mro.common.core.response.R;
import com.mro.common.dubbo.dtwin.request.SaveAssignmentCommand;
import com.mro.common.dubbo.dtwin.response.PersonnelAssignmentDTO;
import com.mro.web.module.dtwin.app.PersonnelAssignmentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人员排班接口
 * Refs: MRO-005
 */
@RestController
@RequestMapping("/api/dtwin/assignments")
@RequiredArgsConstructor
public class PersonnelAssignmentController {

    private final PersonnelAssignmentAppService personnelAssignmentAppService;

    @GetMapping
    public R<List<PersonnelAssignmentDTO>> listAssignments(@RequestParam Long packageId) {
        return R.ok(personnelAssignmentAppService.listAssignmentsByPackage(packageId));
    }

    @PostMapping
    public R<Long> saveAssignment(@RequestBody SaveAssignmentCommand cmd) {
        return R.ok(personnelAssignmentAppService.saveAssignment(cmd));
    }
}
