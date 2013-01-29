/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.filter.ui.FilterSelectableValue;

/**
 * Filter for custom fields
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CustomFieldFilter implements FilterSelectableValue<String> {

	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	/**
	 * The filter type
	 */
	private FilterTypeFactory filterType;
	/**
	 * The associated custom field
	 */
	private CustomFieldAdapter customField;
	/**
	 * The filter value
	 */
	private ObjectProperty<String> value = new SimpleObjectProperty<>();

	/**
	 * Constructs a CompletedFilter
	 *
	 * @param taskManager the TaskManager reference
	 * @param filterType the filter type (creator of this objevt)
	 * @param customField the associated custom field
	 */
	public CustomFieldFilter(TaskManager taskManager, FilterTypeFactory filterType, CustomFieldAdapter customField) {
		this.taskManager = taskManager;
		this.filterType = filterType;
		this.customField = customField;
		this.value.set(""); //NOI18N
	}

	@Override
	public FilterTypeFactory getType() {
		return filterType;
	}

	@Override
	public ObjectProperty<String> valueProperty() {
		return value;
	}

	@Override
	public ObservableList<String> getAllowedValues() {
		return taskManager.getCustomFieldValues(customField);
	}
}
