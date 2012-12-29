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

	public TimeSegment() {
		startTime = new Date();
		endTime = new Date();
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
		this.owner = owner;
	}

	public Period getDuration() {
		return new Interval(new DateTime(startTime), new DateTime(endTime)).toPeriod();
	}
}
