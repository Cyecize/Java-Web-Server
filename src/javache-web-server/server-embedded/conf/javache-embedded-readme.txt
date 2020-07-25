Just import the jar file into your project and run this code in your main method.

public static void main(String[] args) throws Exception {
        JavacheEmbedded.startServer(8000, StartUp.class);
    }
	

if you are running a Summer MVC app, then your whole startup class should be like this:

public class StartUp extends DispatcherSolet {

    public static void main(String[] args) throws Exception {
        JavacheEmbedded.startServer(8000, StartUp.class);
    }
}

For more details, read the documentation.