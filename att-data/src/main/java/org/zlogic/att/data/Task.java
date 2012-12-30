package org.zlogic.att.data;

import org.joda.time.Period;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

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
	private Boolean completed;

	public Task() {
		id = -1;
		description = "";
		name = "";
		completed = false;
		timeSegments = new TreeSet<>();
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

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean complete) {
		this.completed = complete;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Task && id == ((Task) obj).id;
	}
}
