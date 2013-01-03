package org.zlogic.att.ui.adapters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Adapter to interface JPA with Java FX observable properties for TimeSegment classes.
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 30.12.12
 * Time: 0:15
 */
public class TimeSegmentAdapter {
	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private ObjectProperty<Date> start = new SimpleObjectProperty<>(), end = new SimpleObjectProperty<>();
	private TimeSegment segment;

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
							setTimeSegment(entityManager.merge(getTimeSegment()));
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
							setTimeSegment(entityManager.merge(getTimeSegment()));
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
							setTimeSegment(entityManager.merge(getTimeSegment()));
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
		else if (obj == null)
			return segment == null;
		else
			return false;
	}
}
