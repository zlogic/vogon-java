package org.zlogic.att.data;

import org.joda.time.Period;

import javax.persistence.*;
import java.util.Set;

/**
 * Entity class for a tracked task.
 * Each task's time is tracked with TimeSegments.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 21:03
 */
@Entity
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	private String description;
	private String name;
	@OneToMany
	private Set<TimeSegment> timeSegments;
	private Boolean complete;

	public Task() {
		description = "";
		name = "";
		complete = new Boolean(false);
	}

	public void addSegment(TimeSegment segment) {
		timeSegments.add(segment);
	}

	public void removeSegment(TimeSegment segment) {
		timeSegments.remove(segment);
	}

	public Period getTotalTime() {
		Period totalTime = new Period();
		for (TimeSegment segment : timeSegments)
			totalTime.plus(segment.getDuration());
		return totalTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
}
