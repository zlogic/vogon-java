/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.io.Serializable;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * Entity class for a time segment Each task's time is tracked with
 * TimeSegments.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public class TimeSegment implements Serializable, Comparable<TimeSegment> {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/data/messages");
	/**
	 * JPA ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	/**
	 * The start time
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	/**
	 * The ending time
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	/**
	 * The time segment description
	 */
	private String description;
	/**
	 * The Task owning this TimeSegment
	 */
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	private Task owner;

	/**
	 * Default constructor
	 */
	protected TimeSegment() {
		id = -1;
		startTime = new Date();
		endTime = new Date();
		description = ""; //NOI18N
	}

	/**
	 * Returns the start time
	 *
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(Date startTime) {
		if (endTime.before(startTime))
			throw new java.lang.IllegalArgumentException(messages.getString("START_TIME_CANNOT_BE_LATER_THAN_END_TIME"));
		this.startTime = startTime;
	}

	/**
	 * Returns the end time
	 *
	 * @return the end time
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time
	 *
	 * @param endTime the new end time
	 */
	public void setEndTime(Date endTime) {
		if (endTime.before(startTime))
			throw new java.lang.IllegalArgumentException(messages.getString("END_TIME_CANNOT_BE_EARLIER_THAN_START_TIME"));
		this.endTime = endTime;
	}

	/**
	 * Sets the start and end time in a single call to prevent race conditions
	 *
	 * @param startTime the new start time
	 * @param endTime the new end time
	 */
	public void setStartEndTime(Date startTime, Date endTime) {
		if (endTime.before(startTime))
			throw new java.lang.IllegalArgumentException(messages.getString("START_TIME_CANNOT_BE_LATER_THAN_END_TIME"));
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Returns the description
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the owner task
	 *
	 * @return the owner task
	 */
	public Task getOwner() {
		return owner;
	}

	/**
	 * Assigns a new owner task. Removes this segment from the previous owner
	 * (if present)
	 *
	 * @param owner the new owner task
	 */
	public void setOwner(Task owner) {
		if (this.owner != null) {
			this.owner.removeSegment(this);
			this.owner = null;
		}
		this.owner = owner;
		owner.addSegment(this);
	}

	/**
	 * Returns the JPA ID
	 *
	 * @return the JPA ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the calculated time segment duration
	 *
	 * @return the calculated time segment duration
	 */
	public Period getDuration() {
		return new Interval(new DateTime(startTime), new DateTime(endTime)).toPeriod().normalizedStandard(PeriodType.time());
	}

	/**
	 * Returns the end time, clipped to fit into the specified time period, or
	 * null if this segment doesn't belong to the clipping time period
	 *
	 * @param clipStartTime start time of clip period
	 * @param clipEndTime end time of clip period
	 * @return the clipped start time
	 */
	public Date getClippedStartTime(Date clipStartTime, Date clipEndTime) {
		Date clippedStartTime = clipStartTime.before(startTime) ? startTime : clipStartTime;
		Date clippedEndTime = clipEndTime.after(endTime) ? endTime : clipEndTime;
		if (clippedStartTime.after(clippedEndTime))
			return null;
		else
			return clippedStartTime;
	}

	/**
	 * Returns the start time, clipped to fit into the specified time period, or
	 * null if this segment doesn't belong to the clipping time period
	 *
	 * @param clipStartTime start time of clip period
	 * @param clipEndTime end time of clip period
	 * @return the clipped end time
	 */
	public Date getClippedEndTime(Date clipStartTime, Date clipEndTime) {
		Date clippedStartTime = clipStartTime.before(startTime) ? startTime : clipStartTime;
		Date clippedEndTime = clipEndTime.after(endTime) ? endTime : clipEndTime;
		if (clippedStartTime.after(clippedEndTime))
			return null;
		else
			return clippedEndTime;
	}

	/**
	 * Returns the calculated time segment duration, clipped to fit into the
	 * specified time period
	 *
	 * @param clipStartTime start time of clip period
	 * @param clipEndTime end time of clip period
	 * @return the calculated time segment duration
	 */
	public Period getClippedDuration(Date clipStartTime, Date clipEndTime) {
		Date clippedStartTime = (clipStartTime == null || clipStartTime.before(startTime)) ? startTime : clipStartTime;
		Date clippedEndTime = (clipEndTime == null || clipEndTime.after(endTime)) ? endTime : clipEndTime;
		if (clippedStartTime.after(clippedEndTime))
			return new Period();
		return new Interval(new DateTime(clippedStartTime), new DateTime(clippedEndTime)).toPeriod().normalizedStandard(PeriodType.time());
	}

	/**
	 * Returns this class instance (return this); required for some reflection
	 * functions
	 *
	 * @return this TimeSegment instance
	 */
	public TimeSegment getTimeSegment() {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TimeSegment && id == ((TimeSegment) obj).id;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}

	@Override
	public int compareTo(TimeSegment timeSegment) {
		return Long.compare(id, timeSegment.id);
	}
}
