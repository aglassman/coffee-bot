package org.gmjm.slack.brew.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="brew")
public final class Brew {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String brewName;

	private Date brewDate;

	private String brewedBy;

	private boolean gone;

	public Brew() {
		gone = false;
	}


	public Brew(Long id, String brewName, Date brewDate, String brewedBy, boolean gone)
	{
		this.id = id;
		this.brewName = brewName;
		this.brewDate = brewDate;
		this.brewedBy = brewedBy;
		this.gone = gone;
	}


	public Long getId()
	{
		return id;
	}


	public void setId(Long id)
	{
		this.id = id;
	}


	public String getBrewName()
	{
		return brewName;
	}


	public void setBrewName(String brewName)
	{
		this.brewName = brewName;
	}


	public Date getBrewDate()
	{
		return brewDate;
	}


	public void setBrewDate(Date brewDate)
	{
		this.brewDate = brewDate;
	}


	public String getBrewedBy()
	{
		return brewedBy;
	}


	public void setBrewedBy(String brewedBy)
	{
		this.brewedBy = brewedBy;
	}


	public boolean isGone()
	{
		return gone;
	}


	public void setGone(boolean gone)
	{
		this.gone = gone;
	}
}
