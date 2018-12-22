package com.matha.controller;

import com.matha.domain.*;
import com.matha.service.SchoolService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.matha.util.UtilConstants.*;
import static com.matha.util.Utils.*;
import static org.apache.commons.lang3.StringUtils.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AddReturnController
{

	private static final Logger LOGGER = LogManager.getLogger("AddReturnController");

	@Autowired
	private SchoolService schoolService;

	@FXML
	private DatePicker returnDate;

	@FXML
	private TextField creditNoteNum;

	@FXML
	private TextField notes;

	@FXML
	private Button cancelBtn;

	@FXML
	private Label schoolName;

	@FXML
	private TextField bookName;
	
	@FXML
	private TextField quantity;
	
	@FXML
	private TextField price;

	@FXML
	private TableView<SalesReturnDet> addedBooks;

	@FXML
	private ToggleGroup discType;

	@FXML
	private RadioButton rupeeRad;

	@FXML
	private RadioButton percentRad;

	@FXML
	private TextField discText;

	@FXML
	private Label discTypeInd;

	@FXML
	private TextField subTotal;

	@FXML
	private TextField calcDiscount;

	@FXML
	private TextField netTotal;

	private School school;
	private SchoolReturn schoolReturn;
	private Map<String, Book> bookMap;
//	private AtomicInteger index = new AtomicInteger();
	private Collector<SalesReturnDet, ?, Double> summingDblCollector = Collectors.summingDouble(SalesReturnDet::getTotalBought);
	
	void initData(School schoolIn, Map<String, Book> bookMapIn, SchoolReturn returnIn)
	{
		this.school = schoolIn;
		this.schoolReturn = returnIn;
		this.loadReturnData(returnIn);
		this.schoolName.setText(schoolIn.getName());
		this.bookMap = bookMapIn;

		List<String> items = new ArrayList<>(this.bookMap.keySet());
		TextFields.bindAutoCompletion(bookName, items);

		this.discText.textProperty().addListener((observable, oldValue, newValue) -> {
			updateNetAmt();
		});

		discType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle)
			{
				if (discType.getSelectedToggle() != null)
				{
					calcNetAmount(discText.getText());
					loadDiscSymbol();
				}
			}
		});
	}

	private void loadReturnData(SchoolReturn returnIn)
	{
		if (returnIn != null)
		{
			if (returnIn.getSalesReturnDetSet() != null)
			{
				ObservableList<SalesReturnDet> orderItemsIn = FXCollections
						.observableList(new ArrayList<SalesReturnDet>(returnIn.getSalesReturnDetSet()));
				this.addedBooks.setItems(orderItemsIn);
			}
			else
			{
				this.addedBooks.setItems(FXCollections.observableList(new ArrayList<>()));
			}
			this.creditNoteNum.setText(returnIn.getCreditNoteNum());
			this.returnDate.setValue(returnIn.getSalesTxn().getTxnDate());
			this.notes.setText(returnIn.getSalesTxn().getNote());
			
			Double subTotalDbl = returnIn.getSubTotal();
			if (subTotalDbl != null)
			{
				this.subTotal.setText(subTotalDbl.toString());
			}
			this.netTotal.setText(getStringVal(returnIn.getNetAmount()));

			if (returnIn.getDiscType() == null || !returnIn.getDiscType())
			{
				this.rupeeRad.setSelected(true);
				this.discTypeInd.setText(RUPEE_SIGN);
			}
			this.discText.setText(getStringVal(returnIn.getDiscAmt()));
			this.calcDiscount.setText(getStringVal(returnIn.getDiscount()));
		}
		else
		{
			this.addedBooks.setItems(FXCollections.observableList(new ArrayList<>()));
		}
	}

	private void loadSubTotal()
	{
		ObservableList<SalesReturnDet> orderItemsIn = this.addedBooks.getItems();
		Double subTotalDbl = orderItemsIn.stream().collect(summingDblCollector);
		this.subTotal.setText(subTotalDbl.toString());		
	}
	
	@FXML
	void addBookData(ActionEvent event)
	{
		String bookStr = this.bookName.getText();
		Book book = this.bookMap.get(bookStr);
		if(book == null)
		{
			showErrorAlert("Invalid Book", "Invalid Book Entry", bookStr + " is not corresponding to a valid Book Entry");
			return;
		}
		SalesReturnDet itemIn = new SalesReturnDet(this.addedBooks.getItems().size(), getIntegerVal(this.quantity), Double.parseDouble(this.price.getText()), book);
		itemIn.setBook(book);
		this.addedBooks.getItems().add(itemIn);
		LOGGER.debug("Added" + itemIn);
		loadSubTotal();
		calcNetAmount(discText.getText());
		clearBookFields();
		this.bookName.requestFocus();
	}
	
	private void clearBookFields()
	{
		this.bookName.clear();
		this.quantity.clear();
		this.price.clear();
	}

	private boolean validateData()
	{
		boolean valid = true;
		StringBuilder errorMsg = new StringBuilder();
		if (isBlank(this.creditNoteNum.getText()))
		{
			errorMsg.append("Please provide a Credit Note Number");
			errorMsg.append(NEW_LINE);
			valid = false;
		}
		if (this.returnDate.getValue() == null)
		{
			errorMsg.append("Please provide a date");
			valid = false;
		}
		if(!valid)
		{
			showErrorAlert("Error in Saving Order", "Please correct the following errors", errorMsg.toString());
		}
		return valid;
	}
	
	@FXML
	void saveData(ActionEvent event)
	{
		try
		{
			if (!validateData()) {
				return;
			}
			SchoolReturn returnIn = this.schoolReturn;
			SalesTransaction salesTxn = null;
			if (returnIn == null) {
				returnIn = new SchoolReturn();
				salesTxn = new SalesTransaction();
				salesTxn.setSchool(school);
			} else {
				salesTxn = returnIn.getSalesTxn();
			}

			if (percentRad.isSelected())
			{
				returnIn.setDiscType(true);
			}
			else
			{
				returnIn.setDiscType(false);
			}
			returnIn.setDiscAmt(getDoubleVal(this.discText.getText()));
			returnIn.setSubTotal(getDoubleVal(this.subTotal.getText()));
			returnIn.setCreditNoteNum(this.creditNoteNum.getText());

			List<SalesReturnDet> orderItems = new ArrayList<>(this.addedBooks.getItems());

			salesTxn.setTxnDate(this.returnDate.getValue());
			salesTxn.setNote(this.notes.getText());

			String subTotalStr = this.netTotal.getText();
			if (isNotBlank(subTotalStr)) {
				salesTxn.setAmount(Double.parseDouble(subTotalStr));
			}

			schoolService.saveSchoolReturn(returnIn, salesTxn, orderItems);

			((Stage) cancelBtn.getScene().getWindow()).close();
		}
		catch (DataIntegrityViolationException e)
		{
			LOGGER.error("Error...", e);
			showErrorAlert("Error in Saving Credit Note", "Please correct the following errors", "Duplicate Entry Found");
		}
		catch (Exception e)
		{
			LOGGER.error("Error...", e);
			showErrorAlert("Error in Saving Credit Note", "Please correct the following errors", e.getMessage());
		}
	}

	@FXML
	void cancelOperation(ActionEvent event)
	{
		((Stage) cancelBtn.getScene().getWindow()).close();
	}

	@FXML
	void removeOperation(ActionEvent event)
	{
		int orderNumSel = this.addedBooks.getSelectionModel().getSelectedIndex();
		this.addedBooks.getItems().remove(orderNumSel);

		loadSubTotal();
		calcNetAmount(this.discText.getText());
	}

	private void updateNetAmt()
	{
		String discAmtStr = defaultString(discText.getText());
		if (!isEmpty(discAmtStr))
		{
			calcNetAmount(discAmtStr);
		}
	}

	private void calcNetAmount(String discAmtStr)
	{
		String netTotalStr = netTotal.getText();
		Double netTotalDbl = isEmpty(netTotalStr) ? 0.0 : Double.parseDouble(netTotalStr);

		String subTotalStr = this.subTotal.getText();
		double discVal = 0;
		if (subTotalStr != null)
		{
			double subTotalDbl = Double.parseDouble(subTotalStr);
			if (subTotalDbl > 0)
			{
				double discAmtDbl = isEmpty(discAmtStr) ? 0 : Double.parseDouble(discAmtStr);
				if (rupeeRad.isSelected())
				{
					discVal = discAmtDbl;
					netTotalDbl = subTotalDbl - discVal;
				}
				else if (percentRad.isSelected())
				{
					discVal = subTotalDbl * discAmtDbl / 100;
					netTotalDbl = subTotalDbl - discVal;
				}
			}
			else
			{
				netTotalDbl = subTotalDbl;
			}
		}
		this.netTotal.setText(getStringVal(netTotalDbl));
		this.calcDiscount.setText(getStringVal(discVal));
	}

	private void loadDiscSymbol()
	{
		if (this.percentRad.isSelected())
		{
			this.discTypeInd.setText(PERCENT_SIGN);
		}
		else if (this.rupeeRad.isSelected())
		{
			this.discTypeInd.setText(RUPEE_SIGN);
		}
	}

}
