package org.zlogic.att.data;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity class for a time segment
 * Each task's time is tracked with TimeSegments.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 13:47
 */
@Entity
public class TimeSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private String description;
	@ManyToOne
	private Task owner;

	protected TimeSegment() {
		id = -1;
		startTime = new Date();
		endTime = new Date();
		description = "";
	}

	public TimeSegment(Task owner) {
		this();
		setOwner(owner);
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Task getOwner() {
		return owner;
	}

	public void setOwner(Task owner) {
		//TODO: test inside transactions
		if (this.owner != null) {
			this.owner.removeSegment(this);
			this.owner = null;
		}
		this.owner = owner;
		owner.addSegment(this);
	}

	public Period getDuration() {
		return new Interval(new DateTime(startTime), new DateTime(endTime)).toPeriod();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TimeSegment && id == ((TimeSegment) obj).id;
	}
}
