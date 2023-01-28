# How to run the system

## Prerequisites

The Prerequisites to use this project are: (the bricks at the lab already satisfy this prerequisites)

- Your MINDSTORMS Brick needs to have installed latest `Debian Stretch` version. https://www.ev3dev.org/docs/getting-started/
- To increase the EV3 CPU Speed, read the following article: https://lechnology.com/2018/06/overclocking-lego-mindstorms-ev3-part-2/
- Your MINDSTORMS Brick needs to be connected to the same LAN than your laptop. http://www.ev3dev.org/docs/getting-started/#step-5-set-up-a-network-connection

Note: Update the EV3Dev kernel
https://www.ev3dev.org/docs/tutorials/upgrading-ev3dev/ 

```
sudo apt-get update
sudo apt-get install linux-image-ev3dev-ev3
```

Once you have all steps done, continue with the next section.

## Deploying the code to the robots

Once you download the project
open your favourite Java IDE ( [Eclipse](https://eclipse.org/home/index.php) or [IntelliJ](https://www.jetbrains.com/idea/))
to import this [Gradle](https://gradle.org/) project.

The project includes some tasks to reduce the time to deploy on your robot.

Review the IP of your Brick and update the file `./config.gradle`:

```
remotes {
    ev3dev {
        host = '192.168.1.180'
        user = 'robot'
        password = 'maker'
    }
}
```

also update the file `./src/main/java/SimpleContainer/SimpleContainer.java` with the brick ip and the used tag id (lines 15 and 16)
```
String robot_ip = "192.168.0.180";
String tag_id = "6a75";
```

now you can use the gradle deploy task to deploy the code to the robot. Do this for each robot that you want to add to the system.

## Running the containers

The main container can be run using the file `./src/main/java/MainContainer/MainContainer.java`

after that you have to run the simple container of each robot that you want to add to the system, to do this first ssh to the robot with

```
ssh robot@<robot_ip>
```

the password to access the brick pi is "maker", once you are inside you can run the simple container with

```
java -jar BelgaFruitPortRobots.jar
```
