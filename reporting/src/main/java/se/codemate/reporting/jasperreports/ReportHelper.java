package se.codemate.reporting.jasperreports;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ReportHelper {

    public static Integer convertDateToAge(Date birthDate) {

        Calendar birth = new GregorianCalendar();
        Calendar today = new GregorianCalendar();

        int age = 0;
        int factor = 0; //to correctly calculate age when birthday has not been yet celebrated

        birth.setTime(birthDate);

        // check if birthday has been celebrated this year
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            factor = -1; //birthday not celebrated
        }

        age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR) + factor;

        return age;

    }

}
