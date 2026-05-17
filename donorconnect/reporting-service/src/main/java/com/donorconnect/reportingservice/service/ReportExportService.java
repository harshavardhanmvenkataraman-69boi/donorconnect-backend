package com.donorconnect.reportingservice.service;

import com.donorconnect.reportingservice.dto.InventoryBalanceDto;
import com.donorconnect.reportingservice.entity.LabReportPack;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final ReportingService reportingService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── CSV ───────────────────────────────────────────────────────────────────

    public byte[] exportInventorySnapshotCsv() {
        Map<String, Object> data = reportingService.getInventorySnapshot();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Inventory Snapshot Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");

        sb.append("By Component Type\n");
        sb.append("Component Type,Available Units\n");
        @SuppressWarnings("unchecked")
        Map<String, Long> byType = (Map<String, Long>) data.get("availableByComponentType");
        byType.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));

        sb.append("\nBy Blood Group\n");
        sb.append("Blood Group,Available Units\n");
        @SuppressWarnings("unchecked")
        Map<String, Long> byBg = (Map<String, Long>) data.get("availableByBloodGroup");
        byBg.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));

        sb.append("\nTotal Available Units,").append(data.get("totalAvailableUnits")).append("\n");
        return sb.toString().getBytes();
    }

    public byte[] exportDonorActivityCsv() {
        Map<String, Long> data = reportingService.getDonorActivity();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Donor Activity Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Status,Count\n");
        data.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportComponentWastageCsv() {
        Map<String, Object> data = reportingService.getComponentWastage();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Component Wastage Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Category,Count\n");
        data.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportUtilizationCsv() {
        Map<String, Object> data = reportingService.getUtilization();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Blood Utilization Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Status,Count\n");
        data.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportDeferralsCsv() {
        Map<String, Object> data = reportingService.getDeferralTrends();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Deferral Trends Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Category,Count\n");
        data.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportReactiveCountCsv() {
        Map<String, Long> data = reportingService.getReactiveCount();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Reactive Test Results Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Test Type,Reactive Count\n");
        data.forEach((k, v) -> sb.append(k).append(",").append(v).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportExpiryRiskCsv() {
        List<InventoryBalanceDto> data = reportingService.getExpiryRisk();
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Expiry Risk Report (Next 7 Days)\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");
        sb.append("Component ID,Blood Group,Rh Factor,Component Type,Bag Number,Expiry Date,Status,Quantity\n");
        data.forEach(i -> sb
                .append(nvl(i.getComponentId())).append(",")
                .append(nvl(i.getBloodGroup())).append(",")
                .append(nvl(i.getRhFactor())).append(",")
                .append(nvl(i.getComponentType())).append(",")
                .append(nvl(i.getBagNumber())).append(",")
                .append(nvl(i.getExpiryDate())).append(",")
                .append(nvl(i.getStatus())).append(",")
                .append(nvl(i.getQuantity())).append("\n"));
        return sb.toString().getBytes();
    }

    public byte[] exportFullReportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("DonorConnect - Full Analytics Report\n");
        sb.append("Generated:,").append(LocalDateTime.now().format(FMT)).append("\n\n");

        sb.append("=== INVENTORY SNAPSHOT ===\n");
        sb.append(new String(exportInventorySnapshotCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        sb.append("\n=== DONOR ACTIVITY ===\n");
        sb.append(new String(exportDonorActivityCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        sb.append("\n=== COMPONENT WASTAGE ===\n");
        sb.append(new String(exportComponentWastageCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        sb.append("\n=== BLOOD UTILIZATION ===\n");
        sb.append(new String(exportUtilizationCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        sb.append("\n=== DEFERRAL TRENDS ===\n");
        sb.append(new String(exportDeferralsCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        sb.append("\n=== REACTIVE TEST COUNTS ===\n");
        sb.append(new String(exportReactiveCountCsv()).lines()
                .skip(3).reduce("", (a, b) -> a + b + "\n"));

        return sb.toString().getBytes();
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    public byte[] exportFullReportExcel() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // Styles
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFontWhite = wb.createFont();
            headerFontWhite.setBold(true);
            headerFontWhite.setColor(IndexedColors.WHITE.getIndex());
            headerFontWhite.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFontWhite);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle subHeaderStyle = wb.createCellStyle();
            Font subFont = wb.createFont();
            subFont.setBold(true);
            subHeaderStyle.setFont(subFont);
            subHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Sheet 1: Inventory Snapshot ──
            Sheet invSheet = wb.createSheet("Inventory Snapshot");
            addTitle(invSheet, titleStyle, "Inventory Snapshot Report");
            Map<String, Object> inv = reportingService.getInventorySnapshot();

            int r = 2;
            addSubHeader(invSheet, subHeaderStyle, r++, "By Component Type");
            addRow(invSheet, headerStyle, r++, "Component Type", "Available Units");
            @SuppressWarnings("unchecked")
            Map<String, Long> byType = (Map<String, Long>) inv.get("availableByComponentType");
            for (Map.Entry<String, Long> e : byType.entrySet())
                addRow(invSheet, null, r++, e.getKey(), e.getValue().toString());

            r++;
            addSubHeader(invSheet, subHeaderStyle, r++, "By Blood Group");
            addRow(invSheet, headerStyle, r++, "Blood Group", "Available Units");
            @SuppressWarnings("unchecked")
            Map<String, Long> byBg = (Map<String, Long>) inv.get("availableByBloodGroup");
            for (Map.Entry<String, Long> e : byBg.entrySet())
                addRow(invSheet, null, r++, e.getKey(), e.getValue().toString());

            r++;
            addRow(invSheet, subHeaderStyle, r, "Total Available Units", inv.get("totalAvailableUnits").toString());
            autoSize(invSheet, 2);

            // ── Sheet 2: Donor Activity ──
            Sheet donorSheet = wb.createSheet("Donor Activity");
            addTitle(donorSheet, titleStyle, "Donor Activity Report");
            addRow(donorSheet, headerStyle, 2, "Status", "Count");
            int dr = 3;
            for (Map.Entry<String, Long> e : reportingService.getDonorActivity().entrySet())
                addRow(donorSheet, null, dr++, e.getKey(), e.getValue().toString());
            autoSize(donorSheet, 2);

            // ── Sheet 3: Component Wastage ──
            Sheet wastageSheet = wb.createSheet("Component Wastage");
            addTitle(wastageSheet, titleStyle, "Component Wastage Report");
            addRow(wastageSheet, headerStyle, 2, "Category", "Count");
            int wr = 3;
            for (Map.Entry<String, Object> e : reportingService.getComponentWastage().entrySet())
                addRow(wastageSheet, null, wr++, e.getKey(), e.getValue().toString());
            autoSize(wastageSheet, 2);

            // ── Sheet 4: Utilization ──
            Sheet utilSheet = wb.createSheet("Utilization");
            addTitle(utilSheet, titleStyle, "Blood Utilization Report");
            addRow(utilSheet, headerStyle, 2, "Status", "Count");
            int ur = 3;
            for (Map.Entry<String, Object> e : reportingService.getUtilization().entrySet())
                addRow(utilSheet, null, ur++, e.getKey(), e.getValue().toString());
            autoSize(utilSheet, 2);

            // ── Sheet 5: Deferrals ──
            Sheet defSheet = wb.createSheet("Deferral Trends");
            addTitle(defSheet, titleStyle, "Deferral Trends Report");
            addRow(defSheet, headerStyle, 2, "Category", "Count");
            int dfr = 3;
            for (Map.Entry<String, Object> e : reportingService.getDeferralTrends().entrySet())
                addRow(defSheet, null, dfr++, e.getKey(), e.getValue().toString());
            autoSize(defSheet, 2);

            // ── Sheet 6: Reactive Count ──
            Sheet reactSheet = wb.createSheet("Reactive Tests");
            addTitle(reactSheet, titleStyle, "Reactive Test Results Report");
            addRow(reactSheet, headerStyle, 2, "Test Type", "Reactive Count");
            int rr = 3;
            for (Map.Entry<String, Long> e : reportingService.getReactiveCount().entrySet())
                addRow(reactSheet, null, rr++, e.getKey(), e.getValue().toString());
            autoSize(reactSheet, 2);

            // ── Sheet 7: Expiry Risk ──
            Sheet expirySheet = wb.createSheet("Expiry Risk");
            addTitle(expirySheet, titleStyle, "Expiry Risk Report (Next 7 Days)");
            addRow(expirySheet, headerStyle, 2,
                    "Component ID", "Blood Group", "Rh Factor", "Component Type", "Bag Number", "Expiry Date", "Status", "Quantity");
            int er = 3;
            for (InventoryBalanceDto i : reportingService.getExpiryRisk()) {
                Row row = expirySheet.createRow(er++);
                row.createCell(0).setCellValue(nvl(i.getComponentId()));
                row.createCell(1).setCellValue(nvl(i.getBloodGroup()));
                row.createCell(2).setCellValue(nvl(i.getRhFactor()));
                row.createCell(3).setCellValue(nvl(i.getComponentType()));
                row.createCell(4).setCellValue(nvl(i.getBagNumber()));
                row.createCell(5).setCellValue(nvl(i.getExpiryDate()));
                row.createCell(6).setCellValue(nvl(i.getStatus()));
                row.createCell(7).setCellValue(nvl(i.getQuantity()));
            }
            autoSize(expirySheet, 8);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void addTitle(Sheet sheet, CellStyle style, String title) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("DonorConnect | " + title);
        cell.setCellStyle(style);
        Row genRow = sheet.createRow(1);
        genRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(FMT));
    }

    private void addSubHeader(Sheet sheet, CellStyle style, int rowNum, String label) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(label);
        cell.setCellStyle(style);
    }

    private void addRow(Sheet sheet, CellStyle style, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i] != null ? values[i] : "");
            if (style != null) cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private String nvl(Object o) { return o != null ? o.toString() : ""; }
}
