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
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonUser;

/**
 * The transactions JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface TransactionRepository extends JpaRepository<FinanceTransaction, Long> {

	/**
	 * Finds transactions by their VogonUser owner
	 *
	 * @param owner the VogonUser owner
	 * @param pageable the Pageable object
	 * @return transactions for owner
	 */
	public Page<FinanceTransaction> findByOwner(VogonUser owner, Pageable pageable);

	/**
	 * Finds transactions by their VogonUser owner
	 *
	 * @param owner the VogonUser owner
	 * @return transactions for owner
	 */
	public Collection<FinanceTransaction> findByOwner(VogonUser owner);
}
