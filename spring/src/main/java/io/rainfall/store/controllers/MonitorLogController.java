package io.rainfall.store.controllers;

import io.rainfall.store.data.CompressionService;
import io.rainfall.store.data.Payload;
import io.rainfall.store.dataset.MonitorLogDataset;
import io.rainfall.store.dataset.MonitorLogRecord;
import io.rainfall.store.dataset.Record;
import io.rainfall.store.dataset.RunRecord;
import io.rainfall.store.values.MonitorLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

import static io.rainfall.store.data.CompressionServiceFactory.compressionService;

@Controller
@SuppressWarnings("unused")
public class MonitorLogController extends ChildController<
        MonitorLog, MonitorLogRecord, RunRecord, MonitorLogDataset> {

    MonitorLogController(MonitorLogDataset dataset) {
        super(dataset, "Monitor log", "/stats");
    }

    @PostMapping("/stats/{runId}")
    public ResponseEntity<?> postLog(@PathVariable long runId, @RequestBody MonitorLog log) {
        return super.post(runId, log);
    }

    @GetMapping({"/stats/{id}"})
    @ResponseBody
    public String getLog(ModelMap model, @PathVariable long id) throws IOException {
        Payload payload = dataset()
                .getPayload(id)
                .map(Record::getValue)
                .orElseThrow(() -> new IllegalStateException("Payload is null for monitor log: " + id));
        CompressionService compressionService = compressionService(payload.getFormat());
        byte[] data = compressionService.decompress(payload);
        return Payload.toUtfString(data);
    }

    @GetMapping({"/runs/{parentId}/stats"})
    public ModelAndView findMonitorLogsForRun(ModelMap model,
                                              @PathVariable long parentId) {
        return getByParentId(model, parentId);
    }

    @GetMapping({"/runs/{parentId}/stats/{host}"})
    public ModelAndView findMonitorLogsForRunAndHost(ModelMap model,
                                                    @PathVariable long parentId,
                                                    @PathVariable String host) {
        List<MonitorLogRecord> records = dataset()
                .findMonitorLogsForRunAndHost(parentId, host);
        return getRecords(model, records);
    }
}
