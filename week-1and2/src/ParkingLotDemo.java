import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    SpotStatus status;

    ParkingSpot() {
        status = SpotStatus.EMPTY;
    }
}

enum SpotStatus {
    EMPTY, OCCUPIED, DELETED
}

class ParkingLot {

    private ParkingSpot[] table;
    private int capacity;
    private int occupied = 0;
    private int totalProbes = 0;
    private int operations = 0;
    private Map<Integer, Integer> hourly = new HashMap<>();

    ParkingLot(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    private int hash(String plate) {
        return Math.abs(plate.hashCode()) % capacity;
    }

    public void parkVehicle(String plate) {

        int index = hash(plate);
        int probes = 0;

        while (table[index].status == SpotStatus.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].licensePlate = plate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = SpotStatus.OCCUPIED;

        occupied++;
        totalProbes += probes;
        operations++;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourly.put(hour, hourly.getOrDefault(hour, 0) + 1);

        System.out.println("parkVehicle(\"" + plate + "\") → Assigned spot #" + index + " (" + probes + " probes)");
    }

    public void exitVehicle(String plate) {

        int index = hash(plate);

        while (table[index].status != SpotStatus.EMPTY) {

            if (table[index].status == SpotStatus.OCCUPIED &&
                    table[index].licensePlate.equals(plate)) {

                long durationMs = System.currentTimeMillis() - table[index].entryTime;
                double hours = durationMs / 3600000.0;
                double fee = hours * 5.5;

                table[index].status = SpotStatus.DELETED;
                table[index].licensePlate = null;

                occupied--;

                System.out.printf("exitVehicle(\"%s\") → Spot #%d freed, Duration: %.2fh, Fee: $%.2f\n",
                        plate, index, hours, fee);
                return;
            }

            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found");
    }

    public void getStatistics() {

        double occupancy = (occupied * 100.0) / capacity;
        double avgProbes = operations == 0 ? 0 : (double) totalProbes / operations;

        int peakHour = -1;
        int max = 0;

        for (Map.Entry<Integer, Integer> e : hourly.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                peakHour = e.getKey();
            }
        }

        System.out.printf("Occupancy: %.0f%%\n", occupancy);
        System.out.printf("Avg Probes: %.2f\n", avgProbes);

        if (peakHour != -1) {
            System.out.println("Peak Hour: " + peakHour + "-" + (peakHour + 1));
        }
    }
}

public class ParkingLotDemo {

    public static void main(String[] args) throws Exception {

        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}