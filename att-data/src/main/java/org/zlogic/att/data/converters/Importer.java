/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

import javax.persistence.EntityManager;

/**
 * Interface for a generic importer. The importer should use the supplied entity
 * manager to persist new items or check existence of imported items. All
 * configuration should be done in the constructor.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface Importer {

	/**
	 * Import data
	 *
	 * @param entityManager the EntityManager to be used for storing new items
	 * and checking for duplicates
	 * @throws ImportException exception which happens during importing
	 */
	public void importData(EntityManager entityManager) throws ImportException;
}
