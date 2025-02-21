import java.util.*;

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

    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

class DurationType {
    int duration; // in minutes

    public DurationType(int d) {
        this.duration = d;
    }

    public static DurationType randomSimulationDuration() {
        Random rand = new Random();
        int d = rand.nextInt(15) + 1;
        return new DurationType(d);
    }

    // Generate a random service duration between 1 and 10 minutes (ensuring at
    // least 1 minute)
    public static DurationType randomServiceDuration() {
        Random rand = new Random();
        int d = rand.nextInt(10) + 1;
        return new DurationType(d);
    }
}

class CustomerType {
    int ID;
    TimeType arrivalT;
    DurationType serviceT; // actual service duration (random 1-10 minutes)
    DurationType waitingTime; // base waiting time (set to 5 minutes for display)
    int queueAssigned;
}

class ServiceStationType {
    String name;
    Queue<CustomerType> queue;
    int numbersOfCustomers; // for tracking purposes
    TimeType arrivalTime;
    DurationType waitingTime; // base waiting time for display
    DurationType serviceTime; // fixed average service rate (5 minutes)
    boolean isBusy;
    int totalServiceTime; // additional statistics if needed

    ServiceStationType(String name) {
        this.name = name;
        this.queue = new LinkedList<>();
        this.numbersOfCustomers = 0;
        this.arrivalTime = TimeType.randomTime();
        this.waitingTime = new DurationType(5);
        this.serviceTime = new DurationType(5);
        this.isBusy = false;
        this.totalServiceTime = 0;
    }

    // Sum the service durations of all waiting customers.
    public int getTotalWaitingTime() {
        int total = 0;
        for (CustomerType c : queue) {
            total += c.serviceT.duration;
        }
        return total;
    }

    // For display: if this is the RoundRobinQueue, return the maximum service
    // duration among waiting customers;
    // otherwise, return the sum.
    public int getDisplayWaitingTime() {
        if (this.name.equals("RoundRobinQueue")) {
            int max = 0;
            for (CustomerType c : queue) {
                if (c.serviceT.duration > max)
                    max = c.serviceT.duration;
            }
            return max;
        } else {
            return getTotalWaitingTime();
        }
    }
}

public class QueueSimulation {
    static List<ServiceStationType> serviceStations = Arrays.asList(
            new ServiceStationType("SingleQueue"),
            new ServiceStationType("RoundRobinQueue"),
            new ServiceStationType("ShortestQueue"),
            new ServiceStationType("RegularQ1"),
            new ServiceStationType("RegularQ2"));

    static int simulationTime = 1, ID = 1;
    // totalCustomerWaiting reflects the initial batch size (for display only).
    static int totalCustomerWaiting = new Random().nextInt(15) + 1;
    static TimeType arrivalRate = TimeType.randomTime();

    static Scanner scanner = new Scanner(System.in);

    // Frequency map to track next quickest station selection.
    static Map<String, Integer> nextQuickestFrequency = new HashMap<>();

    // Helper: get a station by its name.
    public static ServiceStationType getStation(String name) {
        return serviceStations.stream()
                .filter(s -> s.name.equals(name))
                .findFirst().get();
    }

    // Helper: convert a queue of customers to a string of their IDs.
    public static String getQueueCustomerIDs(Queue<CustomerType> queue) {
        StringBuilder sb = new StringBuilder();
        for (CustomerType c : queue) {
            sb.append(c.ID).append(" ");
        }
        return sb.toString().trim();
    }

    // Estimated waiting time if a new customer were to join.
    // For RoundRobinQueue: if empty, assume 5 minutes; if not, return a high value.
    // For others, return the sum of service durations.
    public static int estimatedWaitingTime(ServiceStationType station) {
        if (station.name.equals("RoundRobinQueue")) {
            if (station.queue.isEmpty())
                return 5;
            else
                return Integer.MAX_VALUE;
        } else {
            return station.getTotalWaitingTime();
        }
    }

    // Determine the next quickest service station for a new customer.
    // It considers only stations that are "available" (RoundRobinQueue only if
    // empty; SingleQueue only if <15).
    // It first finds the minimum queue size among available stations.
    // If ShortestQueue's size equals that minimum, it is chosen.
    // Otherwise, it returns the station among available ones with the lowest
    // estimated waiting time.
    public static ServiceStationType nextQuickestStationForNewCustomer() {
        int minSize = Integer.MAX_VALUE;
        for (ServiceStationType s : serviceStations) {
            if (s.name.equals("RoundRobinQueue") && !s.queue.isEmpty())
                continue;
            if (s.name.equals("SingleQueue") && s.queue.size() >= 15)
                continue;
            minSize = Math.min(minSize, s.queue.size());
        }
        ServiceStationType shortestQueue = getStation("ShortestQueue");
        if (shortestQueue.queue.size() == minSize) {
            return shortestQueue;
        }
        int minEst = Integer.MAX_VALUE;
        ServiceStationType best = null;
        for (ServiceStationType s : serviceStations) {
            if (s.name.equals("RoundRobinQueue") && !s.queue.isEmpty())
                continue;
            if (s.name.equals("SingleQueue") && s.queue.size() >= 15)
                continue;
            int est = estimatedWaitingTime(s);
            if (est < minEst) {
                minEst = est;
                best = s;
            }
        }
        return best;
    }

    // Display the next quickest service station and update its frequency.
    public static void displayNextQuickestServiceStation() {
        ServiceStationType best = nextQuickestStationForNewCustomer();
        if (best == null) {
            System.out.println("No station is available for new assignment according to the rules.");
        } else {
            int est = estimatedWaitingTime(best);
            // Update frequency count.
            nextQuickestFrequency.put(best.name, nextQuickestFrequency.getOrDefault(best.name, 0) + 1);
            System.out.println("Next quickest service station: " + best.name
                    + " (Estimated waiting time: " + est + " minutes)");
        }
    }

    // Display the most efficient service station (i.e. the one with highest
    // frequency).
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
            System.out.println("Most efficient service station (next quickest frequency): "
                    + mostEfficient + " (" + maxCount + " times)");
        }
    }

    // Check if a station is available according to its rules.
    public static boolean isAvailable(ServiceStationType station) {
        if (station.name.equals("RoundRobinQueue"))
            return station.queue.isEmpty();
        if (station.name.equals("SingleQueue"))
            return station.queue.size() < 15;
        return true;
    }

    // Display starting information.
    public static void displayStartingInfo() {
        System.out.println("The simulation will begin now, sit tight!.");
        System.out.println("There are five service stations:");
        System.out.println(
                "1. A regular single queue that will serve 15 customers, service station will take customers from the front of the queue.");
        System.out.println(
                "2. A round robin queue where each time it will only take 5 customers, and will not take new round until the current 5 customer services are done.");
        System.out.println("3. The queue will be assigned a new customer if it is the shortest.");
        System.out.println(
                "4. The remaining two queues will be assigned new customers randomly with no preference or priority.");
        System.out.println("Initial Customer Batch: " + totalCustomerWaiting);
        System.out.println("Total Customer Waiting Duration: " + (totalCustomerWaiting * 5) + " minutes");
        System.out.println("First Customer Arrival Time: " + arrivalRate);
        System.out.println("Average Service Rate: 5 minutes\n");
    }

    // New assignment method:
    // First, assign as many new customers as possible to the next quickest station.
    // Then assign any remaining customers using our fallback rules.
    public static void assignNewBatch(List<CustomerType> newCustomers) {
        ServiceStationType nextStation = nextQuickestStationForNewCustomer();
        while (!newCustomers.isEmpty() && isAvailable(nextStation)) {
            nextStation.queue.add(newCustomers.remove(0));
            nextStation.numbersOfCustomers++;
        }
        // For any remaining customers, use previous rules.
        // Rule 1: RoundRobinQueue.
        ServiceStationType rrQueue = getStation("RoundRobinQueue");
        if (rrQueue.queue.isEmpty() && !newCustomers.isEmpty()) {
            int toAssign = Math.min(5, newCustomers.size());
            for (int i = 0; i < toAssign; i++) {
                rrQueue.queue.add(newCustomers.remove(0));
                rrQueue.numbersOfCustomers++;
            }
        }
        // Rule 2: SingleQueue.
        ServiceStationType singleQueue = getStation("SingleQueue");
        while (singleQueue.queue.size() < 15 && !newCustomers.isEmpty()) {
            singleQueue.queue.add(newCustomers.remove(0));
            singleQueue.numbersOfCustomers++;
        }
        // Rule 3: For remaining new customers, assign based on overall minimum queue
        // size.
        while (!newCustomers.isEmpty()) {
            int minSize = Integer.MAX_VALUE;
            for (ServiceStationType s : serviceStations) {
                minSize = Math.min(minSize, s.queue.size());
            }
            List<ServiceStationType> candidates = new ArrayList<>();
            for (ServiceStationType s : serviceStations) {
                if (s.queue.size() == minSize)
                    candidates.add(s);
            }
            ServiceStationType fallback = candidates.get(new Random().nextInt(candidates.size()));
            fallback.queue.add(newCustomers.remove(0));
            fallback.numbersOfCustomers++;
        }
    }

    // Process (serve) up to 1 customer per station.
    public static void processCustomers() {
        for (ServiceStationType station : serviceStations) {
            int customersToProcess = Math.min(1, station.queue.size());
            for (int i = 0; i < customersToProcess; i++) {
                station.queue.poll();
                station.numbersOfCustomers = Math.max(0, station.numbersOfCustomers - 1);
            }
        }
    }

    // Display current system status.
    public static void displayStatus() {
        int totalRemainingCustomers = 0;
        int totalWaitingTimeSystem = 0;
        System.out.println("\nCurrent System Status:");
        for (ServiceStationType station : serviceStations) {
            int stationRemaining = station.queue.size();
            totalRemainingCustomers += stationRemaining;
            int stationWaitingTime = station.getDisplayWaitingTime();
            totalWaitingTimeSystem += stationWaitingTime;
            System.out.println(station.name + " Customers ID: " + getQueueCustomerIDs(station.queue)
                    + " | Total Waiting Time: " + stationWaitingTime + " minutes");
        }
        System.out.println("Total Waiting Customers in System: " + totalRemainingCustomers);
        System.out.println("Total Waiting Time for the System: " + totalWaitingTimeSystem + " minutes");
    }

    // Each simulation round: generate a new batch, allow user input for
    // continuation,
    // display next quickest station, pause 2 seconds, assign new customers,
    // process, and display status.
    public static void simulate() {
        Random rand = new Random();
        while (true) {
            System.out.println(
                    "\nPress any key to continue simulation, type 0 to exit, or type 1 to display the most efficient service station:");
            String input = scanner.next();
            if (input.equals("0"))
                break;
            if (input.equals("1")) {
                displayMostEfficientServiceStation();
                continue; // Prompt again.
            }

            displayNextQuickestServiceStation();
            // Sleep for 2 seconds.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // Ignore interruption.
            }

            int numNewCustomers = rand.nextInt(15) + 1; // New customers per round (1 to 15)
            List<CustomerType> newCustomers = new ArrayList<>();
            for (int i = 0; i < numNewCustomers; i++) {
                CustomerType customer = new CustomerType();
                customer.ID = ID++;
                customer.arrivalT = new TimeType(arrivalRate.hours, arrivalRate.minutes, arrivalRate.seconds);
                customer.waitingTime = new DurationType(5);
                customer.serviceT = DurationType.randomServiceDuration();
                newCustomers.add(customer);
            }

            assignNewBatch(newCustomers);
            processCustomers();
            displayStatus();
        }
    }

    public static void main(String[] args) {
        // Initialize frequency map for each station.
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
            customer.serviceT = DurationType.randomServiceDuration();
            initialCustomers.add(customer);
        }
        assignNewBatch(initialCustomers);
        simulate();
    }
}
