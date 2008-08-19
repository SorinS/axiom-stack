/*
 * Axiom Stack Web Application Framework
 * Copyright (C) 2008  Axiom Software Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Axiom Software Inc., 11480 Commerce Park Drive, Third Floor, Reston, VA 20191 USA
 * email: info@axiomsoftwareinc.com
 */
package axiom.scripting.rhino.extensions;

import java.io.*;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.*;

import org.mozilla.javascript.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class DOMParser extends ScriptableObject {
    
    public static final String[][] XHTML_ENTITIES = new String[][] {
        {"nbsp","\u00A0"},
        {"iexcl","\u00A1"},
        {"cent","\u00A2"},
        {"pound","\u00A3"},
        {"curren","\u00A4"},
        {"yen","\u00A5"},
        {"brvbar","\u00A6"},
        {"sect","\u00A7"},
        {"uml","\u00A8"},
        {"copy","\u00A9"},
        {"ordf","\u00AA"},
        {"laquo","\u00AB"},
        {"not","\u00AC"},
        {"shy","\u00AD"},
        {"reg","\u00AE"},
        {"macr","\u00AF"},
        {"deg","\u00B0"},
        {"plusmn","\u00B1"},
        {"sup2","\u00B2"},
        {"sup3","\u00B3"},
        {"acute","\u00B4"},
        {"micro","\u00B5"},
        {"para","\u00B6"},
        {"middot","\u00B7"},
        {"cedil","\u00B8"},
        {"sup1","\u00B9"},
        {"ordm","\u00BA"},
        {"raquo","\u00BB"},
        {"frac14","\u00BC"},
        {"frac12","\u00BD"},
        {"frac34","\u00BE"},
        {"iquest","\u00BF"},
        {"Agrave","\u00C0"},
        {"Aacute","\u00C1"},
        {"Acirc","\u00C2"},
        {"Atilde","\u00C3"},
        {"Auml","\u00C4"},
        {"Aring","\u00C5"},
        {"AElig","\u00C6"},
        {"Ccedil","\u00C7"},
        {"Egrave","\u00C8"},
        {"Eacute","\u00C9"},
        {"Ecirc","\u00CA"},
        {"Euml","\u00CB"},
        {"Igrave","\u00CC"},
        {"Iacute","\u00CD"},
        {"Icirc","\u00CE"},
        {"Iuml","\u00CF"},
        {"ETH","\u00D0"},
        {"Ntilde","\u00D1"},
        {"Ograve","\u00D2"},
        {"Oacute","\u00D3"},
        {"Ocirc","\u00D4"},
        {"Otilde","\u00D5"},
        {"Ouml","\u00D6"},
        {"times","\u00D7"},
        {"Oslash","\u00D8"},
        {"Ugrave","\u00D9"},
        {"Uacute","\u00DA"},
        {"Ucirc","\u00DB"},
        {"Uuml","\u00DC"},
        {"Yacute","\u00DD"},
        {"THORN","\u00DE"},
        {"szlig","\u00DF"},
        {"agrave","\u00E0"},
        {"aacute","\u00E1"},
        {"acirc","\u00E2"},
        {"atilde","\u00E3"},
        {"auml","\u00E4"},
        {"aring","\u00E5"},
        {"aelig","\u00E6"},
        {"ccedil","\u00E7"},
        {"egrave","\u00E8"},
        {"eacute","\u00E9"},
        {"ecirc","\u00EA"},
        {"euml","\u00EB"},
        {"igrave","\u00EC"},
        {"iacute","\u00ED"},
        {"icirc","\u00EE"},
        {"iuml","\u00EF"},
        {"eth","\u00F0"},
        {"ntilde","\u00F1"},
        {"ograve","\u00F2"},
        {"oacute","\u00F3"},
        {"ocirc","\u00F4"},
        {"otilde","\u00F5"},
        {"ouml","\u00F6"},
        {"divide","\u00F7"},
        {"oslash","\u00F8"},
        {"ugrave","\u00F9"},
        {"uacute","\u00FA"},
        {"ucirc","\u00FB"},
        {"uuml","\u00FC"},
        {"yacute","\u00FD"},
        {"thorn","\u00FE"},
        {"yuml","\u00FF"},
        {"quot","\""},
        {"apos","\u0027"},
        {"OElig","\u0152"},
        {"oelig","\u0153"},
        {"Scaron","\u0160"},
        {"scaron","\u0161"},
        {"Yuml","\u0178"},
        {"circ","\u02C6"},
        {"tilde","\u02DC"},
        {"ensp","\u2002"},
        {"emsp","\u2003"},
        {"thinsp","\u2009"},
        {"zwnj","\u200C"},
        {"zwj","\u200D"},
        {"lrm","\u200E"},
        {"rlm","\u200F"},
        {"ndash","\u2013"},
        {"mdash","\u2014"},
        {"lsquo","\u2018"},
        {"rsquo","\u2019"},
        {"sbquo","\u201A"},
        {"ldquo","\u201C"},
        {"rdquo","\u201D"},
        {"bdquo","\u201E"},
        {"dagger","\u2020"},
        {"Dagger","\u2021"},
        {"permil","\u2030"},
        {"lsaquo","\u2039"},
        {"rsaquo","\u203A"},
        {"euro","\u20AC"},
        {"fnof","\u0192"},
        {"Alpha","\u0391"},
        {"Beta","\u0392"},
        {"Gamma","\u0393"},
        {"Delta","\u0394"},
        {"Epsilon","\u0395"},
        {"Zeta","\u0396"},
        {"Eta","\u0397"},
        {"Theta","\u0398"},
        {"Iota","\u0399"},
        {"Kappa","\u039A"},
        {"Lambda","\u039B"},
        {"Mu","\u039C"},
        {"Nu","\u039D"},
        {"Xi","\u039E"},
        {"Omicron","\u039F"},
        {"Pi","\u03A0"},
        {"Rho","\u03A1"},
        {"Sigma","\u03A3"},
        {"Tau","\u03A4"},
        {"Upsilon","\u03A5"},
        {"Phi","\u03A6"},
        {"Chi","\u03A7"},
        {"Psi","\u03A8"},
        {"Omega","\u03A9"},
        {"alpha","\u03B1"},
        {"beta","\u03B2"},
        {"gamma","\u03B3"},
        {"delta","\u03B4"},
        {"epsilon","\u03B5"},
        {"zeta","\u03B6"},
        {"eta","\u03B7"},
        {"theta","\u03B8"},
        {"iota","\u03B9"},
        {"kappa","\u03BA"},
        {"lambda","\u03BB"},
        {"mu","\u03BC"},
        {"nu","\u03BD"},
        {"xi","\u03BE"},
        {"omicron","\u03BF"},
        {"pi","\u03C0"},
        {"rho","\u03C1"},
        {"sigmaf","\u03C2"},
        {"sigma","\u03C3"},
        {"tau","\u03C4"},
        {"upsilon","\u03C5"},
        {"phi","\u03C6"},
        {"chi","\u03C7"},
        {"psi","\u03C8"},
        {"omega","\u03C9"},
        {"thetasym","\u03D1"},
        {"upsih","\u03D2"},
        {"piv","\u03D6"},
        {"bull","\u2022"},
        {"hellip","\u2026"},
        {"prime","\u2032"},
        {"Prime","\u2033"},
        {"oline","\u203E"},
        {"frasl","\u2044"},
        {"weierp","\u2118"},
        {"image","\u2111"},
        {"real","\u211C"},
        {"trade","\u2122"},
        {"alefsym","\u2135"},
        {"larr","\u2190"},
        {"uarr","\u2191"},
        {"rarr","\u2192"},
        {"darr","\u2193"},
        {"harr","\u2194"},
        {"crarr","\u21B5"},
        {"lArr","\u21D0"},
        {"uArr","\u21D1"},
        {"rArr","\u21D2"},
        {"dArr","\u21D3"},
        {"hArr","\u21D4"},
        {"forall","\u2200"},
        {"part","\u2202"},
        {"exist","\u2203"},
        {"empty","\u2205"},
        {"nabla","\u2207"},
        {"isin","\u2208"},
        {"notin","\u2209"},
        {"ni","\u220B"},
        {"prod","\u220F"},
        {"sum","\u2211"},
        {"minus","\u2212"},
        {"lowast","\u2217"},
        {"radic","\u221A"},
        {"prop","\u221D"},
        {"infin","\u221E"},
        {"ang","\u2220"},
        {"and","\u2227"},
        {"or","\u2228"},
        {"cap","\u2229"},
        {"cup","\u222A"},
        {"int","\u222B"},
        {"there4","\u2234"},
        {"sim","\u223C"},
        {"cong","\u2245"},
        {"asymp","\u2248"},
        {"ne","\u2260"},
        {"equiv","\u2261"},
        {"le","\u2264"},
        {"ge","\u2265"},
        {"sub","\u2282"},
        {"sup","\u2283"},
        {"nsub","\u2284"},
        {"sube","\u2286"},
        {"supe","\u2287"},
        {"oplus","\u2295"},
        {"otimes","\u2297"},
        {"perp","\u22A5"},
        {"sdot","\u22C5"},
        {"lceil","\u2308"},
        {"rceil","\u2309"},
        {"lfloor","\u230A"},
        {"rfloor","\u230B"},
        {"lang","\u2329"},
        {"rang","\u232A"},
        {"loz","\u25CA"},
        {"spades","\u2660"},
        {"clubs","\u2663"},
        {"hearts","\u2665"},
        {"diams","\u2666"}
    };
    
    private static final HashMap ENTITIES_MAP = new HashMap();
    
    static {
        final int length = XHTML_ENTITIES.length;
        for (int i = 0; i < length; i++) {
            ENTITIES_MAP.put(XHTML_ENTITIES[i][0], XHTML_ENTITIES[i][1]);
        }
    }
    
    public String getClassName() {
        return "DOMParser";
    }
    
    public String toString() {
        return "[DOMParser]";
    }
    
    public Object parse(String xml) throws Exception {
        return parseXml(xml);
    }
    
    public static Object parseXmlFragment(String fragment) throws Exception {
        fragment = replaceEntitiesWithChars(fragment);
    	fragment = "<fragment>" + fragment + "</fragment>";
        Document doc = null;
        com.sun.org.apache.xerces.internal.parsers.DOMParser parser =
            new com.sun.org.apache.xerces.internal.parsers.DOMParser();
        DocumentFragment docfrag = null;
        
        try {
            parser.parse(new InputSource(new StringReader(fragment)));
            doc = parser.getDocument();
            org.w3c.dom.Node node = doc.importNode(doc.getDocumentElement(), true);
            docfrag = doc.createDocumentFragment();
            while (node.hasChildNodes()) {
                docfrag.appendChild(node.removeChild(node.getFirstChild()));
            }
        } finally {
            parser = null;
        }
        
        return docfrag;
    }
    
    public static Object parseXmlFragment(InputSource source) throws Exception {
        Document doc = null;
        com.sun.org.apache.xerces.internal.parsers.DOMParser parser =
            new com.sun.org.apache.xerces.internal.parsers.DOMParser();
        DocumentFragment docfrag = null;
        
        try {
            parser.parse(source);
            doc = parser.getDocument();
            org.w3c.dom.Node node = doc.importNode(doc.getDocumentElement(), true);
            docfrag = doc.createDocumentFragment();
            while (node.hasChildNodes()) {
                docfrag.appendChild(node.removeChild(node.getFirstChild()));
            }
        } finally {
            parser = null;
        }
        
        return docfrag;
    }
  

    public static Object parseXml(String xml) throws Exception {
        Document doc = null;
        com.sun.org.apache.xerces.internal.parsers.DOMParser parser =
            new com.sun.org.apache.xerces.internal.parsers.DOMParser();
        
        try {
            xml = replaceEntitiesWithChars(xml);
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parser.parse(new org.xml.sax.InputSource(new StringReader(xml)));
            doc = parser.getDocument();
        } finally {
            parser = null;
        }
        
        return doc;
    }
    
    public static DOMParser DOMParserObjCtor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        return new axiom.scripting.rhino.extensions.DOMParser();
    }
    
    public static void init(Scriptable scope) {
        Method[] methods = DOMParser.class.getDeclaredMethods();
        ScriptableObject proto = new DOMParser();
        proto.setPrototype(getObjectPrototype(scope));
        Member ctorMember = null;
        
        final int length = methods.length;
        for (int i = 0; i < length; i++) {
            if ("DOMParserObjCtor".equals(methods[i].getName())) {
                ctorMember = methods[i];
                break;
            }
        }
        
        FunctionObject ctor = new FunctionObject("DOMParser", ctorMember, scope);
        ctor.addAsConstructor(scope, proto);
        String[] domParserFuncs = { "parse" };
        
        try {
            proto.defineFunctionProperties(domParserFuncs, DOMParser.class, 0);
        } catch (Exception ignore) {
            System.err.println ("Error defining function properties: "+ignore);
        }
    }
    
    public static String replaceEntitiesWithChars(String xml) {
        Pattern pattern = Pattern.compile("&([a-z0-9A-Z]+);");
        Matcher matcher = pattern.matcher(xml);
        List<MatchResult> results = new ArrayList<MatchResult>();
        while (matcher.find()) {
            results.add(matcher.toMatchResult());
        }
        
        final int size = results.size();
        String[] entities = new String[size];
        for (int i = 0; i < size; i++) {
            entities[i] = results.get(i).group();
        }
        
        for (int i = 0; i < size; i++) {
            String ucode = (String) ENTITIES_MAP.get(entities[i].substring(1, entities[i].length()-1));
            if (ucode != null) {
               xml = xml.replaceAll(entities[i], ucode);
            }
        }
        
        return xml;
    }
    
} 