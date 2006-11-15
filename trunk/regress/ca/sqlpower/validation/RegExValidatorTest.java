package ca.sqlpower.validation;

import junit.framework.TestCase;

/**
 * JUnit tests for RegexValidatator
 */
public class RegExValidatorTest extends TestCase {

    /**
     * If this pattern works, any regex pattern should work
     * assuming that java.util.Regex.* have also been tested.
     */
    public void testValidateDigits() {
        RegExValidator val = new RegExValidator("\\d+");
        // These work
        assertEquals(Status.OK, val.validate("0"));
        assertEquals(Status.OK, val.validate("42"));

        // These should not
        assertFalse(Status.OK == val.validate(""));
        assertFalse(Status.OK == val.validate("abcde"));
        assertFalse(Status.OK == val.validate("abc123")); // uses match, not find
    }

}
