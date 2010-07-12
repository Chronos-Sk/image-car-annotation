package car.orientor.input;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Represents a slider control in an HTML form. Generally this is represented by
 * an <code>&lt;input&gt;</code> element with a <code>type</code> of <code>range
 * </code>. In FireFox, however, the slider control has to be emulated using
 * JavaScript and CSS.
 * 
 * Currently, this class simply wraps an instance of {@link SliderImpl} obtained
 * via deffered binding.
 * 
 * @author Joshua Little
 */
public class Slider extends Composite implements HasValueChangeHandlers<Double>{
	
	//The actual implementation, which is different FireFox than other browsers.
	private static final SliderImpl impl = GWT.create(SliderImpl.class);
	
	/**
	 * Constructs a new <code>Slider</code> with a range of 0 to 100.
	 */
	public Slider() {
		initWidget(impl.createWidget());
		
		setMinimum(0);
		setMaximum(100);
	}
	
	/**
	 * Sets the minimum value the <code>Slider</code> will take.
	 * 
	 * @param min the new minimum value.
	 */
	public void setMinimum(double min) {
		impl.setMinimum(getWidget(), min);
	}

	/**
	 * Returns the minimum value the <code>Slider</code> will take.
	 * 
	 * @return the minimum value.
	 */
	public double getMinimum() {
		return impl.getMinimum(getWidget());
	}

	/**
	 * Sets the maximum value the <code>Slider</code> will take.
	 * 
	 * @param max the new maximum value.
	 */
	public void setMaximum(double max) {
		impl.setMaximum(getWidget(), max);
	}

	/**
	 * Returns the maximum value the <code>Slider</code> will take.
	 * 
	 * @return the maximum value.
	 */
	public double getMaximum() {
		return impl.getMaximum(getWidget());
	}

	/**
	 * Changes the value of this <code>Slider</code> to the supplied value.
	 * 
	 * @param value the new value.
	 */
	public void setValue(double value) {
		impl.setValue(getWidget(), value);
	}
	
	/**
	 * Returns the current value of this <code>Slider</code>.
	 * 
	 * @return the current value.
	 */
	public double getValue() {
		return impl.getValue(getWidget());
	}
	
	/**
	 * Registers a <code>ValueChangeListener</code> to this <code>Slider</code>.
	 * The listener will get a <code>ValueChangeEvent</code> everytime the value
	 * of this <code>Slider</code> changes.
	 * 
	 * @param handler the <code>ValueChangeHandler</code> to register.
	 */
	@Override
	public HandlerRegistration addValueChangeHandler(
					ValueChangeHandler<Double> handler) {
		impl.addValueChangeHandler(getWidget(), handler);
		return null;
	}
}
