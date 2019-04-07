
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

Summer MVC is a fully custom MVC framework that has a templating engine, Dependency Container, Path variables, BindingModels, Validation, Security, Interceptors, Custom data adapters
and more.

Multipart encoding is also supported.

Embedded Web Server
-------------------
Javache Embedded is an embedded version of Javache web server, which provides better debugging, faster built times.

Another benefit of the embedded server is that it can be used to create desktop applications with html, css and javaScript.
For example we can have an embedded browser (CEF) that will be in the same app and it will browse on localhost and the server
behind will act as it is a normal browser and will return views or JSON data. This is a much better approach for creating desktop applications for people that are more familiar with HTML rather than Java FX / Swing.

Technologies used
---------------------
	* Java 11
	
	* Maven
	
	* JavaDelight org.javadelight:delight-fileupload used in Broccolina for multipart request parsing.
	
	* Gson (com.google.code.gson) used in SummerMVC.
	
	* Apache Commons (codec) used in SummerMVC for md5 hashing.
	
	* jTwig (org.jtwig) used as a templating engine in SummerMVC.
	
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

To better understand, you can extract some of the provided example applications or read the documentation inside Documentation folder.

In the exams folder you can find a working javache demo with everything set up.
You just need to run a command.

More info
-------------
If you are having trouble running the app, contact me at ceci2205@abv.bg .

Credits
-------
The idea for this project came from a workshop in a software academy that I went to.

The web server that we made there was intended to show really vaguely the idea behind Java EE and Spring and since we 
had about 5 labs the final app was really buggy and slow, and it lacked many functionalities that are essential, such as asynchronous requests, uploading files and Dependency Injection for the MVC framework. Basically the MVC framework was a bunch of annotations with no real templating engine.

So I decided to start from the ground up and create somewhat similar application, but more functional which resulted in something that was totally different than the original project except the names of the mini applications such as Summer, Javache and so on.

