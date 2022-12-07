package SimpleContainer;

import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.ev3.EV3UltrasonicSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

public class Device {
    static EV3MediumRegulatedMotor motor_left = null;
    static EV3MediumRegulatedMotor motor_right = null;
    static EV3UltrasonicSensor ultrasonic_front = null;
    static EV3UltrasonicSensor ultrasonic_right = null;
    static EV3UltrasonicSensor ultrasonic_left = null;



    public static void init() {
        System.out.println("Motor/Sensors Init starts");
        motor_right = new EV3MediumRegulatedMotor(MotorPort.D);
        motor_left = new EV3MediumRegulatedMotor(MotorPort.A);
        ultrasonic_front = new EV3UltrasonicSensor(SensorPort.S2);   // Front
        //ultrasonic_right = new EV3UltrasonicSensor(SensorPort.S2);   // Right
        //ultrasonic_left = new EV3UltrasonicSensor(SensorPort.S3);   // Left
        System.out.println("Motor/Sensors Init finishes");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Stop motors Ctrl+C");
                motor_left.stop();
                motor_right.stop();
            }
        }));

    }

    public static void stop() {
        motor_left.stop();
        motor_right.stop();
    }

    public static void backward() {
        motor_left.backward();
        motor_right.backward();
    }

    public static void forward() {
        motor_left.forward();
        motor_right.forward();
    }

    public static void turnLeft() {
        motor_left.backward();
        motor_right.forward();
    }

    public static void turnRight() {
        motor_left.forward();
        motor_right.backward();
    }

    public static void setSpeed(int speed) {
        motor_left.setSpeed(speed);
        motor_right.setSpeed(speed);
    }

    public static int getFrontDistance() {
        final SampleProvider sp = ultrasonic_front.getDistanceMode();
        int distanceValue = 0;
        float [] sample = new float[sp.sampleSize()];
        sp.fetchSample(sample, 0);
        distanceValue = (int)sample[0];

        if (distanceValue > 10000){
            distanceValue = 420;
        }

        return distanceValue;
    }

    public static int getLeftDistance() {
        final SampleProvider sp = ultrasonic_left.getDistanceMode();
        int distanceValue = 0;
        float [] sample = new float[sp.sampleSize()];
        sp.fetchSample(sample, 0);
        distanceValue = (int)sample[0];

        if (distanceValue > 10000){
            distanceValue = 420;
        }

        return distanceValue;
    }

    public static int getRightDistance() {
        final SampleProvider sp = ultrasonic_right.getDistanceMode();
        int distanceValue = 0;
        float [] sample = new float[sp.sampleSize()];
        sp.fetchSample(sample, 0);
        distanceValue = (int)sample[0];

        if (distanceValue > 10000){
            distanceValue = 420;
        }

        return distanceValue;
    }
}
