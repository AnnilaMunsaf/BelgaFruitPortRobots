package util;

public class WorkItem {
    Point2D pickup;
    Point2D dropoff;

    public WorkItem(Point2D pickup, Point2D dropoff){
        this.pickup = pickup;
        this.dropoff = dropoff;
    }

    public WorkItem(int pickupX, int pickupY, int dropoffX, int dropoffY){
        this.pickup = new Point2D(pickupX, pickupY);
        this.dropoff = new Point2D(dropoffX, dropoffY);
    }

    public Point2D getPickup() {
        return this.pickup;
    }

    public void setPickup(Point2D pickup) {
        this.pickup = pickup;
    }

    public Point2D getDropoff() {
        return this.dropoff;
    }

    public void setDropoff(Point2D dropoff) {
        this.dropoff = dropoff;
    }
}
