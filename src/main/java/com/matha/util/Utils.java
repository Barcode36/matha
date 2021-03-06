package com.matha.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.print.PrintException;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;

import com.matha.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.matha.controller.PrintOrderController;
import com.matha.controller.PrintSalesBillController;
import com.matha.service.SchoolService;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.util.Callback;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;

import static com.matha.util.UtilConstants.*;

public class Utils
{

	private static final Logger LOGGER = LogManager.getLogger(Utils.class);
	
	public static Double getDoubleVal(TextField txt)
	{
		Double dblVal = null;
		String txtStr = txt.getText();
		if (StringUtils.isNotBlank(txtStr))
		{
			dblVal = Double.parseDouble(txtStr);
		}
		return dblVal;
	}

	public static Double getDoubleVal(String txt)
	{
		Double dblVal = null;		
		if (StringUtils.isNotBlank(txt))
		{
			dblVal = Double.parseDouble(txt);
		}
		return dblVal;
	}
	
	public static Integer getIntegerVal(TextField txt)
	{
		Integer dblVal = null;
		String txtStr = txt.getText();
		if (StringUtils.isNotBlank(txtStr))
		{
			dblVal = Integer.parseInt(txtStr);
		}
		return dblVal;
	}

	public static String getStringVal(Double txt)
	{
		String txtVal = null;
		if (txt != null)
		{
			txtVal = String.format("%.2f", txt);
		}
		return txtVal;
	}

	public static String getStringVal(Integer txt)
	{
		String txtVal = null;
		if (txt != null)
		{
			txtVal = txt.toString();
		}
		return txtVal;
	}

	public static void print(Node node, Window parentWindow, Label jobStatus)
	{

		if (jobStatus == null)
		{
			jobStatus = new Label();
		}
		// Define the Job Status Message
		jobStatus.textProperty().unbind();
		jobStatus.setText("Printing...");

		Printer printer = Printer.getDefaultPrinter();
		ChoiceDialog<Printer> dialog = new ChoiceDialog<Printer>(Printer.getDefaultPrinter(), Printer.getAllPrinters());
		dialog.setHeaderText("Choose the printer!");
		dialog.setContentText("Choose a printer from available printers");
		dialog.setTitle("Printer Choice");
		Optional<Printer> opt = dialog.showAndWait();
		if (opt.isPresent())
		{
			printer = opt.get();
		}

		// Create a printer job for the default printer
		PrinterJob job = PrinterJob.createPrinterJob(printer);
		Paper paper = Paper.A4;
		PageOrientation orient = PageOrientation.PORTRAIT;

		PageLayout lout = printer.createPageLayout(paper, orient, 0, 0, 0, 0);
		double scaleX = 0.75;
		// double scaleY = 0.5;
		node.getTransforms().add(new Scale(scaleX, 1));

		if (job != null && job.showPageSetupDialog(parentWindow))
		{
			// Show the printer job status
			jobStatus.textProperty().bind(job.jobStatusProperty().asString());

			// Print the node
			boolean printed = job.printPage(lout, node);

			if (printed)
			{
				// End the printer job
				job.endJob();
			}
			else
			{
				// Write Error Message
				jobStatus.textProperty().unbind();
				jobStatus.setText("Printing failed.");
			}
		}
		else
		{
			// Write Error Message
			jobStatus.setText("Could not create a printer job.");
		}
	}

	public static Scene preparePrintScene(Sales purchase, FXMLLoader createOrderLoader, InputStream jasperStream, Address addrIn, Account acct)
	{
		Scene addOrderScene = null;
		try
		{			
			Parent addOrderRoot = createOrderLoader.load();
			PrintSalesBillController ctrl = createOrderLoader.getController();						
			JasperPrint jasperPrint = prepareSaleBillPrint(purchase.getSchool(), purchase, addrIn, acct, jasperStream);
			ctrl.initData(jasperPrint);
			addOrderScene = new Scene(addOrderRoot);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return addOrderScene;
	}
	
	public static Map<String, Object> prepareSalesStmtParmMap(HashMap<String, Object> hmIn, List<SalesTransaction> tableData)
	{		
		HashMap<String, Object> hm = new HashMap<>();
		Double openingBalance = 0.0;
		Double closingBalance = 0.0;
		
		if(tableData != null && !tableData.isEmpty())
		{
			if(tableData.get(0).getPrevTxn() != null)
			{
				openingBalance = tableData.get(0).getPrevTxn().getBalance();
			}			
			closingBalance = tableData.get(tableData.size() - 1).getBalance();
		} 
		Double totalDebit = tableData.stream().collect(Collectors.summingDouble(o->  o.getMultiplier() == 1 ? o.getAmount() : 0.0)); 
		Double totalCredit = tableData.stream().collect(Collectors.summingDouble(o->  o.getMultiplier() == -1 ? o.getAmount() : 0.0));
		hm.putAll(hmIn);
		hm.put("openingBalance", openingBalance);						
		hm.put("totalDebit", totalDebit);
		hm.put("totalCredit", totalCredit);
		hm.put("closingBalance", closingBalance);
		
		return hm;
	}

	public static JasperPrint prepareJasperPrint(Publisher pub, Purchase purchase, Address salesAddr, InputStream jasperStream)
	{
		JasperPrint jasperPrint = null;
		HashMap<String, Object> hm = new HashMap<>();
		try
		{
			StringBuilder strBuild = new StringBuilder();
			strBuild.append(salesAddr.getAddress1());
			strBuild.append(NEW_LINE);
			strBuild.append(salesAddr.getAddress2());
			strBuild.append(COMMA_SIGN);
			strBuild.append(salesAddr.getAddress3());
			strBuild.append(HYPHEN_SPC_SIGN);
			strBuild.append(salesAddr.getPin());

			Set<PurchaseDet> tableData = purchase.getPurchaseItems();
			Set<String> orderIdSet = tableData.stream()
					.filter(pd -> pd.getOrderItem() != null)
					.map(PurchaseDet::getOrderItem)
					.filter(oi -> oi.getOrder() != null)
					.map(oi -> oi.getOrder().getSerialNo())
					.collect(Collectors.toSet());
			String orderIds = String.join(",", orderIdSet);
			Double subTotal = purchase.getSubTotal();
			Double discAmt = purchase.getCalculatedDisc();

			hm.put("publisherName", pub.getName());
			hm.put("publisherDetails", pub.getInvAddress());
			hm.put("partyName", "MATHA DISTRIBUTORS.");
			hm.put("partyAddress", strBuild.toString());
			hm.put("partyPhone", "Ph - " + salesAddr.getPhone1());
			hm.put("documentsThrough", purchase.getDocsThrough());
			hm.put("despatchedTo", purchase.getDespatchedTo());
			hm.put("invoiceNo", purchase.getId());
			hm.put("txnDate", purchase.getTxnDate());
			hm.put("orderNumbers", orderIds);
			hm.put("despatchedPer", purchase.getDespatchPer());
			hm.put("grNo", purchase.getGrNum());
			hm.put("packageCnt", getStringVal(purchase.getPackages()));
			hm.put("total", purchase.getSubTotal());
			hm.put("discount", discAmt);
			hm.put("grandTotal", purchase.getNetAmount());
			hm.put("grandTotalInWords", convertDouble(purchase.getNetAmount()));
			hm.put("imageFileName",pub.getLogoFileName());

			JasperReport compiledFile = JasperCompileManager.compileReport(jasperStream);

			jasperPrint = JasperFillManager.fillReport(compiledFile, hm, new JRBeanCollectionDataSource(tableData));
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

	public static JasperPrint prepareSaleBillPrint(School sch, Sales sale, Address salesAddr, Account acct, InputStream jasperStream)
	{

		JasperPrint jasperPrint = null;		
		HashMap<String, Object> hm = new HashMap<>();
		try
		{
			StringBuilder strBuild = new StringBuilder();
			strBuild.append(salesAddr.getAddress1());
			strBuild.append(NEW_LINE); 
			strBuild.append(salesAddr.getAddress2());
			strBuild.append(COMMA_SIGN);
			strBuild.append(SPACE_SIGN);
			strBuild.append(salesAddr.getAddress3());
			strBuild.append(HYPHEN_SPC_SIGN);
			strBuild.append(salesAddr.getPin());
			strBuild.append(NEW_LINE);
			strBuild.append("Ph: ");
			strBuild.append(salesAddr.getPhone1());
			strBuild.append(SPACE_SIGN);
			strBuild.append("Mob: ");
			strBuild.append(salesAddr.getPhone2());
			strBuild.append(NEW_LINE);
			strBuild.append("Email: ");
			strBuild.append(salesAddr.getEmail());

			StringBuilder strBuildAcct = new StringBuilder(acct.getName());
			strBuildAcct.append(COMMA_SIGN);
			strBuildAcct.append(" State Bank of India");
			strBuildAcct.append(COMMA_SIGN);
			strBuildAcct.append(" Vazhakulam Branch");
			strBuildAcct.append(COMMA_SIGN);
			strBuildAcct.append(" A/C No: ");			
			strBuildAcct.append(acct.getAccountNum());
			strBuildAcct.append(COMMA_SIGN);
			strBuildAcct.append(" IFSC");
			strBuildAcct.append(HYPHEN_SPC_SIGN);
			strBuildAcct.append(acct.getIfsc());
			
			Set<SalesDet> tableData = sale.getSaleItems();
			Set<String> orderIdSet = tableData.stream()
					.map(SalesDet::getOrderItem)
					.filter(o-> o != null)
					.map(o -> o.getOrder().getSerialNo())
					.collect(Collectors.toSet());
			String orderIds = String.join(",", orderIdSet);
			Double subTotal = sale.getSubTotal();
			Double discAmt = sale.getDiscAmt();
			if(discAmt != null)
			{
				discAmt = sale.getDiscType() != null && sale.getDiscType() ? subTotal * discAmt /100 : discAmt;
			}
			 			
			hm.put("partyName", sch.getName());
			hm.put("partyAddress", sch.addressText());
			hm.put("agencyName", "MATHA AGENCIES");
			hm.put("agencyDetails", strBuild.toString());
			hm.put("partyPhone", sch.getPhone1() == null ? sch.getPhone2() : sch.getPhone1());
			hm.put("documentsThrough", sale.getDocsThru());
			hm.put("invoiceNo", getStringVal(sale.getSerialNo()));
			hm.put("txnDate", sale.getInvoiceDateStr());
			hm.put("orderNumbers", orderIds);
			hm.put("despatchedPer", sale.getDespatch());
			hm.put("grNo", sale.getGrNum());
			hm.put("packageCnt", sale.getPackages());
			hm.put("total", sale.getSubTotal());
			hm.put("discount", discAmt);
			hm.put("grandTotal", sale.getNetAmount());
			hm.put("accountDet", strBuildAcct.toString());
			hm.put("grandTotalInWords", convertDouble(sale.getNetAmount()));
			hm.put("otherCharges", sale.getOtherAmount());
			
			JasperReport compiledFile = JasperCompileManager.compileReport(jasperStream);

			jasperPrint = JasperFillManager.fillReport(compiledFile, hm, new JRBeanCollectionDataSource(tableData));
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
	
	public static void printJasper(JasperPrint jasperPrint) throws PrintException, JRException
	{
		Label jobStatus = new Label();

		// Define the Job Status Message
		jobStatus.textProperty().unbind();
		jobStatus.setText("Printing...");

		Printer printerJob = Printer.getDefaultPrinter();
		ChoiceDialog<Printer> dialog = new ChoiceDialog<Printer>(Printer.getDefaultPrinter(), Printer.getAllPrinters());
		dialog.setHeaderText("Choose the printer!");
		dialog.setContentText("Choose a printer from available printers");
		dialog.setTitle("Printer Choice");
		Optional<Printer> opt = dialog.showAndWait();
		if (opt.isPresent())
		{
			printerJob = opt.get();
		}
		else
		{
			return;
		}

		// Set the printing settings
		PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
		printRequestAttributeSet.add(MediaSizeName.ISO_A4);
		if (jasperPrint.getOrientationValue() == net.sf.jasperreports.engine.type.OrientationEnum.LANDSCAPE)
		{
			printRequestAttributeSet.add(OrientationRequested.LANDSCAPE);
		}
		else
		{
			printRequestAttributeSet.add(OrientationRequested.PORTRAIT);
		}
		PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
		printServiceAttributeSet.add(new PrinterName(printerJob.getName(), null));

		JRPrintServiceExporter exporter = new JRPrintServiceExporter();
		SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
		configuration.setPrintRequestAttributeSet(printRequestAttributeSet);
		configuration.setPrintServiceAttributeSet(printServiceAttributeSet);
		// configuration.setDisplayPageDialog(true);
		// configuration.setDisplayPrintDialog(true);

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setConfiguration(configuration);

		exporter.exportReport();
	}

	public void loadWebInvoice(JasperPrint printIn, WebView invoiceData)
	{
		try
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
			exporter.setExporterInput(new SimpleExporterInput(printIn));
			exporter.exportReport();

			String content = StringUtils.toEncodedString(outputStream.toByteArray(), Charset.defaultCharset());
			invoiceData.getEngine().loadContent(content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static Scene prepareOrderPrintScene(Order order, FXMLLoader createOrderLoader, InputStream jasperStream, SchoolService schoolService)
	{
		Scene addOrderScene = null;
		try
		{			
			Parent addOrderRoot = createOrderLoader.load();
			PrintOrderController ctrl = createOrderLoader.getController();						
			JasperPrint jasperPrint = ctrl.prepareJasperPrint(order);
			ctrl.initData(jasperPrint);
			addOrderScene = new Scene(addOrderRoot);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return addOrderScene;
	}
	
	public static Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> fetchPriceColumnFactory()
	{
		Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> priceColumnFactory = new Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<OrderItem, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getBookPrice()));
			}
		};
		return priceColumnFactory;
	}

	public static Callback<CellDataFeatures<PurchaseDet, String>, ObservableValue<String>> fetchPurPriceColumnFactory()
	{
		Callback<CellDataFeatures<PurchaseDet, String>, ObservableValue<String>> priceColumnFactory = new Callback<CellDataFeatures<PurchaseDet, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PurchaseDet, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getBookPrice()));
			}
		};
		return priceColumnFactory;
	}

	public static Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>> fetchSalesPrcColFactory()
	{
		Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>> priceColumnFactory = new Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SalesDet, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getBookPrice()));
			}
		};
		return priceColumnFactory;
	}

	public static Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> fetchQuantityFactory()
	{
		Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> qtyColumnFactory = new Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<OrderItem, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getCount()));
			}
		};
		return qtyColumnFactory;
	}

	public static Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> fetchPurCntFactory()
	{
		Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> qtyColumnFactory = new Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<OrderItem, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getFullFilledCnt()));
			}
		};
		return qtyColumnFactory;
	}

	public static Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> fetchSaleCntFactory()
	{
		Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> qtyColumnFactory = new Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<OrderItem, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getSoldCnt()));
			}
		};
		return qtyColumnFactory;
	}

	public static Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>> fetchSalesCntFactory()
	{
		Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>> qtyColumnFactory = new Callback<CellDataFeatures<SalesDet, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SalesDet, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(getStringVal(p.getValue().getQty()));
			}
		};
		return qtyColumnFactory;
	}

	public static void calcNetAmountGen(String discAmtStr, TextField subTotal, RadioButton percentRad, RadioButton rupeeRad, String otherChargesField, TextField netAmt, TextField calcDisc)
	{
		String netTotalStr = netAmt.getText();
		Double netTotalDbl = StringUtils.isBlank(netTotalStr) ? 0.0 : Double.parseDouble(netTotalStr);
		Double otherCharges = StringUtils.isBlank(otherChargesField) ? 0.0 : getDoubleVal(otherChargesField);

		String subTotalStr = subTotal.getText();
		if (subTotalStr != null)
		{
			double subTotalDbl = Double.parseDouble(subTotalStr);
			if (subTotalDbl > 0)
			{
				double discAmtDbl = StringUtils.isBlank(discAmtStr) ? 0 : Double.parseDouble(discAmtStr);
				if (discAmtDbl > 0)
				{
					double discAmt = calculateDisc(subTotalDbl, discAmtDbl, rupeeRad.isSelected(), percentRad.isSelected());
					calcDisc.setText(getStringVal(discAmt));
					netTotalDbl = subTotalDbl - discAmt; 
				}
				else
				{
					netTotalDbl = subTotalDbl;
				}
			}
		}
		if(otherCharges != null)
		{
			netTotalDbl += otherCharges;
		}
		netAmt.setText(getStringVal(netTotalDbl));
	}

	public static double calculateDisc(double subTotalDbl, double discAmtDbl, boolean rupeeRad, boolean percentRad)
	{
		if (percentRad)
		{
			return subTotalDbl * discAmtDbl / 100;
		}
		return discAmtDbl;
	}
	
	public static void loadDiscSymbol(RadioButton percentRad, RadioButton rupeeRad, Label discTypeInd)
	{
		if (percentRad.isSelected())
		{
			discTypeInd.setText(PERCENT_SIGN);
		}
		else if (rupeeRad.isSelected())
		{
			discTypeInd.setText(RUPEE_SIGN);
		}
	}

	public static void showErrorAlert(String title, String header, String content)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	public static String convertDouble(double dbl)
	{
		int mainPart = (int) dbl;
		int decPart = (int) (dbl*100 - mainPart*100);
		if(decPart == 0)
		{
			return convert(mainPart) + " rupees and Zero paise only";
		}
		else
		{
			return convert(mainPart) + " rupees and " + convert(decPart) + " paise only";
		}
	}
	
	public static String convert(int n)
	{
		LOGGER.debug("Converting " + n);
		final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
				"Eighteen", "Nineteen" };

		final String[] tens = { "", // 0
				"", // 1
				"Twenty", // 2
				"Thirty", // 3
				"Forty", // 4
				"Fifty", // 5
				"Sixty", // 6
				"Seventy", // 7
				"Eighty", // 8
				"Ninety" // 9
		};

		if (n < 0)
		{
			return "Minus " + convert(-n);
		}

		if (n < 20)
		{
			return units[n];
		}

		if (n < 100)
		{
			return tens[n / 10] + ((n % 10 != 0) ? " " : "") + units[n % 10];
		}

		if (n < 1000)
		{
			return units[n / 100] + " Hundred" + ((n % 100 != 0) ? " And " : "") + convert(n % 100);
		}

		if (n < 100000)
		{
			return convert(n / 1000) + " Thousand" + ((n % 10000 != 0) ? " " : "") + convert(n % 1000);
		}

		if (n < 10000000)
		{
			return convert(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convert(n % 100000);
		}

		return convert(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convert(n % 10000000);
	}
	
	public static Integer calcFinYear(LocalDate dt)
	{
		return dt.minusMonths(3).getYear() - 2007;
	}
}
