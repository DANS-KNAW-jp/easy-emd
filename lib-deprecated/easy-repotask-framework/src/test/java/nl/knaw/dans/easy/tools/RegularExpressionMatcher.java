package nl.knaw.dans.easy.tools;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class RegularExpressionMatcher extends TypeSafeMatcher<String> {

    private final Pattern pattern;

    public RegularExpressionMatcher(String pattern) {
        this(Pattern.compile(pattern));
    }

    public RegularExpressionMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches regular expression ").appendValue(pattern);
    }

    @Override
    public boolean matchesSafely(String item) {
        return pattern.matcher(item).matches();
    }

    @Factory
    public static Matcher<String> matchesPattern(Pattern pattern) {
        return new RegularExpressionMatcher(pattern);
    }

    @Factory
    public static Matcher<String> matchesPattern(String pattern) {
        return new RegularExpressionMatcher(pattern);
    }
}
