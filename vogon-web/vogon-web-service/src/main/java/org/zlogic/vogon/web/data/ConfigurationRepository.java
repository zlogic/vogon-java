/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zlogic.vogon.data.ConfigurationElement;

/**
 * The configuration elements JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationElement, String> {
}
