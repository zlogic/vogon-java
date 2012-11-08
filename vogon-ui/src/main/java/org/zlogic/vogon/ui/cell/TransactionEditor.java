/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.ui.MessageDialog;
import org.zlogic.vogon.ui.TransactionComponentsController;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;

/**
 * Transactions components editor. Expands from a simple string to a
 * full-featured editor.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionEditor extends TableCell<TransactionModelAdapter, TransactionModelAdapter> {

	/**
	 * The editor parent container
	 */
	protected VBox editor;
	/**
	 * The editor controller
	 */
	protected TransactionComponentsController componentsController;
	/**
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
	/**
	 * Cell alignment in view (not edit) state
	 */
	protected Pos alignment;

	/**
	 * Constructs a Transaction properties editor/viewer
	 *
	 * @param financeData the FinanceData to be used
	 */
	public TransactionEditor(FinanceData financeData) {
		this(financeData, null);
		this.financeData = financeData;
	}

	/**
	 * Constructs a Transaction properties editor/viewer
	 *
	 * @param financeData the FinanceData to be used
	 * @param alignment the cell alignment in view state
	 */
	public TransactionEditor(FinanceData financeData, Pos alignment) {
		if (alignment != null)
			setAlignment(alignment);
		this.financeData = financeData;
	}

	/**
	 * Prepares the cell for editing
	 */
	@Override
	public void startEdit() {
		super.startEdit();

		if (editor == null)
			createEditor();
		setText(null);
		componentsController.setFinanceData(financeData);
		componentsController.setTransaction(getItem().getTransaction());
		setGraphic(editor);
	}

	/**
	 * Cancels cell editing
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setStatusColor();
		setGraphic(null);
	}

	/**
	 * Performs an item update
	 *
	 * @param item the updated TransactionModelAdapter instance
	 * @param empty true if the item is empty
	 */
	@Override
	public void updateItem(TransactionModelAdapter item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				setText(null);
				setGraphic(editor);
			} else {
				setText(getString());
				setStatusColor();
				setGraphic(null);
			}
		}
	}

	/*
	 * Creates the Transaction components cell editor
	 */
	private void createEditor() {
		FXMLLoader loader = new FXMLLoader(TransactionModelAdapter.class.getResource("TransactionComponents.fxml")); //NOI18N
		loader.setResources(ResourceBundle.getBundle("org/zlogic/vogon/ui/messages")); //NOI18N
		loader.setLocation(TransactionComponentsController.class.getResource("TransactionComponents.fxml")); //NOI18N
		try {
			editor = (VBox) loader.load();
			editor.autosize();
			componentsController = loader.getController();
		} catch (IOException ex) {
			Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Sets the status color, if the property can return its status
	 */
	protected void setStatusColor() {
		if (getItem() instanceof CellStatus)
			if (!((CellStatus) getItem()).isOK())
				setTextFill(Color.RED);
	}

	/**
	 * Returns the string value of the edited property (before editing)
	 *
	 * @return the string value of the edited property
	 */
	protected String getString() {
		return getItem() == null ? "" : getItem().getAmount().toString();
	}
}
