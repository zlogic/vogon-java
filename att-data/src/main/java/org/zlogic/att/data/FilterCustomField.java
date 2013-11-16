/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Tasks filter which filters the custom field value.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
@Entity
public class FilterCustomField extends Filter {

	/**
	 * The associated CustomField
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	private CustomField customField;
	/**
	 * The CustomField value for this filter
	 */
	private String customFieldValue;

	/**
	 * Default constructor
	 */
	protected FilterCustomField() {
	}

	/**
	 * Constructs a CustomField filter for a specific CustomField
	 *
	 * @param customField the associated CustomField
	 */
	protected FilterCustomField(CustomField customField) {
		this.customField = customField;
	}

	/**
	 * Returns the associated CustomField
	 *
	 * @return the associated CustomField
	 */
	public CustomField getCustomField() {
		return customField;
	}

	/**
	 * Returns the CustomField value for this filter
	 *
	 * @return the CustomField value for this filter
	 */
	public String getCustomFieldValue() {
		return customFieldValue;
	}

	/**
	 * Sets the CustomField value for this filter
	 *
	 * @param customFieldValue the new CustomField value for this filter
	 */
	public void setCustomFieldValue(String customFieldValue) {
		this.customFieldValue = customFieldValue;
	}

	@Override
	public Predicate getFilterPredicate(CriteriaBuilder criteriaBuilder, Root<Task> taskRoot) {
		if (customFieldValue == null || customFieldValue.isEmpty())
			return criteriaBuilder.conjunction();//This will be dealt with later
		MapJoin<Task, CustomField, String> customFieldJoin = taskRoot.join(Task_.customFields);
		return criteriaBuilder.and(
				criteriaBuilder.equal(customFieldJoin.key(), customField),
				criteriaBuilder.equal(customFieldJoin.value(), customFieldValue));
	}
}
