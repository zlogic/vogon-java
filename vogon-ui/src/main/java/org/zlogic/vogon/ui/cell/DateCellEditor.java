/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * String cell editor with date validation & parsing
 *
 * @param <BaseType> the row type
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class DateCellEditor<BaseType> extends TableCell<BaseType, Date> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(DateCellEditor.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");

	/**
	 * The editor component
	 */
	protected DatePicker dateField;
	/**
	 * The date format to be used for validation
	 */
	protected DateTimeFormatter dateFormat;
	/**
	 * Creates a date editor
	 */
	public DateCellEditor() {
		dateFormat = DateTimeFormatter.ofPattern(messages.getString("PARSER_DATE"));
	}


	/**
	 * Prepares the cell for editing
	 */
	@Override
	public void startEdit() {
		super.startEdit();

		if (dateField == null)
			createDateField();
		setText(null);
		setGraphic(dateField);
		dateField.setValue(localDateFromDate(getItem()));
	}

	/**
	 * Cancels cell editing
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setGraphic(null);
	}

	/**
	 * Performs an item update
	 *
	 * @param item the updated cell type class instance
	 * @param empty true if the item is empty
	 */
	@Override
	public void updateItem(Date item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (dateField != null) {
					dateField.setValue(localDateFromDate(item));
				}
				setText(null);
				setGraphic(dateField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	/**
	 * Creates the editor
	 */
	private void createDateField() {
		dateField = new DatePicker();
		dateField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		dateField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				commitEdit(dateFromLocalDate(dateField.getValue()));
			}
		});
		dateField.editorProperty().get().setAlignment(getAlignment());

		dateField.editorProperty().get().setOnKeyPressed(
				//dateField.editorProperty().get().addEventFilter(KeyEvent.KEY_PRESSED,
				new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ENTER) {
							//event.consume();
							commitEdit(dateFromLocalDate(dateField.getConverter().fromString(dateField.editorProperty().get().getText())));
						} else if (event.getCode() == KeyCode.ESCAPE) {
							cancelEdit();
						}
					}
				}
		);
	}

	/**
	 * Converts a LocalDate into a Date
	 * @param localDate the LocalDate to convert
	 * @return the Date corresponding to the start of day of localDate
	 */
	private Date dateFromLocalDate(LocalDate localDate){
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a Date into a LocalDate
	 * @param date the Date to convert
	 * @return the LocalDate corresponding to date
	 */
	private LocalDate localDateFromDate(Date date){
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Returns the string value of the edited property (before editing)
	 *
	 * @return the string value of the edited property
	 */
	protected String getString() {
		Date date =  getItem();
		return date == null ? "" : localDateFromDate(date).format(dateFormat);//NOI18N
	}
}
