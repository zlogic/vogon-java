/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Implements a transaction (amount associated with a specific account)
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class TransactionComponent implements Serializable {

    /**
     * The transaction ID (only for persistence)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    /**
     * The account
     */
    @ManyToOne
    protected FinanceAccount account;
    /**
     * The amount this component changes the account's balance
     */
    protected Double amount;

    /**
     * Default constructor for a transaction component
     */
    public TransactionComponent() {
    }

    /**
     * Constructor for a transaction component
     *
     * @param account The account
     * @param amount The amount which this component modifies the account, can
     * be both negative and positive
     */
    public TransactionComponent(FinanceAccount account, double amount) {
	this.account = account;
	this.amount = amount;
    }

    /*
     * Getters/setters
     */
    /**
     * @return the account
     */
    public FinanceAccount getAccount() {
	return account;
    }

    /**
     * @return the amount
     */
    public Double getAmount() {
	return amount;
    }
}
