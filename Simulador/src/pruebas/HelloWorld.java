package pruebas;

/**
 * A simple component for getting familiar with component writing.
 * The component contains a "greetings" port which responds with "Hello World"
 * when receiving greetings "hi".
 */
public class HelloWorld extends drcl.comp.Component
{
	drcl.comp.Port greetingsPort;
	drcl.comp.Port backPort; //^^

	public HelloWorld()
	{
		super("hello");
		// TODO kljlk
		// TODO
		// esto añade un puerto a los puertos controlados por el componente
		// creado. es como una especie de inicializacion. la variable solo 
		// guarda un puerto
		
		greetingsPort = addPort("greetings");
		backPort = addPort("backdoor");
	}

	/**
	 * The method is invoked when data is received at one of its ports.
	 * <code>inPort_</code> should be the greetings port created in the constructor.  
	 */
	// esto es lo que procesa cuando te llega algo por el puerto
	// los datos en este caso son tratados 
	public void process(Object data_, drcl.comp.Port inPort_)
	{
		if (inPort_ == greetingsPort && ((String)data_).toLowerCase().startsWith("hi")) 
			inPort_.doSending("Hello World!!!\n");
		if (inPort_ == backPort) 
			inPort_.doSending("que haces muchachote, no entres por la puerta de atras");
	}
}
