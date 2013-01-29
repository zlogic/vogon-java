/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.zlogic.att.ui.adapters.TaskManager;

/**
 * Filter factory class
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterFactory {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/filter/messages");
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	private ObservableList<FilterTypeFactory> availableFilters = FXCollections.observableArrayList();
	private FilterTypeFactory defaultFilterConstructor;

	/**
	 * Creates a filter factory
	 *
	 * @param taskManager the TaskManager reference
	 */
	public FilterFactory(TaskManager taskManager) {
		this.taskManager = taskManager;

		//Populate available filters list
		availableFilters.add(new FilterTypeFactory(messages.getString("START_DATE")) {
			@Override
			public Filter createFilter() {
				return new DateFilter(this, DateFilter.DateType.DATE_START);
			}
		});
		availableFilters.add(new FilterTypeFactory(messages.getString("END_DATE")) {
			@Override
			public Filter createFilter() {
				return new DateFilter(this, DateFilter.DateType.DATE_END);
			}
		});
		defaultFilterConstructor = new FilterTypeFactory("") {
			@Override
			public Filter createFilter() {
				return new EmptyFilter(this);
			}
		};
	}

	public ObservableList<FilterTypeFactory> getAvailableFilters() {
		return availableFilters;
	}

	public FilterTypeFactory getDefaultFilterConstructor() {
		return defaultFilterConstructor;
	}

	public Filter createFilter() {
		return defaultFilterConstructor.createFilter();
	}
}
