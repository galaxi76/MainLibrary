package com.soundtouch.main_library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;


/** an xml tag , includes its name, value and attributes */
public class XmlTag
{
  private String                  _tagName;
  private String                  _tagValue;
  private HashMap<String, String> _tagAttributes;
  private ArrayList<XmlTag>       _innerTags;

  void addInnerXmlTag(final XmlTag tag)
  {
    if (tag == null)
      return;
    if (_innerTags == null)
      _innerTags = new ArrayList<XmlTag>();
    _innerTags.add(tag);
  }

  /** formats the xmlTag back to its string format,including its inner tags */
  public String getStringFormat()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("<" + getTagName());
    final int numberOfAttributes = getTagAttributes() != null ? getTagAttributes().size() : 0;
    if (numberOfAttributes != 0)
      for (final Entry<String, String> attributeEntry:getTagAttributes().entrySet())
        sb.append(" " + attributeEntry.getKey() + "=\"" + attributeEntry.getValue() + "\"");
    final int numberOfInnerTags = getInnerXmlTags() != null ? getInnerXmlTags().size() : 0;
    if (numberOfInnerTags == 0)
      sb.append(" />");
    else
    {
      sb.append(numberOfAttributes == 0 ? ">" : " >");
      for (final XmlTag innerTag:getInnerXmlTags())
        sb.append(innerTag.getStringFormat());
      sb.append("</" + getTagName() + ">");
    }
    return sb.toString();
  }

  @Override
  public String toString()
  {
    return getStringFormat();
  };

  /** returns the root xml tag of the given xml resourceId , or null if not succeeded . */
  public static XmlTag getXmlRootTagOfXmlFileResourceId(final Context context, final int xmlFileResourceId)
  {
    XmlTag currentTag = null, rootTag = null;
    final Stack<XmlTag> tagsStack = new Stack<XmlTag>();
    final Resources res = context.getResources();
    final XmlResourceParser xmlParser = res.getXml(xmlFileResourceId);
    try
    {
      xmlParser.next();
      int eventType = xmlParser.getEventType();
      boolean doneParsing = false;
      while (eventType != XmlPullParser.END_DOCUMENT && !doneParsing)
      {
        switch (eventType)
        {
          case XmlPullParser.START_DOCUMENT:
            break;
          case XmlPullParser.START_TAG:
            final String xmlTagName = xmlParser.getName();
            currentTag = new XmlTag();
            if (tagsStack.isEmpty())
              rootTag = currentTag;
            tagsStack.push(currentTag);
            final int numberOfAttributes = xmlParser.getAttributeCount();
            if (numberOfAttributes > 0)
            {
              final HashMap<String, String> attributes = new HashMap<String, String>();
              for (int i = 0; i < numberOfAttributes; ++i)
              {
                final String attrName = xmlParser.getAttributeName(i);
                final String attrValue = xmlParser.getAttributeValue(i);
                attributes.put(attrName, attrValue);
              }
              currentTag.setTagAttributes(attributes);
            }
            currentTag.setTagName(xmlTagName);
            break;
          case XmlPullParser.END_TAG:
            currentTag = tagsStack.pop();
            if (!tagsStack.isEmpty())
            {
              final XmlTag parentTag = tagsStack.peek();
              parentTag.addInnerXmlTag(currentTag);
              currentTag = parentTag;
            }
            else
              doneParsing = true;
            break;
          case XmlPullParser.TEXT:
            final String tagValue = xmlParser.getText();
            if (currentTag != null)
              currentTag.setTagValue(tagValue);
            break;
        }
        eventType = xmlParser.next();
      }
    }
    catch (final Exception e)
    {
      // if something is wrong with the xml file, set result to null.
      rootTag = null;
    }
    return rootTag;
  }

  // //////////
  // getters //
  // //////////
  /** returns a hashmap of all of the tag attributes. example: <a c="d" e="f">b</a> . attributes: {{"c"="d"},{"e"="f"}} */
  public HashMap<String, String> getTagAttributes()
  {
    return _tagAttributes;
  }

  /** returns the name of the xml tag . for example : <a>b</a> . the name of the tag is "a" */
  public String getTagName()
  {
    return _tagName;
  }

  /** returns the value that is inside of the xml tag . for example : <a>b</a> . the value of the tag is "b" */
  public String getTagValue()
  {
    return _tagValue;
  }

  public ArrayList<XmlTag> getInnerXmlTags()
  {
    return _innerTags;
  }

  // //////////
  // setters //
  // //////////
  /** sets the hashmap of all of the tag attributes. example: <a c="d" e="f">b</a> . attributes: {{"c"="d"},{"e"="f"}} */
  void setTagAttributes(final HashMap<String, String> tagAttributes)
  {
    _tagAttributes = tagAttributes;
  }

  /** sets the name of the xml tag . for example : <a>b</a> . the name of the tag is "a" */
  void setTagName(final String tagName)
  {
    _tagName = tagName;
  }

  /** sets the value that is inside of the xml tag . for example : <a>b</a> . the value of the tag is "b" */
  void setTagValue(final String tagValue)
  {
    _tagValue = tagValue;
  }
}
