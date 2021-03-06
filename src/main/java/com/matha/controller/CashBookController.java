package com.matha.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.matha.domain.CashBook;
import com.matha.domain.CashHead;
import com.matha.service.SchoolService;
import com.matha.util.LoadUtils;
import com.matha.util.UtilConstants;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class CashBookController {

	@Autowired
	private SchoolService srvc;

	@FXML
	private DatePicker fromDate;

	@FXML
	private TableView<CashBook> txnData;

	@FXML
	private DatePicker toDate;

	@FXML
	private TextField transactionId;

	@FXML
	private TextField transactionDesc;
	
	@FXML 
	private ComboBox<CashHead> type; 
	
	@FXML
	protected void initialize()
	{
		List<CashBook> transactions = srvc.getAllTransactions();
		this.txnData.setItems(FXCollections.observableList(transactions));
	}

	@FXML
	void addTransaction(ActionEvent event) {

		try {
			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.addTransactionFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void deleteTransaction(ActionEvent event) {

		CashBook selectedOrder = this.txnData.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Order Confirmation");
		alert.setHeaderText("Are you sure you want to delete the transaction?");
		alert.setContentText("Click Ok to Delete");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			srvc.deleteTransaction(selectedOrder);
			this.initialize();
		}
	

	}

	@FXML
	void editTransaction(ActionEvent event) {

		try {
			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.addTransactionFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddTxnController ctrl = createOrderLoader.getController();
			ctrl.updateFormData(this.txnData.getSelectionModel().getSelectedItem());			
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@FXML
	void addCashHead(ActionEvent event) {

		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add Cash Head");
		dialog.setHeaderText("Please provide Cash Head Details");
		dialog.setContentText("Cash Head: ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    String cashHead = result.get();
		    srvc.saveCashHead(cashHead);
		}
	}

	@FXML
	void addMode(ActionEvent event) {

	}

	@FXML
	void idSearch(KeyEvent event) {

	}

	@FXML
	void descSearch(KeyEvent event) {

	}
	
	@FXML
	void search(ActionEvent event)
	{
		List<CashBook> transactions = srvc.searchTransactions(fromDate.getValue(), toDate.getValue(), this.transactionId.getText(), this.transactionDesc.getText(), this.type.getSelectionModel().getSelectedItem());
		this.txnData.setItems(FXCollections.observableList(transactions));
		
	}

	private void prepareAndShowStage(ActionEvent e, Scene childScene) {
		Stage stage = LoadUtils.loadChildStage(e, childScene);
		stage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent event) {
				initialize();
			}
		});
		stage.show();
	}

}
