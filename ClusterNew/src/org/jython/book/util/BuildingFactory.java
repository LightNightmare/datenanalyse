/**
 *
 * Object Factory that is used to coerce python module into a
 * Java class
 */
package org.jython.book.util;

import org.jython.book.interfaces.BuildingType;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class BuildingFactory {

    private PyObject buildingClass;

    /**
     * Create a new PythonInterpreter object, then use it to
     * execute some python code. In this case, we want to
     * import the python module that we will coerce.
     *
     * Once the module is imported than we obtain a reference to
     * it and assign the reference to a Java variable
     */

    public BuildingFactory() {
        PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.exec("import sys");
        PySystemState sys = Py.getSystemState(); 
        sys.path.clear();
        sys.path.append(new PyString("C:\\Work\\Repository\\ClusterJPNew\\Cluster\\src\\org\\jython\\book\\util"));
        interpreter.exec("import sys.path");
        interpreter.exec("import urllib.parse");
        interpreter.exec("import urllib.request");
        //interpreter.exec("print sys.path");
        interpreter.exec("from Building import Building");
        buildingClass = interpreter.get("Building");
        
        
    }

    /**
     * The create method is responsible for performing the actual
     * coercion of the referenced python module into Java bytecode
     */

    public BuildingType create (String query) {

        PyObject buildingObject = buildingClass.__call__(new PyString(query));
        return (BuildingType)buildingObject.__tojava__(BuildingType.class);
    }

}