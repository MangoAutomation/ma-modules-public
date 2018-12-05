/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.DocumentationItem;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.help.HelpModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.help.RelatedHelpItemModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Help", description="Mango Help")
@RestController
@RequestMapping("/help")
public class HelpRestController extends MangoRestController{
	
	public HelpRestController(){ }

	@ApiOperation(
			value = "Get Help",
			notes = "request help via the internal Mango help id",
			response=HelpModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, value="/by-id/{helpId}")
    public ResponseEntity<HelpModel> getHelp(
    		@PathVariable String helpId,
    		HttpServletRequest request) {
		
		RestProcessResult<HelpModel> result = new RestProcessResult<HelpModel>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){
    		HelpModel model = new HelpModel();
            DocumentationItem item = Common.documentationManifest.getItem(helpId);
            if (item == null){
                result.addRestMessage(this.getDoesNotExistMessage());
            }else {
            	model.setId(item.getId());
            	model.setTitle(new TranslatableMessage(item.getKey()).translate(Common.getTranslations()));
            	
                // Find the file appropriate for the locale.
                Locale locale = ControllerUtils.getLocale(request);
                File file = Common.documentationManifest.getDocumentationFile(item, locale.getLanguage(),
                        locale.getCountry(), locale.getVariant());

                // Read the content.
                try {
                    Reader in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                    StringWriter out = new StringWriter();
                    StreamUtils.transfer(in, out);
                    in.close();
                    
                    model.setContent(out.toString());
                    
                    List<RelatedHelpItemModel> relatedItems = new ArrayList<RelatedHelpItemModel>();
                    for (String relatedId : item.getRelated()) {
                        DocumentationItem relatedItem = Common.documentationManifest.getItem(relatedId);
                        if (relatedItem == null)
                            throw new RuntimeException("Related document '" + relatedId + "' not found");
                        relatedItems.add(new RelatedHelpItemModel(relatedItem.getId(),new TranslatableMessage(relatedItem.getKey()).translate(Common.getTranslations()) ));
                    }
                    model.setRelatedList(relatedItems);
                    return result.createResponseEntity(model);
                }
                catch (FileNotFoundException e) {
                    result.addRestMessage(HttpStatus.NOT_FOUND, new TranslatableMessage("dox.fileNotFound", file.getPath()));
                }
                catch (IOException e) {
                	result.addRestMessage(HttpStatus.NOT_FOUND, new TranslatableMessage("dox.readError", e.getClass().getName(), e.getMessage()));
                }
            }
    		
    	}
    	
    	return result.createResponseEntity();
	}
	

}
