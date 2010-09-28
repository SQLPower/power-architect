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
        assertEquals(Status.OK, val.validate("0").getStatus());
        assertEquals(Status.OK, val.validate("42").getStatus());

        // These should not
        assertFalse(Status.OK == val.validate("").getStatus());
        assertFalse(Status.OK == val.validate("123 112").getStatus());
        assertFalse(Status.OK == val.validate("123 ").getStatus());
        assertFalse(Status.OK == val.validate(" 123").getStatus());
        assertFalse(Status.OK == val.validate("abcde").getStatus());
        assertFalse(Status.OK == val.validate("abc123").getStatus()); // uses match, not find
    }

    public void testCaseSensitive() {
        RegExValidator val = new RegExValidator("[a-z]*", "must be all lowercase", true);
        assertEquals(Status.OK, val.validate("abc").getStatus());
        assertFalse(Status.OK == val.validate("ABC").getStatus());
    }

    public void testCaseInsensitive() {
        RegExValidator val = new RegExValidator("[a-z]*", "must be all lowercase", false);
        assertEquals(Status.OK, val.validate("abc").getStatus());
        assertEquals(Status.OK, val.validate("ABC").getStatus());
    }
}
