package org.devocative.demeter.iservice.persistor;

public enum EJoinMode {
	Inner("join"),
	Left("left outer join"),
	Right("right outer join"),
	Full("full outer join"),
	LeftFetch("left join fetch");

	private String expr;

	EJoinMode(String expr) {
		this.expr = expr;
	}

	public String getExpr() {
		return expr;
	}
}
