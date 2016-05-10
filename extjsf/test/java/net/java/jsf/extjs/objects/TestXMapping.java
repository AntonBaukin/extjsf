package net.java.jsf.extjs.objects;

/* Spring Framework */

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.SpringPoint;


/**
 * Test of Eclipse MOXy JAXB mapping of the
 * sample objects to XML and JSON.
 *
 * @author anton.baukin@gmail.com.
 */
public class TestXMapping
{
	@org.junit.Test
	public void testInit()
	{
		String x = XPoint.xml().
		  write(new SampleObject());

		String j = XPoint.json().
		  write(new SampleObject());

		EX.assertx(!x.trim().isEmpty());
		EX.assertx(!j.trim().isEmpty());
	}

	@org.junit.Test
	public void testEmpty()
	{
		//~: XML

		String X =
		  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tst:object xmlns:tst=\"urn:extjs.jsf" +
		  ".java.net:test\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		  "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>";

		String x = XPoint.xml().
		  write(new SampleObject());

		EX.assertx(eqx(X, x));


		//~: JSON

		String J = "{}";

		String j = XPoint.json().
		  write(new SampleObject());

		EX.assertx(eqj(J, j));
	}

	@org.junit.Test
	public void testSimple()
	{
		//~: sample object

		SampleObject o = new SampleObject();

		o.setId("abc");
		o.setName("Test name");
		o.setTotal(new BigDecimal("123.45060"));


		//~: XML

		String X =
		  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tst:object xmlns:tst=\"urn:extjs.jsf" +
		  ".java.net:test\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		  "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" id=\"abc\"><name>Test " +
		  "name</name><total>123.45060</total></tst:object>";

		String x = XPoint.xml().write(o);
		EX.assertx(eqx(X, x));


		//~: JSON

		String J = "{\"id\":\"abc\",\"name\":\"Test name\",\"total\":\"123.45060\"}";

		String j = XPoint.json().write(o);
		EX.assertx(eqj(J, j));
	}

	@org.junit.Test
	public void testFull()
	{
		//~: XML

		String X =
		  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tst:object xmlns:tst=\"urn:extjs.jsf" +
		  ".java.net:test\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		  "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" id=\"abc\"><name>Test " +
		  "object</name><total>123.45060</total><best><code>0</code><volume>19" +
		  ".95</volume></best><items><item><code>1</code><volume>1" +
		  ".33</volume></item><item><code>2</code><volume>2" +
		  ".66</volume></item><item><code>3</code><volume>3" +
		  ".99</volume></item><item><code>4</code><volume>5" +
		  ".32</volume></item><item><code>5</code><volume>6" +
		  ".65</volume></item></items></tst:object>";

		String x = XPoint.xml().write(sample());
		EX.assertx(eqx(X, x));


		//~: JSON

		String J =
		  "{\"id\":\"abc\",\"name\":\"Test object\",\"total\":\"123.45060\"," +
		  "\"best\":{\"code\":\"0\",\"volume\":\"19.95\"},\"items\":[{\"code\":\"1\"," +
		  "\"volume\":\"1.33\"},{\"code\":\"2\",\"volume\":\"2.66\"},{\"code\":\"3\"," +
		  "\"volume\":\"3.99\"},{\"code\":\"4\",\"volume\":\"5.32\"},{\"code\":\"5\"," +
		  "\"volume\":\"6.65\"}]}";

		String j = XPoint.json().write(sample());
		EX.assertx(eqj(J, j));
	}

	@org.junit.Test
	public void testRestore()
	{
		String       x = XPoint.xml().write(sample());
		SampleObject o = XPoint.xml().read(x, SampleObject.class);
		EX.assertx(sample().equals(o));

		String       j = XPoint.json().write(sample());
		o = XPoint.json().read(j, SampleObject.class);
		EX.assertx(sample().equals(o));
	}


	/* Full Test Sample */

	private SampleObject sample()
	{
		SampleObject o = new SampleObject();

		o.setId("abc");
		o.setName("Test object");
		o.setTotal(new BigDecimal("123.45060"));

		SampleItem i = new SampleItem();
		i.setCode("0");
		i.setVolume(new BigDecimal("19.95"));
		o.setBest(i);

		ArrayList<SampleItem> is = new ArrayList<>();
		o.setItems(is);

		for(int k = 1;(k <= 5);k++)
		{
			i = new SampleItem();
			i.setCode(""+k);
			i.setVolume(new BigDecimal("1.33").multiply(new BigDecimal(k)));
			is.add(i);
		}

		return o;
	}

	private boolean eqx(String t, String x)
	{
		return t.equals(x);
	}

	private boolean eqj(String t, String j)
	{
		return t.equals(j);
	}


	/* Spring Activation */

	@org.junit.Before
	public void startSpring()
	{
		//~: create test Spring context
		SpringPoint.getInstance().setTestContext(
		  new ClassPathXmlApplicationContext("applicationContext.xml"));
	}

	@org.junit.After
	public void stopSpring()
	{
		ConfigurableApplicationContext ctx =
		  (ConfigurableApplicationContext)
		    SpringPoint.getInstance().getSpringContext();

		//~: close the context
		SpringPoint.getInstance().setTestContext(null);
		ctx.close();
	}
}