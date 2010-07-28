package car.shared.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * The {@link SliderImpl} implementation used for most web browsers.
 * 
 * @author Joshua Little
 */
public class SliderImplGeneral implements SliderImpl {
	
	/**
	 * An extension of <code>FocusWidget</code> used to wrap the slider HTML
	 * element. Has code for enabling <code>ChangeEvent</code>s and handling
	 * <code>ValueChangeEvent</code>s
	 * 
	 * @author Joshua Little
	 */
	private class SliderImplWidget extends FocusWidget
					implements HasValueChangeHandlers<Double> {
		
		/**
		 * Creates an instance of <code>SliderImplWidget</code>. Creates the
		 * element, sets its <code>type</code> attribute, enables <code>
		 * ChangeEvent</code>'s and sets up a <code>ChangeEventHandler</code>
		 * that fires off <code>ValueChangeEvent</code>s.
		 */
		public SliderImplWidget() {
			super(Document.get().createElement("input"));
			getElement().setAttribute("type", "range");
			
			sinkEvents(Event.ONCHANGE);
			
			addHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(SliderImplWidget.this, getValue(getElement()));
				}
			}, ChangeEvent.getType());
		}
		
		/**
		 * Registers the supplied <code>ValueChangeHandler</code>.
		 * 
		 * @param handler the <code>ValueChangeHandler</code> to register.
		 * @return the <code>HandlerRegistration</code> representing this registration.
		 */
		public HandlerRegistration addValueChangeHandler(
						ValueChangeHandler<Double> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}
	}
	
	@Override
	public Widget createWidget() {
		return new SliderImplWidget();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			Widget slider, final ValueChangeHandler<Double> handler) {
		// The SliderImplWidget takes care of this.
		return ((SliderImplWidget) slider).addValueChangeHandler(handler);
	}
	
	// These all just pass off the request to the JSNI code.
	
	@Override
	public double getMaximum(Widget slider) {
		return getMaximum(slider.getElement());
	}

	@Override
	public double getMinimum(Widget slider) {
		return getMinimum(slider.getElement());
	}

	@Override
	public double getValue(Widget slider) {
		return getValue(slider.getElement());
	}

	@Override
	public double getStep(Widget slider) {
		return getStep(slider.getElement());
	}

	@Override
	public void setMaximum(Widget slider, double max) {
		setMaximum(slider.getElement(), max);
	}

	@Override
	public void setMinimum(Widget slider, double min) {
		setMinimum(slider.getElement(), min);
	}

	@Override
	public void setValue(Widget slider, double value) {
		setValue(slider.getElement(), value);
	}

	@Override
	public void setStep(Widget slider, double step) {
		setStep(slider.getElement(), step);
	}
	
	// The JSNI code, which allows direct access to the slider HTML element.
	
	public native void setMinimum(Element input, double min) /*-{
		input.min = min;
	}-*/;

	public native double getMinimum(Element input) /*-{
		return parseFloat(input.min);
	}-*/;
	
	public native void setMaximum(Element input, double max) /*-{
		input.max = max;
	}-*/;
	
	public native double getMaximum(Element input) /*-{
		return parseFloat(input.max);
	}-*/;
	
	public native void setValue(Element input, double value) /*-{
		input.value = value;
	}-*/;
	
	public native double getValue(Element input) /*-{
		return parseFloat(input.value);
	}-*/;

	public native void setStep(Element input, double value) /*-{
		input.step = value;
	}-*/;
	
	public native double getStep(Element input) /*-{
		return parseFloat(input.step);
	}-*/;

}
