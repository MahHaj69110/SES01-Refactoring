package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public Map<String, Boolean> enroll(Student student, List<CSE> courses) {
        Map<String, Boolean> status= new HashMap<String, Boolean>() {{
            put("alreadyPassing", true);
            put("prerequestNotPassing", true);
            put("sameExamTime", true);
            put("requestingTwice", true);
            put("gpa", true);
        }};
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        try {
            alreadyPassingCtrl(student, courses);
        }
        catch (EnrollmentRulesViolationException exception){
            System.out.println(exception.getMessage());
            status.put("alreadyPassing", false);
        }
        try {
            prerequestNotPassingCntrl(student, courses);
        }
        catch (EnrollmentRulesViolationException exception){
            System.out.println(exception.getMessage());
            status.put("prerequestNotPassing", false);
        }
        try {
            sameExamTimeCntrl(student, courses);
        }
        catch (EnrollmentRulesViolationException exception){
            System.out.println(exception.getMessage());
            status.put("sameExamTime", false);
        }
        try {
            requestingTwiceCntrl(student, courses);
        }
        catch (EnrollmentRulesViolationException exception){
            System.out.println(exception.getMessage());
            status.put("requestingTwice", false);
        }
        try {
            gpaCntrl(student, courses);
        }
        catch (EnrollmentRulesViolationException exception){
            System.out.println(exception.getMessage());
            status.put("gpa", false);
        }
        return status;
	}
    private void alreadyPassingCtrl(Student student, List<CSE> courses) throws EnrollmentRulesViolationException{
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        for (CSE courseSectionExamTime : courses) {
            for (Map.Entry<Term, Map<Course, Double>> transcriptEntry : transcript.entrySet()) {
                for (Map.Entry<Course, Double> courseWithGrade : transcriptEntry.getValue().entrySet()) {
                    if (courseWithGrade.getKey().equals(courseSectionExamTime.getCourse()) && courseWithGrade.getValue() >= 10)
                        throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", courseSectionExamTime.getCourse().getName()));
                }
            }
        }
    }
    private void prerequestNotPassingCntrl(Student student, List<CSE> courses) throws EnrollmentRulesViolationException{
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        for (CSE courseSectionExamTime : courses) {
            List<Course> prereqs = courseSectionExamTime.getCourse().getPrerequisites();
            nextPre:
            for (Course prereq : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> transcriptEntry : transcript.entrySet()) {
                    for (Map.Entry<Course, Double> courseWithGrade : transcriptEntry.getValue().entrySet()) {
                        if (courseWithGrade.getKey().equals(prereq) && courseWithGrade.getValue() >= 10)
                            continue nextPre;
                    }
                }
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", prereq.getName(), courseSectionExamTime.getCourse().getName()));
            }
        }
    }
    private void sameExamTimeCntrl(Student student, List<CSE> courses) throws EnrollmentRulesViolationException{
        for (CSE courseSectionExamTime : courses) {
            for (CSE courseSectionExamTime2 : courses) {
                if (courseSectionExamTime == courseSectionExamTime2)
                    continue;
                if (courseSectionExamTime.getExamTime().equals(courseSectionExamTime2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", courseSectionExamTime, courseSectionExamTime2));
            }
        }
    }
    private void requestingTwiceCntrl(Student student, List<CSE> courses) throws EnrollmentRulesViolationException{
        for (CSE courseSectionExamTime : courses) {
            for (CSE courseSectionExamTime2 : courses) {
                if (courseSectionExamTime == courseSectionExamTime2)
                    continue;
                if (courseSectionExamTime.getCourse().equals(courseSectionExamTime2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", courseSectionExamTime.getCourse().getName()));
            }
        }
    }
    private void gpaCntrl(Student student, List<CSE> courses) throws EnrollmentRulesViolationException{
        Map<Term, Map<Course, Double>> transcript = student.getTranscript();
        int unitsRequested = 0;
        for (CSE courseSectionExamTime : courses)
            unitsRequested += courseSectionExamTime.getCourse().getUnits();
        double points = 0;
        int totalUnits = 0;
        for (Map.Entry<Term, Map<Course, Double>> transcriptEntry : transcript.entrySet()) {
            for (Map.Entry<Course, Double> courseWithGrade : transcriptEntry.getValue().entrySet()) {
                points += courseWithGrade.getValue() * courseWithGrade.getKey().getUnits();
                totalUnits += courseWithGrade.getKey().getUnits();
            }
        }
        double gpa = points / totalUnits;
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
        for (CSE courseSectionExamTime : courses)
            student.takeCourse(courseSectionExamTime.getCourse(), courseSectionExamTime.getSection());
    }
}
