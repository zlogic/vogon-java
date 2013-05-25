/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui.filter;

import org.zlogic.att.ui.filter.adapters.FilterAdapter;

/**
 * FilterAdapter factory class for a specific filter type. Can also be used to
 * classify filter types.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public abstract class FilterTypeFactory {

	/**
	 * The filter type name
	 */
	protected String name;

	/**
	 * Constructs a FilterTypeFactory with the specified name
	 *
	 * @param name the filter type name
	 */
	protected FilterTypeFactory(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Creates a new filter
	 *
	 * @return the new created filter
	 */
	public abstract FilterAdapter createFilter();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}
