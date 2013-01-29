/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

/**
 * Filter factory class for a specific filter type
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public abstract class FilterTypeFactory {

	private String name;

	protected FilterTypeFactory(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract Filter createFilter();
}
