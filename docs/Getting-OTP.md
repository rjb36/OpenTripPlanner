# Getting OpenTripPlanner

## Pre-built JARs

OpenTripPlanner is now distributed as a single stand-alone runnable JAR file. The JAR file for each release is
published [here](http://dev.opentripplanner.org/jars/). In addition, whenever new changes are made to the master branch 
of the main OTP repository on Github, the resulting JAR is published to the
 [same location](http://dev.opentripplanner.org/jars/otp-latest-master.jar).


## Building from Source

You may also choose to build OTP from its source code. If you will be modifying OTP you will need to know how to rebuild
 it (though your IDE may take care of this build cycle for you). If you have the right software installed, 
 building OTP locally from its source code is not particularly difficult. You should only need the following software:

- Git, a version control system

- Java Development Kit, preferably version 7 (AKA version 1.7)

- Maven, a build and dependency management system

 
You will also need a reliable internet connection so Maven can fetch all of OTP's dependencies (the libraries it uses). 
To install these software packages on a Debian or Ubuntu system, run `sudo apt-get install openjdk-7-jdk maven git`.

Once you have these packages installed, create and/or switch to the directory where you will keep your Git repositories and make a local copy of the OTP source code:

```shell
mkdir git
cd git
git clone git@github.com:opentripplanner/OpenTripPlanner.git
```

Then change to the newly cloned OpenTripPlanner repository directory and start a build:

```shell
cd OpenTripPlanner
mvn clean package
```
Maven should then be able to download all the libraries and other dependencies necessary to compile OTP. 
If all goes well you should see a success message like the following:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 42.164s
[INFO] Finished at: Tue Feb 18 19:35:48 CET 2014
[INFO] Final Memory: 88M/695M
[INFO] ------------------------------------------------------------------------
```

This build process should produce a JAR file called `otp.jar` in the `target/` directory which contains all the 
compiled OTP classes and their dependencies. The shell script called 'otp' in the root of the cloned repository will 
start the main class of that JAR file under a Java virtual machine, so after the Maven build completes you should be 
able to run `./otp --help` and see an OTP help message including command line options.

The words "clean package" are the build steps you want to run. You're telling maven to clean up any extraneous junk in
 the directory, then perform all the build steps including compilation up to "package", which bundles the compiled program
 into a single JAR file for distribution. 
 
If you have just cloned OTP you will be working with the default "master" branch, where most active development occurs.
 This is not the most stable or deployment-ready code available. To avoid newly minted bugs or undocumented behavior,
 you can use Git to check out a specific release (tag or branch) of OTP to work with. The Maven build also includes 
 many time-consuming integration tests. When working with a stable release of OTP, 
 you may want to turn them off by adding the switch: `-DskipTests`.

For example, you could do the following:

```bash
cd OpenTripPlanner
git checkout opentripplanner-0.13.0
git clean -df
mvn clean package -DskipTests
```

## Maven Repository

OpenTripPlanner is a Maven project. Maven is a combined build and dependency management system: it fetches
all the external libraries that OTP uses, runs all the commands to compile the OTP source code into runnable form,
performs tests, and can then deploy the final "artifact" (the runnable JAR file) to our Maven repository, from which it
can be automatically included in other Java projects.

This repository is machine-readable (by Maven or other build systems) and for the moment does not include any human readable indexes. 
You can nonetheless fetch an OTP JAR from this repository by constructing the proper URL for the release
you want. For example, release 0.13.0 will be found at `http://maven.conveyal.com/org/opentripplanner/otp/0.13.0/otp-0.13.0.jar`

To make use of OTP in another project, you must first specify our Maven repository in the Project Object Model (POM):

```XML
<repositories>
  <repository>
    <id>Conveyal</id>
    <name>Conveyal Maven Repository</name>
    <url>http://maven.conveyal.com/</url> 
  </repository>
</repositories>
```

And then specify OpenTripPlanner as a dependency:

```XML
<dependency>
  <groupId>org.opentripplanner</groupId>
  <artifactId>otp</artifactId>
  <version>0.13.0</version>
</dependency>
```

After each successful build, our continuous integration (CI) server deploys the final OTP "artifact" (the runnable JAR) 
to our Maven repository as a "SNAPSHOT" build. This means that a Maven project depending on OTP as a library can 
always fetch the latest work in progress by specifying the following artifact:
 
 ```XML
<dependency>
  <groupId>org.opentripplanner</groupId>
  <artifactId>otp</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
 ```
 
We may eventually migrate to the Gradle build system, but Gradle uses the same dependency management and 
repository system as Maven.


## Continuous Integration

This section does not belong here, but can be moved later.
The OpenTripPlanner project has a continuous integration (CI) server at http://ci.opentripplanner.org. Any time a change
is pushed to the main OpenTripPlanner repository on GitHub, this server will compile and test the new code, providing
feedback on the stability of the build. It is also configured to run a battery of speed tests so that we can track
improvements due to optimizations and spot drops in performance as an unintended consequence of changes.

