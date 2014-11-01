/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.zlogic.vogon.web.configuration.VogonConfiguration;

/**
 * Spring MVC controller for index.jsp configuration (add VogonConfiguration
 * reference)
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping("/")
public class JspController {

	/**
	 * The configuration handler
	 */
	@Autowired
	private VogonConfiguration configuration;

	/**
	 * Returns the configured ModelAndView
	 *
	 * @return the configured ModelAndView
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView indexModelAndView() {
		//Name should be the same as ViewControllerRegistry.addViewController.setViewName
		ModelAndView model = new ModelAndView("index"); //NOI18N
		model.addObject("configuration", configuration); //NOI18N
		return model;
	}
}
