package org.zlogic.att.ui.adapters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

import javax.persistence.EntityManager;

/**
 * Adapter to interface JPA with Java FX observable properties for TimeSegment classes.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 30.12.12
 * Time: 0:15
 */
public class TimeSegmentAdapter {
	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private TimeSegment segment;

	public TimeSegmentAdapter(TimeSegment segment) {
		this.segment = segment;

		this.description.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue))
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
			}
		});
	}

	public void updateFxProperties() {
		description.setValue(segment.getDescription());
	}

	public TimeSegment getTimeSegment() {
		return segment;
	}

	private void setTimeSegment(TimeSegment segment) {
		this.segment = segment;
	}
}
