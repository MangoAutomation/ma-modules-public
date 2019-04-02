/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.textRenderer;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.NoneRenderer;
import com.serotonin.m2m2.view.text.PlainRenderer;
import com.serotonin.m2m2.view.text.RangeRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.view.text.TimeRenderer;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class TextRendererFactory {

	/**
	 * @param vo
	 * @return
	 */
	public static BaseTextRendererModel<?> createModel(DataPointVO vo) {
		TextRenderer renderer = vo.getTextRenderer();
		//For when a VO is created that has no renderer
		if(renderer == null)
			return new NoneTextRendererModel();
		
		if(renderer.getTypeName().equals(AnalogRenderer.getDefinition().getName())){
			AnalogRenderer r = (AnalogRenderer)renderer;
			AnalogTextRendererModel model = new AnalogTextRendererModel(
					r.isUseUnitAsSuffix(),
					UnitUtil.formatLocal(r.getUnit()),
					UnitUtil.formatLocal(r.getRenderedUnit()),
					r.getFormat(), r.getSuffix());
			return model;
		}else if(renderer.getTypeName().equals(BinaryTextRenderer.getDefinition().getName())){
			BinaryTextRendererModel model = new BinaryTextRendererModel();
			BinaryTextRenderer btr = (BinaryTextRenderer)renderer;
			model.setOneColour(btr.getOneColour());
			model.setOneLabel(btr.getOneLabel());
			model.setZeroColour(btr.getZeroColour());
			model.setZeroLabel(btr.getZeroLabel());
			return model;
		}else if(renderer.getTypeName().equals(MultistateRenderer.getDefinition().getName())){
			MultistateRenderer r = (MultistateRenderer)renderer;
			MultistateTextRendererModel model = new MultistateTextRendererModel(r.getMultistateValues());
			return model;
		}else if(renderer.getTypeName().equals(NoneRenderer.getDefinition().getName())){
			return new NoneTextRendererModel();
		}if(renderer.getTypeName().equals(PlainRenderer.getDefinition().getName())){
			PlainRenderer r = (PlainRenderer)renderer;
			PlainTextRendererModel model = new PlainTextRendererModel(
					r.isUseUnitAsSuffix(),
					UnitUtil.formatLocal(r.getUnit()),
					UnitUtil.formatLocal(r.getRenderedUnit()),
					r.getSuffix());
			return model;
		}else if(renderer.getTypeName().equals(RangeRenderer.getDefinition().getName())){
			RangeRenderer r = (RangeRenderer)renderer;
			RangeTextRendererModel model = new RangeTextRendererModel(
					r.isUseUnitAsSuffix(),
					UnitUtil.formatLocal(r.getUnit()),
					UnitUtil.formatLocal(r.getRenderedUnit()),
					r.getFormat(), r.getRangeValues());
			return model;
		}else if(renderer.getTypeName().equals(TimeRenderer.getDefinition().getName())){
			TimeRenderer r = (TimeRenderer)renderer;
			TimeTextRendererModel model = new TimeTextRendererModel(r.getFormat(), r.getConversionExponent());
			return model;
		}else{
			throw new ShouldNeverHappenException("Unknown Text Renderer: " + renderer.getDef().getName());
		}
	}

	/**
	 * @param data
	 * @param renderer
	 */
	public static void updateDataPoint(DataPointVO vo,
			BaseTextRendererModel<?> baseRendererModel) {
	    vo.setTextRenderer(createTextRenderer(baseRendererModel));
	}
	
	/**
	 * Create a renderer from a model
	 * @param baseRendererModel
	 * @return
	 */
	public static TextRenderer createTextRenderer(BaseTextRendererModel<?> baseRendererModel) {
	       if(baseRendererModel.getType().equals(AnalogRenderer.getDefinition().getName())){
	            AnalogTextRendererModel model = (AnalogTextRendererModel)baseRendererModel;
	            AnalogRenderer r = new AnalogRenderer();
	            r.setUseUnitAsSuffix(model.isUseUnitAsSuffix());
	            r.setFormat(model.getFormat());
	            r.setSuffix(model.getSuffix());
	            return r;
	        }else if(baseRendererModel.getType().equals(BinaryTextRenderer.getDefinition().getName())){
	            BinaryTextRendererModel model = (BinaryTextRendererModel)baseRendererModel;
	            BinaryTextRenderer btr = new BinaryTextRenderer();
	            btr.setOneColour(model.getOneColour());
	            btr.setOneLabel(model.getOneLabel());
	            btr.setZeroColour(model.getZeroColour());
	            btr.setZeroLabel(model.getZeroLabel());
	            return btr;
	        }else if(baseRendererModel.getType().equals(MultistateRenderer.getDefinition().getName())){
	            MultistateRenderer r = new MultistateRenderer();
	            MultistateTextRendererModel model = (MultistateTextRendererModel)baseRendererModel;
	            r.setMultistateValues(model.getMultistateValues());
	            return r;
	        }else if(baseRendererModel.getType().equals(NoneRenderer.getDefinition().getName())){
	            NoneRenderer r = new NoneRenderer();
	            return r;
	        }else if(baseRendererModel.getType().equals(PlainRenderer.getDefinition().getName())){
	            PlainTextRendererModel model = (PlainTextRendererModel)baseRendererModel;
	            PlainRenderer r = new PlainRenderer();
	            r.setUseUnitAsSuffix(model.isUseUnitAsSuffix());
	            r.setSuffix(model.getSuffix());
	            return r;
	        }else if(baseRendererModel.getType().equals(RangeRenderer.getDefinition().getName())){
	            RangeTextRendererModel model = (RangeTextRendererModel)baseRendererModel;
	            RangeRenderer r = new RangeRenderer();
	            r.setUseUnitAsSuffix(model.isUseUnitAsSuffix());
	            r.setFormat(model.getFormat());
	            r.setRangeValues(model.getRangeValues());
	            return r;
	        }else if(baseRendererModel.getType().equals(TimeRenderer.getDefinition().getName())){
	            TimeRenderer r = new TimeRenderer();
	            TimeTextRendererModel model = (TimeTextRendererModel)baseRendererModel;
	            r.setFormat(model.getFormat());
	            r.setConversionExponent(model.getConversionExponent());
	            return r;
	        }else{
	            throw new ShouldNeverHappenException("Unknown Text Renderer: " + baseRendererModel.getType());
	        }
	}

}
