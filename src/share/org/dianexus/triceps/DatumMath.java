/* ******************************************************** 
** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
** $Header$
******************************************************** */ 

package org.dianexus.triceps;

/*import java.lang.*;*/
/*import java.util.*;*/
/*import java.text.Format;*/
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

/*public*/ final class DatumMath implements VersionIF {
	static Datum hasError(Datum a, Datum b) {
		// This function needs to be reconsidered as to the proper way to handle error propagation
		if (a.isType(Datum.INVALID) || (b != null && b.isType(Datum.INVALID))) {
			return Datum.getInstance(a.triceps,Datum.INVALID);
		}
		/*
		if (a.isType(Datum.REFUSED) || (b != null && b.isType(Datum.REFUSED))) {
			return Datum.REFUSED_DATUM;
		}
		// Do NOT throw an error message if try to access a NA datatype?
		if (a.isType(Datum.NA) || (b != null && b.isType(Datum.NA))) {
			return Datum.NA_DATUM;
		}
		*/
		return null;	// to indicate that there is no error that needs propagating
	}

	static int datumToCalendar(int datumType) {
		switch (datumType) {
			case Datum.YEAR: return Calendar.YEAR;
			case Datum.MONTH: return Calendar.MONTH;
			case Datum.DAY: return Calendar.DAY_OF_MONTH;
			case Datum.WEEKDAY: return Calendar.DAY_OF_WEEK;
			case Datum.HOUR: return Calendar.HOUR_OF_DAY;
			case Datum.MINUTE: return Calendar.MINUTE;
			case Datum.SECOND: return Calendar.SECOND;
			case Datum.MONTH_NUM: return Calendar.MONTH;
			case Datum.DAY_NUM: return Calendar.DAY_OF_YEAR;
			default: return 0;	// should never get here
		}
	}

	static Date createDate(int val, int datumType) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(System.currentTimeMillis()));
		calendar.set(datumToCalendar(datumType),val);
		return calendar.getTime();
	}

	static int getCalendarField(Datum d, int datumType) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(d.dateVal());
		return calendar.get(DatumMath.datumToCalendar(datumType));
	}

	/** This method returns the sum of two Datum objects of type double. */
	static /* synchronized */ Datum add(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		switch (a.type()) {
			default:
			case Datum.NUMBER:
			case Datum.STRING:
				return new Datum(a.triceps, a.doubleVal() + b.doubleVal());
 			case Datum.DATE:
 			case Datum.TIME:
 				/* need way to throw error here */
				return Datum.getInstance(a.triceps,Datum.INVALID);
			case Datum.WEEKDAY:
			case Datum.MONTH:
			case Datum.YEAR:
			case Datum.DAY:
			case Datum.HOUR:
			case Datum.MINUTE:
			case Datum.SECOND:
			case Datum.MONTH_NUM:
			case Datum.DAY_NUM:
				if (!b.isNumeric()) {
					/* need way to throw an error here */
					return Datum.getInstance(a.triceps,Datum.INVALID);
				}
				else {
					int field = DatumMath.datumToCalendar(a.type());
					GregorianCalendar gc = new GregorianCalendar();	// should happen infrequently (not a garbage collection problem?)

					gc.setTime(a.dateVal());
					gc.add(field, (int) b.doubleVal());
					return new Datum(a.triceps,gc.getTime(),a.type());	// set to type of first number in expression
				}
		}
	}

	static /* synchronized */ Datum and(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, (double) ((long) a.doubleVal() & (long) b.doubleVal()));
	}
	static /* synchronized */ Datum andand(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, a.booleanVal() && b.booleanVal());
	}
	/** This method concatenates two Datum objects of type String and returns the resulting Datum object. */
	static /* synchronized */ Datum concat(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			return new Datum(a.triceps, a.stringVal().concat(b.stringVal()),Datum.STRING);
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ concat");
			return new Datum(a.triceps, a.stringVal(),Datum.STRING);
		}
	}
	/**
	 * This method evaluates the boolean value of the first Datum object, returns the second Datum object if true,
	 * returns the third Datum object if false.
	 */
	static /* synchronized */ Datum conditional(Datum a, Datum b, Datum c) {
		Datum d = DatumMath.hasError(a,null);	// if conditional based upon a REFUSED or INVALID, always return that type
		if (d != null)
			return d;

		if (a.booleanVal())
			return b;
		else
			return c;
	}
	/** This method returns the division of two Datum objects of type double. */
	static /* synchronized */ Datum divide(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			return new Datum(a.triceps, a.doubleVal() / b.doubleVal());
		}
		catch(ArithmeticException e) {
if (DEBUG) Logger.writeln("##ArithmeticException @ divide");
			return Datum.getInstance(a.triceps,Datum.INVALID);
		}
	}
	/**
	 * This method returns a Datum of type boolean, value true, if two Datum objects of type String or double are equal
	 * or value false if not equal.
	 */
	static /* synchronized */ Datum eq(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps, (a.dateVal().equals(b.dateVal())));
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) == DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()== b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##eq(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() == b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) == 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() == b.doubleVal());
				default:
					return new Datum(a.triceps, false);
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ eq");
		}
		return new Datum(a.triceps, false);
	}
	/**
	 * This method returns a Datum of type boolean upon comparing two Datum objects of type String or double for
	 * greater than or equal.
	 */
	static /* synchronized */ Datum ge(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps,
						(a.dateVal().after(b.dateVal())) ||
						(a.dateVal().equals(b.dateVal()))
						);
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) >= DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()>= b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##ge(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() >= b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) >= 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() >= b.doubleVal());
				default:
					return new Datum(a.triceps, false);
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ ge");
		}
		return new Datum(a.triceps, false);
	}
	/**
	 * This method returns a Datum of type boolean upon comparing two Datum objects of type String or double for greater than.
	 */
	static /* synchronized */ Datum gt(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps,
						(a.dateVal().after(b.dateVal()))
						);
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) > DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()> b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##gt(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() > b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) > 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() > b.doubleVal());
				default:
					return new Datum(a.triceps, false);
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ gt");
		}
		return new Datum(a.triceps, false);
	}
	/**
	 * This method returns a Datum of type boolean upon comparing two Datum objects of type String or double for
	 * less than or equal.
	 */
	static /* synchronized */ Datum le(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps,
						(a.dateVal().before(b.dateVal())) ||
						(a.dateVal().equals(b.dateVal()))
						);
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) <= DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()<= b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##le(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() <= b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) <= 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() <= b.doubleVal());
				default:
					return new Datum(a.triceps, false);
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ le");
		}
		return new Datum(a.triceps, false);
	}
	/** This method returns a Datum of type boolean upon comparing two Datum objects of type String or double for less than. */
	static /* synchronized */ Datum lt(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps,
						(a.dateVal().before(b.dateVal()))
						);
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) < DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()< b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##lt(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() < b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) < 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() < b.doubleVal());
				default:
					return new Datum(a.triceps, false);
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ lt");
		}
		return new Datum(a.triceps, false);
	}
	/** This method returns a Datum object of type double that is the modulus of two Datum objects of type double. */
	static /* synchronized */ Datum modulus(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		try {
			return new Datum(a.triceps, a.doubleVal() % b.doubleVal());
		}
		catch(ArithmeticException e) {
if (DEBUG) Logger.writeln("##ArithmeticException @ modulus");
			return Datum.getInstance(a.triceps,Datum.INVALID);
		}
	}
	/** This method returns the product of two Datum objects of type double. */
	static /* synchronized */ Datum multiply(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, a.doubleVal() * b.doubleVal());
	}
	/** This method returns a Datum object of type double that is the negative of the passed Datum object. */
	static /* synchronized */ Datum neg(Datum a) {
		Datum d = DatumMath.hasError(a,null);
		if (d != null)
			return d;

		return new Datum(a.triceps, -a.doubleVal());
	}
	/**
	 * This method returns a Datum of type boolean, value true, if two Datum objects of type String or double are not equal
	 * or value false if equal.
	 */
	static /* synchronized */ Datum neq(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		if (a.type() == Datum.NA || b.type() == Datum.NA) {
			return new Datum(a.triceps, true);	// neq to anything
		}

		try {
			switch (a.type()) {
				case Datum.DATE:
				case Datum.TIME:
					return new Datum(a.triceps, !
						(a.dateVal().equals(b.dateVal()))
						);
				case Datum.WEEKDAY:
				case Datum.MONTH:
				case Datum.YEAR:
				case Datum.DAY:
				case Datum.HOUR:
				case Datum.MINUTE:
				case Datum.SECOND:
				case Datum.MONTH_NUM:
				case Datum.DAY_NUM:
				{
					boolean ans = false;
					if (b.isDate()) {
						ans = (DatumMath.getCalendarField(a,a.type()) != DatumMath.getCalendarField(b,a.type()));
					}
					else if (b.isNumeric()) {
						ans = (a.doubleVal()!= b.doubleVal());
					}
//if (DEBUG) Logger.writeln("##neq(" + a.doubleVal() + "," + a.dateVal() + ";" + b.doubleVal() + "," + b.dateVal() + ")-> " + ans);
					return new Datum(a.triceps, ans);
				}
				case Datum.STRING:
					if (a.isNumeric())
						return new Datum(a.triceps, a.doubleVal() != b.doubleVal());
					else {
						return new Datum(a.triceps, a.stringVal().compareTo(b.stringVal()) != 0);
					}
				case Datum.NUMBER:
					return new Datum(a.triceps, a.doubleVal() != b.doubleVal());
				default:
					return new Datum(a.triceps, false);	// value is indeterminate - neither eq nor neq
			}
		}
		catch(NullPointerException e) {
if (DEBUG) Logger.writeln("##NullPointerException @ neq");
		}
		return new Datum(a.triceps, false);
	}
	/** This method returns a Datum object of the opposite value of the Datum object passed. */
	static /* synchronized */ Datum not(Datum a) {
		Datum d = DatumMath.hasError(a,null);
		if (d != null)
			return d;

		return new Datum(a.triceps, !a.booleanVal());
	}
	/** This method returns a Datum of type boolean, value true, if either of two Datum objects of type long are true. */
	static /* synchronized */ Datum or(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, (double) ((long) a.doubleVal() | (long) b.doubleVal()));
	}
	static /* synchronized */ Datum oror(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, a.booleanVal() || b.booleanVal());
	}
	/** This method returns the difference between two Datum objects of type double. */
	static /* synchronized */ Datum subtract(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		switch (a.type()) {
			default:
				return new Datum(a.triceps, a.doubleVal() - b.doubleVal());
 			case Datum.DATE:
 			case Datum.TIME:
 				/* need way to throw error here */
				return Datum.getInstance(a.triceps,Datum.INVALID);
			case Datum.WEEKDAY:
			case Datum.MONTH:
			case Datum.YEAR:
			case Datum.DAY:
			case Datum.HOUR:
			case Datum.MINUTE:
			case Datum.SECOND:
			case Datum.MONTH_NUM:
			case Datum.DAY_NUM:
				if (!b.isNumeric()) {
					/* need way to throw an error here */
					return Datum.getInstance(a.triceps,Datum.INVALID);
				}
				else {
					int field = DatumMath.datumToCalendar(a.type());
					GregorianCalendar gc = new GregorianCalendar();	// should happen infrequently (not a garbage collection problem?)

					gc.setTime(a.dateVal());
					gc.add(field, -((int) b.doubleVal()));
					return new Datum(a.triceps,gc.getTime(),a.type());	// set to type of first number in expression
				}
		}
	}
	static /* synchronized */ Datum xor(Datum a, Datum b) {
		Datum d = DatumMath.hasError(a,b);
		if (d != null)
			return d;

		return new Datum(a.triceps, (double) ((long) a.doubleVal() ^ (long) b.doubleVal()));
	}
}
