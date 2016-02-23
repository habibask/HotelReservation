import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * @author ummehabibashaik
 */
class Hotel {

    private String hotelName;       //Hotel name
    private int totalRooms;         //Actual number of rooms the hotel has
    private HashMap<Date, Integer> reservations; //Reserved room count per day

    Hotel(String hotelName, int totalRooms) {
        this.hotelName = hotelName;
        this.totalRooms = totalRooms;
        reservations = new HashMap<Date, Integer>();
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String HotelName) {
        this.hotelName = HotelName;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public HashMap<Date, Integer> getReservations() {
        return reservations;
    }

    public void setReservations(HashMap<Date, Integer> reservations) {
        this.reservations = reservations;
    }
}

public class HotelAvailability {

    //A HashMap to store all the commandline key value pairs. This will accommodate any arguments that may be added in future.
    public static HashMap<String, String> commandLineArgs = new HashMap<String, String>();
    public static DateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    //Parses File related arguments [--hotels, --bookings]
    public static void parseFileArgs(String argName, String fileName) {

        File f = new File(fileName);

        //check if the file exists and exit if the file doesn't exist
        if (f.exists()) {
            commandLineArgs.put(argName.substring(2), fileName);
        } else {
            System.out.println("File provided for " + argName + " does not exist");
            printUsage();
            System.exit(1);
        }
    }

    //Parses Date related arguments [--checkin, --checkout]
    public static void parseDateArgs(String argName, String date) {
        try {
            //If the date format is wrong or the date is invalid, Exit
            sd.parse(date);
            commandLineArgs.put(argName.substring(2), date);
        } catch (Exception e) {
            System.out.println("Date format is not correct for the option " + argName);
            printUsage();
            System.exit(1);
        }
    }

    //Prints correct usage
    public static void printUsage() {
        System.out.println("Usage: java HotelAvailability ");
        System.out.println("      --hotels <path> --bookings <path>");
        System.out.println("      --checkin YYYY-MM-DD --checkout YYYY-MM-DD");
    }

    //Loads Hotels data from metropolis_hotels.csv to a HashMap
    public static HashMap<String, Hotel> readHotels() {
        HashMap<String, Hotel> hotelsMap = new HashMap<String, Hotel>();

        try {
            FileReader hotels = new FileReader(new File(commandLineArgs.get("hotels")));

            BufferedReader hotelsBuf = new BufferedReader(hotels);

            String curLine;
            String[] hotelsLine;

            //Load Hotels list into a HashMap
            while ((curLine = hotelsBuf.readLine()) != null) {
                if (curLine.startsWith("#")) {
                    continue;
                }
                hotelsLine = curLine.split(",");
                if (!hotelsMap.containsKey(hotelsLine[0].trim())) {
                    hotelsMap.put(hotelsLine[0].trim(), new Hotel(hotelsLine[0].trim(), Integer.parseInt(hotelsLine[1].trim())));
                }
            }
        } catch (IOException p) {
            p.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hotelsMap;

    }

    //Updates Bookings information into the provided HashMap of Hotels
    public static void readBookings(HashMap<String, Hotel> hotelsMap) {
        try {
            //Load Bookings list into a HashMap
            String[] bookingsLine;
            String curLine;

            FileReader bookings = new FileReader(new File(commandLineArgs.get("bookings")));
            BufferedReader bookingsBuf = new BufferedReader(bookings);

            Calendar rowCheckin = Calendar.getInstance();
            Calendar rowCheckout = Calendar.getInstance();

            while ((curLine = bookingsBuf.readLine()) != null) {
                if (curLine.startsWith("#")) {
                    continue;
                }
                //splits a string with seperator ","
                bookingsLine = curLine.split(",");
                rowCheckin.setTime(sd.parse(bookingsLine[1]));
                rowCheckout.setTime(sd.parse(bookingsLine[2]));

                if (hotelsMap.containsKey(bookingsLine[0])) {
                    Hotel hotel = hotelsMap.get(bookingsLine[0]);
                    
                    //Update reservation count per each day of the booking
                    HashMap<Date, Integer> roomsPerDay = hotel.getReservations();
                    for (Calendar currentDate = (Calendar) rowCheckin.clone(); currentDate.before(rowCheckout); currentDate.add(Calendar.DATE, 1)) {
                        if (!roomsPerDay.containsKey(currentDate.getTime())) {
                            roomsPerDay.put(currentDate.getTime(), 1);
                        } else {
                            roomsPerDay.put(currentDate.getTime(), roomsPerDay.get(currentDate.getTime()) + 1);
                        }
                    }
                    hotel.setReservations(roomsPerDay);
                    hotelsMap.put(bookingsLine[0], hotel);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//Checks availability of rooms for each day for each hotel between given checkin and checkout dates
    public static void checkAvailability(HashMap<String, Hotel> hotelsMap) {
        try {

            Calendar checkin = Calendar.getInstance();
            checkin.setTime(sd.parse(commandLineArgs.get("checkin")));
            Calendar checkout = Calendar.getInstance();
            checkout.setTime(sd.parse(commandLineArgs.get("checkout")));

            //Iterating over each hotel
            for (String hotel : hotelsMap.keySet()) {
                int totalRooms = hotelsMap.get(hotel).getTotalRooms();
                HashMap<Date, Integer> roomsPerDay = hotelsMap.get(hotel).getReservations();
                boolean available = true;
                //Iterating for each day in the required period to see if there are reservations
                for (Calendar currentDate = (Calendar) checkin.clone(); currentDate.before(checkout); currentDate.add(Calendar.DATE, 1)) {
                    if (roomsPerDay.containsKey(currentDate.getTime()) && roomsPerDay.get(currentDate.getTime()) >= totalRooms) {
                        available = false;
                        break;
                    }
                }
                if (available) {
                    System.out.println(hotel);
                }
            }

        } catch (Exception e) {
            System.out.println("Exception in checkAvaialbility:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //parses all given arguments
    public static void parseArgs(String[] args) {

        //Check for 8 arguments
        if (args.length != 8) {
            printUsage();
            System.exit(1);
        }

        //parse arguments
        for (int i = 0; i < args.length; i = i + 2) {
            if (commandLineArgs.containsKey(args[i].substring(2))) {
                System.out.println("Option " + args[i] + " passed multiple times");
                printUsage();
                System.exit(1);
            }
            if (args[i].substring(0, 2).equals("--")) {
                switch (args[i]) {
                    case "--hotels":
                    case "--bookings":
                        parseFileArgs(args[i], args[i + 1]);
                        break;
                    case "--checkin":
                    case "--checkout":
                        parseDateArgs(args[i], args[i + 1]);
                        break;
                    default:
                        printUsage();
                        System.exit(1);
                }
            } else {
                printUsage();
                System.exit(1);
            }
        }

        try {
            Calendar checkin = Calendar.getInstance();
            checkin.setTime(sd.parse(commandLineArgs.get("checkin")));
            Calendar checkout = Calendar.getInstance();
            checkout.setTime(sd.parse(commandLineArgs.get("checkout")));

            //Exit if the checkin date is later than checkout date
            if (checkin.after(checkout) || checkin.equals(checkout)) {
                System.out.println("Checkout date should be later than Checkin date");
                printUsage();
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    public static void main(String[] args) {

        //set Lenient false to validate date
        sd.setLenient(false);

        parseArgs(args);  //Parse commandline arguments
        HashMap<String, Hotel> hotelsMap = new HashMap<String, Hotel>();
        hotelsMap = readHotels();
        readBookings(hotelsMap);
        checkAvailability(hotelsMap);
    }
}
