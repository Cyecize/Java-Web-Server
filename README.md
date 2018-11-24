
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

Summer MVC is really simple but it does have a templating engine IOC (@Service, @Bean), Path variables, BindingModels and so on.

Technologies used
---------------------
	* Java 11
	
	* Maven
	
	* JavaDelight org.javadelight:delight-fileupload used in Broccolina for multipart request parsing.
	
	* Gson (com.google.code.gson) used in SummerMVC
	
	* jTwig (org.jtwig) used as a templating engine in SummerMVC;
	
How to run the app?
------------------
Javache Web Server has 3 directories where you can add extension/configurations.

Config folder contains configurations for the server like the request handlers and their priorities.

Lib folder is where you can put any libraries that you might use in your application like hibernate.
You also need to put broccolina, toyote, soletApi and httpApi there.

Webapps folder is where you put your applications. ROOT.jar is the main app. Any other app
will be accessed by its name. For example shop.jar will be accessible on localhost:8000/shop/.

Your app has to be .jar and has to be structured like so:
* classes - put the compiled output there (classes, resources)

* lib - you can put the .jar libraries here or put them instead in javache's lib folder.

To better understand, you can extract some of the provided example applications.

In the exams folder you can find a working javache demo with everything set up.
You just need to run a command.

More info
-------------
If you are having trouble running the app, contact me at ceci2205@abv.bg

Credits
-------
This project was a lab work in my university but it had some problems mainly with dependencies and the 
difficulty to run the application so I decided to rewrite the whole project and add some more
functionality.

