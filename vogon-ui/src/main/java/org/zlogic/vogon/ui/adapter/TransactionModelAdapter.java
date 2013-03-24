/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactedChange;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 * Transactions helper class for rendering with property change detection.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionModelAdapter implements CellStatus {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The associated transaction
	 */
	private FinanceTransaction transaction;
	/**
	 * The DataManager instance
	 */
	private DataManager dataManager;
	/**
	 * The transaction description property
	 */
	private final StringProperty description = new SimpleStringProperty();
	/**
	 * The transaction tags property
	 */
	private final ListProperty<String> tags = new SimpleListProperty<>(FXCollections.observableArrayList(new LinkedList<String>()));
	/**
	 * The transaction date property
	 */
	private final ObjectProperty<Date> date = new SimpleObjectProperty<>();
	/**
	 * The transaction type property
	 */
	private final ObjectProperty<FinanceTransaction.Type> type = new SimpleObjectProperty<>();
	/**
	 * The transaction amount property
	 */
	private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();
	/**
	 * The transaction is OK (e.g. zero sum for a transfer transaction)
	 */
	private BooleanProperty isOkProperty = new SimpleBooleanProperty();
	/**
	 * The transaction account(s) property (rendered string)
	 */
	private final StringProperty accountName = new SimpleStringProperty();
	private ObservableList<TransactionComponentModelAdapter> components = FXCollections.observableList(new LinkedList<TransactionComponentModelAdapter>());
	private ChangeListener<String> descriptionListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private String description;

				public TransactedChange setDescription(String description) {
					this.description = description;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setTransaction(dataManager.getFinanceData().getUpdatedTransactionFromDatabase(entityManager, transaction));
					getTransaction().setDescription(description);
				}
			}.setDescription(newValue));
			updateFxProperties();
		}
	};
	private ListChangeListener<String> tagsListener = new ListChangeListener<String>() {
		@Override
		public void onChanged(ListChangeListener.Change<? extends String> change) {
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				@Override
				public void performChange(EntityManager entityManager) {
					String[] updatedTags = tags.toArray(new String[0]);
					setTransaction(dataManager.getFinanceData().getUpdatedTransactionFromDatabase(entityManager, transaction));
					getTransaction().setTags(updatedTags);
				}
			});
			updateFxProperties();
		}
	};
	private ChangeListener<Date> dateListener = new ChangeListener<Date>() {
		@Override
		public void changed(ObservableValue<? extends Date> ov, Date oldValue, Date newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private Date date;

				public TransactedChange setDate(Date date) {
					this.date = date;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setTransaction(dataManager.getFinanceData().getUpdatedTransactionFromDatabase(entityManager, transaction));
					getTransaction().setDate(date);
				}
			}.setDate(newValue));
			updateFxProperties();
		}
	};
	private ChangeListener<FinanceTransaction.Type> typeListener = new ChangeListener<FinanceTransaction.Type>() {
		@Override
		public void changed(ObservableValue<? extends FinanceTransaction.Type> ov, FinanceTransaction.Type oldValue, FinanceTransaction.Type newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private FinanceTransaction.Type type;

				public TransactedChange setType(FinanceTransaction.Type type) {
					this.type = type;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setTransaction(dataManager.getFinanceData().getUpdatedTransactionFromDatabase(entityManager, transaction));
					getTransaction().setType(type);
				}
			}.setType(newValue));
			updateFxProperties();
		}
	};

	/**
	 * Default constructor
	 *
	 * @param transaction the associated transaction
	 * @param financeData the associated FinanceData instance
	 */
	public TransactionModelAdapter(FinanceTransaction transaction, DataManager dataManager) {
		this.transaction = transaction;
		this.dataManager = dataManager;
		((TransactionModelAdapter) this).updateFxProperties();
		updateComponents();
	}

	public TransactionComponentModelAdapter createComponent() {
		TransactionComponent component = dataManager.getFinanceData().createTransactionComponent(null, transaction, 0);
		TransactionComponentModelAdapter componentAdapter = new TransactionComponentModelAdapter(component, dataManager);
		components.add(componentAdapter);
		return componentAdapter;
	}

	public void deleteComponent(TransactionComponentModelAdapter component) {
		dataManager.getFinanceData().deleteTransactionComponent(component.getTransactionComponent());
		components.remove(component);

		updateFromDatabase();
		updateFxProperties();
	}

	/**
	 * Returns this class instance (used in JavaFX to use a
	 * TransactionModelAdapter as its own property)
	 *
	 * @return this class instance
	 */
	public TransactionModelAdapter getModelTransaction() {
		return this;
	}

	/**
	 * Returns the associated transaction
	 *
	 * @return the associated transaction
	 */
	public FinanceTransaction getTransaction() {
		return transaction;
	}

	protected void setTransaction(FinanceTransaction transaction) {
		this.transaction = transaction;
	}

	protected void updateFromDatabase() {
		dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
			@Override
			public void performChange(EntityManager entityManager) {
				setTransaction(dataManager.getFinanceData().getUpdatedTransactionFromDatabase(entityManager, transaction));
			}
		});
	}

	/**
	 * Returns the converter for tags.
	 *
	 * @return the converter for tags
	 */
	public static StringConverter<List<String>> getTagsListConverter() {
		return new StringConverter<List<String>>() {
			@Override
			public String toString(List<String> t) {
				return org.zlogic.vogon.data.Utils.join(t, ","); //NOI18N
			}

			@Override
			public List<String> fromString(String string) {
				return FXCollections.observableList(new LinkedList<>(Arrays.asList(string.split(","))));//NOI18N
			}
		};
	}

	/**
	 * Returns the transaction amount
	 *
	 * @return the transaction amount
	 */
	public AmountModelAdapter getAmount() {
		List<Currency> transactionCurrencies = transaction.getCurrencies();
		Currency currency;
		double amountValue;
		if (transactionCurrencies.size() == 1) {
			amountValue = transaction.getAmount();
			currency = transactionCurrencies.get(0);
		} else {
			amountValue = dataManager.getFinanceData().getAmountInCurrency(transaction, dataManager.getDefaultCurrency().get().getCurrency());
			currency = dataManager.getDefaultCurrency().get().getCurrency();
		}
		return new AmountModelAdapter(amountValue, transaction.isAmountOk(), currency, transactionCurrencies.size() != 1, transaction.getType());
	}

	/**
	 * Returns the transactions accounts (rendered)
	 *
	 * @return the transactions accounts
	 */
	public String getAccountName() {
		//Build the accounts string
		if (transaction.getType() == FinanceTransaction.Type.EXPENSEINCOME) {
			List<FinanceAccount> accountsList = transaction.getAccounts();
			StringBuilder builder = new StringBuilder();
			for (FinanceAccount account : accountsList) {
				AccountModelAdapter accountAdapter = dataManager.findAccountAdapter(account);
				FinanceAccount currentAccount = (accountAdapter != null && accountAdapter.getAccount() != null) ? accountAdapter.getAccount() : null;
				builder.append(account != accountsList.get(0) ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
			}
			return builder.toString();
		} else if (transaction.getType() == FinanceTransaction.Type.TRANSFER) {
			FinanceAccount[] toAccounts = transaction.getToAccounts();
			FinanceAccount[] fromAccounts = transaction.getFromAccounts();
			StringBuilder builder = new StringBuilder();
			if (fromAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount account : fromAccounts) {
					AccountModelAdapter accountAdapter = dataManager.findAccountAdapter(account);
					FinanceAccount currentAccount = (account != null && accountAdapter.getAccount() != null) ? accountAdapter.getAccount() : null;
					builder.append(account != fromAccounts[0] ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				}
				builder.append(")"); //NOI18N
			} else if (fromAccounts.length == 1) {
				AccountModelAdapter accountAdapter = dataManager.findAccountAdapter(fromAccounts[0]);
				FinanceAccount currentAccount = (accountAdapter != null && accountAdapter.getAccount() != null) ? accountAdapter.getAccount() : null;
				builder.append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT"));
			}
			builder.append("->"); //NOI18N
			if (toAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount account : toAccounts) {
					AccountModelAdapter accountAdapter = dataManager.findAccountAdapter(account);
					FinanceAccount currentAccount = (account != null && accountAdapter.getAccount() != null) ? accountAdapter.getAccount() : null;
					builder.append(account != toAccounts[0] ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				}
				builder.append(")"); //NOI18N
			} else if (toAccounts.length == 1) {
				AccountModelAdapter accountAdapter = dataManager.findAccountAdapter(toAccounts[0]);
				FinanceAccount currentAccount = (accountAdapter != null && accountAdapter.getAccount() != null) ? accountAdapter.getAccount() : null;
				builder.append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT"));
			}
			return builder.toString();
		} else
			return ""; //NOI18N
	}

	/**
	 * Returns the transaction description property
	 *
	 * @return the transaction description property
	 */
	public StringProperty descriptionProperty() {
		return description;
	}

	/**
	 * Returns the transaction tags property
	 *
	 * @return the transaction tags property
	 */
	public ListProperty<String> tagsProperty() {
		return tags;
	}

	/**
	 * Returns the transaction date property
	 *
	 * @return the transaction date property
	 */
	public ObjectProperty<Date> dateProperty() {
		return date;
	}

	/**
	 * Returns the transaction amount property
	 *
	 * @return the transaction amount property
	 */
	public ObjectProperty<AmountModelAdapter> amountProperty() {
		return amount;
	}

	/**
	 * Returns the transaction type property
	 *
	 * @return the transaction type property
	 */
	public ObjectProperty<FinanceTransaction.Type> typeProperty() {
		return type;
	}

	/**
	 * Returns the transaction accounts name property (rendered to string)
	 *
	 * @return the transaction accounts name property
	 */
	public StringProperty accountNameProperty() {
		return accountName;
	}

	@Override
	public BooleanProperty okProperty() {
		return isOkProperty;
	}

	public ObservableList<TransactionComponentModelAdapter> transactionComponentsProperty() {
		return components;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return this == null;
		if (transaction == null)
			return obj instanceof TransactionModelAdapter && ((TransactionModelAdapter) obj).transaction == null;
		if (!(obj instanceof TransactionModelAdapter))
			return false;
		TransactionModelAdapter adapter = (TransactionModelAdapter) obj;
		return transaction.getId() == adapter.transaction.getId();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + Objects.hashCode(this.transaction.getId());
		return hash;
	}

	private TransactionComponentModelAdapter findComponent(TransactionComponent component) {
		for (TransactionComponentModelAdapter currentComponent : components)
			if (currentComponent.getTransactionComponent().equals(component))
				return currentComponent;
		return null;
	}

	/**
	 * Updates the components list
	 */
	private void updateComponents() {
		List<TransactionComponentModelAdapter> orphanedComponents = new LinkedList<>(components);
		for (TransactionComponent component : transaction.getComponents()) {
			TransactionComponentModelAdapter existingComponent = findComponent(component);
			if (existingComponent == null) {
				components.add(new TransactionComponentModelAdapter(component, dataManager));
			} else {
				orphanedComponents.remove(existingComponent);
				existingComponent.setTransactionComponent(component);
				existingComponent.updateFxProperties();
			}
		}
		components.removeAll(orphanedComponents);
	}

	/**
	 * Updates the properties from the current transaction, causing
	 * ChangeListeners to trigger.
	 */
	protected void updateFxProperties() {
		//Remove property change listeners
		description.removeListener(descriptionListener);
		tags.removeListener(tagsListener);
		date.removeListener(dateListener);
		type.removeListener(typeListener);
		if (transaction != null) {
			description.set(transaction.getDescription());
			date.set(transaction.getDate());
			tags.setAll(transaction.getTags());
			amount.set(getAmount());
			accountName.set(getAccountName());
			type.set(transaction.getType());
			isOkProperty.set(transaction.isAmountOk());
		}
		//Restore property change listeners
		description.addListener(descriptionListener);
		tags.addListener(tagsListener);
		date.addListener(dateListener);
		type.addListener(typeListener);
	}
}
