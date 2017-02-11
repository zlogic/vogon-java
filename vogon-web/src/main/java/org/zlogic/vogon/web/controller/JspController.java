/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.text.MessageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.zlogic.vogon.web.configuration.VogonConfiguration;

/**
 * Spring MVC controller for index.jsp configuration (add VogonConfiguration
 * reference)
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
public class JspController {

	/**
	 * The configuration handler
	 */
	@Autowired
	private VogonConfiguration configuration;

	/**
	 * Returns the index ("/") ModelAndView
	 *
	 * @return the ("/") ModelAndView
	 */
	@RequestMapping(value = {"/"})
	public ModelAndView indexModelAndView() {
		ModelAndView model = new ModelAndView("main"); //NOI18N
		model.addObject("configuration", configuration); //NOI18N
		return model;
	}

	/**
	 * Returns the fragment ("/fragments") path
	 *
	 * @param fragment the fragment name
	 * @return the fragment ("/fragments") path
	 */
	@RequestMapping(value = {"/fragments/{fragment}.fragment"})
	public ModelAndView fragmentModelAndView(@PathVariable String fragment) {
		String target = MessageFormat.format("fragments/{0}", new Object[]{fragment}); //NOI18N
		ModelAndView model = new ModelAndView(target);
		model.addObject("configuration", configuration); //NOI18N
		return model;
	}
}
