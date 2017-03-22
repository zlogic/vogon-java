/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.Version;

/**
 * Interface for storing a single finance transaction
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class FinanceTransaction implements Serializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");

	/**
	 * The transaction type
	 */
	public enum Type {

		/**
		 * Income or expense
		 */
		EXPENSEINCOME,
		/**
		 * Transfer
		 */
		TRANSFER,
		/**
		 * Unknown (default) value
		 */
		UNDEFINED
	};
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The transaction ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected Long id;
	/**
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The transaction owner
	 */
	@ManyToOne
	@JoinColumn
	protected VogonUser owner;
	/**
	 * The transaction type
	 */
	protected Type type;
	/**
	 * Contains the expense description string
	 */
	protected String description;
	/**
	 * Contains the expense tags
	 */
	@ElementCollection
	protected Set<String> tags;
	/**
	 * Contains the related accounts and the transaction's distribution into
	 * them
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	@JoinColumn
	protected Set<TransactionComponent> components;
	/**
	 * Contains the transaction date
	 */
	@Temporal(javax.persistence.TemporalType.DATE)
	protected Date transactionDate;

	/**
	 * Default constructor
	 */
	public FinanceTransaction() {
		type = Type.UNDEFINED;
	}

	/**
	 * Creates a new FinanceTransaction
	 *
	 * @param owner the transaction owner
	 * @param description the transaction description
	 * @param tags the transaction tags
	 * @param date the transaction date
	 * @param type the transaction type
	 */
	public FinanceTransaction(VogonUser owner, String description, String[] tags, Date date, Type type) {
		//TODO: consider removing this
		this();
		this.description = description;
		FinanceTransaction.this.setTags(tags != null ? tags : new String[0]);
		this.transactionDate = date;
		this.components = new HashSet<>();
		this.type = type;
		FinanceTransaction.this.setOwner(owner);
	}

	/**
	 * Creates a new FinanceTransaction by merging another transaction's
	 * properties
	 *
	 * @param owner the transaction owner
	 * @param transaction the transaction from which to merge properties
	 */
	public FinanceTransaction(VogonUser owner, FinanceTransaction transaction) {
		//TODO: consider removing this
		this.components = new HashSet<>();
		FinanceTransaction.this.merge(transaction, false);
		FinanceTransaction.this.setOwner(owner);
	}

	/**
	 * Merges properties from another FinanceTransaction instance to this
	 * instance; only merges properties, and not components
	 *
	 * @param transaction the transaction from which to merge properties
	 */
	public void merge(FinanceTransaction transaction) {
		merge(transaction, true);
	}

	/**
	 * Merges properties from another FinanceTransaction instance to this
	 * instance; only merges properties, and not components
	 *
	 * @param transaction the transaction from which to merge properties
	 * @param verifyVersion true if a version mismatch would throw an exception
	 */
	protected void merge(FinanceTransaction transaction, boolean verifyVersion) {
		if (verifyVersion && version != transaction.version)
			throw new ConcurrentModificationException(messages.getString("TRANSACTION_WAS_ALREADY_UPDATED"));
		this.type = transaction.type;
		this.description = transaction.description;
		this.tags = new TreeSet<>();
		for (String tag : transaction.tags)
			this.tags.add(tag);
		this.transactionDate = (Date) transaction.transactionDate.clone();
	}

	/**
	 * Returns a list of all components
	 *
	 * @return the list of all transaction components
	 */
	public List<TransactionComponent> getComponents() {
		return new ArrayList<>(components);
	}

	/**
	 * Adds a TransactionComponent to this account, updating the balance if it's
	 * a new component
	 *
	 * @param component component to add
	 */
	void addComponent(TransactionComponent component) {
		if (components.add(component))
			component.setTransaction(this);
	}

	/**
	 * Removes a TransactionComponent fromm this account, updating the balance
	 * if it's was assigned to this account
	 *
	 * @param component component to remove
	 */
	void removeComponent(TransactionComponent component) {
		if (components.remove(component))
			component.setTransaction(null);
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Adds a tag
	 *
	 * @param tag the tag to add
	 */
	void addTag(String tag) {
		if (tags == null)
			tags = new TreeSet<>();
		tags.add(tag);
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
		return tags.toArray(new String[0]);
	}

	/**
	 * Sets the transaction's tags
	 *
	 * @param tags the new transaction's tags
	 */
	public void setTags(String... tags) {
		this.tags = new TreeSet<>(Arrays.asList(tags));
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
	 * @param date the transaction date
	 */
	public void setDate(Date date) {
		this.transactionDate = date;
	}

	/**
	 * Returns the transaction type
	 *
	 * @return the transaction type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the transaction type
	 *
	 * @param type the transaction type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Returns the transaction owner
	 *
	 * @return the transaction owner
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Sets the transaction owner
	 *
	 * @param owner the owner to set
	 */
	public void setOwner(VogonUser owner) {
		this.owner = owner;
	}

	/**
	 * Returns the ID for this class instance
	 *
	 * @return the ID for this class instance
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	protected void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FinanceTransaction)
			return id != null ? id.equals(((FinanceTransaction) obj).id) : false;
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
