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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperHtmlExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.group.CustomGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIExpression;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.reporting.DateTools;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.data.reporting.ReportQuery;

/*
 * TODO: move internal classes into other files 
 */
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
	private AbstractValueFormatter<String, Date> dateTimeFormatter = new AbstractValueFormatter<String, Date>() {
		@Override
		public String format(Date value, ReportParameters reportParameters) {
			return MessageFormat.format("{0,date,short}, {0,time,medium}", new Object[]{value});
		}
	};
	private AbstractValueFormatter<String, Period> periodFormatter = new AbstractValueFormatter<String, Period>() {
		@Override
		public String format(Period value, ReportParameters reportParameters) {
			return value.toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter());
		}
	};
	private AbstractValueFormatter<String, CustomField> customFieldNameFormatter = new AbstractValueFormatter<String, CustomField>() {
		@Override
		public String format(CustomField value, ReportParameters reportParameters) {
			return value.getName();
		}
	};
	private AbstractValueFormatter<String, CustomField> customFieldValueFormatter = new AbstractValueFormatter<String, CustomField>() {
		@Override
		public String format(CustomField value, ReportParameters reportParameters) {
			Task task = reportParameters.getFieldValue("task");
			return task.getCustomField(value);
		}
	};

	public class DateTimeSegment {

		private Date date;
		private TimeSegment timeSegment;

		private DateTimeSegment(Date date, TimeSegment timeSegment) {
			this.date = date;
			this.timeSegment = timeSegment;
		}

		public Date getDate() {
			return date;
		}

		public TimeSegment getTimeSegment() {
			return timeSegment;
		}

		public double getDurationHours() {
			Period period = timeSegment.getClippedDuration(startDate, endDate);
			return ((double) period.toStandardDuration().getStandardSeconds()) / 3600;
		}

		public String getDuration() {
			Period period = timeSegment.getClippedDuration(startDate, endDate);
			return period.toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter());
		}
	}

	public Report(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = DateTools.getInstance().convertDateToStartOfDay(startDate);
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = DateTools.getInstance().convertDateToEndOfDay(endDate);
	}

	public DoubleProperty progressProperty() {
		return progress;
	}

	protected String getDateFormat() {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		if (dateFormat instanceof SimpleDateFormat)
			return ((SimpleDateFormat) dateFormat).toPattern();
		else
			return null;
	}

	protected String getTimeFormat() {
		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		if (timeFormat instanceof SimpleDateFormat)
			return ((SimpleDateFormat) timeFormat).toPattern();
		else
			return null;
	}

	protected String getDateTimeFormat() {
		String dateFormat = getDateFormat();
		String timeFormat = getTimeFormat();
		if (dateFormat != null && timeFormat != null)
			return dateFormat + " " + timeFormat;
		else
			return null;
	}

	/**
	 * Returns the style for table titles
	 *
	 * @return the style for table titles
	 */
	protected StyleBuilder getTableTitleStyle() {
		return DynamicReports.stl.style()
				.setHorizontalAlignment(HorizontalAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setFontSize(20)
				.setBold(true);
	}

	/**
	 * Returns the style for column titles
	 *
	 * @return the style for column titles
	 */
	protected StyleBuilder getColumnTitleStyle() {
		return DynamicReports.stl.style()
				.setHorizontalAlignment(HorizontalAlignment.CENTER)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setBorder(DynamicReports.stl.pen1Point())
				.setFontSize(14);
	}

	/**
	 * Returns the builder for the report title component
	 *
	 * @param reportQuery the report query (used to get the timeframe)
	 * @return the builder for the report title component
	 */
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

	/**
	 * Returns the builder for the task's name and custom fields as a vertical
	 * list
	 *
	 * @return the builder for the task's name and custom fields as a vertical
	 * list
	 */
	protected VerticalListBuilder getTaskWithCustomFields() {
		//Create title and list
		StyleBuilder titleStyle = DynamicReports.stl.style()
				.setBold(true);
		VerticalListBuilder customFieldsList = DynamicReports.cmp.verticalList()
				.add(
				DynamicReports.cmp.text(DynamicReports.field("name", String.class))
				.setStyle(titleStyle));
		//Style for custom field names
		StyleBuilder customFieldNameStyle = DynamicReports.stl.style()
				.setItalic(true);
		//Add custom columns
		for (CustomFieldAdapter customField : taskManager.getCustomFields()) {
			//Extract custom field
			DRIExpression<CustomField> customFieldExpression = new AbstractSimpleExpression<CustomField>() {
				private CustomField customField;

				public AbstractSimpleExpression<CustomField> setCustomField(CustomField customField) {
					this.customField = customField;
					return this;
				}

				@Override
				public CustomField evaluate(ReportParameters rp) {
					return customField;
				}
			}.setCustomField(customField.getCustomField());
			//Build the column
			customFieldsList = customFieldsList
					.add(
					DynamicReports.cmp.horizontalFlowList(
					DynamicReports.cmp.text(customFieldExpression).setValueFormatter(customFieldNameFormatter).setStyle(customFieldNameStyle).setRemoveLineWhenBlank(true),
					DynamicReports.cmp.text(customFieldExpression).setValueFormatter(customFieldValueFormatter).setRemoveLineWhenBlank(true)));
		}
		return customFieldsList;
	}

	protected JasperReportBuilder buildTimeSegmentsReport(ReportQuery reportQuery, List<TimeSegment> timeSegments) {
		AbstractSimpleExpression<Date> startTimeExpression = new AbstractSimpleExpression<Date>() {
			@Override
			public Date evaluate(ReportParameters rp) {
				TimeSegment timeSegment = rp.getFieldValue("timeSegment");
				Date clippedStartDate = timeSegment.getClippedStartTime(startDate, endDate);
				if (clippedStartDate != null)
					return clippedStartDate;
				else
					return null;
			}
		};
		AbstractSimpleExpression<Date> endTimeExpression = new AbstractSimpleExpression<Date>() {
			@Override
			public Date evaluate(ReportParameters rp) {
				TimeSegment timeSegment = rp.getFieldValue("timeSegment");
				Date clippedEndDate = timeSegment.getClippedEndTime(startDate, endDate);
				if (clippedEndDate != null)
					return clippedEndDate;
				else
					return null;
			}
		};
		return DynamicReports.report()
				.title(
				DynamicReports.cmp.text("Full time report")
				.setStyle(getTableTitleStyle()))
				.addField(DynamicReports.field("timeSegment", Date.class))
				.sortBy(DynamicReports.asc(startTimeExpression))
				.columns(
				DynamicReports.col.column("Task", "owner.name", DynamicReports.type.stringType()),
				DynamicReports.col.column("Specifics", "description", DynamicReports.type.stringType()),
				DynamicReports.col.column("Start time", startTimeExpression).setValueFormatter(dateTimeFormatter).setHorizontalAlignment(HorizontalAlignment.RIGHT),
				DynamicReports.col.column("End time", endTimeExpression).setValueFormatter(dateTimeFormatter).setHorizontalAlignment(HorizontalAlignment.RIGHT))
				.setHighlightDetailEvenRows(true)
				.setColumnTitleStyle(getColumnTitleStyle())
				.setDataSource(new JRBeanCollectionDataSource(timeSegments));
	}

	protected JasperReportBuilder buildTasksReport(ReportQuery reportQuery, List<Task> tasks) {
		AbstractSimpleExpression<Date> startTimeExpression = new AbstractSimpleExpression<Date>() {
			@Override
			public Date evaluate(ReportParameters rp) {
				Task task = rp.getFieldValue("task");
				Date earliestStartDate = null;
				for (TimeSegment timeSegment : task.getTimeSegments()) {
					Date taskStartDate = timeSegment.getClippedStartTime(startDate, endDate);
					earliestStartDate = (earliestStartDate == null || (taskStartDate != null && timeSegment.getStartTime().before(earliestStartDate))) ? taskStartDate : earliestStartDate;
				}
				return earliestStartDate;
			}
		};
		AbstractSimpleExpression<Period> totalTimeExpression = new AbstractSimpleExpression<Period>() {
			@Override
			public Period evaluate(ReportParameters rp) {
				Task task = rp.getFieldValue("task");
				return task.getTotalTime(startDate, endDate);
			}
		};
		return DynamicReports.report()
				.title(DynamicReports.cmp.text("Tasks").setStyle(getTableTitleStyle()))
				.addField(DynamicReports.field("task", Task.class))
				.sortBy(DynamicReports.asc(startTimeExpression))
				.sortBy(DynamicReports.desc(totalTimeExpression))
				.columns(
				DynamicReports.col.componentColumn("Task", getTaskWithCustomFields()),
				//DynamicReports.col.column("Task", "name", DynamicReports.type.stringType()),
				DynamicReports.col.column("Description", "description", DynamicReports.type.stringType()),
				DynamicReports.col.column("Total time", totalTimeExpression).setValueFormatter(periodFormatter).setHorizontalAlignment(HorizontalAlignment.RIGHT))
				.setHighlightDetailEvenRows(true)
				.setColumnTitleStyle(getColumnTitleStyle())
				.setDataSource(new JRBeanCollectionDataSource(tasks));
	}

	protected JasperReportBuilder buildTimesheetReport(ReportQuery reportQuery, List<TimeSegment> timeSegments) {
		//Date-TimeSegment association list
		List<DateTimeSegment> dataSource = new LinkedList<>();
		{
			Calendar calendar = new GregorianCalendar();
			for (calendar.setTime(startDate); !calendar.getTime().after(endDate); calendar.add(Calendar.DAY_OF_MONTH, 1)) {
				Date dayStart = DateTools.getInstance().convertDateToStartOfDay(calendar.getTime());
				Date dayEnd = DateTools.getInstance().convertDateToEndOfDay(calendar.getTime());
				for (TimeSegment timeSegment : timeSegments)
					if (!timeSegment.getClippedDuration(dayStart, dayEnd).equals(Period.ZERO))
						dataSource.add(new DateTimeSegment(dayStart, timeSegment));
			}
		}

		//Style for day headers
		StyleBuilder dayHeaderStyle = DynamicReports.stl.style()
				.setBold(true);

		AbstractSimpleExpression<String> dateTitleExpression = new AbstractSimpleExpression<String>() {
			@Override
			public String evaluate(ReportParameters rp) {
				Date date = rp.getFieldValue("date");
				return MessageFormat.format("{0,date,long}", new Object[]{date});
			}
		};
		CustomGroupBuilder dateGroup = DynamicReports.grp.group(dateTitleExpression)
				.setStyle(dayHeaderStyle);
		return DynamicReports.report()
				.title(
				DynamicReports.cmp.text("Timesheet")
				.setStyle(getTableTitleStyle()))
				.columns(
				DynamicReports.col.column("Task", "timeSegment.owner.name", DynamicReports.type.stringType()),
				DynamicReports.col.column("Specifics", "timeSegment.description", DynamicReports.type.stringType()),
				//DynamicReports.col.column("Duration", "duration", DynamicReports.type.stringType()),
				DynamicReports.col.column("Hours", "durationHours", DynamicReports.type.doubleType()))
				.groupBy(dateGroup)
				.sortBy(DynamicReports.asc("date", Date.class))
				.setHighlightDetailEvenRows(true)
				.setColumnTitleStyle(getColumnTitleStyle())
				.setDataSource(new JRBeanCollectionDataSource(dataSource));
	}

	/**
	 * Returns the report in HTML form
	 *
	 * @return the report in HTML form
	 */
	public String getReportHTML() {
		return reportHTML;
	}

	/**
	 * Saves report to PDF
	 *
	 * @param outputFile destination file
	 * @throws FileNotFoundException if path is incorrect
	 * @throws DRException when an internal error occurs
	 */
	public void savePdfReport(File outputFile) throws FileNotFoundException, DRException {
		JasperPdfExporterBuilder pdfExporter = Exporters.pdfExporter(outputFile);
		report.toPdf(pdfExporter);
	}

	/**
	 * Builds the report in DynamicReports form; prepares an HTML report for
	 * preview.
	 */
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
					DynamicReports.cmp.pageBreak(),
					DynamicReports.cmp.subreport(buildTimeSegmentsReport(reportQuery, timeSegments)),
					DynamicReports.cmp.pageBreak(),
					DynamicReports.cmp.subreport(buildTimesheetReport(reportQuery, timeSegments))).setDataSource(new JREmptyDataSource())
					.toHtml(htmlExporter);
			reportHTML = stream.toString("utf-8");
		} catch (UnsupportedEncodingException | DRException ex) {
			Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Throwable ex) {
			Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
