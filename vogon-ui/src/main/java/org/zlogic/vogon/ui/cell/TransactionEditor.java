/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.vogon.ui.cell;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.stage.Popup;
import org.zlogic.vogon.ui.ExceptionHandler;
import org.zlogic.vogon.ui.TransactionComponentsController;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;

/**
 * Transactions components editor. Expands from a simple string to a
 * full-featured editor.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class TransactionEditor extends TableCell<TransactionModelAdapter, AmountModelAdapter> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TransactionEditor.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Exception handler
	 */
	private ObjectProperty<ExceptionHandler> exceptionHandler;
	/**
	 * The editor parent container
	 */
	private Parent editor;
	/**
	 * The popup editor
	 */
	private Popup popup;
	/**
	 * The editor controller
	 */
	private TransactionComponentsController componentsController;
	/**
	 * The DataManager instance
	 */
	private DataManager dataManager;
	/**
	 * The transaction currently being edited (used to filter and ignore updates
	 * while editing)
	 */
	private TransactionModelAdapter editedTransaction;
	/**
	 * True if cancelEdit() should be ignored (used to filter and ignore updates
	 * while editing)
	 */
	private boolean doNotCancelEdit = false;
	/**
	 * Listener for changes in the "OK" property
	 */
	protected ChangeListener<Boolean> okPropertyListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
			getStyleClass().removeAll("transaction-invalid", "transaction-valid");//NOI18N
			getStyleClass().add(newValue ? "transaction-valid" : "transaction-invalid");//NOI18N
		}
	};
	/**
	 * Listener for changes in the "Value" property
	 */
	protected ChangeListener<AmountModelAdapter> amountPropertyListener = new ChangeListener<AmountModelAdapter>() {
		@Override
		public void changed(ObservableValue<? extends AmountModelAdapter> ov, AmountModelAdapter oldValue, AmountModelAdapter newValue) {
			setText(getString());
		}
	};

	/**
	 * Constructs a Transaction properties editor/viewer
	 *
	 * @param dataManager the DataManager to be used
	 * @param exceptionHandler the exception handler
	 */
	public TransactionEditor(DataManager dataManager, ObjectProperty<ExceptionHandler> exceptionHandler) {
		this.dataManager = dataManager;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Prepares the cell for editing and shows the popup control
	 */
	@Override
	public void startEdit() {
		super.startEdit();

		if (editor == null)
			createEditor();

		editedTransaction = (TransactionModelAdapter) getTableRow().getItem();
		componentsController.setTransaction(editedTransaction);

		Point2D bounds = localToScene(0, getHeight());
		popup.show(this, getScene().getWindow().getX() + getScene().getX() + bounds.getX(), getScene().getWindow().getY() + getScene().getY() + bounds.getY());
	}

	/**
	 * Cancels cell editing and hides the popup control
	 */
	@Override
	public void cancelEdit() {
		if (doNotCancelEdit)
			return;
		super.cancelEdit();
		setText(getString());
		popup.hide();
		editedTransaction = null;
	}

	/**
	 * Commits the edit and hides the popup control
	 *
	 * @param item the updated item
	 */
	@Override
	public void commitEdit(AmountModelAdapter item) {
		super.commitEdit(item);
		setText(getString());
		popup.hide();
		editedTransaction = null;
	}

	/**
	 * Performs an item update
	 *
	 * @param item the updated TransactionModelAdapter instance
	 * @param empty true if the item is empty
	 */
	@Override
	public void updateItem(AmountModelAdapter item, boolean empty) {
		if (item != null) {
			item.okProperty().removeListener(okPropertyListener);
		}
		boolean setDoNotCancelEdit = editedTransaction != null && editedTransaction.equals(getTableRow().getItem());
		try {
			if (setDoNotCancelEdit)
				doNotCancelEdit = true;
			super.updateItem(item, empty);
		} finally {
			if (setDoNotCancelEdit)
				doNotCancelEdit = false;
		}
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			setText(getString());
			item.okProperty().addListener(okPropertyListener);
			okPropertyListener.changed(null, null, item.okProperty().get());
		}
	}

	/**
	 * Creates the Transaction components cell editor
	 */
	private void createEditor() {
		FXMLLoader loader = new FXMLLoader(TransactionModelAdapter.class.getResource("TransactionComponents.fxml")); //NOI18N
		loader.setResources(ResourceBundle.getBundle("org/zlogic/vogon/ui/messages")); //NOI18N
		loader.setLocation(TransactionComponentsController.class.getResource("TransactionComponents.fxml")); //NOI18N
		try {
			editor = (Parent) loader.load();
			editor.autosize();
			componentsController = loader.getController();
			componentsController.setDataManager(dataManager);
			componentsController.exceptionHandlerProperty().bind(exceptionHandler);
			popup = new Popup();
			popup.getContent().add(editor);
		} catch (IOException ex) {
			log.log(Level.SEVERE, null, ex);
			if (exceptionHandler.get() != null)
				exceptionHandler.get().showException(MessageFormat.format(messages.getString("CANNOT_LOAD_TRANSACTION_EDITOR"), new Object[]{ex.getMessage()}), ex);
		}
	}

	/**
	 * Returns the string value of the edited property (before editing)
	 *
	 * @return the string value of the edited property
	 */
	protected String getString() {
		return getItem() == null ? "" : getItem().toString();//NOI18N
	}
}
