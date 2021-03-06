package com.matha.controller;

import static com.matha.util.UtilConstants.Docx;
import static com.matha.util.UtilConstants.Excel;
import static com.matha.util.UtilConstants.PDF;
import static com.matha.util.UtilConstants.salesStmtJrxml;
import static com.matha.util.Utils.printJasper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.matha.domain.SalesTransaction;
import com.matha.domain.School;
import com.matha.service.SchoolService;
import com.matha.util.Utils;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Component
public class PrintSalesStmtController
{

	private static final Logger LOGGER = LogManager.getLogger(PrintSalesStmtController.class);
	
	@Autowired
	private SchoolService schoolService;
	
	@Value("${agencyName}")
	private String agencyName;
	
	@Value("${agencyAddress1}")
	private String agencyAddress1;
	
	@Value("${agencyAddress2}")
	private String agencyAddress2;

	@FXML
	private TextField schoolName;

    @FXML
    private DatePicker fromDate;
    
    @FXML
    private DatePicker toDate;
    
	@FXML
	private ChoiceBox<String> saveType;

	@FXML
	private WebView invoiceData;

	private JasperPrint print;
	private School school;

	public void initData(School school, JasperPrint printIn, LocalDate fromDateVal, LocalDate toDateVal)
	{
		this.school = school;
		this.print = printIn;
		this.fromDate.setValue(fromDateVal);
		this.toDate.setValue(toDateVal);
		this.loadWebInvoice(printIn);
		List<String> saveTypes = Arrays.asList(PDF,Excel,Docx);
		this.saveType.setItems(FXCollections.observableList(saveTypes));
		this.saveType.getSelectionModel().selectFirst();
	}

	public JasperPrint prepareJasperPrint(School school, List<SalesTransaction> tableDataIn, LocalDate fromDateVal, LocalDate toDateVal)
	{
		JasperPrint jasperPrint = null;
		InputStream jasperStream = getClass().getResourceAsStream(salesStmtJrxml);
		HashMap<String, Object> hm = new HashMap<>();
		try
		{
			if (toDateVal == null)
			{
				LOGGER.debug("Null toDateVal");
				toDateVal = LocalDate.now();
			}

			hm.put("agencyName", agencyName);
			hm.put("agencyAddress1", agencyAddress1);
			hm.put("agencyAddress2", agencyAddress2);
			hm.put("schoolName", school.getAddress1());
			hm.put("schoolDetails", school.getStmtAddress());
			hm.put("fromDate", fromDateVal);
			hm.put("toDate", toDateVal);
			
			JasperReport compiledFile = JasperCompileManager.compileReport(jasperStream);
			LOGGER.info("Total Size: " + tableDataIn.size());
			for (int i = 0; i <= tableDataIn.size() / 38; i++)
			{
				int fromIndex = i * 38;
				int toIndex = Math.min(fromIndex + 38, tableDataIn.size());
				List<SalesTransaction> tableData = tableDataIn.subList(fromIndex, toIndex);
				LOGGER.info("i: " + i + " fromIndex: " + fromIndex + " toIndex: " + toIndex + " tabSize: " + tableData.size());
				
				Map<String, Object> hmOut = Utils.prepareSalesStmtParmMap(hm, tableData);
				LOGGER.debug("hmOut");
				for (Entry<String, Object> obj : hmOut.entrySet())
				{
					LOGGER.debug(obj.getKey());	
					LOGGER.debug(obj.getValue());
				}
	
				if(jasperPrint == null)
				{
					jasperPrint = JasperFillManager.fillReport(compiledFile, hmOut, new JRBeanCollectionDataSource(tableData));
				}
				else
				{
					JasperPrint jasperPrintNxt = JasperFillManager.fillReport(compiledFile, hmOut, new JRBeanCollectionDataSource(tableData));
					jasperPrint.addPage(jasperPrintNxt.getPages().get(0));
				}				
			}
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

	public JasperPrint prepareMasterStmtJasperPrint(List<SalesTransaction> tableDataIn, LocalDate fromDateVal, LocalDate toDateVal)
	{
		JasperPrint jasperPrint = null;
		InputStream jasperStream = getClass().getResourceAsStream(salesStmtJrxml);
		HashMap<String, Object> hm = new HashMap<>();
		try
		{
			if (toDateVal == null)
			{
				LOGGER.debug("Null toDateVal");
				toDateVal = LocalDate.now();
			}

			hm.put("agencyName", agencyName);
			hm.put("agencyAddress1", agencyAddress1);
			hm.put("agencyAddress2", agencyAddress2);
			hm.put("fromDate", fromDateVal);
			hm.put("toDate", toDateVal);

			JasperReport compiledFile = JasperCompileManager.compileReport(jasperStream);
			LOGGER.info("Total Size: " + tableDataIn.size());
			for (int i = 0; i <= tableDataIn.size() / 38; i++)
			{
				int fromIndex = i * 38;
				int toIndex = Math.min(fromIndex + 38, tableDataIn.size());
				List<SalesTransaction> tableData = tableDataIn.subList(fromIndex, toIndex);
				LOGGER.info("i: " + i + " fromIndex: " + fromIndex + " toIndex: " + toIndex + " tabSize: " + tableData.size());

				Map<String, Object> hmOut = Utils.prepareSalesStmtParmMap(hm, tableData);
				LOGGER.debug("hmOut");
				for (Entry<String, Object> obj : hmOut.entrySet())
				{
					LOGGER.debug(obj.getKey());
					LOGGER.debug(obj.getValue());
				}

				if(jasperPrint == null)
				{
					jasperPrint = JasperFillManager.fillReport(compiledFile, hmOut, new JRBeanCollectionDataSource(tableData));
				}
				else
				{
					JasperPrint jasperPrintNxt = JasperFillManager.fillReport(compiledFile, hmOut, new JRBeanCollectionDataSource(tableData));
					jasperPrint.addPage(jasperPrintNxt.getPages().get(0));
				}
			}
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

	private void loadWebInvoice(JasperPrint printIn)
	{
		try
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
			exporter.setExporterInput(new SimpleExporterInput(printIn));
			exporter.exportReport();

			String content = StringUtils.toEncodedString(outputStream.toByteArray(), Charset.defaultCharset());
			this.invoiceData.getEngine().loadContent(content);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

    @FXML
    void generateStmt(ActionEvent event) 
    {
		LocalDate fromDateVal = this.fromDate.getValue();
		LocalDate toDateVal = this.toDate.getValue();
		List<SalesTransaction> tableData = schoolService.fetchTransactions(school, fromDateVal, toDateVal);
    	JasperPrint jPrint = prepareJasperPrint(this.school, tableData, fromDateVal, toDateVal);
    	this.loadWebInvoice(jPrint);
    }
    
    
	@FXML
	public void exportAndSave(ActionEvent ev)
	{
		try
		{

			String selection = saveType.getSelectionModel().getSelectedItem(); 
			String filterStr = "*.*";
			if(selection.equals(PDF))
			{
				filterStr = "*.pdf";
			}
			else if(selection.equals(Excel))
			{
				filterStr = "*.xls";
			}
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(selection, filterStr)
                );
            
			File file = fileChooser.showSaveDialog(((Node) ev.getSource()).getScene().getWindow());
			if(selection.equals(PDF))
			{
				JasperExportManager.exportReportToPdfFile(print, file.getAbsolutePath());
			}
			else if(selection.equals(Excel))
			{
		        JRXlsxExporter exporter = new JRXlsxExporter();
		        exporter.setExporterInput(new SimpleExporterInput(print));		        
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));

		        exporter.exportReport();
			}
			else if(selection.equals(Docx))
			{
				JRDocxExporter exporter = new JRDocxExporter();
		        exporter.setExporterInput(new SimpleExporterInput(print));		        
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));

		        exporter.exportReport();
			}
			
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	@FXML
	public void printStmt(ActionEvent ev)
	{
		try
		{
			printJasper(print);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
