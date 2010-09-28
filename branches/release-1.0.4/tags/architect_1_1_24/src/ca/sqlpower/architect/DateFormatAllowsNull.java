package ca.sqlpower.architect;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * to handle to null date object
 *
 */
public class DateFormatAllowsNull extends SimpleDateFormat {


	public DateFormatAllowsNull(String string) {
		super(string);
	}

    public DateFormatAllowsNull() {
        super("yyyy-MM-dd hh:mm:ss");
    }

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		if ( date == null )
			return toAppendTo;
		return super.format(date, toAppendTo, pos);
	}
}