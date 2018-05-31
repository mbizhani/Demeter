package org.devocative.demeter.test;

import org.devocative.adroit.date.EUniCalendar;
import org.devocative.adroit.date.UniDate;
import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.filter.CollectionUtil;
import org.devocative.demeter.iservice.persistor.FilterOption;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class TestCollectionUtil {
	@Test
	public void testFilterCollection() {
		List<TheVO> list = Arrays.asList(
			new TheVO("Jack", 1, UniDate.of(EUniCalendar.Gregorian, "2017-01-01", "yyyy-MM-dd").toDate()),
			new TheVO("Joe", 2, UniDate.of(EUniCalendar.Gregorian, "2017-01-02", "yyyy-MM-dd").toDate()),
			new TheVO("Jim", 3, UniDate.of(EUniCalendar.Gregorian, "2017-01-03", "yyyy-MM-dd").toDate()),
			new TheVO("Jade", 3, UniDate.of(EUniCalendar.Gregorian, "2017-01-08", "yyyy-MM-dd").toDate())
		);

		List<TheVO> theVOs;

		theVOs = CollectionUtil.filterCollection(list, new TheFVO1("Jack", null, null));
		System.out.println("FVO1.theVOs = " + theVOs);
		doAssert(theVOs, "Jack");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO1(null, 3, null));
		System.out.println("FVO1.theVOs = " + theVOs);
		doAssert(theVOs, "Jim", "Jade");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO1(null, 3, UniDate.of(EUniCalendar.Gregorian, "2017-01-01", "yyyy-MM-dd").toDate()));
		System.out.println("FVO1.theVOs = " + theVOs);
		doAssert(theVOs);

		theVOs = CollectionUtil.filterCollection(list, new TheFVO1("Jim", 3, UniDate.of(EUniCalendar.Gregorian, "2017-01-03", "yyyy-MM-dd").toDate()));
		System.out.println("FVO1.theVOs = " + theVOs);
		doAssert(theVOs, "Jim");

		System.out.println("-----------------------------");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("j", Arrays.asList(2, 3, 5), null));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs, "Joe", "Jim", "Jade");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("e", null, null));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs, "Joe", "Jade");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("j", null, new RangeVO<>(
			UniDate.of(EUniCalendar.Gregorian, "2017-01-03", "yyyy-MM-dd").toDate(),
			UniDate.of(EUniCalendar.Gregorian, "2017-01-08", "yyyy-MM-dd").toDate())
		));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs, "Jim");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("j", null, new RangeVO<>(
			UniDate.of(EUniCalendar.Gregorian, "2017-01-03", "yyyy-MM-dd").toDate(),
			null)
		));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs, "Jim", "Jade");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("j", null, new RangeVO<>(
			null,
			UniDate.of(EUniCalendar.Gregorian, "2017-01-03", "yyyy-MM-dd").toDate())
		));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs, "Jack", "Joe");

		theVOs = CollectionUtil.filterCollection(list, new TheFVO2("j", Arrays.asList(5, 6), null));
		System.out.println("FVO2.theVOs = " + theVOs);
		doAssert(theVOs);
	}

	private void doAssert(List<TheVO> theVOs, String... names) {
		if (names != null) {
			Assert.assertEquals(names.length, theVOs.size());

			for (int i = 0; i < names.length; i++) {
				Assert.assertEquals(names[i], theVOs.get(i).getString());
			}
		} else {
			Assert.assertEquals(0, theVOs.size());
		}
	}

	// ---------------

	public static class TheVO {
		private String string;
		private Integer number;
		private Date date;

		public TheVO(String string, Integer number, Date date) {
			this.string = string;
			this.number = number;
			this.date = date;
		}

		public String getString() {
			return string;
		}

		public Integer getNumber() {
			return number;
		}

		public Date getDate() {
			return date;
		}

		@Override
		public String toString() {
			return "TheVO{" +
				"string='" + string + '\'' +
				", number=" + number +
				", date=" + date +
				'}';
		}
	}

	// ---------------

	public static class TheFVO1 {
		private String string;
		private Integer number;
		private Date date;

		public TheFVO1(String string, Integer number, Date date) {
			this.string = string;
			this.number = number;
			this.date = date;
		}

		public String getString() {
			return string;
		}

		public Integer getNumber() {
			return number;
		}

		public Date getDate() {
			return date;
		}
	}

	// ---------------

	public static class TheFVO2 {
		@FilterOption(useLike = true)
		private String string;
		private List<Integer> number;
		private RangeVO<Date> date;

		public TheFVO2(String string, List<Integer> number, RangeVO<Date> date) {
			this.string = string;
			this.number = number;
			this.date = date;
		}

		public String getString() {
			return string;
		}

		public List<Integer> getNumber() {
			return number;
		}

		public RangeVO<Date> getDate() {
			return date;
		}
	}
}
