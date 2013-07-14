/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.data.converters;

import javax.persistence.EntityManager;
import org.zlogic.att.data.PersistenceHelper;

/**
 * Interface for a generic importer. The importer should use the supplied entity
 * manager to persist new items or check existence of imported items. All
 * configuration should be done in the constructor.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public interface Importer {

	/**
	 * Import data
	 *
	 * @param persistenceHelper the PersistenceHelper to be used for obtaining
	 * data
	 * @param entityManager the EntityManager to be used for storing new items
	 * and checking for duplicates
	 * @throws ImportException exception which happens during importing
	 */
	public void importData(PersistenceHelper persistenceHelper, EntityManager entityManager) throws ImportException;
}
