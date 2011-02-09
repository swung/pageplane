package org.pagebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class PagePlane
{

	private String html = null;
	private static Map<String, String> selectorMap;
	static {
		selectorMap = new HashMap<String, String>();
		selectorMap.put("select", "select");
		selectorMap.put("link", "a[href]");
		selectorMap.put("textarea", "textarea");
		selectorMap.put("form", "form");
		selectorMap.put("checkbox", "input[type=checkbox]");
		selectorMap.put("image", "input[type=image]");
		selectorMap.put("radio", "input[type=radio]");
		selectorMap.put("text", "input[type=text]");

	}

	/**
	 * User need init selenium by selenium.open(url)
	 * 
	 * @param selenium
	 */
	public PagePlane(Selenium selenium)
	{
		html = selenium.getHtmlSource();
	}

	public void excavate()
	{
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(generateInstanceVariablesFromHtml(html, "select"));
		list.add(generateInstanceVariablesFromHtml(html, "link"));
		list.add(generateInstanceVariablesFromHtml(html, "textarea"));
		list.add(generateInstanceVariablesFromHtml(html, "form"));
		list.add(generateInstanceVariablesFromHtml(html, "checkbox"));
		list.add(generateInstanceVariablesFromHtml(html, "image"));
		list.add(generateInstanceVariablesFromHtml(html, "radio"));
		list.add(generateInstanceVariablesFromHtml(html, "text"));

		mergeElements(list);

	}

	private Map<String, String> generateInstanceVariablesFromHtml(String html, String selectorKey)
	{
		String selector = selectorMap.get(selectorKey);
		Map<String, String> result = new HashMap<String, String>();
		Document doc = Jsoup.parse(html);
		Elements elements = doc.select(selector);
		for (Element element : elements) {
			String attr = getXPath(element);
			if ((attr != null) && (attr.length() > 0)) {
				String attrName = createAttrName(attr);
				if (attrName != "") {
					if (attrName.startsWith("_")) {
						attrName = selectorKey.toUpperCase() + attrName;
					} else {
						attrName = selectorKey.toUpperCase() + "_" + attrName;
					}
					result.put(attrName, attr);
				}
			}
		}
		return result;
	}

	private void mergeElements(List<Map<String, String>> list)
	{
		System.out.println("public class Page");
		System.out.println("{");
		System.out.println("public Page(Selenium s){}\n");
		for (Map<String, String> elements : list) {
			if (elements.size() == 0)
				continue;
			for (String name : elements.keySet()) {
				System.out.println("\tpublic final String " + name + " = \"" + elements.get(name) + "\";");
			}
			System.out.println();
		}
		System.out.println("}");
	}

	private static String createAttrName(String attr)
	{
		String res;
		res = attr.replaceAll("input-", "");
		res = res.replaceAll("select-", "");
		res = res.replaceAll("([A-Z]+)", "_$1");
		res = res.replaceAll("\\\\", "");
		res = res.replaceAll(" ", "_");
		res = res.replaceAll("\\.", "_");
		res = res.replaceAll("//", "");
		res = res.replaceAll("/", "_");
		res = res.replaceAll("'", "");
		res = res.replaceAll("@", "");
		res = res.replaceAll("&", "");
		res = res.replaceAll("=", "_");
		res = res.replaceAll("\\[", "_");
		res = res.replaceAll("\\]", "");
		res = res.replaceAll("-", "_");
		res = res.replaceAll(",", "_");
		res = res.replaceAll("__", "_");
		res = res.replaceAll("__", "_");
		return res.toLowerCase();
	}

	private String getXPath(Element element)
	{
		// if (element.hasAttr("id")) {
		// return element.attr("id");
		// } else if (element.hasAttr("name")) {
		// return element.attr("name");
		// } else if (element.hasAttr("title")) {
		// return element.attr("title");
		// }
		return getXPath(element, null);
	}

	private String getXPath(Element element, String curPath)
	{
		String result;

		if (element.hasAttr("id")) {
			if (curPath == null) {
				result = element.attr("id");
			} else {
				result = "//" + element.tagName() + "[@id='" + element.attr("id") + "']/" + curPath;
			}
		} else if (element.hasAttr("name")) {
			if (curPath == null) {
				result = element.attr("name");
			} else {
				result = "//" + element.tagName() + "[@name='" + element.attr("name") + "']/" + curPath;
			}
		} else if (element.hasAttr("title")) {
			if (curPath == null) {
				result = element.attr("title");
			} else {
				result = "//" + element.tagName() + "[@title='" + element.attr("title") + "']/" + curPath;
			}
		} else {
			Element parent = element.parent();
			if (parent.tagName() == "html") {
				return "//html/" + curPath;
			} else {
				int i = 0;
				while (!(parent.child(i) == element)) {
					i++;
				}
				String newPath;
				if (curPath == null) {
					newPath = "";
				} else {
					newPath = "/" + curPath;
				}
				// if (i == 0) {
				// newPath = element.tagName() + newPath;
				// } else {
				newPath = element.tagName() + "[" + (i + 1) + "]" + newPath;
				// }
				return getXPath(parent, newPath);
			}
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Selenium selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://computers.shop.ebay.com");
		selenium.start();
		selenium.windowMaximize();
		selenium.open("/ebayadvsearch");
		PagePlane pp = new PagePlane(selenium);
		pp.excavate();
		selenium.stop();
	}

}

