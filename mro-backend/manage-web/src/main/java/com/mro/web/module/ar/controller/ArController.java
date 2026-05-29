package com.mro.web.module.ar.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import com.mro.web.module.ar.app.ArAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ar")
@Validated
@RequiredArgsConstructor
public class ArController {

    private final ArAppService arAppService;

    @GetMapping("/inspections")
    public R<PageResult<InspectionTaskDTO>> listInspections(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String aircraftId) {
        return R.ok(arAppService.listInspections(
                new InspectionQueryParam(pageNum, pageSize, status, aircraftId)));
    }

    @PostMapping("/inspections")
    public R<Long> createInspection(@Valid @RequestBody CreateInspectionCommand cmd) {
        return R.ok(arAppService.createInspection(cmd));
    }

    @PutMapping("/inspections/{id}/start")
    public R<Void> startInspection(@PathVariable Long id) {
        arAppService.startInspection(id);
        return R.ok();
    }

    @PutMapping("/inspections/{id}/complete")
    public R<Void> completeInspection(@PathVariable Long id) {
        arAppService.completeInspection(id);
        return R.ok();
    }

    @GetMapping("/inspections/{id}/anomalies")
    public R<PageResult<AnomalyRecordDTO>> listAnomalies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(arAppService.listAnomalies(id, pageNum, pageSize));
    }

    @PostMapping("/sessions")
    public R<ArSessionDTO> createSession(@Valid @RequestBody CreateSessionCommand cmd) {
        return R.ok(arAppService.createSession(cmd));
    }

    @PutMapping("/sessions/{id}/join")
    public R<Void> joinSession(@PathVariable Long id) {
        arAppService.joinSession(id);
        return R.ok();
    }

    @PutMapping("/sessions/{id}/end")
    public R<Void> endSession(@PathVariable Long id) {
        arAppService.endSession(id);
        return R.ok();
    }

    @PostMapping("/sessions/{id}/annotations")
    public R<Void> sendAnnotation(@PathVariable Long id,
                                  @RequestBody AnnotationDTO annotation) {
        arAppService.sendAnnotation(id, annotation);
        return R.ok();
    }

    @GetMapping("/archives")
    public R<PageResult<VideoArchiveDTO>> listArchives(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long sessionId) {
        return R.ok(arAppService.listArchives(
                new ArchiveQueryParam(pageNum, pageSize, taskId, sessionId)));
    }
}
