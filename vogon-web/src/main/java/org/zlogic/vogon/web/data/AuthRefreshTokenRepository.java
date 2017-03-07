/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zlogic.vogon.data.AuthRefreshToken;

/**
 * The OAuth2 Refresh Token JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, String> {

}
