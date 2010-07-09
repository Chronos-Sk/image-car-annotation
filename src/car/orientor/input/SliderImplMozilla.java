package car.orientor.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

/**
 * The {@link SliderImp} implementation used for Mozilla Firefox, since it
 * doesn't have a native slider implementation.
 * 
 * This class delegates to GWT Incubator's <code>SliderBar</code> class.
 * 
 * @author Joshua Little
 */
@SuppressWarnings("deprecation") // SliderBar uses ChangeListeners.
public class SliderImplMozilla implements SliderImpl {
	
	/**
	 * An extension of <code>SliderBar</code> that fixes some compatibility
	 * issues with Firefox and adds support for <code>ChangeHandler</code>s
	 * and <code>ValueChangeHandler</code>s.
	 * 
	 * @author Joshua
	 */
	private static class SliderBarExtra extends SliderBar
				implements HasChangeHandlers, HasValueChangeHandlers<Double> {
		
		// Delegating constructors.
		
		public SliderBarExtra(double minValue, double maxValue) {
			super(minValue, maxValue);
			fixit();
		}

		public SliderBarExtra(double minValue, double maxValue,
				LabelFormatter labelFormatter, SliderBarImages images) {
			super(minValue, maxValue, labelFormatter, images);
			fixit();
		}

		public SliderBarExtra(double minValue, double maxValue,
				LabelFormatter labelFormatter) {
			super(minValue, maxValue, labelFormatter);
			fixit();
		}
		
		/**
		 * Adds the <code>ChangeListener</code> that fires <code>ChangeEvent
		 * </code>s and <code>ValueChangeEvent</code>s. Also calls
		 * {@link #fixMozillaCompatibility()}.
		 */
		private void fixit() {
			fixMozillaCompatibility();
			
			// SliderBar only generates listener events. This converts them into handler events.
			addChangeListener(new ChangeListener() {
				@Override
				public void onChange(Widget sender) {
					NativeEvent changeEvent
							= Document.get().createChangeEvent();
					ChangeEvent.fireNativeEvent(
							changeEvent, SliderBarExtra.this);
					ValueChangeEvent.fire(
							SliderBarExtra.this, getCurrentValue());
				}
			});
			
			setStepSize(1.0);
			setCurrentValue(50.0);
		}
		
		/**
		 * FireFox apparently doesn't have the function document.getBoxObjectFor().
		 * SliderBar needs this, so we'll have to do it ourselves.
		 * 
		 * Thank you, StackOverflow.com.
		 */
		private static native void fixMozillaCompatibility() /*-{
			if ( !$doc.getBoxObjectFor ) {
				$doc.getBoxObjectFor = function (element) {
					var box = element.getBoundingClientRect();
					return {
						"x" : box.left,
						"y" : box.top,
						"width" : box.width,
						"height" : box.height,
						"screenX": box.left,
						"screenY":box.top };
					}
			}
		}-*/;

		/**
		 * Registers the supplied <code>ChangeHandler</code>.
		 * 
		 * @param handler the <code>ChangeHandler</code> to register.
		 * @return the <code>HandlerRegistration</code> representing this registration.
		 */
		@Override
		public HandlerRegistration addChangeHandler(ChangeHandler handler) {
			return addHandler(handler, ChangeEvent.getType());
		}

		/**
		 * Registers the supplied <code>ValueChangeHandler</code>.
		 * 
		 * @param handler the <code>ValueChangeHandler</code> to register.
		 * @return the <code>HandlerRegistration</code> representing this registration.
		 */
		@Override
		public HandlerRegistration addValueChangeHandler(
									ValueChangeHandler<Double> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}
		
	}
	
	public Widget createWidget() {
		return new SliderBarExtra(0, 100);
	}
	
	// Simply delegates.
	
	public HandlerRegistration addValueChangeHandler(Widget slider, ValueChangeHandler<Double> handler) {
		return getSliderBar(slider).addValueChangeHandler(handler);
	}

	private SliderBarExtra getSliderBar(Widget slider) {
		return (SliderBarExtra) slider;
	}
	
	@Override
	public double getMaximum(Widget slider) {
		return getSliderBar(slider).getMaxValue();
	}

	@Override
	public double getMinimum(Widget slider) {
		return getSliderBar(slider).getMinValue();
	}

	@Override
	public double getValue(Widget slider) {
		return getSliderBar(slider).getCurrentValue();
	}

	@Override
	public void setMaximum(Widget slider, double max) {
		getSliderBar(slider).setMaxValue(max);
	}

	@Override
	public void setMinimum(Widget slider, double min) {
		getSliderBar(slider).setMinValue(min);
		
	}

	@Override
	public void setValue(Widget slider, double value) {
		getSliderBar(slider).setCurrentValue(value);
	}

	@Override
	public double getStep(Widget slider) {
		return getSliderBar(slider).getStepSize();
	}

	@Override
	public void setStep(Widget slider, double step) {
		getSliderBar(slider).setStepSize(step);
	}

}
