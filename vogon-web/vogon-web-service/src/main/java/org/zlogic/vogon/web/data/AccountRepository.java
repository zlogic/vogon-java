/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.VogonUser;

/**
 * The accounts JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface AccountRepository extends JpaRepository<FinanceAccount, Long> {

	/**
	 * Finds an account by its VogonUser owner and ID
	 *
	 * @param owner the VogonUser owner
	 * @param id the account ID
	 * @return account for owner and id
	 */
	public FinanceAccount findByOwnerAndId(VogonUser owner, Long id);

	/**
	 * Finds accounts by their VogonUser owner
	 *
	 * @param owner the VogonUser owner
	 * @param pageable the Pageable object
	 * @return accounts for owner
	 */
	public Page<FinanceAccount> findByOwner(VogonUser owner, Pageable pageable);

	/**
	 * Finds accounts by their VogonUser owner
	 *
	 * @param owner the VogonUser owner
	 * @return accounts for owner
	 */
	public Collection<FinanceAccount> findByOwner(VogonUser owner);
}
