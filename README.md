

What is Java Web Server
-----------------------
Java Web Server consists of multiple applications and APIs that are all custom implemented.
* Apache Tomcat -> Javache Web Server
* TomEE -> Javache Embedded
* Catalina -> Broccolina
* Coyote -> Toyote
* Spring MVC -> Summer Framework
* Custom Http API
* Custom Servlet API -> Solet API

The goal
-------
Create a simplified but still functional web server platform and by doing that also help me and
visitors better understand how things such as IOC, HTTP and Resource handling work under the hood.

Main Functionalities
-------------------
Similar to  Tomcat, Javache can
  * Put websites in a webapps (name of the folder is configurable) folder, extract them and then run them.

  * Have multiple resource handlers (Broccolina and Toyote) just by implementing ResourceHandler interface

Broccolina and Toyote jar files are placed in a folder where javache will read and execute them upon running.

Summer MVC is a fully custom MVC framework that has a templating engine, Dependency Container, Path variables, Binding Models, Validation, Security, Interceptors, Custom data adapters
and more.

Multipart encoding is also supported.

Embedded Web Server
-------------------
Javache Embedded is an embedded version of Javache web server, which provides better debugging and much faster build times 
while developing. Javache Embedded can also be used in production.

Another benefit of the embedded server is that it can be used to create desktop applications with html, css and javaScript.
For example we can have an embedded browser (CEF) that will be in the same app and it will browse on localhost and the server
behind will act as it is a normal browser and will return views or JSON data. This is a much better approach for creating desktop applications for people that are more familiar with HTML rather than Java FX / Swing.

Technologies used
---------------------
	* Java 11
	
	* Maven
	
	* nio-multipart-parser used in Toyote for parsing multipart requests.
	
	* Gson (com.google.code.gson) used in SummerMVC.
	
	* Apache Tika used in Toyote for detecting media types.
	
	* jTwig (org.jtwig) used as a templating engine in SummerMVC.
	
[Magic-IoC-Container](https://github.com/Cyecize/Magic-IoC-Container) - Independent Depenency Container library that is used by javache since version 1.3. I have the code in my profile and a video on my YouTube channel demonstrating how to build one.
	
How to run the app?
------------------
First, extract the files from the file in Examples/00JavacheLatest.zip.
By default you need to put your app in the 'webapps' directory.

Your app has to be .jar and has to be structured like so:
* classes - put the compiled output there (classes, resources)
* lib - you can put the .jar libraries here or put them instead in javache's lib folder.

To better understand, you can extract some of the provided example applications and/or read the documentation inside Documentation folder.

In the exams folder you can find a working javache demo with everything set up.
You just need to run a command.

More info
-------------
If you are having trouble running the app, contact me at ceci2205@abv.bg .

Credits
-------
The idea for this project came from a workshop in a software academy that I went to.

The web server that we made there was intended to show really vaguely the idea behind web servers and how they operate.
had about 5 labs the final app was really buggy and slow, and it lacked many functionalities that are essential, such as asynchronous requests, uploading files, dependency injection, template engine, exception handling, security, interceptors and basically the whole app was really primitive.

So I decided to start from the ground up and create somewhat similar application, but more functional which resulted in something that was totally different than the original project except the names of the mini applications such as Summer, Javache and so on.

