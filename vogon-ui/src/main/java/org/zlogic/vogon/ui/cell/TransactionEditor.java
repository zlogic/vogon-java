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
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
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
	protected Parent editor;
	/**
	 * The popup editor
	 */
	protected Popup popup;
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
	 * Prepares the cell for editing and shows the popup control
	 */
	@Override
	public void startEdit() {
		super.startEdit();

		if (editor == null)
			createEditor();

		componentsController.setFinanceData(financeData);
		componentsController.setTransaction(getItem().getTransaction());

		Point2D bounds = localToScene(0, getHeight());
		popup.show(this, getScene().getWindow().getX() + getScene().getX() + bounds.getX(), getScene().getWindow().getY() + getScene().getY() + bounds.getY());
	}

	/**
	 * Cancels cell editing and hides the popup control
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setStatusColor();
		popup.hide();
	}

	/**
	 * Commits the edit and hides the popup control
	 * @param item the updated item
	 */
	@Override
	public void commitEdit(TransactionModelAdapter item) {
		super.commitEdit(item);
		setText(getString());
		setStatusColor();
		popup.hide();
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
			setText(getString());
			setStatusColor();
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
			editor = (Parent) loader.load();
			editor.autosize();
			componentsController = loader.getController();
			popup = new Popup();
			popup.getContent().add(editor);
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
