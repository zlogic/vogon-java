/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

/**
 * Interface for storing a single finance transaction
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public abstract class FinanceTransaction implements Serializable {
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The transaction ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected long id;
	/**
	 * Contains the expense description string
	 */
	protected String description;
	/**
	 * Contains the expense tags
	 */
	protected String[] tags;

	/**
	 * Contains the related accounts and the transaction's distribution into them
	 */
	@OneToMany
	protected List<TransactionComponent> components;

	/**
	 * Contains the transaction date
	 */
	@Temporal(javax.persistence.TemporalType.DATE)
	protected Date transactionDate;

	/**
	 * The transaction amount
	 */
	protected long amount;

	/**
	 * Updates the transaction's amount from its components
	 */
	public void updateAmounts(){
		amount = 0;
		for (TransactionComponent component : components)
			amount += component.getRawAmount();
	}

	/**
	 * Returns a list of all components associated with an account
	 * 
	 * @param account The account to search
	 * @return The list of transaction components associated with the searched account
	 */
	public List<TransactionComponent> getComponentsForAccount(FinanceAccount account) {
		LinkedList<TransactionComponent> foundComponents = new LinkedList<>();
		for (TransactionComponent component : components)
			if(component.getAccount()==account)
				foundComponents.add(component);
		return foundComponents;
	}

	/**
	 * Delete the listed components
	 * 
	 * @param components the components to delete
	 */
	public void removeComponents(List<TransactionComponent> components){
		this.components.removeAll(components);
		updateAmounts();
	}

	/*
	 * Getters/setters
	 */

	/**
	 * Returns the raw amount (should be divided by 100 to get the real amount)
	 *
	 * @return the transaction amount
	 */
	public double getRawAmount() {
		return amount;
	}

	/**
	 * Returns the transaction amount
	 *
	 * @return the transaction amount
	 */
	public double getAmount() {
		return amount/100.0D;
	}


	/**
	 * Adds a tag
	 * @param tag the tag to add
	 */
	void addTag(String tag) {
		tags = Arrays.copyOf(tags, tags.length + 1);
		tags[tags.length - 1] = tag;
	}

	/**
	 * Returns the transaction's description
	 *
	 * @return the transaction's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the transaction's description
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the transaction's tags
	 *
	 * @return the transaction's tags
	 */
	public String[] getTags() {
		return tags;
	}

	/**
	 * Returns the transaction date
	 *
	 * @return the transaction date
	 */
	public Date getDate() {
		return transactionDate;
	}

	/**
	 * Sets the transaction date
	 *
	 * @param date The transaction date
	 */
	public void setDate(Date date) {
		this.transactionDate = date;
	}
}
