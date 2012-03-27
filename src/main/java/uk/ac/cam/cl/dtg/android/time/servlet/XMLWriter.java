package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Simple XML Utility class 
 * 
 * @author acr31
 *
 */
public class XMLWriter {

	private XMLStreamWriter w;
	private LinkedList<String> stack;
	private Writer result;

	public XMLWriter() {
		this(new StringWriter());
	}

	public XMLWriter(Writer result) {
		this.result = result;
		try {
			this.w = XMLOutputFactory.newInstance().createXMLStreamWriter(
					result);
		} catch (XMLStreamException e) {
			throw new Error("Failed to construct XMLStreamWriter", e);
		} catch (FactoryConfigurationError e) {
			throw new Error("Failed to create XMLOutputFactory", e);
		}
		stack = new LinkedList<String>();
	}

	public void writeAttributes(String... attributes) {
		try {
			if (attributes != null) {
				for (int i = 0; i < attributes.length; i += 2) {
					if (attributes[i + 1] != null) {
						String key = attributes[i];
						String value = attributes[i + 1];
						w.writeAttribute(key, value);
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new Error("Failed to write attributes", e);
		}
	}

	public XMLWriter raw(String xml) {
		try {
			w.writeCharacters(xml);
		} catch (XMLStreamException e) {
			throw new Error("Error in raw");
		}
		return this;
	}

	public XMLWriter open(String name, String... attributes) {
		try {
			w.writeStartElement(name);
			stack.addLast(name);
			writeAttributes(attributes);
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in open " + name, e);
		}
	}

	public XMLWriter empty(String name, String... attributes) {
		try {
			w.writeStartElement(name);
			writeAttributes(attributes);
			w.writeEndElement();
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in empty " + name, e);
		}
	}

	public XMLWriter dtd(String name) {
		try {
			w.writeDTD(name);
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in dtd " + name, e);
		}
	}

	public XMLWriter pi(String name, Object... attributes) {
		try {
			String data = "";
			for (int i = 0; i < attributes.length; i += 2) {
				if (attributes[i + 1] != null) {
					data += attributes[i].toString() + "=\""
							+ attributes[i + 1].toString() + "\" ";
				}
			}
			if (data.equals("")) {
				w.writeProcessingInstruction(name);
			} else {
				w.writeProcessingInstruction(name, data);
			}
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in pi " + name);
		}
	}

	public XMLWriter text(String name) {
		try {
			w.writeCharacters(name);
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in text " + name);
		}
	}

	public XMLWriter close(String tag) {
		try {
			do {
				w.writeEndElement();
			} while (!stack.removeLast().equals(tag));
			return this;
		} catch (XMLStreamException e) {
			throw new Error("Error in close " + tag);
		}
	}

	public XMLWriter textElement(String element, String text,
			String... attributes) {
		try {
			w.writeStartElement(element);
			writeAttributes(attributes);
			w.writeCharacters(text);
			w.writeEndElement();
			return this;
		} catch (XMLStreamException e) {
			throw new Error(e);
		}
	}

	public static String dtdAttlist(String elementName, Object... attributes) {
		StringBuffer result = new StringBuffer();
		for (Object o : attributes) {
			result.append(" " + o + " CDATA #REQUIRED ");
		}
		return "<!ATTLIST " + elementName + result + ">";
	}

	public static String dtdElement(String elementName, Object... children) {
		StringBuffer result = new StringBuffer();
		if (children == null || children.length == 0) {
			return "<!ELEMENT " + elementName + " (#PCDATA)>";
		}
		for (Object o : children) {
			if (o instanceof Class<?>) {
				result.append("|" + ((Class<?>) o).getSimpleName());
			} else {
				result.append("|" + o);
			}
		}
		return "<!ELEMENT " + elementName + " (" + result.substring(1) + ")*>";
	}

	public void flush() {
		try {
			w.flush();
		} catch (XMLStreamException e) {
			throw new Error(e);
		}
	}

	public String toString() {
		flush();
		return result.toString();
	}
}
