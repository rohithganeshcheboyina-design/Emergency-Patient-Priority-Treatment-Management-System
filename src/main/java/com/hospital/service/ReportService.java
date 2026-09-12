package com.hospital.service;

import com.hospital.dao.ReportDAO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service managing dashboard statistics, chart datasets, and report CSV exports.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public Map<String, Integer> getDashboardSummary() {
        return reportDAO.getDashboardSummaryCounts();
    }

    public Map<String, Integer> getEmergencyDistribution() {
        return reportDAO.getEmergencyLevelDistribution();
    }

    public Map<String, Integer> getStatusDistribution() {
        return reportDAO.getStatusDistribution();
    }

    public Map<String, Integer> getDailyRegistrationTrend(int days) {
        return reportDAO.getDailyRegistrations(days);
    }

    /**
     * Exports all patient records to a formatted CSV file.
     */
    public boolean exportPatientsToCSV(File targetFile) throws IOException {
        if (targetFile == null) return false;
        List<String[]> data = reportDAO.getPatientExportData();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            for (String[] row : data) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    String cell = row[i] != null ? row[i] : "";
                    // Escape quotes
                    cell = cell.replace("\"", "\"\"");
                    if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                        cell = "\"" + cell + "\"";
                    }
                    line.append(cell);
                    if (i < row.length - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
            writer.flush();
            return true;
        }
    }
}
