/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for TimeSegment
 * classes.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TimeSegmentAdapter {

	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private ObjectProperty<Date> start = new SimpleObjectProperty<>(), end = new SimpleObjectProperty<>();
	private TimeSegment segment;
	private Timer timer;

	public TimeSegmentAdapter(TimeSegment segment) {
		this.segment = segment;

		updateFxProperties();
		this.description.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private String newDescription;

						public TransactedChange setNewDescription(String newDescription) {
							this.newDescription = newDescription;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
							getTimeSegment().setDescription(newDescription);
						}
					}.setNewDescription(newValue));
					updateFxProperties();
				}
			}
		});

		this.start.addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private Date newDate;

						public TransactedChange setNewDate(Date newDate) {
							this.newDate = newDate;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
							getTimeSegment().setStartTime(newDate);
						}
					}.setNewDate(newValue));
					updateFxProperties();
				}
			}
		});

		this.end.addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private Date newDate;

						public TransactedChange setNewDate(Date newDate) {
							this.newDate = newDate;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
							getTimeSegment().setEndTime(newDate);
						}
					}.setNewDate(newValue));
					updateFxProperties();
				}
			}
		});
	}

	public ObjectProperty<Date> startProperty() {
		return start;
	}

	public ObjectProperty<Date> endProperty() {
		return end;
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public void startTiming() {
		startProperty().setValue(new Date());
		if (timer != null)
			timer.cancel();
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			private ObjectProperty<Date> endProperty;

			public TimerTask setEndProperty(ObjectProperty<Date> endProperty) {
				this.endProperty = endProperty;
				return this;
			}

			@Override
			public void run() {
				endProperty.setValue(new Date());
			}
		}.setEndProperty(endProperty()), 0, 1000);
	}

	public void stopTiming() {
		timer.cancel();
		timer = null;
		endProperty().setValue(new Date());
	}

	public void updateFxProperties() {
		description.setValue(segment.getDescription());
		start.setValue(segment.getStartTime());
		end.setValue(segment.getEndTime());
	}

	public TimeSegment getTimeSegment() {
		return segment;
	}

	private void setTimeSegment(TimeSegment segment) {
		this.segment = segment;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeSegment)
			return obj.equals(segment);
		else if (obj instanceof TimeSegmentAdapter)
			return ((TimeSegmentAdapter) obj).getTimeSegment().equals(segment);
		else
			return obj == null && segment == null;
	}
}
