
package ca.sqlpower.architect.olap;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MondrianModel {

/** A schema is a collection of cubes and virtual cubes.
            It can also contain shared dimensions (for use by those
            cubes), named sets, roles, and declarations of
            user-defined functions. */
public static class Schema extends OLAPObject {
    
    /**
     * Creates a new Schema with all attributes
     * set to their defaults.
     */
    public Schema() {
    }
    

    /** Name of this schema */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Label for the measures dimension.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ measuresCaption;
    
    public String /* */ getMeasuresCaption() {
        return measuresCaption;
    }
    
    public void setMeasuresCaption(String /* */ newval) {
        String /* */ oldval = measuresCaption;
        measuresCaption = newval;
        pcs.firePropertyChange("measuresCaption", oldval, newval);
    }

    /** The name of the default role for connections to this schema */
    private String /* */ defaultRole;
    
    public String /* */ getDefaultRole() {
        return defaultRole;
    }
    
    public void setDefaultRole(String /* */ newval) {
        String /* */ oldval = defaultRole;
        defaultRole = newval;
        pcs.firePropertyChange("defaultRole", oldval, newval);
    }

    /** 
                This schema's parameter definitions.
             */
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                This schema's parameter definitions.
            
     */
    public void addParameter(int pos, Parameter newChild) {
        parameters.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Parameter.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                This schema's parameter definitions.
             */
    public void addParameter(Parameter newChild) {
        addParameter(parameters.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeParameter(Parameter removeChild) {
        int pos = parameters.indexOf(removeChild);
        if (pos != -1) {
            removeParameter(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Parameter removeParameter(int pos) {
        Parameter removedItem = parameters.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Parameter.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    

    /** 
                Shared dimensions in this schema.
             */
    private final List<Dimension> dimensions = new ArrayList<Dimension>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Shared dimensions in this schema.
            
     */
    public void addDimension(int pos, Dimension newChild) {
        dimensions.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Dimension.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Shared dimensions in this schema.
             */
    public void addDimension(Dimension newChild) {
        addDimension(dimensions.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeDimension(Dimension removeChild) {
        int pos = dimensions.indexOf(removeChild);
        if (pos != -1) {
            removeDimension(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Dimension removeDimension(int pos) {
        Dimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Dimension.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Dimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }
    

    /** 
                Cubes in this schema.
             */
    private final List<Cube> cubes = new ArrayList<Cube>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Cubes in this schema.
            
     */
    public void addCube(int pos, Cube newChild) {
        cubes.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Cube.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Cubes in this schema.
             */
    public void addCube(Cube newChild) {
        addCube(cubes.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeCube(Cube removeChild) {
        int pos = cubes.indexOf(removeChild);
        if (pos != -1) {
            removeCube(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Cube removeCube(int pos) {
        Cube removedItem = cubes.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Cube.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Cube> getCubes() {
        return Collections.unmodifiableList(cubes);
    }
    

    /** 
                Virtual cubes in this schema.
             */
    private final List<VirtualCube> virtualCubes = new ArrayList<VirtualCube>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Virtual cubes in this schema.
            
     */
    public void addVirtualCube(int pos, VirtualCube newChild) {
        virtualCubes.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(VirtualCube.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Virtual cubes in this schema.
             */
    public void addVirtualCube(VirtualCube newChild) {
        addVirtualCube(virtualCubes.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeVirtualCube(VirtualCube removeChild) {
        int pos = virtualCubes.indexOf(removeChild);
        if (pos != -1) {
            removeVirtualCube(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public VirtualCube removeVirtualCube(int pos) {
        VirtualCube removedItem = virtualCubes.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(VirtualCube.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<VirtualCube> getVirtualCubes() {
        return Collections.unmodifiableList(virtualCubes);
    }
    

    /** 
                Named sets in this schema.
             */
    private final List<NamedSet> namedSets = new ArrayList<NamedSet>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Named sets in this schema.
            
     */
    public void addNamedSet(int pos, NamedSet newChild) {
        namedSets.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(NamedSet.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Named sets in this schema.
             */
    public void addNamedSet(NamedSet newChild) {
        addNamedSet(namedSets.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeNamedSet(NamedSet removeChild) {
        int pos = namedSets.indexOf(removeChild);
        if (pos != -1) {
            removeNamedSet(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(NamedSet.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    

    /** 
                Roles in this schema.
             */
    private final List<Role> roles = new ArrayList<Role>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Roles in this schema.
            
     */
    public void addRole(int pos, Role newChild) {
        roles.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Role.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Roles in this schema.
             */
    public void addRole(Role newChild) {
        addRole(roles.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeRole(Role removeChild) {
        int pos = roles.indexOf(removeChild);
        if (pos != -1) {
            removeRole(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Role removeRole(int pos) {
        Role removedItem = roles.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Role.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }
    

    /** 
                Declarations of user-defined functions in this schema.
             */
    private final List<UserDefinedFunction> userDefinedFunctions = new ArrayList<UserDefinedFunction>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Declarations of user-defined functions in this schema.
            
     */
    public void addUserDefinedFunction(int pos, UserDefinedFunction newChild) {
        userDefinedFunctions.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(UserDefinedFunction.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Declarations of user-defined functions in this schema.
             */
    public void addUserDefinedFunction(UserDefinedFunction newChild) {
        addUserDefinedFunction(userDefinedFunctions.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeUserDefinedFunction(UserDefinedFunction removeChild) {
        int pos = userDefinedFunctions.indexOf(removeChild);
        if (pos != -1) {
            removeUserDefinedFunction(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public UserDefinedFunction removeUserDefinedFunction(int pos) {
        UserDefinedFunction removedItem = userDefinedFunctions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(UserDefinedFunction.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<UserDefinedFunction> getUserDefinedFunctions() {
        return Collections.unmodifiableList(userDefinedFunctions);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(parameters);
        
        children.addAll(dimensions);
        
        children.addAll(cubes);
        
        children.addAll(virtualCubes);
        
        children.addAll(namedSets);
        
        children.addAll(roles);
        
        children.addAll(userDefinedFunctions);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Schema
/** 
            A CubeDimension is either a usage of a Dimension ('shared
            dimension', in MSOLAP parlance), or a 'private dimension'.
         */
public static class CubeDimension extends OLAPObject {
    
    /**
     * Creates a new CubeDimension with all attributes
     * set to their defaults.
     */
    public CubeDimension() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the Dimension's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the column in the fact table which joins
                to the leaf level of this dimension. Required in a
                private Dimension or a DimensionUsage, but not in a
                public Dimension.
             */
    private String /* */ foreignKey;
    
    public String /* */ getForeignKey() {
        return foreignKey;
    }
    
    public void setForeignKey(String /* */ newval) {
        String /* */ oldval = foreignKey;
        foreignKey = newval;
        pcs.firePropertyChange("foreignKey", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class CubeDimension
/** 
            Definition of a cube.
         */
public static class Cube extends OLAPObject {
    
    /**
     * Creates a new Cube with all attributes
     * set to their defaults.
     */
    public Cube() {
    }
    

    /** 
                Name of this cube.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the cube's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the measure that would be taken as the default
                measure of the cube.
             */
    private String /* */ defaultMeasure;
    
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        pcs.firePropertyChange("defaultMeasure", oldval, newval);
    }

    /** 
                Should the Fact table data for this Cube be cached
                by Mondrian or not. The default action is to cache
                the data.
             */
    private Boolean /* */ cache;
    
    public Boolean /* */ getCache() {
        return cache;
    }
    
    public void setCache(Boolean /* */ newval) {
        Boolean /* */ oldval = cache;
        cache = newval;
        pcs.firePropertyChange("cache", oldval, newval);
    }

    /** 
                Whether element is enabled - if true, then the Cube is
                realized otherwise it is ignored.
             */
    private Boolean /* */ enabled;
    
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        pcs.firePropertyChange("enabled", oldval, newval);
    }

    /** 
                The fact table is the source of all measures in this cube. If
                this is a Table and the schema name is not
                present, table name is left unqualified.
             */
    private Relation /* */ fact;
    
    public Relation /* */ getFact() {
        return fact;
    }
    
    public void setFact(Relation /* */ newval) {
        Relation /* */ oldval = fact;
        fact = newval;
        pcs.firePropertyChange("fact", oldval, newval);
    }

    /**  */
    private final List<CubeDimension> dimensions = new ArrayList<CubeDimension>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimension(int pos, CubeDimension newChild) {
        dimensions.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CubeDimension.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addDimension(CubeDimension newChild) {
        addDimension(dimensions.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeDimension(CubeDimension removeChild) {
        int pos = dimensions.indexOf(removeChild);
        if (pos != -1) {
            removeDimension(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CubeDimension removeDimension(int pos) {
        CubeDimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CubeDimension.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CubeDimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }
    

    /**  */
    private final List<Measure> measures = new ArrayList<Measure>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMeasure(int pos, Measure newChild) {
        measures.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Measure.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMeasure(Measure newChild) {
        addMeasure(measures.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMeasure(Measure removeChild) {
        int pos = measures.indexOf(removeChild);
        if (pos != -1) {
            removeMeasure(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Measure removeMeasure(int pos) {
        Measure removedItem = measures.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Measure.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Measure> getMeasures() {
        return Collections.unmodifiableList(measures);
    }
    

    /** 
                Calculated members in this cube.
             */
    private final List<CalculatedMember> calculatedMembers = new ArrayList<CalculatedMember>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Calculated members in this cube.
            
     */
    public void addCalculatedMember(int pos, CalculatedMember newChild) {
        calculatedMembers.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CalculatedMember.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Calculated members in this cube.
             */
    public void addCalculatedMember(CalculatedMember newChild) {
        addCalculatedMember(calculatedMembers.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeCalculatedMember(CalculatedMember removeChild) {
        int pos = calculatedMembers.indexOf(removeChild);
        if (pos != -1) {
            removeCalculatedMember(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CalculatedMember removeCalculatedMember(int pos) {
        CalculatedMember removedItem = calculatedMembers.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CalculatedMember.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMember> getCalculatedMembers() {
        return Collections.unmodifiableList(calculatedMembers);
    }
    

    /** 
                Named sets in this cube.
             */
    private final List<NamedSet> namedSets = new ArrayList<NamedSet>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Named sets in this cube.
            
     */
    public void addNamedSet(int pos, NamedSet newChild) {
        namedSets.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(NamedSet.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Named sets in this cube.
             */
    public void addNamedSet(NamedSet newChild) {
        addNamedSet(namedSets.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeNamedSet(NamedSet removeChild) {
        int pos = namedSets.indexOf(removeChild);
        if (pos != -1) {
            removeNamedSet(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(NamedSet.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Cube
/** 
            A VirtualCube is a set of dimensions and
            measures gleaned from other cubes.
         */
public static class VirtualCube extends OLAPObject {
    
    /**
     * Creates a new VirtualCube with all attributes
     * set to their defaults.
     */
    public VirtualCube() {
    }
    

    /** 
                Whether this element is enabled - if true, then the Virtual
                Cube is realized otherwise it is ignored.
             */
    private Boolean /* */ enabled;
    
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        pcs.firePropertyChange("enabled", oldval, newval);
    }

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** The name of the measure that would be taken as the default
                measure of the cube.
             */
    private String /* */ defaultMeasure;
    
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        pcs.firePropertyChange("defaultMeasure", oldval, newval);
    }

    /** 
                A string being displayed instead of the cube's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /**  */
    private CubeUsages /* */ cubeUsage;
    
    public CubeUsages /* */ getCubeUsage() {
        return cubeUsage;
    }
    
    public void setCubeUsage(CubeUsages /* */ newval) {
        CubeUsages /* */ oldval = cubeUsage;
        cubeUsage = newval;
        pcs.firePropertyChange("cubeUsage", oldval, newval);
    }

    /**  */
    private final List<VirtualCubeDimension> dimensions = new ArrayList<VirtualCubeDimension>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimension(int pos, VirtualCubeDimension newChild) {
        dimensions.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(VirtualCubeDimension.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addDimension(VirtualCubeDimension newChild) {
        addDimension(dimensions.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeDimension(VirtualCubeDimension removeChild) {
        int pos = dimensions.indexOf(removeChild);
        if (pos != -1) {
            removeDimension(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public VirtualCubeDimension removeDimension(int pos) {
        VirtualCubeDimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(VirtualCubeDimension.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<VirtualCubeDimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }
    

    /**  */
    private final List<VirtualCubeMeasure> measures = new ArrayList<VirtualCubeMeasure>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMeasure(int pos, VirtualCubeMeasure newChild) {
        measures.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(VirtualCubeMeasure.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMeasure(VirtualCubeMeasure newChild) {
        addMeasure(measures.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMeasure(VirtualCubeMeasure removeChild) {
        int pos = measures.indexOf(removeChild);
        if (pos != -1) {
            removeMeasure(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public VirtualCubeMeasure removeMeasure(int pos) {
        VirtualCubeMeasure removedItem = measures.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(VirtualCubeMeasure.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<VirtualCubeMeasure> getMeasures() {
        return Collections.unmodifiableList(measures);
    }
    

    /** 
                Calculated members that belong to this virtual cube.
                (Calculated members inherited from other cubes should not be
                in this list.)
             */
    private final List<CalculatedMember> calculatedMembers = new ArrayList<CalculatedMember>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Calculated members that belong to this virtual cube.
                (Calculated members inherited from other cubes should not be
                in this list.)
            
     */
    public void addCalculatedMember(int pos, CalculatedMember newChild) {
        calculatedMembers.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CalculatedMember.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Calculated members that belong to this virtual cube.
                (Calculated members inherited from other cubes should not be
                in this list.)
             */
    public void addCalculatedMember(CalculatedMember newChild) {
        addCalculatedMember(calculatedMembers.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeCalculatedMember(CalculatedMember removeChild) {
        int pos = calculatedMembers.indexOf(removeChild);
        if (pos != -1) {
            removeCalculatedMember(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CalculatedMember removeCalculatedMember(int pos) {
        CalculatedMember removedItem = calculatedMembers.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CalculatedMember.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMember> getCalculatedMembers() {
        return Collections.unmodifiableList(calculatedMembers);
    }
    

    /** 
                Named sets in this cube.
             */
    private final List<NamedSet> namedSets = new ArrayList<NamedSet>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
                Named sets in this cube.
            
     */
    public void addNamedSet(int pos, NamedSet newChild) {
        namedSets.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(NamedSet.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * 
                Named sets in this cube.
             */
    public void addNamedSet(NamedSet newChild) {
        addNamedSet(namedSets.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeNamedSet(NamedSet removeChild) {
        int pos = namedSets.indexOf(removeChild);
        if (pos != -1) {
            removeNamedSet(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(NamedSet.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element VirtualCube
/** 
            List of base cubes used by the virtual cube.
         */
public static class CubeUsages extends OLAPObject {
    
    /**
     * Creates a new CubeUsages with all attributes
     * set to their defaults.
     */
    public CubeUsages() {
    }
    

    /**  */
    private final List<CubeUsage> cubeUsages = new ArrayList<CubeUsage>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addCubeUsage(int pos, CubeUsage newChild) {
        cubeUsages.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CubeUsage.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addCubeUsage(CubeUsage newChild) {
        addCubeUsage(cubeUsages.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeCubeUsage(CubeUsage removeChild) {
        int pos = cubeUsages.indexOf(removeChild);
        if (pos != -1) {
            removeCubeUsage(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CubeUsage removeCubeUsage(int pos) {
        CubeUsage removedItem = cubeUsages.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CubeUsage.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CubeUsage> getCubeUsages() {
        return Collections.unmodifiableList(cubeUsages);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(cubeUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element CubeUsages
/**  */
public static class CubeUsage extends OLAPObject {
    
    /**
     * Creates a new CubeUsage with all attributes
     * set to their defaults.
     */
    public CubeUsage() {
    }
    

    /** 
                Name of the cube which the virtualCube uses.
             */
    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Unrelated dimensions to measures in this cube will be pushed to
                top level member.
             */
    private Boolean /* */ ignoreUnrelatedDimensions;
    
    public Boolean /* */ getIgnoreUnrelatedDimensions() {
        return ignoreUnrelatedDimensions;
    }
    
    public void setIgnoreUnrelatedDimensions(Boolean /* */ newval) {
        Boolean /* */ oldval = ignoreUnrelatedDimensions;
        ignoreUnrelatedDimensions = newval;
        pcs.firePropertyChange("ignoreUnrelatedDimensions", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element CubeUsage
/** 
            A VirtualCubeDimension is a usage of a Dimension in a VirtualCube.
         */
public static class VirtualCubeDimension extends CubeDimension {
    
    /**
     * Creates a new VirtualCubeDimension with all attributes
     * set to their defaults.
     */
    public VirtualCubeDimension() {
    }
    

    /** 
                Name of the cube which the dimension belongs to, or
                unspecified if the dimension is shared.
             */
    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Name of the dimension.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element VirtualCubeDimension
/** 
            A VirtualCubeMeasure is a usage of a Measure in a VirtualCube.
         */
public static class VirtualCubeMeasure extends OLAPObject {
    
    /**
     * Creates a new VirtualCubeMeasure with all attributes
     * set to their defaults.
     */
    public VirtualCubeMeasure() {
    }
    

    /** 
                Name of the cube which the measure belongs to.
             */
    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Unique name of the measure within its cube.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element VirtualCubeMeasure
/** 
            A DimensionUsage is usage of a shared
            Dimension within the context of a cube.
         */
public static class DimensionUsage extends CubeDimension {
    
    /**
     * Creates a new DimensionUsage with all attributes
     * set to their defaults.
     */
    public DimensionUsage() {
    }
    

    /** Name of the source dimension. Must be a dimension in
            this schema. Case-sensitive. */
    private String /* */ source;
    
    public String /* */ getSource() {
        return source;
    }
    
    public void setSource(String /* */ newval) {
        String /* */ oldval = source;
        source = newval;
        pcs.firePropertyChange("source", oldval, newval);
    }

    /** 
                Name of the level to join to. If not specified, joins to the
                lowest level of the dimension.
             */
    private String /* */ level;
    
    public String /* */ getLevel() {
        return level;
    }
    
    public void setLevel(String /* */ newval) {
        String /* */ oldval = level;
        level = newval;
        pcs.firePropertyChange("level", oldval, newval);
    }

    /** 
                If present, then this is prepended to the Dimension column
                names during the building of collapse dimension aggregates
                allowing 1) different dimension usages to be disambiguated
                during aggregate table recognition and 2) multiple shared
                dimensions that have common column names to be disambiguated.
             */
    private String /* */ usagePrefix;
    
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        pcs.firePropertyChange("usagePrefix", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element DimensionUsage
/** 
            A Dimension is a collection of hierarchies. There are
            two kinds: a public dimension belongs to a
            Schema, and be used by several cubes; a
            private dimension belongs to a
            Cube. The foreignKey field is only
            applicable to private dimensions.
         */
public static class Dimension extends CubeDimension {
    
    /**
     * Creates a new Dimension with all attributes
     * set to their defaults.
     */
    public Dimension() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                The dimension's type may be one of "Standard" or "Time". A
                time
                dimension will allow the use of the MDX time functions (WTD,
                YTD, QTD, etc.). Use a standard dimension
                if the dimension is not a time-related dimension. The default
                value is "Standard".
             */
    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    /** 
                A string being displayed instead of the dimensions's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                If present, then this is prepended to the Dimension column
                names during the building of collapse dimension aggregates
                allowing 1) different dimensions to be disambiguated
                during aggregate table recognition.
                This should only be set for private dimensions.
             */
    private String /* */ usagePrefix;
    
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        pcs.firePropertyChange("usagePrefix", oldval, newval);
    }

    /**  */
    private final List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addHierarchy(int pos, Hierarchy newChild) {
        hierarchies.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Hierarchy.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addHierarchy(Hierarchy newChild) {
        addHierarchy(hierarchies.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeHierarchy(Hierarchy removeChild) {
        int pos = hierarchies.indexOf(removeChild);
        if (pos != -1) {
            removeHierarchy(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Hierarchy removeHierarchy(int pos) {
        Hierarchy removedItem = hierarchies.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Hierarchy.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Hierarchy> getHierarchies() {
        return Collections.unmodifiableList(hierarchies);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(hierarchies);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Dimension
/** 
            Defines a hierarchy.

            You must specify at most one <Relation>
            or memberReaderClass. If you specify none, the
            hierarchy is assumed to come from the same fact table of the
            current cube.
         */
public static class Hierarchy extends OLAPObject {
    
    /**
     * Creates a new Hierarchy with all attributes
     * set to their defaults.
     */
    public Hierarchy() {
    }
    

    /** 
                Name of the hierarchy. If this is not specified, the hierarchy
                has the same name as its dimension.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether this hierarchy has an 'all' member.
             */
    private Boolean /* */ hasAll;
    
    public Boolean /* */ getHasAll() {
        return hasAll;
    }
    
    public void setHasAll(Boolean /* */ newval) {
        Boolean /* */ oldval = hasAll;
        hasAll = newval;
        pcs.firePropertyChange("hasAll", oldval, newval);
    }

    /** 
                Name of the 'all' member. If this attribute is not specified,
                the all member is named 'All hierarchyName', for
                example, 'All Store'.
             */
    private String /* */ allMemberName;
    
    public String /* */ getAllMemberName() {
        return allMemberName;
    }
    
    public void setAllMemberName(String /* */ newval) {
        String /* */ oldval = allMemberName;
        allMemberName = newval;
        pcs.firePropertyChange("allMemberName", oldval, newval);
    }

    /** 
                A string being displayed instead as the all member's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ allMemberCaption;
    
    public String /* */ getAllMemberCaption() {
        return allMemberCaption;
    }
    
    public void setAllMemberCaption(String /* */ newval) {
        String /* */ oldval = allMemberCaption;
        allMemberCaption = newval;
        pcs.firePropertyChange("allMemberCaption", oldval, newval);
    }

    /** 
                Name of the 'all' level. If this attribute is not specified,
                the all member is named '(All)'.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ allLevelName;
    
    public String /* */ getAllLevelName() {
        return allLevelName;
    }
    
    public void setAllLevelName(String /* */ newval) {
        String /* */ oldval = allLevelName;
        allLevelName = newval;
        pcs.firePropertyChange("allLevelName", oldval, newval);
    }

    /** 
                The name of the column which identifies members, and
                which is referenced by rows in the fact table.
                If not specified, the key of the lowest level is used.
                See also CubeDimension.foreignKey.
             */
    private String /* */ primaryKey;
    
    public String /* */ getPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(String /* */ newval) {
        String /* */ oldval = primaryKey;
        primaryKey = newval;
        pcs.firePropertyChange("primaryKey", oldval, newval);
    }

    /** 
                The name of the table which contains primaryKey.
                If the hierarchy has only one table, defaults to that;
                it is required.
             */
    private String /* */ primaryKeyTable;
    
    public String /* */ getPrimaryKeyTable() {
        return primaryKeyTable;
    }
    
    public void setPrimaryKeyTable(String /* */ newval) {
        String /* */ oldval = primaryKeyTable;
        primaryKeyTable = newval;
        pcs.firePropertyChange("primaryKeyTable", oldval, newval);
    }

    /**  */
    private String /* */ defaultMember;
    
    public String /* */ getDefaultMember() {
        return defaultMember;
    }
    
    public void setDefaultMember(String /* */ newval) {
        String /* */ oldval = defaultMember;
        defaultMember = newval;
        pcs.firePropertyChange("defaultMember", oldval, newval);
    }

    /** 
                Name of the custom member reader class. Must implement
                the mondrian.rolap.MemberReader interface.
             */
    private String /* */ memberReaderClass;
    
    public String /* */ getMemberReaderClass() {
        return memberReaderClass;
    }
    
    public void setMemberReaderClass(String /* */ newval) {
        String /* */ oldval = memberReaderClass;
        memberReaderClass = newval;
        pcs.firePropertyChange("memberReaderClass", oldval, newval);
    }

    /** 
                A string to be displayed in the user interface.
                If not specified, the hierarchy's name is used.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                The {@link MondrianDef.Table table},
                {@link MondrianDef.Join set of tables},
                {@link MondrianDef.View SQL statement}, or
                {@link MondrianDef.InlineTable inline table}
                which populates this hierarchy.
             */
    private RelationOrJoin /* */ relation;
    
    public RelationOrJoin /* */ getRelation() {
        return relation;
    }
    
    public void setRelation(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = relation;
        relation = newval;
        pcs.firePropertyChange("relation", oldval, newval);
    }

    /**  */
    private final List<Level> levels = new ArrayList<Level>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addLevel(int pos, Level newChild) {
        levels.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Level.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addLevel(Level newChild) {
        addLevel(levels.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeLevel(Level removeChild) {
        int pos = levels.indexOf(removeChild);
        if (pos != -1) {
            removeLevel(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Level removeLevel(int pos) {
        Level removedItem = levels.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Level.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }
    

    /**  */
    private final List<MemberReaderParameter> memberReaderParameters = new ArrayList<MemberReaderParameter>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberReaderParameter(int pos, MemberReaderParameter newChild) {
        memberReaderParameters.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(MemberReaderParameter.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMemberReaderParameter(MemberReaderParameter newChild) {
        addMemberReaderParameter(memberReaderParameters.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMemberReaderParameter(MemberReaderParameter removeChild) {
        int pos = memberReaderParameters.indexOf(removeChild);
        if (pos != -1) {
            removeMemberReaderParameter(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public MemberReaderParameter removeMemberReaderParameter(int pos) {
        MemberReaderParameter removedItem = memberReaderParameters.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(MemberReaderParameter.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<MemberReaderParameter> getMemberReaderParameters() {
        return Collections.unmodifiableList(memberReaderParameters);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(levels);
        
        children.addAll(memberReaderParameters);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Hierarchy
/**  */
public static class Level extends OLAPObject {
    
    /**
     * Creates a new Level with all attributes
     * set to their defaults.
     */
    public Level() {
    }
    

    /** 
                The estimated number of members in this level.
                Setting this property improves the performance of
                MDSCHEMA_LEVELS, MDSCHEMA_HIERARCHIES and
                MDSCHEMA_DIMENSIONS XMLA requests
             */
    private String /* */ approxRowCount;
    
    public String /* */ getApproxRowCount() {
        return approxRowCount;
    }
    
    public void setApproxRowCount(String /* */ newval) {
        String /* */ oldval = approxRowCount;
        approxRowCount = newval;
        pcs.firePropertyChange("approxRowCount", oldval, newval);
    }

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                The name of the table that the column comes from. If
                this hierarchy is based upon just one table, defaults to
                the name of that table; otherwise, it is required.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ table;
    
    public String /* */ getTable() {
        return table;
    }
    
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        pcs.firePropertyChange("table", oldval, newval);
    }

    /** 
                The name of the column which holds the unique identifier of
                this level.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the column which holds the user identifier of
                this level.
             */
    private String /* */ nameColumn;
    
    public String /* */ getNameColumn() {
        return nameColumn;
    }
    
    public void setNameColumn(String /* */ newval) {
        String /* */ oldval = nameColumn;
        nameColumn = newval;
        pcs.firePropertyChange("nameColumn", oldval, newval);
    }

    /** 
                The name of the column which holds member
                ordinals.  If this column is not specified, the
                key column is used for ordering.
             */
    private String /* */ ordinalColumn;
    
    public String /* */ getOrdinalColumn() {
        return ordinalColumn;
    }
    
    public void setOrdinalColumn(String /* */ newval) {
        String /* */ oldval = ordinalColumn;
        ordinalColumn = newval;
        pcs.firePropertyChange("ordinalColumn", oldval, newval);
    }

    /** 
                The name of the column which references the parent member in
                a parent-child hierarchy.
             */
    private String /* */ parentColumn;
    
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        pcs.firePropertyChange("parentColumn", oldval, newval);
    }

    /** 
                Value which identifies null parents in a parent-child
                hierarchy. Typical values are 'NULL' and '0'.
             */
    private String /* */ nullParentValue;
    
    public String /* */ getNullParentValue() {
        return nullParentValue;
    }
    
    public void setNullParentValue(String /* */ newval) {
        String /* */ oldval = nullParentValue;
        nullParentValue = newval;
        pcs.firePropertyChange("nullParentValue", oldval, newval);
    }

    /** 
                Indicates the type of this level's key column:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.
                When generating SQL statements, Mondrian
                encloses values for String columns in quotation marks,
                but leaves values for Integer and Numeric columns un-quoted.

                Date, Time, and Timestamp values are quoted according to the
                SQL dialect. For a SQL-compliant dialect, the values appear
                prefixed by their typename, for example, "DATE '2006-06-01'".
             */
    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    /** 
                Whether members are unique across all parents. For
                example, zipcodes are unique across all states. The
                first level's members are always unique.
             */
    private Boolean /* */ uniqueMembers;
    
    public Boolean /* */ getUniqueMembers() {
        return uniqueMembers;
    }
    
    public void setUniqueMembers(Boolean /* */ newval) {
        Boolean /* */ oldval = uniqueMembers;
        uniqueMembers = newval;
        pcs.firePropertyChange("uniqueMembers", oldval, newval);
    }

    /** 
                Whether this is a regular or a time-related level.
                The value makes a difference to time-related functions
                such as YTD (year-to-date).
             */
    private String /* */ levelType;
    
    public String /* */ getLevelType() {
        return levelType;
    }
    
    public void setLevelType(String /* */ newval) {
        String /* */ oldval = levelType;
        levelType = newval;
        pcs.firePropertyChange("levelType", oldval, newval);
    }

    /** 
                Condition which determines whether a member of this level
                is hidden. If a hierarchy has one or more levels with hidden
                members, then it is possible that not all leaf members are the
                same distance from the root, and it is termed a ragged
                hierarchy.

                Allowable values are:
                Never (a member always appears; the default);
                IfBlankName (a member doesn't appear if its name
                is null or empty); and
                IfParentsName (a member appears unless its name
                matches the parent's. */
    private String /* */ hideMemberIf;
    
    public String /* */ getHideMemberIf() {
        return hideMemberIf;
    }
    
    public void setHideMemberIf(String /* */ newval) {
        String /* */ oldval = hideMemberIf;
        hideMemberIf = newval;
        pcs.firePropertyChange("hideMemberIf", oldval, newval);
    }

    /** 
                Name of a formatter class for the member labels being displayed.
                The class must implement the mondrian.olap.MemberFormatter interface.
             */
    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the level's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the column which holds the caption for
                members.
             */
    private String /* */ captionColumn;
    
    public String /* */ getCaptionColumn() {
        return captionColumn;
    }
    
    public void setCaptionColumn(String /* */ newval) {
        String /* */ oldval = captionColumn;
        captionColumn = newval;
        pcs.firePropertyChange("captionColumn", oldval, newval);
    }

    /** 
                The SQL expression used to populate this level's key.
             */
    private KeyExpression /* */ keyExp;
    
    public KeyExpression /* */ getKeyExp() {
        return keyExp;
    }
    
    public void setKeyExp(KeyExpression /* */ newval) {
        KeyExpression /* */ oldval = keyExp;
        keyExp = newval;
        pcs.firePropertyChange("keyExp", oldval, newval);
    }

    /** 
                The SQL expression used to populate this level's name. If not
                specified, the level's key is used.
             */
    private NameExpression /* */ nameExp;
    
    public NameExpression /* */ getNameExp() {
        return nameExp;
    }
    
    public void setNameExp(NameExpression /* */ newval) {
        NameExpression /* */ oldval = nameExp;
        nameExp = newval;
        pcs.firePropertyChange("nameExp", oldval, newval);
    }

    /** 
                The SQL expression used to populate this level's ordinal.
             */
    private OrdinalExpression /* */ ordinalExp;
    
    public OrdinalExpression /* */ getOrdinalExp() {
        return ordinalExp;
    }
    
    public void setOrdinalExp(OrdinalExpression /* */ newval) {
        OrdinalExpression /* */ oldval = ordinalExp;
        ordinalExp = newval;
        pcs.firePropertyChange("ordinalExp", oldval, newval);
    }

    /** 
                The SQL expression used to join to the parent member in a
                parent-child hierarchy.
             */
    private ParentExpression /* */ parentExp;
    
    public ParentExpression /* */ getParentExp() {
        return parentExp;
    }
    
    public void setParentExp(ParentExpression /* */ newval) {
        ParentExpression /* */ oldval = parentExp;
        parentExp = newval;
        pcs.firePropertyChange("parentExp", oldval, newval);
    }

    /**  */
    private Closure /* */ closure;
    
    public Closure /* */ getClosure() {
        return closure;
    }
    
    public void setClosure(Closure /* */ newval) {
        Closure /* */ oldval = closure;
        closure = newval;
        pcs.firePropertyChange("closure", oldval, newval);
    }

    /**  */
    private final List<Property> properties = new ArrayList<Property>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addProperty(int pos, Property newChild) {
        properties.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Property.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addProperty(Property newChild) {
        addProperty(properties.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeProperty(Property removeChild) {
        int pos = properties.indexOf(removeChild);
        if (pos != -1) {
            removeProperty(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Property removeProperty(int pos) {
        Property removedItem = properties.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Property.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(properties);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Level
/** 
            Specifies the transitive closure of a parent-child hierarchy.
            Optional, but recommended for better performance.
            The closure is provided as a set of (parent/child) pairs:
            since it is the transitive closure these are actually (ancestor/descendant) pairs.
         */
public static class Closure extends OLAPObject {
    
    /**
     * Creates a new Closure with all attributes
     * set to their defaults.
     */
    public Closure() {
    }
    

    /**  */
    private String /* */ parentColumn;
    
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        pcs.firePropertyChange("parentColumn", oldval, newval);
    }

    /**  */
    private String /* */ childColumn;
    
    public String /* */ getChildColumn() {
        return childColumn;
    }
    
    public void setChildColumn(String /* */ newval) {
        String /* */ oldval = childColumn;
        childColumn = newval;
        pcs.firePropertyChange("childColumn", oldval, newval);
    }

    /**  */
    private Table /* */ table;
    
    public Table /* */ getTable() {
        return table;
    }
    
    public void setTable(Table /* */ newval) {
        Table /* */ oldval = table;
        table = newval;
        pcs.firePropertyChange("table", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Closure
/** 
            Member property.
         */
public static class Property extends OLAPObject {
    
    /**
     * Creates a new Property with all attributes
     * set to their defaults.
     */
    public Property() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /**  */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    /** 
                Data type of this property:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.
             */
    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    /** 
                Name of a formatter class for the appropriate property value
                being displayed.
                The class must implement the mondrian.olap.PropertyFormatter
                interface.
             */
    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Property
/**  */
public static class Measure extends OLAPObject {
    
    /**
     * Creates a new Measure with all attributes
     * set to their defaults.
     */
    public Measure() {
    }
    

    /** Name of this measure. */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Column which is source of this measure's values.
                If not specified, a measure expression must be specified.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    /** 
                The datatype of this measure:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.

                The default datatype of a measure is
                'Integer' if the measure's aggregator is 'Count',
                otherwise it is 'Numeric'.
             */
    private String /* */ datatype;
    
    public String /* */ getDatatype() {
        return datatype;
    }
    
    public void setDatatype(String /* */ newval) {
        String /* */ oldval = datatype;
        datatype = newval;
        pcs.firePropertyChange("datatype", oldval, newval);
    }

    /** 
                Format string with which to format cells of this measure. For
                more details, see the mondrian.util.Format class.
             */
    private String /* */ formatString;
    
    public String /* */ getFormatString() {
        return formatString;
    }
    
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        pcs.firePropertyChange("formatString", oldval, newval);
    }

    /** 
                Aggregation function. Allowed values are "sum", "count", "min",
                "max", "avg", and "distinct-count". ("distinct count" is allowed
                for backwards compatibility, but is deprecated because XML
                enumerated attributes in a DTD cannot legally contain spaces.)
             */
    private String /* */ aggregator;
    
    public String /* */ getAggregator() {
        return aggregator;
    }
    
    public void setAggregator(String /* */ newval) {
        String /* */ oldval = aggregator;
        aggregator = newval;
        pcs.firePropertyChange("aggregator", oldval, newval);
    }

    /** 
                Name of a formatter class for the appropriate cell being displayed.
                The class must implement the mondrian.olap.CellFormatter interface.
             */
    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

    /** 
                The SQL expression used to calculate a measure.
                Must be specified if a source column is not specified.
             */
    private MeasureExpression /* */ measureExp;
    
    public MeasureExpression /* */ getMeasureExp() {
        return measureExp;
    }
    
    public void setMeasureExp(MeasureExpression /* */ newval) {
        MeasureExpression /* */ oldval = measureExp;
        measureExp = newval;
        pcs.firePropertyChange("measureExp", oldval, newval);
    }

    /**  */
    private final List<CalculatedMemberProperty> memberProperties = new ArrayList<CalculatedMemberProperty>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberPropertie(int pos, CalculatedMemberProperty newChild) {
        memberProperties.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CalculatedMemberProperty.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMemberPropertie(CalculatedMemberProperty newChild) {
        addMemberPropertie(memberProperties.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMemberPropertie(CalculatedMemberProperty removeChild) {
        int pos = memberProperties.indexOf(removeChild);
        if (pos != -1) {
            removeMemberPropertie(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CalculatedMemberProperty removeMemberPropertie(int pos) {
        CalculatedMemberProperty removedItem = memberProperties.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CalculatedMemberProperty.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberProperties);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Measure
/**  */
public static class CalculatedMember extends OLAPObject {
    
    /**
     * Creates a new CalculatedMember with all attributes
     * set to their defaults.
     */
    public CalculatedMember() {
    }
    

    /** 
                Name of this calculated member.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Format string with which to format cells of this member. For
                more details, see {@link mondrian.util.Format}.
             */
    private String /* */ formatString;
    
    public String /* */ getFormatString() {
        return formatString;
    }
    
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        pcs.firePropertyChange("formatString", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this member.
                Equivalent to the Formula sub-element.
             */
    private String /* */ formula;
    
    public String /* */ getFormula() {
        return formula;
    }
    
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        pcs.firePropertyChange("formula", oldval, newval);
    }

    /** 
                Name of the dimension which this member belongs to.
             */
    private String /* */ dimension;
    
    public String /* */ getDimension() {
        return dimension;
    }
    
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        pcs.firePropertyChange("dimension", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this member.
             */
    private Formula /* */ formulaElement;
    
    public Formula /* */ getFormulaElement() {
        return formulaElement;
    }
    
    public void setFormulaElement(Formula /* */ newval) {
        Formula /* */ oldval = formulaElement;
        formulaElement = newval;
        pcs.firePropertyChange("formulaElement", oldval, newval);
    }

    /**  */
    private final List<CalculatedMemberProperty> memberProperties = new ArrayList<CalculatedMemberProperty>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberPropertie(int pos, CalculatedMemberProperty newChild) {
        memberProperties.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CalculatedMemberProperty.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMemberPropertie(CalculatedMemberProperty newChild) {
        addMemberPropertie(memberProperties.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMemberPropertie(CalculatedMemberProperty removeChild) {
        int pos = memberProperties.indexOf(removeChild);
        if (pos != -1) {
            removeMemberPropertie(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CalculatedMemberProperty removeMemberPropertie(int pos) {
        CalculatedMemberProperty removedItem = memberProperties.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CalculatedMemberProperty.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberProperties);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element CalculatedMember
/** 
            Property of a calculated member defined against a cube.
            It must have either an expression or a value.
         */
public static class CalculatedMemberProperty extends OLAPObject {
    
    /**
     * Creates a new CalculatedMemberProperty with all attributes
     * set to their defaults.
     */
    public CalculatedMemberProperty() {
    }
    

    /** 
                Name of this member property.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the Properties's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    /** 
                MDX expression which defines the value of this property.
                If the expression is a constant string, you could enclose it in
                quotes, or just specify the 'value' attribute instead.
             */
    private String /* */ expression;
    
    public String /* */ getExpression() {
        return expression;
    }
    
    public void setExpression(String /* */ newval) {
        String /* */ oldval = expression;
        expression = newval;
        pcs.firePropertyChange("expression", oldval, newval);
    }

    /** 
                Value of this property.
                If the value is not constant, specify the 'expression' attribute
                instead.
             */
    private String /* */ value;
    
    public String /* */ getValue() {
        return value;
    }
    
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        pcs.firePropertyChange("value", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element CalculatedMemberProperty
/** 
            <p>Defines a named set which can be used in queries in the
            same way as a set defined using a WITH SET clause.</p>

            <p>A named set can be defined against a particular cube,
            or can be global to a schema. If it is defined against a
            cube, it is only available to queries which use that cube.</p>

            <p>A named set defined against a cube is not inherited by
            a virtual cubes defined against that cube. (But you can
            define a named set against a virtual cube.)</p>

            <p>A named set defined against a schema is available in
            all cubes and virtual cubes in that schema. However, it is
            only valid if the cube contains dimensions with the names
            required to make the formula valid.</p>

         */
public static class NamedSet extends OLAPObject {
    
    /**
     * Creates a new NamedSet with all attributes
     * set to their defaults.
     */
    public NamedSet() {
    }
    

    /** 
                Name of this named set.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this set.
                Equivalent to the Formula sub-element.
             */
    private String /* */ formula;
    
    public String /* */ getFormula() {
        return formula;
    }
    
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        pcs.firePropertyChange("formula", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this set.
             */
    private Formula /* */ formulaElement;
    
    public Formula /* */ getFormulaElement() {
        return formulaElement;
    }
    
    public void setFormulaElement(Formula /* */ newval) {
        Formula /* */ oldval = formulaElement;
        formulaElement = newval;
        pcs.firePropertyChange("formulaElement", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element NamedSet
/**  */
public static class Formula extends OLAPObject {
    
    /**
     * Creates a new Formula with all attributes
     * set to their defaults.
     */
    public Formula() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Formula
/** Not used */
public static class MemberReaderParameter extends OLAPObject {
    
    /**
     * Creates a new MemberReaderParameter with all attributes
     * set to their defaults.
     */
    public MemberReaderParameter() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /**  */
    private String /* */ value;
    
    public String /* */ getValue() {
        return value;
    }
    
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        pcs.firePropertyChange("value", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element MemberReaderParameter
/** A table or a join */
public static class RelationOrJoin extends OLAPObject {
    
    /**
     * Creates a new RelationOrJoin with all attributes
     * set to their defaults.
     */
    public RelationOrJoin() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class RelationOrJoin
/** A table, inline table or view */
public static class Relation extends RelationOrJoin {
    
    /**
     * Creates a new Relation with all attributes
     * set to their defaults.
     */
    public Relation() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class Relation
/** 
            A collection of SQL statements, one per dialect.
         */
public static class View extends Relation {
    
    /**
     * Creates a new View with all attributes
     * set to their defaults.
     */
    public View() {
    }
    

    /**  */
    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

    /**  */
    private final List<SQL> selects = new ArrayList<SQL>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addSelect(int pos, SQL newChild) {
        selects.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(SQL.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addSelect(SQL newChild) {
        addSelect(selects.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeSelect(SQL removeChild) {
        int pos = selects.indexOf(removeChild);
        if (pos != -1) {
            removeSelect(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public SQL removeSelect(int pos) {
        SQL removedItem = selects.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(SQL.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<SQL> getSelects() {
        return Collections.unmodifiableList(selects);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(selects);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element View
/**  */
public static class SQL extends OLAPObject {
    
    /**
     * Creates a new SQL with all attributes
     * set to their defaults.
     */
    public SQL() {
    }
    

    /** 
                Dialect of SQL the view is intended for.
             */
    private String /* */ dialect;
    
    public String /* */ getDialect() {
        return dialect;
    }
    
    public void setDialect(String /* */ newval) {
        String /* */ oldval = dialect;
        dialect = newval;
        pcs.firePropertyChange("dialect", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element SQL
/**  */
public static class Join extends RelationOrJoin {
    
    /**
     * Creates a new Join with all attributes
     * set to their defaults.
     */
    public Join() {
    }
    

    /** 
                Defaults to left's alias if left is a table, otherwise
                required.
             */
    private String /* */ leftAlias;
    
    public String /* */ getLeftAlias() {
        return leftAlias;
    }
    
    public void setLeftAlias(String /* */ newval) {
        String /* */ oldval = leftAlias;
        leftAlias = newval;
        pcs.firePropertyChange("leftAlias", oldval, newval);
    }

    /**  */
    private String /* */ leftKey;
    
    public String /* */ getLeftKey() {
        return leftKey;
    }
    
    public void setLeftKey(String /* */ newval) {
        String /* */ oldval = leftKey;
        leftKey = newval;
        pcs.firePropertyChange("leftKey", oldval, newval);
    }

    /** 
                Defaults to right's alias if right is a table, otherwise
                required.
             */
    private String /* */ rightAlias;
    
    public String /* */ getRightAlias() {
        return rightAlias;
    }
    
    public void setRightAlias(String /* */ newval) {
        String /* */ oldval = rightAlias;
        rightAlias = newval;
        pcs.firePropertyChange("rightAlias", oldval, newval);
    }

    /**  */
    private String /* */ rightKey;
    
    public String /* */ getRightKey() {
        return rightKey;
    }
    
    public void setRightKey(String /* */ newval) {
        String /* */ oldval = rightKey;
        rightKey = newval;
        pcs.firePropertyChange("rightKey", oldval, newval);
    }

    /**  */
    private RelationOrJoin /* */ left;
    
    public RelationOrJoin /* */ getLeft() {
        return left;
    }
    
    public void setLeft(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = left;
        left = newval;
        pcs.firePropertyChange("left", oldval, newval);
    }

    /**  */
    private RelationOrJoin /* */ right;
    
    public RelationOrJoin /* */ getRight() {
        return right;
    }
    
    public void setRight(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = right;
        right = newval;
        pcs.firePropertyChange("right", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Join
/**  */
public static class Table extends Relation {
    
    /**
     * Creates a new Table with all attributes
     * set to their defaults.
     */
    public Table() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Optional qualifier for table.
             */
    private String /* */ schema;
    
    public String /* */ getSchema() {
        return schema;
    }
    
    public void setSchema(String /* */ newval) {
        String /* */ oldval = schema;
        schema = newval;
        pcs.firePropertyChange("schema", oldval, newval);
    }

    /** 
                Alias to be used with this table when it is used to
                form queries. If not specified, defaults to the table
                name, but in any case, must be unique within the
                schema. (You can use the same table in different
                hierarchies, but it must have different aliases.)
             */
    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

    /** 
          The SQL WHERE clause expression to be appended to any select statement
         */
    private SQL /* */ filter;
    
    public SQL /* */ getFilter() {
        return filter;
    }
    
    public void setFilter(SQL /* */ newval) {
        SQL /* */ oldval = filter;
        filter = newval;
        pcs.firePropertyChange("filter", oldval, newval);
    }

    /**  */
    private final List<AggExclude> aggExcludes = new ArrayList<AggExclude>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addAggExclude(int pos, AggExclude newChild) {
        aggExcludes.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggExclude.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addAggExclude(AggExclude newChild) {
        addAggExclude(aggExcludes.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeAggExclude(AggExclude removeChild) {
        int pos = aggExcludes.indexOf(removeChild);
        if (pos != -1) {
            removeAggExclude(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggExclude removeAggExclude(int pos) {
        AggExclude removedItem = aggExcludes.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggExclude.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggExclude> getAggExcludes() {
        return Collections.unmodifiableList(aggExcludes);
    }
    

    /**  */
    private final List<AggTable> aggTables = new ArrayList<AggTable>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addAggTable(int pos, AggTable newChild) {
        aggTables.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggTable.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addAggTable(AggTable newChild) {
        addAggTable(aggTables.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeAggTable(AggTable removeChild) {
        int pos = aggTables.indexOf(removeChild);
        if (pos != -1) {
            removeAggTable(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggTable removeAggTable(int pos) {
        AggTable removedItem = aggTables.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggTable.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggTable> getAggTables() {
        return Collections.unmodifiableList(aggTables);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(aggExcludes);
        
        children.addAll(aggTables);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Table
/**  */
public static class InlineTable extends Relation {
    
    /**
     * Creates a new InlineTable with all attributes
     * set to their defaults.
     */
    public InlineTable() {
    }
    

    /** 
                Alias to be used with this table when it is used to
                form queries. If not specified, defaults to the table
                name, but in any case, must be unique within the
                schema. (You can use the same table in different
                hierarchies, but it must have different aliases.)
             */
    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

    /**  */
    private ColumnDefs /* */ columnDefs;
    
    public ColumnDefs /* */ getColumnDefs() {
        return columnDefs;
    }
    
    public void setColumnDefs(ColumnDefs /* */ newval) {
        ColumnDefs /* */ oldval = columnDefs;
        columnDefs = newval;
        pcs.firePropertyChange("columnDefs", oldval, newval);
    }

    /**  */
    private Rows /* */ rows;
    
    public Rows /* */ getRows() {
        return rows;
    }
    
    public void setRows(Rows /* */ newval) {
        Rows /* */ oldval = rows;
        rows = newval;
        pcs.firePropertyChange("rows", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element InlineTable
/** Holder for an array of ColumnDef elements */
public static class ColumnDefs extends OLAPObject {
    
    /**
     * Creates a new ColumnDefs with all attributes
     * set to their defaults.
     */
    public ColumnDefs() {
    }
    

    /**  */
    private final List<ColumnDef> array = new ArrayList<ColumnDef>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addArra(int pos, ColumnDef newChild) {
        array.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(ColumnDef.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addArra(ColumnDef newChild) {
        addArra(array.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeArra(ColumnDef removeChild) {
        int pos = array.indexOf(removeChild);
        if (pos != -1) {
            removeArra(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public ColumnDef removeArra(int pos) {
        ColumnDef removedItem = array.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(ColumnDef.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<ColumnDef> getArray() {
        return Collections.unmodifiableList(array);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element ColumnDefs
/** 
            Column definition for an inline table.
         */
public static class ColumnDef extends OLAPObject {
    
    /**
     * Creates a new ColumnDef with all attributes
     * set to their defaults.
     */
    public ColumnDef() {
    }
    

    /** 
                Name of the column.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Type of the column:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.
             */
    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element ColumnDef
/** Holder for an array of Row elements */
public static class Rows extends OLAPObject {
    
    /**
     * Creates a new Rows with all attributes
     * set to their defaults.
     */
    public Rows() {
    }
    

    /**  */
    private final List<Row> array = new ArrayList<Row>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addArra(int pos, Row newChild) {
        array.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Row.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addArra(Row newChild) {
        addArra(array.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeArra(Row removeChild) {
        int pos = array.indexOf(removeChild);
        if (pos != -1) {
            removeArra(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Row removeArra(int pos) {
        Row removedItem = array.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Row.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Row> getArray() {
        return Collections.unmodifiableList(array);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Rows
/** 
            Row definition for an inline table.
            Must have one Column for each ColumnDef in the InlineTable.
         */
public static class Row extends OLAPObject {
    
    /**
     * Creates a new Row with all attributes
     * set to their defaults.
     */
    public Row() {
    }
    

    /**  */
    private final List<Value> values = new ArrayList<Value>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addValue(int pos, Value newChild) {
        values.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(Value.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addValue(Value newChild) {
        addValue(values.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeValue(Value removeChild) {
        int pos = values.indexOf(removeChild);
        if (pos != -1) {
            removeValue(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public Value removeValue(int pos) {
        Value removedItem = values.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(Value.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<Value> getValues() {
        return Collections.unmodifiableList(values);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(values);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Row
/** 
            Column value for an inline table.
            The CDATA holds the value of the column.
         */
public static class Value extends OLAPObject {
    
    /**
     * Creates a new Value with all attributes
     * set to their defaults.
     */
    public Value() {
    }
    

    /** 
                Name of the column.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Value
/** 
            A definition of an aggregate table for a base fact table.
            This aggregate table must be in the same schema as the
            base fact table.
         */
public static class AggTable extends OLAPObject {
    
    /**
     * Creates a new AggTable with all attributes
     * set to their defaults.
     */
    public AggTable() {
    }
    

    /** 
                Whether or not the match should ignore case.
             */
    private Boolean /* */ ignorecase;
    
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        pcs.firePropertyChange("ignorecase", oldval, newval);
    }

    /** 
                What does the fact_count column look like.
             */
    private AggFactCount /* */ factcount;
    
    public AggFactCount /* */ getFactcount() {
        return factcount;
    }
    
    public void setFactcount(AggFactCount /* */ newval) {
        AggFactCount /* */ oldval = factcount;
        factcount = newval;
        pcs.firePropertyChange("factcount", oldval, newval);
    }

    /**  */
    private final List<AggIgnoreColumn> ignoreColumns = new ArrayList<AggIgnoreColumn>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addIgnoreColumn(int pos, AggIgnoreColumn newChild) {
        ignoreColumns.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggIgnoreColumn.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addIgnoreColumn(AggIgnoreColumn newChild) {
        addIgnoreColumn(ignoreColumns.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeIgnoreColumn(AggIgnoreColumn removeChild) {
        int pos = ignoreColumns.indexOf(removeChild);
        if (pos != -1) {
            removeIgnoreColumn(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggIgnoreColumn removeIgnoreColumn(int pos) {
        AggIgnoreColumn removedItem = ignoreColumns.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggIgnoreColumn.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggIgnoreColumn> getIgnoreColumns() {
        return Collections.unmodifiableList(ignoreColumns);
    }
    

    /**  */
    private final List<AggForeignKey> foreignKeys = new ArrayList<AggForeignKey>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addForeignKey(int pos, AggForeignKey newChild) {
        foreignKeys.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggForeignKey.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addForeignKey(AggForeignKey newChild) {
        addForeignKey(foreignKeys.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeForeignKey(AggForeignKey removeChild) {
        int pos = foreignKeys.indexOf(removeChild);
        if (pos != -1) {
            removeForeignKey(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggForeignKey removeForeignKey(int pos) {
        AggForeignKey removedItem = foreignKeys.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggForeignKey.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggForeignKey> getForeignKeys() {
        return Collections.unmodifiableList(foreignKeys);
    }
    

    /**  */
    private final List<AggMeasure> measures = new ArrayList<AggMeasure>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMeasure(int pos, AggMeasure newChild) {
        measures.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggMeasure.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMeasure(AggMeasure newChild) {
        addMeasure(measures.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMeasure(AggMeasure removeChild) {
        int pos = measures.indexOf(removeChild);
        if (pos != -1) {
            removeMeasure(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggMeasure removeMeasure(int pos) {
        AggMeasure removedItem = measures.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggMeasure.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggMeasure> getMeasures() {
        return Collections.unmodifiableList(measures);
    }
    

    /**  */
    private final List<AggLevel> levels = new ArrayList<AggLevel>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addLevel(int pos, AggLevel newChild) {
        levels.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggLevel.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addLevel(AggLevel newChild) {
        addLevel(levels.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeLevel(AggLevel removeChild) {
        int pos = levels.indexOf(removeChild);
        if (pos != -1) {
            removeLevel(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggLevel removeLevel(int pos) {
        AggLevel removedItem = levels.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggLevel.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggLevel> getLevels() {
        return Collections.unmodifiableList(levels);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(ignoreColumns);
        
        children.addAll(foreignKeys);
        
        children.addAll(measures);
        
        children.addAll(levels);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of class AggTable
/**  */
public static class AggName extends AggTable {
    
    /**
     * Creates a new AggName with all attributes
     * set to their defaults.
     */
    public AggName() {
    }
    

    /** 
                The Table name of a Specific aggregate table.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggName
/**  */
public static class AggPattern extends AggTable {
    
    /**
     * Creates a new AggPattern with all attributes
     * set to their defaults.
     */
    public AggPattern() {
    }
    

    /** 
                A Table pattern used to define a set of aggregate tables.
             */
    private String /* */ pattern;
    
    public String /* */ getPattern() {
        return pattern;
    }
    
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        pcs.firePropertyChange("pattern", oldval, newval);
    }

    /**  */
    private final List<AggExclude> excludes = new ArrayList<AggExclude>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addExclude(int pos, AggExclude newChild) {
        excludes.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(AggExclude.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addExclude(AggExclude newChild) {
        addExclude(excludes.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeExclude(AggExclude removeChild) {
        int pos = excludes.indexOf(removeChild);
        if (pos != -1) {
            removeExclude(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public AggExclude removeExclude(int pos) {
        AggExclude removedItem = excludes.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(AggExclude.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<AggExclude> getExcludes() {
        return Collections.unmodifiableList(excludes);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(excludes);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element AggPattern
/**  */
public static class AggExclude extends OLAPObject {
    
    /**
     * Creates a new AggExclude with all attributes
     * set to their defaults.
     */
    public AggExclude() {
    }
    

    /** 
                A Table pattern not to be matched.
             */
    private String /* */ pattern;
    
    public String /* */ getPattern() {
        return pattern;
    }
    
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        pcs.firePropertyChange("pattern", oldval, newval);
    }

    /** 
                The Table name not to be matched.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether or not the match should ignore case.
             */
    private Boolean /* */ ignorecase;
    
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        pcs.firePropertyChange("ignorecase", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggExclude
/**  */
public static class AggColumnName extends OLAPObject {
    
    /**
     * Creates a new AggColumnName with all attributes
     * set to their defaults.
     */
    public AggColumnName() {
    }
    

    /** 
                The name of the fact count column.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class AggColumnName
/**  */
public static class AggFactCount extends AggColumnName {
    
    /**
     * Creates a new AggFactCount with all attributes
     * set to their defaults.
     */
    public AggFactCount() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggFactCount
/**  */
public static class AggIgnoreColumn extends AggColumnName {
    
    /**
     * Creates a new AggIgnoreColumn with all attributes
     * set to their defaults.
     */
    public AggIgnoreColumn() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggIgnoreColumn
/** 
            The name of the column mapping from base fact table foreign key
            to aggregate table foreign key.
         */
public static class AggForeignKey extends OLAPObject {
    
    /**
     * Creates a new AggForeignKey with all attributes
     * set to their defaults.
     */
    public AggForeignKey() {
    }
    

    /** 
                The name of the base fact table foreign key.
             */
    private String /* */ factColumn;
    
    public String /* */ getFactColumn() {
        return factColumn;
    }
    
    public void setFactColumn(String /* */ newval) {
        String /* */ oldval = factColumn;
        factColumn = newval;
        pcs.firePropertyChange("factColumn", oldval, newval);
    }

    /** 
                The name of the aggregate table foreign key.
             */
    private String /* */ aggColumn;
    
    public String /* */ getAggColumn() {
        return aggColumn;
    }
    
    public void setAggColumn(String /* */ newval) {
        String /* */ oldval = aggColumn;
        aggColumn = newval;
        pcs.firePropertyChange("aggColumn", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggForeignKey
/**  */
public static class AggLevel extends OLAPObject {
    
    /**
     * Creates a new AggLevel with all attributes
     * set to their defaults.
     */
    public AggLevel() {
    }
    

    /** 
                The name of the column mapping to the level name.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the Dimension Hierarchy level.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggLevel
/**  */
public static class AggMeasure extends OLAPObject {
    
    /**
     * Creates a new AggMeasure with all attributes
     * set to their defaults.
     */
    public AggMeasure() {
    }
    

    /** 
                The name of the column mapping to the measure name.
             */
    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the Cube measure.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element AggMeasure
/**  */
public static class Expression extends OLAPObject {
    
    /**
     * Creates a new Expression with all attributes
     * set to their defaults.
     */
    public Expression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class Expression
/**  */
public static class Column extends Expression {
    
    /**
     * Creates a new Column with all attributes
     * set to their defaults.
     */
    public Column() {
    }
    

    /** 
                Alias of the table which contains this column. Not required if
                the query only has one table.
             */
    private String /* */ table;
    
    public String /* */ getTable() {
        return table;
    }
    
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        pcs.firePropertyChange("table", oldval, newval);
    }

    /** 
                Name of the column.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Column
/** 
            A collection of SQL expressions, one per dialect.
         */
public static class ExpressionView extends Expression {
    
    /**
     * Creates a new ExpressionView with all attributes
     * set to their defaults.
     */
    public ExpressionView() {
    }
    

    /**  */
    private final List<SQL> expressions = new ArrayList<SQL>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addExpression(int pos, SQL newChild) {
        expressions.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(SQL.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addExpression(SQL newChild) {
        addExpression(expressions.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeExpression(SQL removeChild) {
        int pos = expressions.indexOf(removeChild);
        if (pos != -1) {
            removeExpression(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public SQL removeExpression(int pos) {
        SQL removedItem = expressions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(SQL.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<SQL> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(expressions);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of class ExpressionView
/**  */
public static class KeyExpression extends ExpressionView {
    
    /**
     * Creates a new KeyExpression with all attributes
     * set to their defaults.
     */
    public KeyExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element KeyExpression
/**  */
public static class ParentExpression extends ExpressionView {
    
    /**
     * Creates a new ParentExpression with all attributes
     * set to their defaults.
     */
    public ParentExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element ParentExpression
/**  */
public static class OrdinalExpression extends ExpressionView {
    
    /**
     * Creates a new OrdinalExpression with all attributes
     * set to their defaults.
     */
    public OrdinalExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element OrdinalExpression
/**  */
public static class NameExpression extends ExpressionView {
    
    /**
     * Creates a new NameExpression with all attributes
     * set to their defaults.
     */
    public NameExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element NameExpression
/**  */
public static class CaptionExpression extends ExpressionView {
    
    /**
     * Creates a new CaptionExpression with all attributes
     * set to their defaults.
     */
    public CaptionExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element CaptionExpression
/**  */
public static class MeasureExpression extends ExpressionView {
    
    /**
     * Creates a new MeasureExpression with all attributes
     * set to their defaults.
     */
    public MeasureExpression() {
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element MeasureExpression
/** 
            A role defines an access-control profile. It has a series of grants
            (or denials) for schema elements.
         */
public static class Role extends OLAPObject {
    
    /**
     * Creates a new Role with all attributes
     * set to their defaults.
     */
    public Role() {
    }
    

    /**  */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /**  */
    private final List<SchemaGrant> schemaGrants = new ArrayList<SchemaGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addSchemaGrant(int pos, SchemaGrant newChild) {
        schemaGrants.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(SchemaGrant.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addSchemaGrant(SchemaGrant newChild) {
        addSchemaGrant(schemaGrants.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeSchemaGrant(SchemaGrant removeChild) {
        int pos = schemaGrants.indexOf(removeChild);
        if (pos != -1) {
            removeSchemaGrant(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public SchemaGrant removeSchemaGrant(int pos) {
        SchemaGrant removedItem = schemaGrants.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(SchemaGrant.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<SchemaGrant> getSchemaGrants() {
        return Collections.unmodifiableList(schemaGrants);
    }
    

    /**  */
    private Union /* */ union;
    
    public Union /* */ getUnion() {
        return union;
    }
    
    public void setUnion(Union /* */ newval) {
        Union /* */ oldval = union;
        union = newval;
        pcs.firePropertyChange("union", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(schemaGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Role
/**  */
public static class Grant extends OLAPObject {
    
    /**
     * Creates a new Grant with all attributes
     * set to their defaults.
     */
    public Grant() {
    }
    

    /** Values correspond to Access. */
    private String /* */ access;
    
    public String /* */ getAccess() {
        return access;
    }
    
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        pcs.firePropertyChange("access", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of class Grant
/** 
            Grants (or denies) this role access to this schema.
            access may be "all", "all_dimensions", or "none".
            If access is "all_dimensions", the role has access
            to all dimensions but still needs explicit access to cubes.
            See mondrian.olap.Role#grant(mondrian.olap.Schema,int).
         */
public static class SchemaGrant extends Grant {
    
    /**
     * Creates a new SchemaGrant with all attributes
     * set to their defaults.
     */
    public SchemaGrant() {
    }
    

    /**  */
    private final List<CubeGrant> cubeGrants = new ArrayList<CubeGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addCubeGrant(int pos, CubeGrant newChild) {
        cubeGrants.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(CubeGrant.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addCubeGrant(CubeGrant newChild) {
        addCubeGrant(cubeGrants.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeCubeGrant(CubeGrant removeChild) {
        int pos = cubeGrants.indexOf(removeChild);
        if (pos != -1) {
            removeCubeGrant(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public CubeGrant removeCubeGrant(int pos) {
        CubeGrant removedItem = cubeGrants.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(CubeGrant.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<CubeGrant> getCubeGrants() {
        return Collections.unmodifiableList(cubeGrants);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(cubeGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element SchemaGrant
/** 
            Grants (or denies) this role access to a cube.
            access may be "all" or "none".
            See mondrian.olap.Role#grant(mondrian.olap.Cube,int).
         */
public static class CubeGrant extends Grant {
    
    /**
     * Creates a new CubeGrant with all attributes
     * set to their defaults.
     */
    public CubeGrant() {
    }
    

    /** The unique name of the cube */
    private String /* */ cube;
    
    public String /* */ getCube() {
        return cube;
    }
    
    public void setCube(String /* */ newval) {
        String /* */ oldval = cube;
        cube = newval;
        pcs.firePropertyChange("cube", oldval, newval);
    }

    /**  */
    private final List<DimensionGrant> dimensionGrants = new ArrayList<DimensionGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimensionGrant(int pos, DimensionGrant newChild) {
        dimensionGrants.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(DimensionGrant.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addDimensionGrant(DimensionGrant newChild) {
        addDimensionGrant(dimensionGrants.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeDimensionGrant(DimensionGrant removeChild) {
        int pos = dimensionGrants.indexOf(removeChild);
        if (pos != -1) {
            removeDimensionGrant(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public DimensionGrant removeDimensionGrant(int pos) {
        DimensionGrant removedItem = dimensionGrants.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(DimensionGrant.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<DimensionGrant> getDimensionGrants() {
        return Collections.unmodifiableList(dimensionGrants);
    }
    

    /**  */
    private final List<HierarchyGrant> hierarchyGrants = new ArrayList<HierarchyGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addHierarchyGrant(int pos, HierarchyGrant newChild) {
        hierarchyGrants.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(HierarchyGrant.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addHierarchyGrant(HierarchyGrant newChild) {
        addHierarchyGrant(hierarchyGrants.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeHierarchyGrant(HierarchyGrant removeChild) {
        int pos = hierarchyGrants.indexOf(removeChild);
        if (pos != -1) {
            removeHierarchyGrant(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public HierarchyGrant removeHierarchyGrant(int pos) {
        HierarchyGrant removedItem = hierarchyGrants.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(HierarchyGrant.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<HierarchyGrant> getHierarchyGrants() {
        return Collections.unmodifiableList(hierarchyGrants);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensionGrants);
        
        children.addAll(hierarchyGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element CubeGrant
/** 
            Grants (or denies) this role access to a dimension.
            access may be "all" or "none".
            Note that a role is implicitly given access to a dimension when it
            is given acess to a cube.
            See also the "all_dimensions" option of the "SchemaGrant" element.
            See mondrian.olap.Role#grant(mondrian.olap.Dimension,int).
         */
public static class DimensionGrant extends Grant {
    
    /**
     * Creates a new DimensionGrant with all attributes
     * set to their defaults.
     */
    public DimensionGrant() {
    }
    

    /** The unique name of the dimension */
    private String /* */ dimension;
    
    public String /* */ getDimension() {
        return dimension;
    }
    
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        pcs.firePropertyChange("dimension", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element DimensionGrant
/** 
            Grants (or denies) this role access to a hierarchy.
            access may be "all", "custom" or "none".
            If access is "custom", you may also specify the
            attributes topLevel, bottomLevel, and
            the member grants.
            See mondrian.olap.Role#grant(mondrian.olap.Hierarchy, int, mondrian.olap.Level).
         */
public static class HierarchyGrant extends Grant {
    
    /**
     * Creates a new HierarchyGrant with all attributes
     * set to their defaults.
     */
    public HierarchyGrant() {
    }
    

    /** The unique name of the hierarchy */
    private String /* */ hierarchy;
    
    public String /* */ getHierarchy() {
        return hierarchy;
    }
    
    public void setHierarchy(String /* */ newval) {
        String /* */ oldval = hierarchy;
        hierarchy = newval;
        pcs.firePropertyChange("hierarchy", oldval, newval);
    }

    /** Unique name of the highest level of the hierarchy from which
            this role is allowed to see members. May only be specified if
            the HierarchyGrant.access is "custom". If not
            specified, role can see members up to the top level. */
    private String /* */ topLevel;
    
    public String /* */ getTopLevel() {
        return topLevel;
    }
    
    public void setTopLevel(String /* */ newval) {
        String /* */ oldval = topLevel;
        topLevel = newval;
        pcs.firePropertyChange("topLevel", oldval, newval);
    }

    /** Unique name of the lowest level of the hierarchy from which
            this role is allowed to see members. May only be specified if
            the HierarchyGrant.access is "custom". If not
            specified, role can see members down to the leaf level. */
    private String /* */ bottomLevel;
    
    public String /* */ getBottomLevel() {
        return bottomLevel;
    }
    
    public void setBottomLevel(String /* */ newval) {
        String /* */ oldval = bottomLevel;
        bottomLevel = newval;
        pcs.firePropertyChange("bottomLevel", oldval, newval);
    }

    /** Policy which determines how cell values are calculated if
                not all of the children of the current cell are visible to
                the current role. Allowable values are 'full' (the default),
                'partial', and 'hidden'. */
    private String /* */ rollupPolicy;
    
    public String /* */ getRollupPolicy() {
        return rollupPolicy;
    }
    
    public void setRollupPolicy(String /* */ newval) {
        String /* */ oldval = rollupPolicy;
        rollupPolicy = newval;
        pcs.firePropertyChange("rollupPolicy", oldval, newval);
    }

    /**  */
    private final List<MemberGrant> memberGrants = new ArrayList<MemberGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberGrant(int pos, MemberGrant newChild) {
        memberGrants.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(MemberGrant.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addMemberGrant(MemberGrant newChild) {
        addMemberGrant(memberGrants.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeMemberGrant(MemberGrant removeChild) {
        int pos = memberGrants.indexOf(removeChild);
        if (pos != -1) {
            removeMemberGrant(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public MemberGrant removeMemberGrant(int pos) {
        MemberGrant removedItem = memberGrants.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(MemberGrant.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<MemberGrant> getMemberGrants() {
        return Collections.unmodifiableList(memberGrants);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element HierarchyGrant
/** 
            Grants (or denies) this role access to a member.
            The children of this member inherit that access.
            You can implicitly see a member if you can see any of its children.
            See mondrian.olap.Role#grant(mondrian.olap.Member,int).
         */
public static class MemberGrant extends OLAPObject {
    
    /**
     * Creates a new MemberGrant with all attributes
     * set to their defaults.
     */
    public MemberGrant() {
    }
    

    /** The unique name of the member */
    private String /* */ member;
    
    public String /* */ getMember() {
        return member;
    }
    
    public void setMember(String /* */ newval) {
        String /* */ oldval = member;
        member = newval;
        pcs.firePropertyChange("member", oldval, newval);
    }

    /**  */
    private String /* */ access;
    
    public String /* */ getAccess() {
        return access;
    }
    
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        pcs.firePropertyChange("access", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element MemberGrant
/** 
            Body of a Role definition which defines a Role to be the union
            of several Roles. The RoleUsage elements must refer to Roles that
            have been declared earlier in this schema file.
         */
public static class Union extends OLAPObject {
    
    /**
     * Creates a new Union with all attributes
     * set to their defaults.
     */
    public Union() {
    }
    

    /**  */
    private final List<RoleUsage> roleUsages = new ArrayList<RoleUsage>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addRoleUsage(int pos, RoleUsage newChild) {
        roleUsages.add(pos, newChild);
        newChild.setParent(this);
        fireChildAdded(RoleUsage.class, pos, newChild);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addRoleUsage(RoleUsage newChild) {
        addRoleUsage(roleUsages.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeRoleUsage(RoleUsage removeChild) {
        int pos = roleUsages.indexOf(removeChild);
        if (pos != -1) {
            removeRoleUsage(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position.
     *
     * @return The item that was removed.
     */
    public RoleUsage removeRoleUsage(int pos) {
        RoleUsage removedItem = roleUsages.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(RoleUsage.class, pos, removedItem);
        }
        return removedItem;
    }

    public List<RoleUsage> getRoleUsages() {
        return Collections.unmodifiableList(roleUsages);
    }
    

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(roleUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }

} // end of element Union
/** 
            Usage of a Role in a union Role.
         */
public static class RoleUsage extends OLAPObject {
    
    /**
     * Creates a new RoleUsage with all attributes
     * set to their defaults.
     */
    public RoleUsage() {
    }
    

    /**  */
    private String /* */ roleName;
    
    public String /* */ getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String /* */ newval) {
        String /* */ oldval = roleName;
        roleName = newval;
        pcs.firePropertyChange("roleName", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element RoleUsage
/** 
            A UserDefinedFunction is a function which
            extends the MDX language. It must be implemented by a Java
            class which implements the interface
            mondrian.spi.UserDefinedFunction.
         */
public static class UserDefinedFunction extends OLAPObject {
    
    /**
     * Creates a new UserDefinedFunction with all attributes
     * set to their defaults.
     */
    public UserDefinedFunction() {
    }
    

    /** Name with which the user-defined function will be referenced in MDX expressions. */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Name of the class which implemenets this user-defined function.
                Must implement the mondrian.spi.UserDefinedFunction
                interface.
             */
    private String /* */ className;
    
    public String /* */ getClassName() {
        return className;
    }
    
    public void setClassName(String /* */ newval) {
        String /* */ oldval = className;
        className = newval;
        pcs.firePropertyChange("className", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element UserDefinedFunction
/** 
            A Parameter defines a schema parameter.
            It can be referenced from an MDX statement using the ParamRef
            function and, if not final, its value can be overridden.
         */
public static class Parameter extends OLAPObject {
    
    /**
     * Creates a new Parameter with all attributes
     * set to their defaults.
     */
    public Parameter() {
    }
    

    /** 
                Name of this parameter.
             */
    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    /** 
                Description of this parameter.
             */
    private String /* */ description;
    
    public String /* */ getDescription() {
        return description;
    }
    
    public void setDescription(String /* */ newval) {
        String /* */ oldval = description;
        description = newval;
        pcs.firePropertyChange("description", oldval, newval);
    }

    /** 
                Indicates the type of this parameter:
                String, Numeric, Integer, Boolean, Date, Time, Timestamp, or Member.
             */
    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    /** 
                If false, statement cannot change the value of this parameter;
                the parameter becomes effectively constant (provided that its default
                value expression always returns the same value).
                Default is true.
             */
    private Boolean /* */ modifiable;
    
    public Boolean /* */ getModifiable() {
        return modifiable;
    }
    
    public void setModifiable(Boolean /* */ newval) {
        Boolean /* */ oldval = modifiable;
        modifiable = newval;
        pcs.firePropertyChange("modifiable", oldval, newval);
    }

    /** 
                Expression for the default value of this parameter.
             */
    private String /* */ defaultValue;
    
    public String /* */ getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String /* */ newval) {
        String /* */ oldval = defaultValue;
        defaultValue = newval;
        pcs.firePropertyChange("defaultValue", oldval, newval);
    }

    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return false;
    }

} // end of element Parameter
} // end of entire model
