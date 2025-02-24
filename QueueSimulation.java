import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

// Represents a time value.
class TimeType {
    int hours, minutes, seconds;
    
    public TimeType(int h, int m, int s) {
        this.hours = h;
        this.minutes = m;
        this.seconds = s;
    }
    
    public static TimeType randomTime() {
        Random rand = new Random();
        int h = rand.nextInt(24);
        int m = rand.nextInt(60);
        int s = rand.nextInt(60);
        return new TimeType(h, m, s);
    }
    
    // Returns a new TimeType by adding minutes.
    public static TimeType addMinutes(TimeType t, int minutesToAdd) {
        int totalMinutes = t.hours * 60 + t.minutes + minutesToAdd;
        int newHours = (totalMinutes / 60) % 24;
        int newMinutes = totalMinutes % 60;
        return new TimeType(newHours, newMinutes, t.seconds);
    }
    
    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

// Represents a duration in minutes.
class DurationType {
    int duration;
    
    public DurationType(int d) {
        this.duration = d;
    }
    
    // Returns a random round duration between 1 and 10 minutes.
    public static DurationType randomRoundDuration() {
        Random rand = new Random();
        int d = rand.nextInt(10) + 1;
        return new DurationType(d);
    }
    
    // Returns a random simulation total duration between 30 and 120 minutes.
    public static DurationType randomSimulationDuration() {
        Random rand = new Random();
        int d = rand.nextInt(91) + 30;
        return new DurationType(d);
    }
    
    // Returns a random service duration between 1 and (2*averageServiceRate - 1) minutes.
    public static DurationType randomServiceDuration(int averageServiceRate) {
        Random rand = new Random();
        int lower = 1;
        int upper = 2 * averageServiceRate - 1;
        int d = lower + rand.nextInt(upper - lower + 1);
        return new DurationType(d);
    }
}

// Represents a customer.
class CustomerType {
    int ID;
    TimeType arrivalT;
    DurationType serviceT;   // actual service duration
    DurationType waitingTime; // fixed waiting time (5 minutes)
    int queueAssigned;
}

// Represents a service station.
class ServiceStationType {
    String name;
    Queue<CustomerType> queue;
    int numbersOfCustomers;
    TimeType arrivalTime;
    DurationType waitingTime; // base waiting time (5 minutes)
    DurationType serviceTime; // fixed average service rate (5 minutes)
    boolean isBusy;
    int totalServiceTime;
    
    ServiceStationType(String name) {
        this.name = name;
        this.queue = new ConcurrentLinkedQueue<>();
        this.numbersOfCustomers = 0;
        this.arrivalTime = TimeType.randomTime();
        this.waitingTime = new DurationType(5);
        this.serviceTime = new DurationType(5);
        this.isBusy = false;
        this.totalServiceTime = 0;
    }
    
    // Total waiting time = (# customers in queue * 5 minutes).
    public int getTotalWaitingTime() {
        return queue.size() * 5;
    }
    
    public int getDisplayWaitingTime() {
        return getTotalWaitingTime();
    }
}

// Each service station runs in its own thread processing customers.
class ServiceStationThread extends Thread {
    ServiceStationType station;
    int processingDelay; // milliseconds per customer
    
    public ServiceStationThread(ServiceStationType station, int delay) {
        this.station = station;
        this.processingDelay = delay;
    }
    
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!station.queue.isEmpty()) {
                station.queue.poll();
                station.numbersOfCustomers = Math.max(0, station.numbersOfCustomers - 1);
                try {
                    Thread.sleep(processingDelay);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}

// Main simulation class.
public class QueueSimulation {
    static List<ServiceStationType> serviceStations = Arrays.asList(
        new ServiceStationType("SingleQueue"),
        new ServiceStationType("RoundRobinQueue"),
        new ServiceStationType("ShortestQueue"),
        new ServiceStationType("RegularQ1"),
        new ServiceStationType("RegularQ2")
    );
    
    static int simulationRound = 1, ID = 1;
    // Total simulation duration (overall simulation, not per round) between 30 and 120 minutes.
    static DurationType totalSimulationDuration = DurationType.randomSimulationDuration();
    static int totalDurationInput = totalSimulationDuration.duration;
    // Initial waiting customers: average ~1 every 2 minutes.
    static int totalCustomerWaiting;
    // Average Service Rate (S) is now tuned to be much higher (between 20 and 40 minutes) to crowd the system.
    static int averageServiceRate;
    
    static TimeType arrivalRate = TimeType.randomTime();
    static Scanner scanner = new Scanner(System.in);
    
    // Track accumulated simulation time (in minutes) and current customer arrival time.
    static int accumulatedSimTime = 0;
    static TimeType currentArrivalTime;
    
    // Frequency map for next quickest station selection.
    static Map<String, Integer> nextQuickestFrequency = new HashMap<>();
    
    // Helper: get a station by its name.
    public static ServiceStationType getStation(String name) {
        return serviceStations.stream()
                .filter(s -> s.name.equals(name))
                .findFirst().get();
    }
    
    // Helper: convert a queue of customer IDs to a string.
    public static String getQueueCustomerIDs(Queue<CustomerType> queue) {
        StringBuilder sb = new StringBuilder();
        for (CustomerType c : queue) {
            sb.append(c.ID).append(" ");
        }
        return sb.toString().trim();
    }
    
    // Helper: format the customer IDs string to fixed width.
    public static String formatCustomerIDs(String ids, int width) {
        String[] parts = ids.split(" ");
        StringBuilder result = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for (String part : parts) {
            if (line.length() + part.length() + 1 > width) {
                result.append(line.toString().trim()).append("\n");
                line = new StringBuilder();
            }
            line.append(part).append(" ");
        }
        result.append(line.toString().trim());
        return result.toString();
    }
    
    // Estimated waiting time: if station is empty, return 5; otherwise (# customers * 5).
    public static int estimatedWaitingTime(ServiceStationType station) {
        int n = station.queue.size();
        return (n == 0) ? 5 : n * 5;
    }
    
    // Determine the next quickest station among available ones.
    public static ServiceStationType nextQuickestStationForNewCustomer() {
        List<ServiceStationType> available = new ArrayList<>();
        for (ServiceStationType s : serviceStations) {
            if ((s.name.equals("SingleQueue") || s.name.equals("RoundRobinQueue")) && s.queue.size() >= 5)
                continue;
            available.add(s);
        }
        if (available.isEmpty())
            return null;
        int minSize = Integer.MAX_VALUE;
        for (ServiceStationType s : available) {
            minSize = Math.min(minSize, s.queue.size());
        }
        List<ServiceStationType> candidates = new ArrayList<>();
        for (ServiceStationType s : available) {
            if (s.queue.size() == minSize)
                candidates.add(s);
        }
        ServiceStationType best = null;
        int minEst = Integer.MAX_VALUE;
        for (ServiceStationType s : candidates) {
            int est = estimatedWaitingTime(s);
            if (est < minEst) {
                minEst = est;
                best = s;
            }
        }
        return best;
    }
    
    // Display the next quickest station and update frequency.
    public static void displayNextQuickestServiceStation() {
        ServiceStationType best = nextQuickestStationForNewCustomer();
        if (best == null) {
            System.out.println("No station is available for new assignment according to the rules.");
        } else {
            int est = estimatedWaitingTime(best);
            nextQuickestFrequency.put(best.name, nextQuickestFrequency.getOrDefault(best.name, 0) + 1);
            System.out.println("Next quickest service station: " + best.name +
                    " (Estimated waiting time: " + est + " minutes)");
        }
    }
    
    // Display the most efficient station (by frequency).
    public static void displayMostEfficientServiceStation() {
        String mostEfficient = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : nextQuickestFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostEfficient = entry.getKey();
            }
        }
        if (mostEfficient == null) {
            System.out.println("No station has been selected yet.");
        } else {
            System.out.println("Most efficient service station (next quickest frequency): " +
                    mostEfficient + " (" + maxCount + " times)");
        }
    }
    
    // Check if a station is available.
    public static boolean isAvailable(ServiceStationType station) {
        if (station.name.equals("SingleQueue") || station.name.equals("RoundRobinQueue"))
            return station.queue.size() < 5;
        return true;
    }
    
    // Display initial simulation inputs (printed only once).
    public static void displayStartingInfo() {
        System.out.println("\nWelcome to the Service Station Simulation!");
        System.out.println("Initial Simulation Inputs:");
        System.out.println("Total Duration: " + totalSimulationDuration.duration + " minutes");
        System.out.println("Initial Waiting Customers: " + totalCustomerWaiting);
        System.out.println("Average Service Rate: " + averageServiceRate + " minutes/per customer");
        System.out.println("First Customer Arrival Time: " + arrivalRate);
        System.out.println("Average Waiting Time per Customer: 5 minutes\n");
    }
    
    // Assignment: assign new customers based on the next quickest station.
    // Returns the number of customers that remain unassigned.
    public static int assignNewBatch(List<CustomerType> newCustomers) {
        while (!newCustomers.isEmpty() && isAvailable(nextQuickestStationForNewCustomer())) {
            ServiceStationType nextStation = nextQuickestStationForNewCustomer();
            if (nextStation == null)
                break;
            while (!newCustomers.isEmpty() && isAvailable(nextStation)) {
                nextStation.queue.add(newCustomers.remove(0));
                nextStation.numbersOfCustomers++;
            }
        }
        // Fallback: assign to RoundRobinQueue if available.
        ServiceStationType rrQueue = getStation("RoundRobinQueue");
        if (rrQueue.queue.size() < 5 && !newCustomers.isEmpty()) {
            int toAssign = Math.min(5 - rrQueue.queue.size(), newCustomers.size());
            for (int i = 0; i < toAssign; i++) {
                rrQueue.queue.add(newCustomers.remove(0));
                rrQueue.numbersOfCustomers++;
            }
        }
        // Then assign to SingleQueue.
        ServiceStationType singleQueue = getStation("SingleQueue");
        while (singleQueue.queue.size() < 5 && !newCustomers.isEmpty()) {
            singleQueue.queue.add(newCustomers.remove(0));
            singleQueue.numbersOfCustomers++;
        }
        // Finally, assign any remaining new customers to stations that are not full.
        while (!newCustomers.isEmpty()) {
            int minSize = Integer.MAX_VALUE;
            for (ServiceStationType s : serviceStations) {
                if ((s.name.equals("SingleQueue") || s.name.equals("RoundRobinQueue")) && s.queue.size() >= 5)
                    continue;
                minSize = Math.min(minSize, s.queue.size());
            }
            List<ServiceStationType> candidates = new ArrayList<>();
            for (ServiceStationType s : serviceStations) {
                if ((s.name.equals("SingleQueue") || s.name.equals("RoundRobinQueue")) && s.queue.size() >= 5)
                    continue;
                if (s.queue.size() == minSize)
                    candidates.add(s);
            }
            if (candidates.isEmpty())
                break;
            ServiceStationType fallback = candidates.get(new Random().nextInt(candidates.size()));
            fallback.queue.add(newCustomers.remove(0));
            fallback.numbersOfCustomers++;
        }
        return newCustomers.size();
    }
    
    // Display current system status in a formatted table.
    public static void displayStatus(int unassigned) {
        int systemWaitingTime = 0;
        for (ServiceStationType s : serviceStations) {
            systemWaitingTime += s.getDisplayWaitingTime();
        }
        
        System.out.println("\n---------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-18s | %-30s | %-30s | %-15s | %-15s\n",
                          "Station", "Customers ID", "Waiting Time", "Max Length", "Occupancy Rate");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        for (ServiceStationType station : serviceStations) {
            String customerIDs = formatCustomerIDs(getQueueCustomerIDs(station.queue), 30);
            int waitingTime = station.getDisplayWaitingTime();
            String waitingLabel = "Max Waiting Time: " + waitingTime + " min";
            String maxLength;
            if (station.name.equals("SingleQueue"))
                maxLength = "5 people";
            else if (station.name.equals("RoundRobinQueue"))
                maxLength = "5 people";
            else
                maxLength = "∞";
            double occupancy = (systemWaitingTime > 0) ? (waitingTime * 100.0 / systemWaitingTime) : 0.0;
            System.out.printf("%-18s | %-30s | %-30s | %-15s | %-15s\n",
                              station.name,
                              customerIDs,
                              waitingLabel,
                              maxLength,
                              String.format("%-15s", String.format("%.2f%%", occupancy)));
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("On all service stations, Average Waiting Time of Each Customer: 5 minutes");
        
        int totalWaitingCustomers = 0;
        for (ServiceStationType s : serviceStations) {
            totalWaitingCustomers += s.queue.size();
        }
        System.out.println("Total Waiting Customers in System: " + totalWaitingCustomers);
        if (unassigned > 0)
            System.out.println("Remaining Unassigned Customers: " + unassigned);
        System.out.println("Total Duration of the Simulation: " + accumulatedSimTime + " minutes");
    }
    
    // Simulation loop.
    public static void simulate() {
        Random rand = new Random();
        currentArrivalTime = arrivalRate;
        while (simulationRound <= totalSimulationDuration.duration) {
            // Generate a random round duration (1 to 10 minutes).
            DurationType roundDuration = DurationType.randomRoundDuration();
            accumulatedSimTime += roundDuration.duration;
            currentArrivalTime = TimeType.addMinutes(currentArrivalTime, roundDuration.duration);
            
            System.out.println("\nPress any key to continue simulation, type 0 to exit, or type 1 to display the most efficient service station:");
            String input = scanner.next();
            if (input.equals("0"))
                break;
            if (input.equals("1")) {
                displayMostEfficientServiceStation();
                continue;
            }
            
            // Print current round's customer arrival time.
            System.out.println("This simulation Customer Arrival Time: " + currentArrivalTime);
            displayNextQuickestServiceStation();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // Ignore.
            }
            
            // Generate new customers based on round duration:
            // On average, 1 customer every 2 minutes => ceil(roundDuration/2).
            int numNewCustomers = (int) Math.ceil(roundDuration.duration / 2.0);
            List<CustomerType> newCustomers = new ArrayList<>();
            for (int i = 0; i < numNewCustomers; i++) {
                CustomerType customer = new CustomerType();
                customer.ID = ID++;
                customer.arrivalT = new TimeType(currentArrivalTime.hours, currentArrivalTime.minutes, currentArrivalTime.seconds);
                customer.waitingTime = new DurationType(5);
                customer.serviceT = DurationType.randomServiceDuration(averageServiceRate);
                newCustomers.add(customer);
            }
            
            int unassigned = assignNewBatch(newCustomers);
            displayStatus(unassigned);
            simulationRound++;
        }
    }
    
    public static void main(String[] args) {
        Random rand = new Random();
        totalSimulationDuration = DurationType.randomSimulationDuration();  // 30-120 minutes
        totalDurationInput = totalSimulationDuration.duration;
        int avgWaiting = totalDurationInput / 2;
        totalCustomerWaiting = rand.nextInt((avgWaiting / 2) + 1) + avgWaiting - (avgWaiting / 4);
        if(totalCustomerWaiting < 1)
            totalCustomerWaiting = 1;
        // Adjust average service rate to be much higher than 5 * arrival rate (to crowd the system).
        averageServiceRate = rand.nextInt(21) + 20;  // between 20 and 40 minutes

        
        List<Thread> stationThreads = new ArrayList<>();
        for (ServiceStationType station : serviceStations) {
            ServiceStationThread thread = new ServiceStationThread(station, 3000);
            thread.start();
            stationThreads.add(thread);
        }
        
        for (ServiceStationType s : serviceStations) {
            nextQuickestFrequency.put(s.name, 0);
        }
        
        displayStartingInfo();
        List<CustomerType> initialCustomers = new ArrayList<>();
        for (int i = 0; i < totalCustomerWaiting; i++) {
            CustomerType customer = new CustomerType();
            customer.ID = ID++;
            customer.arrivalT = new TimeType(arrivalRate.hours, arrivalRate.minutes, arrivalRate.seconds);
            customer.waitingTime = new DurationType(5);
            customer.serviceT = DurationType.randomServiceDuration(averageServiceRate);
            initialCustomers.add(customer);
        }
        assignNewBatch(initialCustomers);
        simulate();
        
        for (Thread t : stationThreads) {
            t.interrupt();
        }
    }
}
