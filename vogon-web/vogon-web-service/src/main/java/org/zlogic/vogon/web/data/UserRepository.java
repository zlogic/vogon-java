/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zlogic.vogon.data.VogonUser;

/**
 * The users JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface UserRepository extends JpaRepository<VogonUser, Long> {

}
