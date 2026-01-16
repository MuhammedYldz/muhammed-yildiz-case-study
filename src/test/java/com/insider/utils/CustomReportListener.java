package com.insider.utils;

import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomReportListener implements IReporter {

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        System.out.println("CustomReportListener: Start generating report...");
        System.out.println("CustomReportListener: Output Directory provided: " + outputDirectory);
        
        // Ensure output directory exists
        File outDir = new File(outputDirectory);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        StringBuilder html = new StringBuilder();
        
        // CSS and Header
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Test Execution Report</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; }");
        html.append(".container { max-width: 1000px; margin: 0 auto; background: #fff; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); border-radius: 8px; }");
        html.append("h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }");
        html.append(".summary { display: flex; justify-content: space-around; margin-bottom: 20px; background: #fafafa; padding: 15px; border-radius: 5px; }");
        html.append(".summary-item { text-align: center; }");
        html.append(".summary-value { font-size: 24px; font-weight: bold; }");
        html.append(".pass { color: #28a745; } .fail { color: #dc3545; } .skip { color: #ffc107; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background-color: #007bff; color: white; }");
        html.append("tr:hover { background-color: #f1f1f1; }");
        html.append(".status-pass { background-color: #d4edda; color: #155724; padding: 5px 10px; border-radius: 4px; font-weight: bold; }");
        html.append(".status-fail { background-color: #f8d7da; color: #721c24; padding: 5px 10px; border-radius: 4px; font-weight: bold; }");
        html.append(".status-skip { background-color: #fff3cd; color: #856404; padding: 5px 10px; border-radius: 4px; font-weight: bold; }");
        html.append(".steps { font-family: monospace; font-size: 0.9em; background: #f8f9fa; padding: 10px; border-radius: 4px; border: 1px solid #e9ecef; }");
        html.append("</style></head><body>");
        
        html.append("<div class='container'>");
        html.append("<h1>Test Execution Report</h1>");
        html.append("<p>Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</p>");

        int totalPass = 0;
        int totalFail = 0;
        int totalSkip = 0;

        // Calculate Summary
        for (ISuite suite : suites) {
            Map<String, ISuiteResult> results = suite.getResults();
            for (ISuiteResult result : results.values()) {
                ITestContext context = result.getTestContext();
                totalPass += context.getPassedTests().size();
                totalFail += context.getFailedTests().size();
                totalSkip += context.getSkippedTests().size();
            }
        }

        html.append("<div class='summary'>");
        html.append("<div class='summary-item'><div class='summary-value'>").append(totalPass + totalFail + totalSkip).append("</div><div>Total</div></div>");
        html.append("<div class='summary-item'><div class='summary-value pass'>").append(totalPass).append("</div><div>Passed</div></div>");
        html.append("<div class='summary-item'><div class='summary-value fail'>").append(totalFail).append("</div><div>Failed</div></div>");
        html.append("<div class='summary-item'><div class='summary-value skip'>").append(totalSkip).append("</div><div>Skipped</div></div>");
        html.append("</div>");

        // Test Details
        html.append("<h2>Test Details</h2>");
        html.append("<table>");
        html.append("<thead><tr><th>Test Name</th><th>Status</th><th>Duration (ms)</th><th>Steps & Logs</th></tr></thead>");
        html.append("<tbody>");

        for (ISuite suite : suites) {
            Map<String, ISuiteResult> results = suite.getResults();
            for (ISuiteResult result : results.values()) {
                ITestContext context = result.getTestContext();

                processTestResult(html, context.getPassedTests(), "PASS");
                processTestResult(html, context.getFailedTests(), "FAIL");
                processTestResult(html, context.getSkippedTests(), "SKIP");
            }
        }

        html.append("</tbody></table>");
        html.append("</div></body></html>");

        // Write file
        File reportFile = new File(outputDirectory + File.separator + "CustomReport.html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(html.toString());
            System.out.println("Custom HTML Report generated: " + reportFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTestResult(StringBuilder html, IResultMap resultMap, String status) {
        Set<ITestResult> results = resultMap.getAllResults();
        for (ITestResult result : results) {
            html.append("<tr>");
            html.append("<td>").append(result.getName()).append("</td>");
            
            String statusClass = status.equals("PASS") ? "status-pass" : (status.equals("FAIL") ? "status-fail" : "status-skip");
            html.append("<td><span class='").append(statusClass).append("'>").append(status).append("</span></td>");
            
            long duration = result.getEndMillis() - result.getStartMillis();
            html.append("<td>").append(duration).append("</td>");
            
            // Steps
            html.append("<td><div class='steps'>");
            List<String> logs = Reporter.getOutput(result);
            if (logs.isEmpty()) {
                html.append("No steps recorded.");
            } else {
                for (String log : logs) {
                    html.append(log).append("<br>"); // <br> is already added in BasePage but good to be safe or just append
                }
            }
            
            if (result.getThrowable() != null) {
                html.append("<br><strong>Error:</strong> ").append(result.getThrowable().getMessage());
            }
            
            html.append("</div></td>");
            html.append("</tr>");
        }
    }
}