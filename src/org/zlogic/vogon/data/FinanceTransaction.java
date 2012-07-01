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
import javax.persistence.OrderBy;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
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
	@OrderBy("id ASC")
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
	 * Adds component to this account
	 * 
	 * @param component the component to add
	 */
	public void addComponent(TransactionComponent component){
		this.components.add(component);
		updateAmounts();

		if(component.getAccount()!=null)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Adds components to this account
	 * 
	 * @param components the components to add
	 */
	public void addComponents(List<TransactionComponent> components){
		this.components.addAll(components);
		updateAmounts();

		for (TransactionComponent component : components)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Removes a transaction component
	 * 
	 * @param component the component to be removed
	 */
	public void removeComponent(TransactionComponent component) {
		if(!components.contains(component))
			return;
		component.getAccount().updateRawBalance(-component.getRawAmount());
		component.setAccount(null);
		component.setTransaction(null);
		components.remove(component);
		updateAmounts();
	}

	/**
	 * Removes all transaction components
	 * 
	 */
	public void removeAllComponents() {
		for(TransactionComponent component : components){
			component.getAccount().updateRawBalance(-component.getRawAmount());
			component.setAccount(null);
			component.setTransaction(null);
		}
		components.clear();
		updateAmounts();
	}

	/**
	 * Updates the transaction's amount from its components
	 */
	public abstract void updateAmounts();

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
	 * Returns a list of all components
	 * 
	 * @return The list of all transaction components
	 */
	public List<TransactionComponent> getComponents() {
		LinkedList<TransactionComponent> foundComponents = new LinkedList<>();
		foundComponents.addAll(components);
		return foundComponents;
	}

	/**
	 * Sets a new amount for a component and updates the transaction and account balance
	 * 
	 * @param component the component to be updated
	 * @param amount the new amount
	 */
	public void updateComponentRawAmount(TransactionComponent component,long amount) {
		if(!components.contains(component))
			return;
		long deltaAmount = amount-component.getRawAmount();
		component.setRawAmount(amount);
		updateAmounts();
		if(component.getAccount()!=null)
			component.getAccount().updateRawBalance(deltaAmount);
	}

	/**
	 * Sets a new account for a component and updates the account balance
	 * 
	 * @param component the component to be updated
	 * @param account the new account
	 */
	public void updateComponentAccount(TransactionComponent component,FinanceAccount account) {
		if(!components.contains(component))
			return;
		if(component.getAccount()!=null)
			component.getAccount().updateRawBalance(-component.getRawAmount());
		component.setAccount(account);
		component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Returns the number of components
	 * 
	 * @return The number of components in this transaction
	 */
	public int getComponentsCount() {
		return components.size();
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
	 * Sets the transaction's tags
	 *
	 * @param tags the new transaction's tags
	 */
	public void setTags(String[] tags) {
		this.tags = tags;
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
