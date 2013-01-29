/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.filter.ui.Filter;

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
	/**
	 * The list of available filter types
	 */
	private ObservableList<FilterTypeFactory> availableFilters = FXCollections.observableArrayList();
	/**
	 * The default filter type/constructor (empty filter)
	 */
	private FilterTypeFactory defaultFilterConstructor;

	/*
	 * Filter types
	 */
	/**
	 * Factory for empty filters
	 */
	private class EmptyFilterFactory extends FilterTypeFactory {

		/**
		 * Constructs an EmptyFilterFactory
		 */
		public EmptyFilterFactory() {
			super(""); //NOI18N
		}

		@Override
		public Filter createFilter() {
			return new EmptyFilter(this);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof EmptyFilterFactory;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			return hash;
		}
	}

	/**
	 * Factory for DateFilters
	 */
	private class DateFilterFactory extends FilterTypeFactory {

		/**
		 * The date type
		 */
		private DateFilter.DateType type;

		/**
		 * Constructs an DateFilterFactory
		 */
		public DateFilterFactory(DateFilter.DateType type) {
			super(type == DateFilter.DateType.DATE_START ? messages.getString("START_DATE") : messages.getString("END_DATE"));
		}

		@Override
		public Filter createFilter() {
			return new DateFilter(this, type);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof DateFilterFactory && ((DateFilterFactory) obj).type == type;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
			return hash;
		}
	}

	/**
	 * Factory for CustomFieldFilters
	 */
	private class CustomFieldFilterFactory extends FilterTypeFactory {

		/**
		 * The associated Custom Field
		 */
		private CustomFieldAdapter customField;

		/**
		 * Constructs an CustomFieldFilterFactory
		 */
		public CustomFieldFilterFactory(CustomFieldAdapter customField) {
			super(customField.nameProperty().get());
			this.customField = customField;
			customField.nameProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
					name = newValue;
				}
			});
		}

		@Override
		public Filter createFilter() {
			return new CustomFieldFilter(taskManager, this, customField);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CustomFieldFilterFactory && customField.equals(((CustomFieldFilterFactory) obj).customField);
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 29 * hash + Objects.hashCode(this.customField.getCustomField().hashCode());
			return hash;
		}
	}

	/**
	 * Factory for CompletedFilters
	 */
	private class CompletedFilterFactory extends FilterTypeFactory {

		/**
		 * Constructs an CompletedFilterFactory
		 */
		public CompletedFilterFactory() {
			super(messages.getString("TASK_COMPLETED"));
		}

		@Override
		public Filter createFilter() {
			return new CompletedFilter(this);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CompletedFilterFactory;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			return hash;
		}
	}

	/**
	 * Creates a filter factory
	 *
	 * @param taskManager the TaskManager reference
	 */
	public FilterFactory(TaskManager taskManager) {
		this.taskManager = taskManager;

		//Populate available filters list
		availableFilters.add(new DateFilterFactory(DateFilter.DateType.DATE_START));
		availableFilters.add(new DateFilterFactory(DateFilter.DateType.DATE_END));
		availableFilters.add(new CompletedFilterFactory());
		updateCustomFieldFilters();
		defaultFilterConstructor = new EmptyFilterFactory();
		taskManager.getCustomFields().addListener(new ListChangeListener<CustomFieldAdapter>() {
			@Override
			public void onChanged(Change<? extends CustomFieldAdapter> change) {
				updateCustomFieldFilters();
			}
		});
	}

	/**
	 * Updates the custom field filters by deleting them and re-inserting to the
	 * end of the list
	 */
	private void updateCustomFieldFilters() {
		List<FilterTypeFactory> deleteFilterList = new LinkedList<>();
		for (FilterTypeFactory filter : availableFilters)
			if (filter instanceof CustomFieldFilterFactory)
				deleteFilterList.add(filter);
		availableFilters.removeAll(deleteFilterList);
		for (CustomFieldAdapter customField : taskManager.getCustomFields())
			availableFilters.add(new CustomFieldFilterFactory(customField));
	}

	/**
	 * Returns the list of all available FilterTypeFactories (e.g. to display in
	 * a combo box)
	 *
	 * @return the list of all available FilterTypeFactories
	 */
	public ObservableList<FilterTypeFactory> getAvailableFilters() {
		return availableFilters;
	}

	/**
	 * Returns the default (empty filter) constructor
	 *
	 * @return the default (empty filter) constructor
	 */
	public FilterTypeFactory getDefaultFilterConstructor() {
		return defaultFilterConstructor;
	}

	/**
	 * Creates a default (empty) filter
	 *
	 * @return the created filter
	 */
	public Filter createFilter() {
		return defaultFilterConstructor.createFilter();
	}
}
