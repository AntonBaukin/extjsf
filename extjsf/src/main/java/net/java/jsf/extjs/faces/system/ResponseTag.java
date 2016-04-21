package net.java.jsf.extjs.faces.system;

/* Java */

import java.util.Map;

/* JavaServer Faces */

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.RequestPoint;


/**
 * General component to handle all the tags
 * of response.xsd related tag library.
 *
 * @author anton.baukin@gmail.com.
 */
public class ResponseTag extends TagHandler
{
	public ResponseTag(TagConfig config)
	{
		super(config);
	}


	/* Tag Handler */

	public void apply(FaceletContext ctx, UIComponent parent)
	  throws java.io.IOException
	{
		//~: search for the same tag in the parent
		for(Object x : parent.getChildren())
			if((x instanceof UITag) && tagId.equals(((UITag)x).tagId))
				return;

		//~: create response tag
		UITag tag = new UITag(this.tag, this.tagId);

		//~: write the attributes
		for(TagAttribute a : this.tag.getAttributes().getAll())
			tag.getAttributes().put(a.getLocalName(), a.getValue(ctx));

		//~: insert response tag
		parent.getChildren().add(tag);

		//~: call the next handler deeply
		nextHandler.apply(ctx, tag);
	}


	/* Response Tag */

	public static class UITag extends UIComponentBase
	{
		public UITag(Tag tag, String tagId)
		{
			this.tag = tag;
			this.tagId = tagId;
		}

		public final Tag    tag;
		public final String tagId;


		/* UI Component */

		public String      getFamily()
		{
			return "net.java.jsf.extjs.faces.Response";
		}

		public void        encodeBegin(FacesContext ctx)
		  throws java.io.IOException
		{
			ResponseWriter w = ctx.getResponseWriter();
			if(!isRendered()) return;

			//~: open tag name
			w.write('\n');
			w.startElement(tag.getLocalName(), this);

			//?: <response>
			if("response".equals(tag.getLocalName()))
			{
				String  NS = "http://extjs.jsf.java.net/response";
				String XSI = "http://www.w3.org/2001/XMLSchema-instance";
				String slo = RequestPoint.formAbsoluteURL(
				  "/resources/response.xsd", false);

				w.writeAttribute("xmlns", NS, null);
				w.writeAttribute("xmlns:xsi", XSI, null);
				w.writeAttribute("xsi:schemaLocation", slo, null);
			}

			//?: <script>
			if("script".equals(tag.getLocalName()))
				if(!getAttributes().containsKey("type"))
					w.writeAttribute("type", "text/javascript", null);

			//~: write all the attributes
			for(Map.Entry<String, Object> a : getAttributes().entrySet())
				w.writeAttribute(a.getKey(), a.getValue(), null);
		}

		public void        encodeEnd(FacesContext ctx)
		  throws java.io.IOException
		{
			ResponseWriter w = ctx.getResponseWriter();
			if(!isRendered()) return;

			//~: close tag name
			w.endElement(tag.getLocalName());
		}

		protected Renderer getRenderer(FacesContext context)
		{
			return null;
		}
	}
}