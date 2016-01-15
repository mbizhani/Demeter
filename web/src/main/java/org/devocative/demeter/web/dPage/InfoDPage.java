package org.devocative.demeter.web.dPage;

import org.devocative.demeter.web.DPage;

import java.util.List;

public class InfoDPage extends DPage {
	public InfoDPage(String id, List<String> params) {
		super(id, params);

		System.out.println(params);
	}
}
