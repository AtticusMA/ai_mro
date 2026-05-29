package com.mro.web.module.training.controller;

import com.mro.common.core.response.PageResult;
import com.mro.common.core.response.R;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.web.module.training.app.TrainingAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/training")
@Validated
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingAppService trainingAppService;

    @GetMapping("/scenarios")
    public R<PageResult<TrainingScenarioDTO>> listScenarios(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(trainingAppService.listScenarios(status, category, pageNum, pageSize));
    }

    @PostMapping("/scenarios")
    public R<Long> createScenario(@RequestBody CreateScenarioCommand cmd) {
        return R.ok(trainingAppService.createScenario(cmd));
    }

    @PutMapping("/scenarios/{id}")
    public R<Void> updateScenario(@PathVariable Long id,
                                   @RequestBody UpdateScenarioCommand cmd) {
        trainingAppService.updateScenario(id, cmd);
        return R.ok();
    }

    @PostMapping("/scenarios/{id}/publish")
    public R<Void> publishScenario(@PathVariable Long id) {
        trainingAppService.publishScenario(id);
        return R.ok();
    }

    @GetMapping("/trainees")
    public R<PageResult<TraineeDTO>> listTrainees(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(trainingAppService.listTrainees(pageNum, pageSize));
    }

    @GetMapping("/trainees/{id}/profile")
    public R<TraineeProfileDTO> getTraineeProfile(@PathVariable Long id) {
        return R.ok(trainingAppService.getTraineeProfile(id));
    }

    @PostMapping("/sessions")
    public R<List<Long>> createSessions(@RequestBody CreateTrainingSessionCommand cmd) {
        return R.ok(trainingAppService.createSessions(cmd));
    }

    @GetMapping("/sessions")
    public R<PageResult<TrainingSessionDTO>> listSessions(
            @RequestParam(required = false) Long scenarioId,
            @RequestParam(required = false) Long traineeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(trainingAppService.listSessions(scenarioId, traineeId, status, mode, pageNum, pageSize));
    }

    @GetMapping("/sessions/{id}")
    public R<TrainingSessionDTO> getSession(@PathVariable Long id) {
        return R.ok(trainingAppService.getSession(id));
    }

    @GetMapping("/assessments/{sessionId}")
    public R<List<SkillAssessmentDTO>> getAssessments(@PathVariable Long sessionId) {
        return R.ok(trainingAppService.getAssessments(sessionId));
    }

    @GetMapping("/reports/individual/{traineeId}")
    public R<IndividualReportDTO> generateIndividualReport(
            @PathVariable Long traineeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return R.ok(trainingAppService.generateIndividualReport(traineeId, start, end));
    }

    @GetMapping("/reports/overview")
    public R<OverviewReportDTO> generateOverviewReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return R.ok(trainingAppService.generateOverviewReport(start, end));
    }
}
