package ca.sqlpower.validation;

import java.util.regex.Pattern;

/**
 * A Validator that uses RegEx matching; there is no notion of
 * warnings, it either matches() or it doesn't.
 */
public class RegExValidator implements Validator {

    private Pattern pattern;
    private String message;

    /**
     * Construct a Validator for regexes
     * @param pattern The regex pattern
     *
     */
    public RegExValidator(String pattern,String message) {
        super();
        this.pattern = Pattern.compile(pattern);
        this.message = message;
    }

    public RegExValidator(String pattern) {
        this(pattern,"Input text must match pattern:" + pattern);
    }

    public ValidateResult validate(Object contents) {
        String value = (String)contents;
        if ( pattern.matcher(value).matches() ) {
            return ValidateResult.createValidateResult(Status.OK, "");
        } else {
            return ValidateResult.createValidateResult(Status.FAIL, message);
        }
    }

}
