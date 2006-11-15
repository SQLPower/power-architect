package ca.sqlpower.validation;

import java.util.regex.Pattern;

/**
 * A Validator that uses RegEx matching
 */
public class RegExValidator implements Validator {

    private Pattern pattern;
    private String message;

    /**
     * Construct a Validator for regexes
     * @param pattern The regex pattern
     *
     */
    public RegExValidator(String pattern) {
        super();
        this.pattern = Pattern.compile(pattern);
        this.message = "Must match " + pattern;
    }

    public RegExValidator(String pattern, String message) {
        this(pattern);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean validate(Object contents) {
        String value = (String)contents;
        return pattern.matcher(value).matches();
    }

}
