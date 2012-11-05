/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.zlogic.vogon.ui.TransactionModelAdapter;

/**
 *
 * @author Dmitry
 */
public class TransactionEditor extends TableCell<TransactionModelAdapter, TransactionModelAdapter> {
	protected VBox editor;
	protected TransactionComponentsController componentsController;
	protected FinanceData financeData;
	protected Pos alignment;
	
	public TransactionEditor(FinanceData financeData){
		this(financeData,null);
		this.financeData = financeData;
	}
	
	public TransactionEditor(FinanceData financeData,Pos alignment) {
		if(alignment!=null)
			setAlignment(alignment);
		this.financeData = financeData;
	}
	
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

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setStatusColor();
		setGraphic(null);
	}

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

	protected void setStatusColor(){
		if(getItem() instanceof CellStatus)
			if(!((CellStatus)getItem()).isOK())
				setTextFill(Color.RED);
	}
	
	protected String getString() {
		return getItem() == null ? "" : getItem().getAmount().toString();
	}
}
