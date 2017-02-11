/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.VogonUser;

/**
 * Spring JPA Repository specification for filtering of transactions
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class TransactionFilterSpecification implements Specification<FinanceTransaction> {

	/**
	 * The transaction owner
	 */
	private VogonUser owner;
	/**
	 * The description substring filter
	 */
	private String filterDescription;
	/**
	 * The date filter
	 */
	private Date filterDate;
	/**
	 * The tags filter
	 */
	private Set<String> filterTags;

	/**
	 * Constructs a default TransactionFilterSpecification for a user
	 *
	 * @param owner the VogonUser owner
	 */
	public TransactionFilterSpecification(VogonUser owner) {
		this.owner = owner;
	}

	/**
	 * Builds the Predicate
	 *
	 * @param root the FinanceTransaction query root
	 * @param cq the CriteriaQuery instance
	 * @param cb the CriteriaBuilder instance
	 * @return the Predicate of this filter
	 */
	@Override
	public Predicate toPredicate(Root<FinanceTransaction> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
		Predicate ownerPredicate = cb.equal(root.get(FinanceTransaction_.owner), owner);
		Predicate descriptionPredicate = filterDescription != null
				? cb.like(cb.lower(root.get(FinanceTransaction_.description)), filterDescription.toLowerCase())
				: cb.conjunction();
		Predicate tagsPredicate = cb.conjunction();
		if (filterTags != null && !filterTags.isEmpty()) {
			Set<String> filterTagsLowercase = new HashSet<>();
			for (String tag : filterTags)
				filterTagsLowercase.add(tag.toLowerCase());
			tagsPredicate = cb.lower(root.join(FinanceTransaction_.tags)).in(cb.literal(filterTagsLowercase));
		}
		Predicate datePredicate = filterDate != null
				? cb.equal(root.get(FinanceTransaction_.transactionDate), new java.sql.Date(filterDate.getTime()))
				: cb.conjunction();
		return cb.and(ownerPredicate, descriptionPredicate, datePredicate, tagsPredicate);
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the owner user
	 *
	 * @return the owner user
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Returns the description substring filter
	 *
	 * @return the description substring filter
	 */
	public String getFilterDescription() {
		return filterDescription;
	}

	/**
	 * Sets the description substring filter
	 *
	 * @param filterDescription the description substring filter
	 */
	public void setFilterDescription(String filterDescription) {
		this.filterDescription = filterDescription;
	}

	/**
	 * Returns the date filter
	 *
	 * @return the date filter
	 */
	public Date getFilterDate() {
		return filterDate;
	}

	/**
	 * Sets the date filter
	 *
	 * @param filterDate the date to be filtered
	 */
	public void setFilterDate(Date filterDate) {
		//Convert to local time (OpenShift and other non-UTC servers workaround)
		if (filterDate != null) {
			Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC")); //NOI18N
			calendar.setTime(filterDate);
			Calendar newCalendar = new GregorianCalendar();
			newCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
			newCalendar.set(Calendar.MILLISECOND, 0);
			filterDate = newCalendar.getTime();
		}
		this.filterDate = filterDate;
	}

	/**
	 * Returns the tags to filter
	 *
	 * @return the tags to filter
	 */
	public Set<String> getFilterTags() {
		return filterTags;
	}

	/**
	 * Sets the tags to filter
	 *
	 * @param filterTags the tags to filter
	 */
	public void setFilterTags(Set<String> filterTags) {
		this.filterTags = filterTags;
	}
}
