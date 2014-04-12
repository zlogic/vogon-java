/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
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
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.zlogic.att.data.Filter;
import org.zlogic.att.data.FilterDate;
import org.zlogic.att.data.TransactedChange;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.filter.adapters.FilterAdapter;
import org.zlogic.att.ui.filter.adapters.FilterCustomFieldAdapter;
import org.zlogic.att.ui.filter.adapters.FilterDateAdapter;
import org.zlogic.att.ui.filter.adapters.FilterEmptyAdapter;
import org.zlogic.att.ui.filter.adapters.FilterTaskCompletedAdapter;

/**
 * FilterAdapter factory class
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class FilterFactory {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/filter/messages");
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
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
		public FilterAdapter createFilter() {
			return new FilterEmptyAdapter();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof EmptyFilterFactory) || (obj instanceof FilterEmptyAdapter);
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
		private FilterDate.DateType type;

		/**
		 * Constructs an DateFilterFactory
		 *
		 * @param type the filter type
		 */
		public DateFilterFactory(FilterDate.DateType type) {
			super(type == FilterDate.DateType.DATE_AFTER ? messages.getString("AFTER_DATE") : messages.getString("BEFORE_DATE"));
			this.type = type;
		}

		@Override
		public FilterAdapter createFilter() {
			return new FilterDateAdapter(dataManager, dataManager.getPersistenceHelper().createFilterDate(type));
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof DateFilterFactory && ((DateFilterFactory) obj).type == type)
					|| (obj instanceof FilterDateAdapter && ((FilterDateAdapter) obj).getFilter().getType() == type);
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
		 *
		 * @param customField the custom field to be filtered
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
		public FilterAdapter createFilter() {
			return new FilterCustomFieldAdapter(dataManager, dataManager.getPersistenceHelper().createFilterCustomField(customField.getCustomField()));
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof CustomFieldFilterFactory && customField.equals(((CustomFieldFilterFactory) obj).customField))
					|| (obj instanceof FilterCustomFieldAdapter && customField.getCustomField().equals(((FilterCustomFieldAdapter) obj).getFilter().getCustomField()));
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
		public FilterAdapter createFilter() {
			return new FilterTaskCompletedAdapter(dataManager, dataManager.getPersistenceHelper().createFilterTaskCompleted());
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CompletedFilterFactory
					|| obj instanceof FilterTaskCompletedAdapter;
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
	 * @param dataManager the DataManager reference
	 */
	public FilterFactory(DataManager dataManager) {
		this.dataManager = dataManager;

		//Populate available filters list
		availableFilters.add(new DateFilterFactory(FilterDate.DateType.DATE_AFTER));
		availableFilters.add(new DateFilterFactory(FilterDate.DateType.DATE_BEFORE));
		availableFilters.add(new CompletedFilterFactory());
		updateCustomFieldFilters();
		defaultFilterConstructor = new EmptyFilterFactory();
		this.dataManager.getCustomFields().addListener(new ListChangeListener<CustomFieldAdapter>() {
			@Override
			public void onChanged(Change<? extends CustomFieldAdapter> change) {
				updateCustomFieldFilters();
			}
		});
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
	public FilterAdapter createFilter() {
		return defaultFilterConstructor.createFilter();
	}

	/**
	 * Returns the FilterTypeFactory which can be used to construct the
	 * specified filter. If no matching FilterTypeFactory can be found, returns
	 * null.
	 *
	 * @param filter the filter to be matched with a FilterTypeFactory
	 * @return the FilterTypeFactory which can be used to construct the
	 * specified filter
	 */
	public FilterTypeFactory getFilterTypeFor(FilterAdapter filter) {
		if (filter == null)
			return null;
		for (FilterTypeFactory filterType : availableFilters)
			if (filterType.equals(filter))
				return filterType;
		return null;
	}

	/**
	 * Deletes a filter
	 *
	 * @param filter the filter to be deleted
	 */
	public void deleteFilter(FilterAdapter filter) {
		if (!(filter instanceof FilterEmptyAdapter))
			dataManager.getPersistenceHelper().performTransactedChange(new TransactedChange() {
				private Filter filter;

				public TransactedChange setDeleteFilter(Filter filter) {
					this.filter = filter;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					Filter filterEntity = entityManager.find(Filter.class, filter.getId());
					entityManager.remove(filterEntity);
				}
			}.setDeleteFilter(filter.getFilter()));
		List<FilterHolder> deleteFiltersHolder = new LinkedList<>();
		for (FilterHolder filterHolder : dataManager.getFilters())
			if (filterHolder.filterProperty().get().equals(filter))
				deleteFiltersHolder.add(filterHolder);
		dataManager.getFilters().removeAll(deleteFiltersHolder);
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
		for (CustomFieldAdapter customField : dataManager.getCustomFields())
			availableFilters.add(new CustomFieldFilterFactory(customField));
	}
}
