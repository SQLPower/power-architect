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
        assertTrue(val.validate("0"));
        assertTrue(val.validate("42"));

        // These should not
        assertFalse(val.validate(""));
        assertFalse(val.validate("abcde"));
        assertFalse(val.validate("abc123")); // uses match, not find
    }

}
