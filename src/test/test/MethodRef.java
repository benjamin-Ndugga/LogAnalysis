package test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 * @author Benjamin
 */
public class MethodRef {

    public static void main(String[] args) throws ParseException {

        
        System.out.println(new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss").parse("02/Dec/2018:03:32:59").toString());
        
        
//        
//        List<HTTPRequest> people = Arrays.asList(
//                new HTTPRequest("Charles", "Dickens", 30),
//                new HTTPRequest("lewis", "Caroll", 50),
//                new HTTPRequest("Ben", "Ash", 60),
//                new HTTPRequest("Chris", "Reed", 20),
//                new HTTPRequest("Hamlet", "Xavier", 50),
//                new HTTPRequest("Roast", "Cream", 80),
//                new HTTPRequest("Andrew", "Quest", 50),
//                new HTTPRequest("Tim", "East", 40),
//                new HTTPRequest("Reachman", "Ellen", 70));
//
//        //people.forEach(p-> System.out.println(p.getFname()));
//        people.parallelStream()
//                .filter(p -> p.getSourceIP().startsWith("C") && p.getAge() > 10)
//                .sorted((p1, p2) -> p1.getRequestTime().compareTo(p2.getRequestTime()))
//                .forEach(System.out::println);
//
//        long count = people.parallelStream()
//                .filter(p -> p.getSourceIP().startsWith("C") && p.getAge() > 10)
//                .sorted((p1, p2) -> p1.getRequestTime().compareTo(p2.getRequestTime()))
//                .count();
//        
//        System.out.println(count);

    }

    public static void printMessage() {
        System.out.println("Hello!");
    }
}//end of class
