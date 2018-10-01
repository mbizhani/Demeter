package org.devocative.demeter.test;

import org.apache.commons.io.IOUtils;
import org.devocative.adroit.ConfigUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class TestDemeterByOracle {

	private List<Failure> failures = new ArrayList<>();

	// ------------------------------

	protected Class<? extends TestDemeter> TEST_DEMETER_CLASS = TestDemeter.class;

	// ------------------------------

	@Test
	public void c00testDemeterByOracle() {
		boolean isOracleOk = true;

		try {
			InputStream config = TestDemeterByOracle.class.getResourceAsStream("/config_oracle.properties");
			ConfigUtil.load(config);

			Class.forName(ConfigUtil.getString(true, "dmt.db.driver"));
			Connection connection = DriverManager.getConnection(
				ConfigUtil.getString(true, "dmt.db.url"),
				ConfigUtil.getString(true, "dmt.db.username"),
				ConfigUtil.getString(true, "dmt.db.password")
			);
			String oracle = IOUtils.toString(TestDemeterByOracle.class.getResourceAsStream("/oracle_remove_all.sql"), "UTF-8");
			System.out.println("oracle = " + oracle);
			connection.createStatement().execute(oracle);
			connection.close();
		} catch (Exception e) {
			System.out.println("Creating Oracle Connection: " + e.getMessage());
			isOracleOk = false;
		}

		if (isOracleOk) {
			TestDemeter.setPROFILE("oracle");
			JUnitCore core = new JUnitCore();
			core.addListener(new RunListener() {
				@Override
				public void testFailure(Failure failure) throws Exception {
					failures.add(failure);
				}

				@Override
				public void testAssumptionFailure(Failure failure) {
					failures.add(failure);
				}
			});
			core.run(TEST_DEMETER_CLASS);

			Assert.assertEquals("Failures:\n" + listOfFailures(), 0, failures.size());
		}
	}

	private String listOfFailures() {
		StringBuilder builder = new StringBuilder();
		for (Failure failure : failures) {
			builder.append("  (Failure): ").append(failure).append("\n");
		}
		return builder.toString();
	}

}
