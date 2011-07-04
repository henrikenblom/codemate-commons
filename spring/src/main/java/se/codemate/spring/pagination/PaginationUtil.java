package se.codemate.spring.pagination;

import java.util.Map;

/**
 * User: bogghed
 * Date: Apr 24, 2008
 * Time: 1:30:33 PM
 *
 * @author Erik Bogghed <erik.bogghed@forefront.se>
 */
public final class PaginationUtil {

    public static int getIntValue(String name, Map parameters, int defaultValue) {
        try {
            return Integer.parseInt(((String[])parameters.get(name))[0]);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getStringValue(String name, Map parameters, String defaultValue) {
        try {
            return ((String[]) parameters.get(name))[0];
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
