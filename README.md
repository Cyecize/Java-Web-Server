
What is Java Web Server
-----------------------
Java Web Server consists of multiple applications and APIs that are all custom implemented.
* Apache Tomcat -> Javache
* Catalina  -> Broccolina
* Coyote -> Toyote
* Spring MVC -> Summer Framework
* Custom Http API
* Custom Servlet API -> Solet API

The goal
-------
Create a simplified but still functional web server platform and by doing that also help me and
visitors better understand how things such as IOC , HTTP and Resource handling work under the hood.

Main Functionalities
-------------------
Similar to  Tomcat, Javache can
  * Put websites in the webapps folder, extract them and then run them.

  * Have multiple resource handlers (Broccolina and Toyote ) just by implementing ResourceHandler interface

Broccolina and Toyote jar files are placed in a folder where javache will read and execute them upon running.

Summer MVC is really simple but it does have a templating engine, Path variables, BindingModels and so on.

Technologies used
---------------------
	* Java 11
	
	* Maven
	
How to run the app?
------------------
Ok so you have javache and the resource handlers ready and you want to build an app.
Create a maven project and include the SummerApi, SoletApi, HttpApi jar files.
from there you can annotate a class with @Controller from the SummerApi package and use the 
class as a controller.
You can add other dependencies like hibernate but make sure you include them in the jar file.
Javache reads the jar files unpon loading and adds them in the classpath.

More info
-------------
If you are having trouble running the app, contact me at ceci2205@abv.bg

Credits
-------
This project was a lab work in my university but it had some problems mainly with dependencies and the 
difficulty to run the application so I decided to rewrite the whole project and add some more
functionality.

