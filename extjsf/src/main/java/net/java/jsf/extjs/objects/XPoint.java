package net.java.jsf.extjs.objects;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Point to access procedures of writing and
 * reading objects marked by JAXB annotations
 * to XML or JSON and back.
 *
 * @author anton.baukin@gmail.com
 */
public class XPoint
{
	/* Singleton */

	public static XPoint getInstance()
	{
		return bean(XPoint.class);
	}


	/* X-Point (static) interface */

	public static XStreamer xml()
	{
		return EX.assertn(getInstance().streamerXML,
		  "XML X-Streamer strategy is not specified!"
		);
	}

	public static XStreamer json()
	{
		return EX.assertn(getInstance().streamerJSON,
		  "JSON X-Streamer strategy is not specified!"
		);
	}


	/* X-Point (bean) interface */

	public void setStreamerXML(XStreamer streamerXML)
	{
		this.streamerXML = streamerXML;
	}

	private XStreamer streamerXML;

	public void setStreamerJSON(XStreamer streamerJSON)
	{
		this.streamerJSON = streamerJSON;
	}

	private XStreamer streamerJSON;
}