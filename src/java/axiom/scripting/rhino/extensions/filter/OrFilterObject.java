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
package axiom.scripting.rhino.extensions.filter;

import java.lang.reflect.Method;

import org.mozilla.javascript.*;

/**
 * The JavaScript OrFilter class, for passing in as a filter parameter to the Query API.
 * The OrFilter acts as a chain, that is, it is comprised of other filters.  The OrFilter
 * performs an OR operation on all of the filters in its chain.  For example, <br><br>
 * <code>
 * var filters = [];<br>
 * filters.push(new Filter({id: "company-picnic"}));<br>
 * filters.push(new NativeFilter("title: Company*"));<br>
 * app.getObjects("Post", new OrFilter(filters));<br>
 * </code>
 * <br>
 * 
 * This will return all Axiom Objects of the Post prototype, with an id exactly matching 
 * "company-picnic", or a title that starts with "Company".
 * 
 * @jsconstructor OrFilter
 * @param {Array} filters An Array of the filter objects that are to be combined by an 
 * 						  OR operation
 */
public class OrFilterObject extends OpFilterObject {
    
    protected OrFilterObject() {
        super();
    }

    protected OrFilterObject(final Object[] args) throws Exception {
        super();
        final int length = args.length;
        if ((length == 1 && args[0] instanceof Scriptable 
                && !((Scriptable) args[0]).getClassName().equals("String"))
             || (length == 2 && args[0] instanceof Scriptable 
                && !((Scriptable) args[0]).getClassName().equals("String")
                && args[1] instanceof Boolean)) {
            Scriptable s = (Scriptable) args[0];
            final int arrlen = s.getIds().length;
            filters = new IFilter[arrlen];
            for (int i = 0; i < arrlen; i++) {
                filters[i] = (IFilter) s.get(i, s);
            }
            if (length == 2) {
                this.cached = ((Boolean) args[1]).booleanValue();
            }
        } else {
            filters = new IFilter[length];
            for (int i = 0; i < length; i++) {
                if (i == length - 1 && args[i] instanceof Boolean) {
                    this.cached = ((Boolean) args[i]).booleanValue();
                    continue;
                }
                if (args[i] instanceof IFilter)  {
                    filters[i] = (IFilter) args[i];
                } else if (args[i] instanceof String) {
                    filters[i] = new NativeFilterObject(new Object[] {args[i]});
                } else if (args[i] instanceof Scriptable) {
                    Scriptable s = (Scriptable) args[i];
                    if (s.getClassName().equals("String")) {
                        filters[i] = new NativeFilterObject(new Object[] {s});
                    } else {
                        filters[i] = new FilterObject(s, null, null);
                    }
                } else {
                    throw new Exception("Parameter " + (i+1) + " to the OrFilter constructor is not a valid filter.");
                }
            }
        }
    }
    
    public String getClassName() {
        return "OrFilter";
    }
    
    public String toString() {
        return "[OrFilter]";
    }
    
    public static OrFilterObject filterObjCtor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) 
    throws Exception {
        if (args.length == 0) {
            throw new Exception("No arguments specified to the OrFilter constructor.");
        }
        
        return new OrFilterObject(args);
    }
    
    public static void init(Scriptable scope) {
        Method[] methods = OrFilterObject.class.getMethods();
        ScriptableObject proto = new OrFilterObject();
        proto.setPrototype(getObjectPrototype(scope));
        final int attributes = READONLY | DONTENUM | PERMANENT;
        
        final int length = methods.length;
        for (int i = 0; i < length; i++) {
            String methodName = methods[i].getName();
            
            if ("filterObjCtor".equals(methodName)) {
                FunctionObject ctor = new FunctionObject("OrFilter", methods[i], scope);
                ctor.addAsConstructor(scope, proto);
            } else if (methodName.startsWith("jsFunction_")) {
                methodName = methodName.substring(11);
                FunctionObject func = new FunctionObject(methodName, methods[i], proto);
                proto.defineProperty(methodName, func, attributes);
            }
        }
    }
    
}