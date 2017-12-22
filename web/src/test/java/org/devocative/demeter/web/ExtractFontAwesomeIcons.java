package org.devocative.demeter.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExtractFontAwesomeIcons {
	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://fontawesome.io/icons/").get();

		Map<String, String> icons = new TreeMap<>();
		Set<String> labels = new TreeSet<>();

		Elements elements = doc.select("div.fa-hover");
		for (Element element : elements) {
			if (element.select("span.text-muted").size() == 0) {
				Element i = element.select("i").first();

				String iconName = i.className().substring(6);
				String iconId = iconName.replace('-', '_').toUpperCase();
				String iconTitle = iconName.replaceAll("-", " ").replaceAll("(\\so$)", "").replaceAll("(\\so\\s)", " ");
				String[] titleParts = iconTitle.split("[ ]");
				String title = "";
				for (String part : titleParts) {
					title += part.substring(0, 1).toUpperCase() + part.substring(1) + " ";
				}
				iconTitle = title.trim();

				String format = String.format("public static final IconFont %1$s = new FontAwesome(\"%2$s\", new ResourceModel(\"label.fa.%2$s\", \"%3$s\"));", iconId, iconName, iconTitle);
				icons.put(iconId, format);

				labels.add(String.format("label.fa.%s=%s", iconName, iconTitle));
			}
		}

		System.out.println("icons.size() = " + icons.size());
		for (String s : icons.values()) {
			System.out.println(s);
		}
		System.out.println("\n\n\n");
		for (String label : labels) {
			System.out.println(label);
		}
	}
}
