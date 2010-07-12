package car.orientor.input;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * The interface specifying functions required for an implementation for the
 * {@link Slider} class. GWT's deferred binding is used to create an instance
 * of the appropriate implementing class.
 * 
 * @author Joshua Little
 */
public interface SliderImpl {

	/**
	 * Creates a <code>Widget</code> that can be used to represent a
	 * {@link Slider}.
	 * 
	 * @return a <code>Widget</code> to represent a <code>Slider</code>
	 */
	Widget createWidget();
	
	/**
	 * Sets the minimum value for the supplied {@link Slider}
	 * 
	 * @param slider the <code>Slider</code> to change.
	 * @param min the new minimum value.
	 */
	void setMinimum(Widget slider, double min);
	
	/**
	 * Returns the minimum value for the supplied {@link Slider}.
	 * 
	 * @param slider the <code>Slider</code> to query.
	 * @return the minimum value.
	 */
	double getMinimum(Widget slider);

	/**
	 * Sets the maximum value for the supplied {@link Slider}
	 * 
	 * @param slider the <code>Slider</code> to change.
	 * @param max the new maximum value.
	 */
	void setMaximum(Widget slider, double max);

	/**
	 * Returns the maximum value for the supplied {@link Slider}.
	 * 
	 * @param slider the <code>Slider</code> to query.
	 * @return the maximum value.
	 */
	double getMaximum(Widget slider);

	/**
	 * Sets the value for the supplied {@link Slider}
	 * 
	 * @param slider the <code>Slider</code> to change.
	 * @param value the new value.
	 */
	void setValue(Widget slider, double value);

	/**
	 * Returns the value for the supplied {@link Slider}.
	 * 
	 * @param slider the <code>Slider</code> to query.
	 * @return the value.
	 */
	double getValue(Widget slider);

	/**
	 * Sets the step value for the supplied {@link Slider}
	 * 
	 * @param slider the <code>Slider</code> to change.
	 * @param step the new step value.
	 */
	void setStep(Widget slider, double step);

	/**
	 * Returns the step value for the supplied {@link Slider}.
	 * 
	 * @param slider the <code>Slider</code> to query.
	 * @return the step value.
	 */
	double getStep(Widget slider);
	
	/**
	 * Registers the supplied <code>ValueChangeHandler</code> to the supplied
	 * <code>Slider</code>
	 * 
	 * @param slider the <code>Slider</code> to register to.
	 * @param handler the <code>ValueChangeHandler</code> to register.
	 * @return the <code>HandlerRegistration</code> for this registration.
	 */
	HandlerRegistration addValueChangeHandler(Widget slider, ValueChangeHandler<Double> handler);
}
