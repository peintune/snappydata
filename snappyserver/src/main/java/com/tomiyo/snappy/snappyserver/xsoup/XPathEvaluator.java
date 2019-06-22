package com.tomiyo.snappy.snappyserver.xsoup;

import org.jsoup.nodes.Element;

/**
 * @author code4crafter@gmail.com
 */
public interface XPathEvaluator {

    XElements evaluate(Element element);

    XElements evaluate(String html);

}
