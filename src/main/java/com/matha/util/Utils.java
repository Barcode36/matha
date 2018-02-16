package com.matha.util;

import static com.matha.util.UtilConstants.PERCENT_SIGN;
import static com.matha.util.UtilConstants.RUPEE_SIGN;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.matha.domain.OrderItem;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.Callback;

public class Utils
{

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

		if (job != null && job.showPrintDialog(parentWindow))
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

	public static Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> fetchPriceColumnFactory()
	{
		Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>> priceColumnFactory = new Callback<CellDataFeatures<OrderItem, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<OrderItem, String> p)
			{
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyStringWrapper(p.getValue().getBookPrice().toString());
			}
		};
		return priceColumnFactory;
	}
	
	public static void calcNetAmountGen(String discAmtStr, TextField subTotal, RadioButton percentRad, RadioButton rupeeRad, TextField netAmt)
	{
		String netTotalStr = netAmt.getText();
		Double netTotalDbl = StringUtils.isEmpty(netTotalStr) ? 0.0 : Double.parseDouble(netTotalStr);

		String subTotalStr = subTotal.getText();
		if (subTotalStr != null)
		{
			double subTotalDbl = Double.parseDouble(subTotalStr);
			if (subTotalDbl > 0)
			{
				double discAmtDbl = StringUtils.isEmpty(discAmtStr) ? 0 : Double.parseDouble(discAmtStr);

				if (discAmtDbl > 0)
				{
					if (rupeeRad.isSelected())
					{
						netTotalDbl = subTotalDbl - discAmtDbl;
					}
					else if (percentRad.isSelected())
					{
						netTotalDbl = subTotalDbl - subTotalDbl * discAmtDbl / 100;
					}
				}
				else
				{
					netTotalDbl = subTotalDbl;
				}
			}
		}
		netAmt.setText(getStringVal(netTotalDbl));
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
}
