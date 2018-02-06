package com.matha.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "SOrderDet")
public class OrderItem implements Serializable, Comparable<OrderItem>
{

	private static final long serialVersionUID = -2960499864752422068L;

	@Id
	@Column(name = "OrderDetId")
	@GenericGenerator(name = "kaugen", strategy = "increment")
	@GeneratedValue(generator = "kaugen")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "SerialId")
	private Order order;

	@ManyToOne
	@JoinColumn(name = "PurchaseId")
	private Purchase purchase;

	@ManyToOne()
	@JoinColumn(name = "BkNo")
	private Book book;

	@Column(name = "Qty")
	private int count;

	@Column(name = "SlNo")
	private int serialNum;

	@Column(name = "BookPrice")
	private Double bookPrice;

	@ManyToOne()
	@JoinColumn(name = "ReturnId")
	private SchoolReturn bookReturn;

	@ManyToOne()
	@JoinColumn(name = "PurReturnId")
	private PurchaseReturn purchReturn;

	public double getTotal()
	{
		return getBookPrice() * count;
	}

	public Double getBookPrice()
	{
		if (bookPrice == null)
		{
			return book.getPrice();
		}
		return bookPrice;
	}

	public void setBookPrice(Double bookPrice)
	{
		this.bookPrice = bookPrice;
	}

	public SchoolReturn getBookReturn()
	{
		return bookReturn;
	}

	public void setBookReturn(SchoolReturn bookReturn)
	{
		this.bookReturn = bookReturn;
	}

	public PurchaseReturn getPurchReturn()
	{
		return purchReturn;
	}

	public void setPurchReturn(PurchaseReturn purchReturn)
	{
		this.purchReturn = purchReturn;
	}

	public String getBookName()
	{
		return book.getName();
	}

	public String getPublisherName()
	{
		return book.getPublisher().getName();
	}

	public String getSchoolName()
	{
		return this.order.getSchool().getName();
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Order getOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	public Purchase getPurchase()
	{
		return purchase;
	}

	public void setPurchase(Purchase purchase)
	{
		this.purchase = purchase;
	}

	public Book getBook()
	{
		return book;
	}

	public void setBook(Book book)
	{
		this.book = book;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getSerialNum()
	{
		return serialNum;
	}

	public void setSerialNum(int serialNum)
	{
		this.serialNum = serialNum;
	}

	public String getOrderId()
	{
		return this.order.getId();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("OrderItem [id=");
		builder.append(id);
		builder.append(", serialId=");
		builder.append(order);
		builder.append(", book=");
		builder.append(book);
		// builder.append(", bookId=");
		// builder.append(bookId);
		builder.append(", count=");
		builder.append(count);
		builder.append(", serialNum=");
		builder.append(serialNum);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(OrderItem o)
	{
		if (o != null && o.getId() != null && this.getId() != null && this.getId().equals(o.getId()))
		{
			return 0;
		}
		else
		{
			return -1;
		}

	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderItem other = (OrderItem) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	
}
