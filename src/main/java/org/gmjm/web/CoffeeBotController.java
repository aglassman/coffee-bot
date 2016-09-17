package org.gmjm.web;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoffeeBotController
{
	@RequestMapping("/hello")
	public String sayHello(@RequestParam String name) {
		Assert.notNull(name);
		return "Hello: " + name.trim()
			.substring(0,
				name.length() >= 20 ? 20 : name.length());
	}

}
