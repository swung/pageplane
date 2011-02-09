package org.pagebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class PageRuler
{
	private Selenium selenium;
	private Map<String, String> missing = new HashMap<String, String>();

	private final String test = "TEST";

	public PageRuler(Selenium selenium)
	{
		this.selenium = selenium;
	}

	public void check(String page) throws Exception
	{
		Class cls = Class.forName(page);
		Class partypes[] = new Class[1];
		partypes[0] = Selenium.class;
		Constructor ct = cls.getConstructor(partypes);
		Object arglist[] = new Object[1];
		arglist[0] = selenium;
		Object pageInstance = ct.newInstance(arglist);
		Field fieldlist[] = cls.getDeclaredFields();

		for (Field field : fieldlist) {
			int mod = field.getModifiers();
			if (Modifier.isPublic(mod) && Modifier.isFinal(mod) && (field.getType() == String.class)) {
				String locator = (String) field.get(pageInstance);
				if (!selenium.isElementPresent(locator)) {
					missing.put(field.getName(), locator);
				}
			}
		}
	}

	public void printMissing()
	{
		System.out.println("Missing " + missing.size() + " locator.");
		for (String key : missing.keySet()) {
			System.out.println("\t" + key + " => " + missing.get(key));
		}
	}

	public static void main(String[] args) throws Exception
	{
		Selenium selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://computers.shop.ebay.com");
		selenium.start();
		selenium.windowMaximize();
		selenium.open("/ebayadvsearch");
		selenium.waitForPageToLoad("5000");
		PageRuler pr = new PageRuler(selenium);
		pr.check("org.pagebox.test.Page");
		pr.printMissing();
		selenium.stop();
	}
}
