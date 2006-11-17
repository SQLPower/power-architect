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
        assertEquals(ValidateResult.Status.OK, val.validate("0").getStatus());
        assertEquals(ValidateResult.Status.OK, val.validate("42").getStatus());

        // These should not
        assertFalse(ValidateResult.Status.OK == val.validate("").getStatus());
        assertFalse(ValidateResult.Status.OK == val.validate("123 112").getStatus());
        assertFalse(ValidateResult.Status.OK == val.validate("123 ").getStatus());
        assertFalse(ValidateResult.Status.OK == val.validate(" 123").getStatus());
        assertFalse(ValidateResult.Status.OK == val.validate("abcde").getStatus());
        assertFalse(ValidateResult.Status.OK == val.validate("abc123").getStatus()); // uses match, not find
    }

}
