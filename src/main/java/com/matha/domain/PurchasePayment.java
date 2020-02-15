package com.matha.domain;

import static com.matha.util.Converters.convertTimestamp;
import static com.matha.util.UtilConstants.DATE_CONV;
import static com.matha.util.UtilConstants.STATUS_MAP;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PPayment")
public class PurchasePayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SerialId")
	private Integer id;

	@Column(name = "ReceiptNum")
	private String receiptNum;

	@Column(name = "Mode")
	private String paymentMode;

	@Column(name = "Dated")
	private Timestamp dated;

	@Column(name = "RefNum")
	private String referenceNum;

	@Column(name = "Status")
	private Integer statusInd;

	@OneToOne
	@JoinColumn(name = "TxnId")
	private PurchaseTransaction salesTxn;

	@Column(name = "DelAmt")
	private Double deletedAmt;

	public String getStatusStr()
	{
		return STATUS_MAP.get(getStatusInd());
	}

	public LocalDate getTxnDate()
	{
		return convertTimestamp(salesTxn.getTxnDate());
	}
	
	public String getTxnDateStr()
	{		
		return DATE_CONV.toString(getTxnDate());
	}
	
	public Double getAmount()
	{
		return salesTxn.getAmount();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Timestamp getDated() {
		return dated;
	}

	public void setDated(Timestamp dated) {
		this.dated = dated;
	}

	public String getReferenceNum() {
		return referenceNum;
	}

	public void setReferenceNum(String referenceNum) {
		this.referenceNum = referenceNum;
	}

	public PurchaseTransaction getSalesTxn() {
		return salesTxn;
	}

	public void setSalesTxn(PurchaseTransaction salesTxn) {
		this.salesTxn = salesTxn;
	}

	public String getReceiptNum() {
		return receiptNum;
	}

	public void setReceiptNum(String receiptNum) {
		this.receiptNum = receiptNum;
	}

	public Integer getStatusInd() {
		return statusInd;
	}

	public void setStatusInd(Integer statusInd) {
		this.statusInd = statusInd;
	}

	public Double getDeletedAmt()
	{
		return deletedAmt;
	}

	public void setDeletedAmt(Double deletedAmt)
	{
		this.deletedAmt = deletedAmt;
	}
}
