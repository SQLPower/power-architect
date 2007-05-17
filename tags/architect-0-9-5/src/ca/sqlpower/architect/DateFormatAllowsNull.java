package ca.sqlpower.architect;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A DateFormat object that safely passes nulls through format().  You still can't parse() null though.
 */
public class DateFormatAllowsNull extends SimpleDateFormat {


	public DateFormatAllowsNull(String format) {
		super(format);
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
