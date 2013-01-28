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
import org.zlogic.att.ui.filter.FilterBuilder.FilterTypeComboItem;

/**
 * Filter factory class
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterBuilder {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/filter/messages");
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	private ObservableList<FilterTypeComboItem> availableFilters = FXCollections.observableArrayList();
	private FilterTypeComboItem defaultFilterConstructor;

	/**
	 * Creates a filter factory
	 *
	 * @param taskManager the TaskManager reference
	 */
	public FilterBuilder(TaskManager taskManager) {
		this.taskManager = taskManager;

		//Populate available filters list
		availableFilters.add(new FilterTypeComboItem(messages.getString("START_DATE")) {
			@Override
			public Filter createFilter() {
				return new DateFilter(this, DateFilter.DateType.DATE_START);
			}
		});
		availableFilters.add(new FilterTypeComboItem(messages.getString("END_DATE")) {
			@Override
			public Filter createFilter() {
				return new DateFilter(this, DateFilter.DateType.DATE_END);
			}
		});
		defaultFilterConstructor = new FilterTypeComboItem("") {
			@Override
			public Filter createFilter() {
				return new EmptyFilter(this);
			}
		};
	}

	public ObservableList<FilterTypeComboItem> getAvailableFilters() {
		return availableFilters;
	}

	public Filter createFilter() {
		return defaultFilterConstructor.createFilter();
	}

	public abstract class FilterTypeComboItem {

		private String name;

		private FilterTypeComboItem(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public abstract Filter createFilter();
	}
}
