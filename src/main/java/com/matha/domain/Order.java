package com.matha.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

import javassist.expr.Instanceof;

@Entity
@Table(name = "SOrder")
public class Order implements Serializable, Comparable
{

	private static final long serialVersionUID = 2735264048303888145L;

	@Id
	@GenericGenerator(name = "orderId", strategy = "com.matha.generator.OrderIdGenerator")
	@GeneratedValue(generator = "orderId")
	@Column(name = "SerialId")
	private String id;

	@Column(name = "SerialNo")
	private String serialNo;

	@ManyToOne
	@JoinColumn(name = "CustId")
	private School school;

	@ManyToOne
	@JoinColumn(name = "SaleId")
	private Sales sale;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "SerialId")
	@Cascade({ CascadeType.SAVE_UPDATE })
	private List<OrderItem> orderItem;

	@Column(name = "TDate")
	private LocalDate orderDate;

	@Column(name = "DlyDate")
	private LocalDate deliveryDate;

	@Column(name = "desLocation")
	private String desLocation;

	public Integer getOrderCount()
	{
		return orderItem == null ? 0 : orderItem.size();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getSerialNo()
	{
		return serialNo;
	}

	public void setSerialNo(String serialNo)
	{
		this.serialNo = serialNo;
	}

	public String getSchoolName()
	{
		return school.getName();
	}

	public School getSchool()
	{
		return school;
	}

	public void setSchool(School school)
	{
		this.school = school;
	}

	public Sales getSale()
	{
		return sale;
	}

	public void setSale(Sales sale)
	{
		this.sale = sale;
	}

	public List<OrderItem> getOrderItem()
	{
		return orderItem;
	}

	public void setOrderItem(List<OrderItem> orderItem)
	{
		this.orderItem = orderItem;
	}

	public LocalDate getOrderDate()
	{
		return orderDate;
	}

	public void setOrderDate(LocalDate dt)
	{
		this.orderDate = dt;
	}

	public LocalDate getDeliveryDate()
	{
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDate deliveryDate)
	{
		this.deliveryDate = deliveryDate;
	}

	public String getDesLocation()
	{
		return desLocation;
	}

	public void setDesLocation(String desLocation)
	{
		this.desLocation = desLocation;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Order [id=");
		builder.append(id);
		builder.append(", school=");
		builder.append(school);
		// builder.append(", orderItem=");
		// builder.append(orderItem);
		builder.append(", orderDate=");
		builder.append(orderDate);
		builder.append(", deliveryDate=");
		builder.append(deliveryDate);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(Object o)
	{
		if(o != null && o instanceof Order && ((Order)o).getId() != null)
		{
			Order order = (Order)o;
			if(order.getId() != null && this.getId() != null && order.getId().equals(this.getId()))
			{
				return 0;
			}
		}
		return -1;
	}

}
