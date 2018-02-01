package com.matha.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.matha.domain.Book;
import com.matha.domain.Order;
import com.matha.domain.Publisher;
import com.matha.domain.Purchase;
import com.matha.domain.PurchasePayment;
import com.matha.domain.PurchaseReturn;
import com.matha.domain.PurchaseTransaction;
import com.matha.repository.PurchaseTxnRepository;
import com.matha.service.SchoolService;
import com.matha.util.Converters;
import com.matha.util.LoadUtils;
import com.matha.util.UtilConstants;
import com.matha.util.Utils;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

@Component
public class PurchaseController
{

	private static final int ROWS_PER_PAGE = 10;

	@Autowired
	private SchoolService schoolService;

	@FXML
	private ChoiceBox<Publisher> publishers;

	@FXML
	private Tab ordersTab;

	@FXML
	private TableView<Order> orderTable;

	@FXML
	private Pagination orderPaginator;

	@FXML
	private Tab purchaseBillTab;

	@FXML
	private TableView<Purchase> purchaseData;

	@FXML
	private Tab returnsTab;

	@FXML
	private TableView<PurchaseReturn> returnsData;
	
	@FXML
	private Tab paymentTab;

	@FXML
	private TableView<PurchasePayment> paymentData;
	
	@FXML
	private Tab statementTab;

	@FXML
	private Tab statementTabHtml;
	
	@FXML
	private DatePicker fromDate;

	@FXML
	private DatePicker toDate;
	
	@FXML
	private WebView reportData;

	private JasperPrint print;

	@FXML
	protected void initialize()
	{
		orderTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		orderPaginator.setPageFactory(this::createPage);

		List<Publisher> allPublishers = schoolService.fetchAllPublishers();
		publishers.setConverter(Converters.getPublisherConverter());
		publishers.setItems(FXCollections.observableList(allPublishers));
		publishers.getSelectionModel().selectFirst();
		
		LocalDate toDateVal = LocalDate.now();
		LocalDate fromDateVal = toDateVal.minusMonths(6);
		fromDate.setValue(fromDateVal);
		toDate.setValue(toDateVal);

	}

	private Node createPage(int pageIndex)
	{
		loadOrderTable(pageIndex);

		orderTable.prefHeightProperty().bind(Bindings.size(orderTable.getItems()).multiply(orderTable.getFixedCellSize()).add(30));

		return new BorderPane(orderTable);
	}

	@FXML
	void changedState(ActionEvent event)
	{
		Publisher pub = publishers.getSelectionModel().getSelectedItem();
		int idx = orderPaginator.getCurrentPageIndex();
		List<Order> orderList = schoolService.fetchOrders(pub, idx, ROWS_PER_PAGE).getContent();
		orderTable.setItems(FXCollections.observableList(orderList));
	}

	@FXML
	void editOrder(ActionEvent event)
	{
		try
		{
			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createOrderFxmlFile);
			Parent addOrderRoot;

			addOrderRoot = createOrderLoader.load();

			AddOrderController ctrl = createOrderLoader.getController();

			List<Book> schools = schoolService.fetchAllBooks();
			// LOGGER.info(schools.toString());
			HashMap<String, Book> bookMap = new HashMap<>();
			for (Book bookIn : schools)
			{
				bookMap.put(bookIn.getName(), bookIn);
			}

			Order order = orderTable.getSelectionModel().getSelectedItem();

			ctrl.initData(order.getSchool(), bookMap, order);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void createPurchase(ActionEvent event)
	{

		try
		{
			ObservableList<Order> selectedOrders = orderTable.getSelectionModel().getSelectedItems();
			Set<Order> orderSet = new HashSet<>(selectedOrders);

			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchaseFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchaseBillController ctrl = createOrderLoader.getController();
			ctrl.initData(orderSet, this.publishers.getSelectionModel().getSelectedItem(), null);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchaseEventHandler);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	void editPurchase(ActionEvent event)
	{

		try
		{

			Purchase purchase = this.purchaseData.getSelectionModel().getSelectedItem();

			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchaseFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchaseBillController ctrl = createOrderLoader.getController();
			ctrl.initData(null, this.publishers.getSelectionModel().getSelectedItem(), purchase);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchaseEventHandler);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@FXML
	void deletePurchase(ActionEvent event)
	{
		Purchase purchase = this.purchaseData.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Purchase Bill Confirmation");
		alert.setHeaderText("Are you sure you want to delete the purchase: " + purchase.getId());
		alert.setContentText("Click Ok to Delete");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			schoolService.deletePurchase(purchase);
			loadPurchases();
		}
	}

	private void loadOrderTable(int idx)
	{
		Publisher pub = publishers.getSelectionModel().getSelectedItem();
		List<Order> orderList = schoolService.fetchOrders(pub, idx, ROWS_PER_PAGE).getContent();
		orderTable.setItems(FXCollections.observableList(orderList));
	}

	@FXML
	public void loadOrders()
	{
		if (ordersTab.isSelected())
		{
			int idx = orderPaginator.getCurrentPageIndex();
			loadOrderTable(idx);
		}
		else
		{
			orderTable.getSelectionModel().clearSelection();
		}
	}

	@FXML
	public void loadPurchases()
	{
		if (purchaseBillTab.isSelected())
		{
			Publisher pub = publishers.getSelectionModel().getSelectedItem();
			List<Purchase> purchaseList = schoolService.fetchPurchasesForPublisher(pub);
			purchaseData.setItems(FXCollections.observableList(purchaseList));
		}
	}

	@FXML
	public void loadReturns()
	{
		if (returnsTab.isSelected())
		{
			Publisher pub = publishers.getSelectionModel().getSelectedItem();
			List<PurchaseReturn> returnDataList = schoolService.fetchPurchaseReturns(pub);
			returnsData.setItems(FXCollections.observableList(returnDataList));
		}
	}

	@FXML
	void addReturn(ActionEvent event)
	{
		try
		{
			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchaseRetFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchaseRetController ctrl = createOrderLoader.getController();
			ctrl.initData(this.publishers.getSelectionModel().getSelectedItem(), null);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchaseRetEventHandler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	void editReturn(ActionEvent event)
	{
		try
		{
			PurchaseReturn purchase = this.returnsData.getSelectionModel().getSelectedItem();

			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchaseRetFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchaseRetController ctrl = createOrderLoader.getController();
			ctrl.initData(this.publishers.getSelectionModel().getSelectedItem(), purchase);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchaseRetEventHandler);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@FXML
	void deleteReturn(ActionEvent event)
	{
		PurchaseReturn purchase = this.returnsData.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Purchase Return Confirmation");
		alert.setHeaderText("Are you sure you want to delete the Credit Note: " + purchase.getId());
		alert.setContentText("Click Ok to Delete");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			schoolService.deletePurchaseReturn(purchase);
			loadReturns();
		}
	}

	@FXML
	public void loadPayments()
	{
		if (paymentTab.isSelected())
		{

		}
	}

	@FXML
	void addPayment(ActionEvent event)
	{
		try
		{
			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchasePayFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchasePayController ctrl = createOrderLoader.getController();
			ctrl.initData(this.publishers.getSelectionModel().getSelectedItem(), null);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchasePayEventHandler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	void editPayment(ActionEvent event)
	{
		try
		{
			PurchasePayment purchase = this.paymentData.getSelectionModel().getSelectedItem();

			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.createPurchasePayFxmlFile);
			Parent addOrderRoot = createOrderLoader.load();
			AddPurchasePayController ctrl = createOrderLoader.getController();
			ctrl.initData(this.publishers.getSelectionModel().getSelectedItem(), purchase);
			Scene addOrderScene = new Scene(addOrderRoot);
			prepareAndShowStage(event, addOrderScene, purchasePayEventHandler);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@FXML
	void deletePayment(ActionEvent event)
	{
		PurchasePayment purchase = this.paymentData.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Purchase Payment Confirmation");
		alert.setHeaderText("Are you sure you want to delete the Payment: " + purchase.getId());
		alert.setContentText("Click Ok to Delete");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			schoolService.deletePurchasePayment(purchase);
			loadPayments();
		}
	}

	@FXML
	public void loadStatement()
	{
		if (statementTab.isSelected())
		{

		}
	}

	@FXML
	public void loadStatementHtml()
	{
		if (statementTabHtml.isSelected() && print == null)
		{			
			try
			{

				print = this.prepareJasperPrint();
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				HtmlExporter exporter = new HtmlExporter();
				exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
				exporter.setExporterInput(new SimpleExporterInput(print));
				exporter.exportReport();

				String content = StringUtils.toEncodedString(outputStream.toByteArray(), Charset.defaultCharset());				
				reportData.getEngine().loadContent(content);

			}
			catch (JRException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private JasperPrint prepareJasperPrint()
	{
		JasperPrint jasperPrint = null;
		InputStream jasperStream = getClass().getResourceAsStream("/jrxml/Invoice.jrxml");
		HashMap<String, Object> hm = new HashMap<>();
		try
		{
			LocalDate fromDateVal = fromDate.getValue();
			LocalDate toDateVal = toDate.getValue();
			if(toDateVal == null)
			{
				toDateVal = LocalDate.now();
			}
			
			Sort sort = new Sort(new Sort.Order(Direction.ASC, "txnDate"));			
			Publisher pub = publishers.getSelectionModel().getSelectedItem();
			List<PurchaseTransaction> tableData = schoolService.fetchPurTransactions(pub, fromDateVal, toDateVal, sort);			
			hm.put("reportData", tableData);
			hm.put("publisherDetails", pub.getAddress());
			JasperReport compiledFile = JasperCompileManager.compileReport(jasperStream);

			jasperPrint = JasperFillManager.fillReport(compiledFile, hm);
		}
		catch (JRException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return jasperPrint;
	}

	@FXML
	public void exportAndSave(ActionEvent ev)
	{
		try
		{

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save File");

			File file = fileChooser.showSaveDialog(((Node) ev.getSource()).getScene().getWindow());
			JasperExportManager.exportReportToPdfFile(print, file.getAbsolutePath());
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	public void printOrder(ActionEvent ev)
	{
		try
		{

			FXMLLoader createOrderLoader = LoadUtils.loadFxml(this, UtilConstants.printOrderFxmlFile);
			Parent addOrderRoot;

			addOrderRoot = createOrderLoader.load();
			PrintOrderController ctrl = createOrderLoader.getController();

			Order orderItem = orderTable.getSelectionModel().getSelectedItem();
			Publisher pub = publishers.getSelectionModel().getSelectedItem();
			ctrl.initData(orderItem, pub);

			Scene parentScene = ((Node) ev.getSource()).getScene();
			Window parentWindow = parentScene.getWindow();

			Utils.print(addOrderRoot, parentWindow, new Label());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void prepareAndShowStage(ActionEvent e, Scene childScene)
	{
		Stage stage = LoadUtils.loadChildStage(e, childScene);
		stage.show();
	}

	private void prepareAndShowStage(ActionEvent e, Scene childScene, EventHandler<WindowEvent> eventHandler)
	{
		Stage stage = LoadUtils.loadChildStage(e, childScene);
		stage.setOnHiding(eventHandler);
		stage.show();
	}

	private EventHandler<WindowEvent> purchaseEventHandler = new EventHandler<WindowEvent>() {
		@Override
		public void handle(final WindowEvent event)
		{
			loadPurchases();
		}
	};
	
	private EventHandler<WindowEvent> purchaseRetEventHandler = new EventHandler<WindowEvent>() {
		@Override
		public void handle(final WindowEvent event)
		{
			loadReturns();
		}
	};
	
	private EventHandler<WindowEvent> purchasePayEventHandler = new EventHandler<WindowEvent>() {
		@Override
		public void handle(final WindowEvent event)
		{
			loadPayments();
		}
	};	
}
