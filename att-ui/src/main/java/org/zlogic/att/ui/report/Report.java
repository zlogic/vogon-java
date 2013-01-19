/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperHtmlExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.ui.adapters.TaskManager;
import reporting.ReportQuery;

/**
 * Class for generating a report
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class Report {

	private TaskManager taskManager;
	private Date startDate;
	private Date endDate;
	private DoubleProperty progress = new SimpleDoubleProperty(-1);
	private String reportHTML;
	private JasperReportBuilder report;

	public Report(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public DoubleProperty progressProperty() {
		return progress;
	}

	protected StyleBuilder getTableTitleStyle() {
		return DynamicReports.stl.style()
				.setHorizontalAlignment(HorizontalAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setFontSize(20)
				.setBold(true);
	}

	protected StyleBuilder getColumnTitleStyle() {
		return DynamicReports.stl.style()
				.setHorizontalAlignment(HorizontalAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setBorder(DynamicReports.stl.pen1Point())
				.setFontSize(14);
	}

	protected ComponentBuilder getTitle(ReportQuery reportQuery) {
		String titleText = MessageFormat.format("Timesheet for {0,date,medium} - {1,date,medium}", new Object[]{reportQuery.getStartDate(), reportQuery.getEndDate()});
		StyleBuilder titleStyle = DynamicReports.stl.style()
				.setHorizontalAlignment(HorizontalAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setBorder(DynamicReports.stl.pen1Point())
				.setFontSize(24)
				.setBold(true);
		ComponentBuilder titleComponentBuilder = DynamicReports.cmp.text(titleText).setStyle(titleStyle);
		return titleComponentBuilder;
	}

	protected JasperReportBuilder buildTimeSegmentsReport(ReportQuery reportQuery, List<TimeSegment> timeSegments) {
		AbstractSimpleExpression<String> startTimeExpression = new AbstractSimpleExpression<String>() {
			@Override
			public String evaluate(ReportParameters rp) {
				return MessageFormat.format("{0,date,short}, {0,time,medium}", new Object[]{(Date) rp.getFieldValue("startTime")});
			}
		};
		AbstractSimpleExpression<String> endTimeExpression = new AbstractSimpleExpression<String>() {
			@Override
			public String evaluate(ReportParameters rp) {
				return MessageFormat.format("{0,date,short}, {0,time,medium}", new Object[]{(Date) rp.getFieldValue("endTime")});
			}
		};
		return DynamicReports.report()
				.title(
				DynamicReports.cmp.text("Full time report")
				.setStyle(getTableTitleStyle()))
				.addField(DynamicReports.field("startTime", Date.class))
				.addField(DynamicReports.field("endTime", Date.class))
				.sortBy(DynamicReports.asc("startTime", Date.class))
				.columns(
				DynamicReports.col.column("Task", "owner.name", DynamicReports.type.stringType()),
				DynamicReports.col.column("Time segment", "description", DynamicReports.type.stringType()),
				DynamicReports.col.column("Start time", startTimeExpression).setHorizontalAlignment(HorizontalAlignment.RIGHT),
				DynamicReports.col.column("End time", endTimeExpression).setHorizontalAlignment(HorizontalAlignment.RIGHT))
				.setHighlightDetailEvenRows(true)
				.setColumnTitleStyle(getColumnTitleStyle())
				.setDataSource(new JRBeanCollectionDataSource(timeSegments));
	}

	protected JasperReportBuilder buildTasksReport(ReportQuery reportQuery, List<Task> tasks) {
		DRDataSource dataSource = new DRDataSource("item", "orderdate", "quantity", "unitprice");
		AbstractSimpleExpression<String> totalTimeExpression = new AbstractSimpleExpression<String>() {
			@Override
			public String evaluate(ReportParameters rp) {
				return ((Period) rp.getFieldValue("totalTime")).toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter());
			}
		};
		return DynamicReports.report()
				.title(DynamicReports.cmp.text("Tasks").setStyle(getTableTitleStyle()))
				.addField(DynamicReports.field("totalTime", Date.class))
				.columns(
				DynamicReports.col.column("Task", "name", DynamicReports.type.stringType()),
				DynamicReports.col.column("Description", "description", DynamicReports.type.stringType()),
				DynamicReports.col.column("Total time", totalTimeExpression).setHorizontalAlignment(HorizontalAlignment.RIGHT))
				.setHighlightDetailEvenRows(true)
				.setColumnTitleStyle(getColumnTitleStyle())
				.setDataSource(new JRBeanCollectionDataSource(tasks));
	}

	public String getReportHTML() {
		return reportHTML;
	}

	public void savePdfReport(File outputFile) throws FileNotFoundException, DRException {
		JasperPdfExporterBuilder pdfExporter = Exporters.pdfExporter(outputFile);
		report.toPdf(pdfExporter);
	}

	public void buildReport() {
		try {
			//Get data
			progressProperty().set(0);
			ReportQuery reportQuery = new ReportQuery(taskManager.getPersistenceHelper());
			reportQuery.setStartDate(startDate);
			reportQuery.setEndDate(endDate);
			List<TimeSegment> timeSegments = reportQuery.queryTimeSegments();
			List<Task> tasks = reportQuery.queryTasks();
			//Build the report
			progressProperty().set(0.2);

			//Prepare exporter
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			JasperHtmlExporterBuilder htmlExporter =
					Exporters.htmlExporter(stream)
					.setUsingImagesToAlign(false);

			//Prepare full report
			report = DynamicReports.report()
					.title(getTitle(reportQuery))
					.detail(
					DynamicReports.cmp.verticalGap(20),
					DynamicReports.cmp.subreport(buildTasksReport(reportQuery, tasks)),
					DynamicReports.cmp.verticalGap(20),
					DynamicReports.cmp.subreport(buildTimeSegmentsReport(reportQuery, timeSegments))).setDataSource(new JREmptyDataSource())
					.toHtml(htmlExporter);
			reportHTML = stream.toString("utf-8");
		} catch (UnsupportedEncodingException | DRException ex) {
			Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Throwable ex) {
			Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
