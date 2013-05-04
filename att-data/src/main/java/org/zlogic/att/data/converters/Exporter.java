/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

import org.zlogic.att.data.PersistenceHelper;

/**
 * Interface for a generic exporter. The exporter should use the supplied entity
 * manager to obtain items to be exported. All configuration should be done in
 * the constructor.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface Exporter {

	/**
	 * Export data
	 *
	 * @param persistenceHelper the PersistenceHelper to be used for obtaining
	 * data
	 * @throws ExportException exception which happens during exporting
	 */
	public void exportData(PersistenceHelper persistenceHelper) throws ExportException;
}
