
package ca.sqlpower.architect.olap;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;


/**
 * This is class is generated from xml-to-java-classes.xsl!  Do not alter it directly.
 */
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
        setName("New Schema");
    }
    
    
    /**
     * Creates a new Schema with all mandatory
     * values passed in.
     */
    @Constructor
    public Schema(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "measuresCaption") 
        String measuresCaption
        , @ConstructorParameter(propertyName = "defaultRole") 
        String defaultRole
    ) {
        this();
        setName(name);
        
        setMeasuresCaption(measuresCaption);
    
        setDefaultRole(defaultRole);
    
    }

    
    /**
     * Creates a new Schema with all
     * attributes copied from the given Schema.
     */
    public Schema(Schema original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.measuresCaption = original.getMeasuresCaption();
    	
    	this.defaultRole = original.getDefaultRole();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Schema:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" measuresCaption = ");
	    retStr.append(measuresCaption);
	    retStr.append(",");
	    
	    retStr.append(" defaultRole = ");
	    retStr.append(defaultRole);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** Name of this schema */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Label for the measures dimension.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ measuresCaption;
    
    @Accessor
    public String /* */ getMeasuresCaption() {
        return measuresCaption;
    }
    
    @Mutator
    public void setMeasuresCaption(String /* */ newval) {
        String /* */ oldval = measuresCaption;
        measuresCaption = newval;
        firePropertyChange("measuresCaption", oldval, newval);
    }

    /** The name of the default role for connections to this schema */
    private String /* */ defaultRole;
    
    @Accessor
    public String /* */ getDefaultRole() {
        return defaultRole;
    }
    
    @Mutator
    public void setDefaultRole(String /* */ newval) {
        String /* */ oldval = defaultRole;
        defaultRole = newval;
        firePropertyChange("defaultRole", oldval, newval);
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
        int overallPosition = childPositionOffset(Parameter.class) + pos;
        fireChildAdded(Parameter.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Parameter removeParameter(int pos) {
        Parameter removedItem = parameters.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Parameter.class) + pos;
            fireChildRemoved(Parameter.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(Dimension.class) + pos;
        fireChildAdded(Dimension.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Dimension removeDimension(int pos) {
        Dimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Dimension.class) + pos;
            fireChildRemoved(Dimension.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(Cube.class) + pos;
        fireChildAdded(Cube.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Cube removeCube(int pos) {
        Cube removedItem = cubes.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Cube.class) + pos;
            fireChildRemoved(Cube.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(VirtualCube.class) + pos;
        fireChildAdded(VirtualCube.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public VirtualCube removeVirtualCube(int pos) {
        VirtualCube removedItem = virtualCubes.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(VirtualCube.class) + pos;
            fireChildRemoved(VirtualCube.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(Role.class) + pos;
        fireChildAdded(Role.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Role removeRole(int pos) {
        Role removedItem = roles.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Role.class) + pos;
            fireChildRemoved(Role.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(UserDefinedFunction.class) + pos;
        fireChildAdded(UserDefinedFunction.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public UserDefinedFunction removeUserDefinedFunction(int pos) {
        UserDefinedFunction removedItem = userDefinedFunctions.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(UserDefinedFunction.class) + pos;
            fireChildRemoved(UserDefinedFunction.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<UserDefinedFunction> getUserDefinedFunctions() {
        return Collections.unmodifiableList(userDefinedFunctions);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Parameter.class, Dimension.class, Cube.class, VirtualCube.class, NamedSet.class, Role.class, UserDefinedFunction.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
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
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Parameter.class.isAssignableFrom(childClass)) return offset;
        offset += parameters.size();
        
        if (Dimension.class.isAssignableFrom(childClass)) return offset;
        offset += dimensions.size();
        
        if (Cube.class.isAssignableFrom(childClass)) return offset;
        offset += cubes.size();
        
        if (VirtualCube.class.isAssignableFrom(childClass)) return offset;
        offset += virtualCubes.size();
        
        if (NamedSet.class.isAssignableFrom(childClass)) return offset;
        offset += namedSets.size();
        
        if (Role.class.isAssignableFrom(childClass)) return offset;
        offset += roles.size();
        
        if (UserDefinedFunction.class.isAssignableFrom(childClass)) return offset;
        offset += userDefinedFunctions.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Parameter) {
            int offset = childPositionOffset(Parameter.class);
            if (index < 0 || index > parameters.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + parameters.size());
            }
            addParameter(index, (Parameter) child);
        
        } else if (child instanceof Dimension) {
            int offset = childPositionOffset(Dimension.class);
            if (index < 0 || index > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + dimensions.size());
            }
            addDimension(index, (Dimension) child);
        
        } else if (child instanceof Cube) {
            int offset = childPositionOffset(Cube.class);
            if (index < 0 || index > cubes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + cubes.size());
            }
            addCube(index, (Cube) child);
        
        } else if (child instanceof VirtualCube) {
            int offset = childPositionOffset(VirtualCube.class);
            if (index < 0 || index > virtualCubes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + virtualCubes.size());
            }
            addVirtualCube(index, (VirtualCube) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if (index < 0 || index > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index, (NamedSet) child);
        
        } else if (child instanceof Role) {
            int offset = childPositionOffset(Role.class);
            if (index < 0 || index > roles.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + roles.size());
            }
            addRole(index, (Role) child);
        
        } else if (child instanceof UserDefinedFunction) {
            int offset = childPositionOffset(UserDefinedFunction.class);
            if (index < 0 || index > userDefinedFunctions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + userDefinedFunctions.size());
            }
            addUserDefinedFunction(index, (UserDefinedFunction) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Parameter) {
            return removeParameter((Parameter) child);
        
        } else if (child instanceof Dimension) {
            return removeDimension((Dimension) child);
        
        } else if (child instanceof Cube) {
            return removeCube((Cube) child);
        
        } else if (child instanceof VirtualCube) {
            return removeVirtualCube((VirtualCube) child);
        
        } else if (child instanceof NamedSet) {
            return removeNamedSet((NamedSet) child);
        
        } else if (child instanceof Role) {
            return removeRole((Role) child);
        
        } else if (child instanceof UserDefinedFunction) {
            return removeUserDefinedFunction((UserDefinedFunction) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Schema
/** 
            A CubeDimension is either a usage of a Dimension ('shared
            dimension', in MSOLAP parlance), or a 'private dimension'.
         */
public abstract static class CubeDimension extends OLAPObject {
    
    /**
     * Creates a new CubeDimension with all attributes
     * set to their defaults.
     */
    public CubeDimension() {
    setName("New CubeDimension");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public CubeDimension(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "foreignKey") 
        String foreignKey
    ) {
        this();
        setName(name);
        
        setCaption(caption);
    
        setForeignKey(foreignKey);
    
    }

    
    /**
     * Creates a new CubeDimension with all
     * attributes copied from the given CubeDimension.
     */
    public CubeDimension(CubeDimension original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.caption = original.getCaption();
    	
    	this.foreignKey = original.getForeignKey();
    	
    }
    

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the Dimension's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the column in the fact table which joins
                to the leaf level of this dimension. Required in a
                private Dimension or a DimensionUsage, but not in a
                public Dimension.
             */
    private String /* */ foreignKey;
    
    @Accessor
    public String /* */ getForeignKey() {
        return foreignKey;
    }
    
    @Mutator
    public void setForeignKey(String /* */ newval) {
        String /* */ oldval = foreignKey;
        foreignKey = newval;
        firePropertyChange("foreignKey", oldval, newval);
    }


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CubeDimension:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" foreignKey = ");
	    retStr.append(foreignKey);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Cube");
    }
    
    
    /**
     * Creates a new Cube with all mandatory
     * values passed in.
     */
    @Constructor
    public Cube(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "defaultMeasure") 
        String defaultMeasure
        , @ConstructorParameter(propertyName = "cache") 
        Boolean cache
        , @ConstructorParameter(propertyName = "enabled") 
        Boolean enabled
    ) {
        this();
        setName(name);
        
        setCaption(caption);
    
        setDefaultMeasure(defaultMeasure);
    
        setCache(cache);
    
        setEnabled(enabled);
    
    }

    
    /**
     * Creates a new Cube with all
     * attributes copied from the given Cube.
     */
    public Cube(Cube original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.caption = original.getCaption();
    	
    	this.defaultMeasure = original.getDefaultMeasure();
    	
    	this.cache = original.getCache();
    	
    	this.enabled = original.getEnabled();
    	
    	this.fact = original.getFact();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Cube:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" defaultMeasure = ");
	    retStr.append(defaultMeasure);
	    retStr.append(",");
	    
	    retStr.append(" cache = ");
	    retStr.append(cache);
	    retStr.append(",");
	    
	    retStr.append(" enabled = ");
	    retStr.append(enabled);
	    retStr.append(",");
	    
	    retStr.append(" fact = ");
	    retStr.append(fact);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of this cube.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the cube's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the measure that would be taken as the default
                measure of the cube.
             */
    private String /* */ defaultMeasure;
    
    @Accessor
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    @Mutator
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        firePropertyChange("defaultMeasure", oldval, newval);
    }

    /** 
                Should the Fact table data for this Cube be cached
                by Mondrian or not. The default action is to cache
                the data.
             */
    private Boolean /* */ cache;
    
    @Accessor
    public Boolean /* */ getCache() {
        return cache;
    }
    
    @Mutator
    public void setCache(Boolean /* */ newval) {
        Boolean /* */ oldval = cache;
        cache = newval;
        firePropertyChange("cache", oldval, newval);
    }

    /** 
                Whether element is enabled - if true, then the Cube is
                realized otherwise it is ignored.
             */
    private Boolean /* */ enabled;
    
    @Accessor
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    @Mutator
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        firePropertyChange("enabled", oldval, newval);
    }

    /** 
                The fact table is the source of all measures in this cube. If
                this is a Table and the schema name is not
                present, table name is left unqualified.
             */
    private Relation /* */ fact;
    
    @NonProperty
    public Relation /* */ getFact() {
        return fact;
    }
    
    @NonProperty
    public void setFact(Relation /* */ newval) {
        Relation /* */ oldval = fact;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Relation.class);
        if (fact != null) {
            fireChildRemoved(Relation.class, oldval, overallPosition);
        }
        fact = newval;
        if (fact != null) {
            fact.setParent(this);
            fireChildAdded(Relation.class, fact, overallPosition);
        }
	}

    /**  */
    private final List<CubeDimension> dimensions = new ArrayList<CubeDimension>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimension(int pos, CubeDimension newChild) {
        dimensions.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(CubeDimension.class) + pos;
        fireChildAdded(CubeDimension.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CubeDimension removeDimension(int pos) {
        CubeDimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CubeDimension.class) + pos;
            fireChildRemoved(CubeDimension.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(Measure.class) + pos;
        fireChildAdded(Measure.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Measure removeMeasure(int pos) {
        Measure removedItem = measures.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Measure.class) + pos;
            fireChildRemoved(Measure.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
        fireChildAdded(CalculatedMember.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CalculatedMember removeCalculatedMember(int pos) {
        CalculatedMember removedItem = calculatedMembers.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
            fireChildRemoved(CalculatedMember.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            CubeDimension.class, Measure.class, CalculatedMember.class, NamedSet.class,Relation.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        if (fact != null) {
        	children.add(fact);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (CubeDimension.class.isAssignableFrom(childClass)) return offset;
        offset += dimensions.size();
        
        if (Measure.class.isAssignableFrom(childClass)) return offset;
        offset += measures.size();
        
        if (CalculatedMember.class.isAssignableFrom(childClass)) return offset;
        offset += calculatedMembers.size();
        
        if (NamedSet.class.isAssignableFrom(childClass)) return offset;
        offset += namedSets.size();
        
        if (Relation.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof CubeDimension) {
            int offset = childPositionOffset(CubeDimension.class);
            if (index < 0 || index > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + dimensions.size());
            }
            addDimension(index, (CubeDimension) child);
        
        } else if (child instanceof Measure) {
            int offset = childPositionOffset(Measure.class);
            if (index < 0 || index > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + measures.size());
            }
            addMeasure(index, (Measure) child);
        
        } else if (child instanceof CalculatedMember) {
            int offset = childPositionOffset(CalculatedMember.class);
            if (index < 0 || index > calculatedMembers.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + calculatedMembers.size());
            }
            addCalculatedMember(index, (CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if (index < 0 || index > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index, (NamedSet) child);
        
        } else if (child instanceof Relation) {
            setFact((Relation) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CubeDimension) {
            return removeDimension((CubeDimension) child);
        
        } else if (child instanceof Measure) {
            return removeMeasure((Measure) child);
        
        } else if (child instanceof CalculatedMember) {
            return removeCalculatedMember((CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            return removeNamedSet((NamedSet) child);
        
        } else if (child instanceof Relation) {
            setFact(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New VirtualCube");
    }
    
    
    /**
     * Creates a new VirtualCube with all mandatory
     * values passed in.
     */
    @Constructor
    public VirtualCube(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "enabled") 
        Boolean enabled
        , @ConstructorParameter(propertyName = "defaultMeasure") 
        String defaultMeasure
        , @ConstructorParameter(propertyName = "caption") 
        String caption
    ) {
        this();
        setName(name);
        
        setEnabled(enabled);
    
        setDefaultMeasure(defaultMeasure);
    
        setCaption(caption);
    
    }

    
    /**
     * Creates a new VirtualCube with all
     * attributes copied from the given VirtualCube.
     */
    public VirtualCube(VirtualCube original) {
    	super(original);
    	
    	this.enabled = original.getEnabled();
    	
    	this.name = original.getName();
    	
    	this.defaultMeasure = original.getDefaultMeasure();
    	
    	this.caption = original.getCaption();
    	
    	this.cubeUsage = original.getCubeUsage();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("VirtualCube:");
	    
	    retStr.append(" enabled = ");
	    retStr.append(enabled);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" defaultMeasure = ");
	    retStr.append(defaultMeasure);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" cubeUsage = ");
	    retStr.append(cubeUsage);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Whether this element is enabled - if true, then the Virtual
                Cube is realized otherwise it is ignored.
             */
    private Boolean /* */ enabled;
    
    @Accessor
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    @Mutator
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        firePropertyChange("enabled", oldval, newval);
    }

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** The name of the measure that would be taken as the default
                measure of the cube.
             */
    private String /* */ defaultMeasure;
    
    @Accessor
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    @Mutator
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        firePropertyChange("defaultMeasure", oldval, newval);
    }

    /** 
                A string being displayed instead of the cube's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /**  */
    private CubeUsages /* */ cubeUsage;
    
    @NonProperty
    public CubeUsages /* */ getCubeUsage() {
        return cubeUsage;
    }
    
    @NonProperty
    public void setCubeUsage(CubeUsages /* */ newval) {
        CubeUsages /* */ oldval = cubeUsage;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(CubeUsages.class);
        if (cubeUsage != null) {
            fireChildRemoved(CubeUsages.class, oldval, overallPosition);
        }
        cubeUsage = newval;
        if (cubeUsage != null) {
            cubeUsage.setParent(this);
            fireChildAdded(CubeUsages.class, cubeUsage, overallPosition);
        }
	}

    /**  */
    private final List<VirtualCubeDimension> dimensions = new ArrayList<VirtualCubeDimension>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimension(int pos, VirtualCubeDimension newChild) {
        dimensions.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(VirtualCubeDimension.class) + pos;
        fireChildAdded(VirtualCubeDimension.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public VirtualCubeDimension removeDimension(int pos) {
        VirtualCubeDimension removedItem = dimensions.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(VirtualCubeDimension.class) + pos;
            fireChildRemoved(VirtualCubeDimension.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(VirtualCubeMeasure.class) + pos;
        fireChildAdded(VirtualCubeMeasure.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public VirtualCubeMeasure removeMeasure(int pos) {
        VirtualCubeMeasure removedItem = measures.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(VirtualCubeMeasure.class) + pos;
            fireChildRemoved(VirtualCubeMeasure.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
        fireChildAdded(CalculatedMember.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CalculatedMember removeCalculatedMember(int pos) {
        CalculatedMember removedItem = calculatedMembers.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
            fireChildRemoved(CalculatedMember.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public NamedSet removeNamedSet(int pos) {
        NamedSet removedItem = namedSets.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            VirtualCubeDimension.class, VirtualCubeMeasure.class, CalculatedMember.class, NamedSet.class,CubeUsages.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        if (cubeUsage != null) {
        	children.add(cubeUsage);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (VirtualCubeDimension.class.isAssignableFrom(childClass)) return offset;
        offset += dimensions.size();
        
        if (VirtualCubeMeasure.class.isAssignableFrom(childClass)) return offset;
        offset += measures.size();
        
        if (CalculatedMember.class.isAssignableFrom(childClass)) return offset;
        offset += calculatedMembers.size();
        
        if (NamedSet.class.isAssignableFrom(childClass)) return offset;
        offset += namedSets.size();
        
        if (CubeUsages.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof VirtualCubeDimension) {
            int offset = childPositionOffset(VirtualCubeDimension.class);
            if (index < 0 || index > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + dimensions.size());
            }
            addDimension(index, (VirtualCubeDimension) child);
        
        } else if (child instanceof VirtualCubeMeasure) {
            int offset = childPositionOffset(VirtualCubeMeasure.class);
            if (index < 0 || index > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + measures.size());
            }
            addMeasure(index, (VirtualCubeMeasure) child);
        
        } else if (child instanceof CalculatedMember) {
            int offset = childPositionOffset(CalculatedMember.class);
            if (index < 0 || index > calculatedMembers.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + calculatedMembers.size());
            }
            addCalculatedMember(index, (CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if (index < 0 || index > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index, (NamedSet) child);
        
        } else if (child instanceof CubeUsages) {
            setCubeUsage((CubeUsages) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof VirtualCubeDimension) {
            return removeDimension((VirtualCubeDimension) child);
        
        } else if (child instanceof VirtualCubeMeasure) {
            return removeMeasure((VirtualCubeMeasure) child);
        
        } else if (child instanceof CalculatedMember) {
            return removeCalculatedMember((CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            return removeNamedSet((NamedSet) child);
        
        } else if (child instanceof CubeUsages) {
            setCubeUsage(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New CubeUsages");
    }
    
    
    /**
     * Creates a new CubeUsages with all mandatory
     * values passed in.
     */
    @Constructor
    public CubeUsages(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new CubeUsages with all
     * attributes copied from the given CubeUsages.
     */
    public CubeUsages(CubeUsages original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CubeUsages:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private final List<CubeUsage> cubeUsages = new ArrayList<CubeUsage>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addCubeUsage(int pos, CubeUsage newChild) {
        cubeUsages.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(CubeUsage.class) + pos;
        fireChildAdded(CubeUsage.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CubeUsage removeCubeUsage(int pos) {
        CubeUsage removedItem = cubeUsages.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CubeUsage.class) + pos;
            fireChildRemoved(CubeUsage.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<CubeUsage> getCubeUsages() {
        return Collections.unmodifiableList(cubeUsages);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            CubeUsage.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(cubeUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (CubeUsage.class.isAssignableFrom(childClass)) return offset;
        offset += cubeUsages.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof CubeUsage) {
            int offset = childPositionOffset(CubeUsage.class);
            if (index < 0 || index > cubeUsages.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + cubeUsages.size());
            }
            addCubeUsage(index, (CubeUsage) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CubeUsage) {
            return removeCubeUsage((CubeUsage) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element CubeUsages
/**  */
public static class CubeUsage extends OLAPObject {
    
    /**
     * Creates a new CubeUsage with all attributes
     * set to their defaults.
     */
    public CubeUsage() {
        setName("New CubeUsage");
    }
    
    
    /**
     * Creates a new CubeUsage with all mandatory
     * values passed in.
     */
    @Constructor
    public CubeUsage(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "cubeName") 
        String cubeName
        , @ConstructorParameter(propertyName = "ignoreUnrelatedDimensions") 
        Boolean ignoreUnrelatedDimensions
    ) {
        this();
        setName(name);
        
        setCubeName(cubeName);
    
        setIgnoreUnrelatedDimensions(ignoreUnrelatedDimensions);
    
    }

    
    /**
     * Creates a new CubeUsage with all
     * attributes copied from the given CubeUsage.
     */
    public CubeUsage(CubeUsage original) {
    	super(original);
    	
    	this.cubeName = original.getCubeName();
    	
    	this.ignoreUnrelatedDimensions = original.getIgnoreUnrelatedDimensions();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CubeUsage:");
	    
	    retStr.append(" cubeName = ");
	    retStr.append(cubeName);
	    retStr.append(",");
	    
	    retStr.append(" ignoreUnrelatedDimensions = ");
	    retStr.append(ignoreUnrelatedDimensions);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of the cube which the virtualCube uses.
             */
    private String /* */ cubeName;
    
    @Accessor
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    @Mutator
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Unrelated dimensions to measures in this cube will be pushed to
                top level member.
             */
    private Boolean /* */ ignoreUnrelatedDimensions;
    
    @Accessor
    public Boolean /* */ getIgnoreUnrelatedDimensions() {
        return ignoreUnrelatedDimensions;
    }
    
    @Mutator
    public void setIgnoreUnrelatedDimensions(Boolean /* */ newval) {
        Boolean /* */ oldval = ignoreUnrelatedDimensions;
        ignoreUnrelatedDimensions = newval;
        firePropertyChange("ignoreUnrelatedDimensions", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New VirtualCubeDimension");
    }
    
    
    /**
     * Creates a new VirtualCubeDimension with all mandatory
     * values passed in.
     */
    @Constructor
    public VirtualCubeDimension(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "cubeName") 
        String cubeName
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "foreignKey") 
        String foreignKey
    ) {
        this();
        setName(name);
        
        setCubeName(cubeName);
    
        setCaption(caption);
    
        setForeignKey(foreignKey);
    
    }

    
    /**
     * Creates a new VirtualCubeDimension with all
     * attributes copied from the given VirtualCubeDimension.
     */
    public VirtualCubeDimension(VirtualCubeDimension original) {
    	super(original);
    	
    	this.cubeName = original.getCubeName();
    	
    	this.name = original.getName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("VirtualCubeDimension:");
	    
	    retStr.append(" cubeName = ");
	    retStr.append(cubeName);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                Name of the cube which the dimension belongs to, or
                unspecified if the dimension is shared.
             */
    private String /* */ cubeName;
    
    @Accessor
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    @Mutator
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Name of the dimension.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    CubeDimension.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New VirtualCubeMeasure");
    }
    
    
    /**
     * Creates a new VirtualCubeMeasure with all mandatory
     * values passed in.
     */
    @Constructor
    public VirtualCubeMeasure(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "cubeName") 
        String cubeName
        , @ConstructorParameter(propertyName = "visible") 
        Boolean visible
    ) {
        this();
        setName(name);
        
        setCubeName(cubeName);
    
        setVisible(visible);
    
    }

    
    /**
     * Creates a new VirtualCubeMeasure with all
     * attributes copied from the given VirtualCubeMeasure.
     */
    public VirtualCubeMeasure(VirtualCubeMeasure original) {
    	super(original);
    	
    	this.cubeName = original.getCubeName();
    	
    	this.name = original.getName();
    	
    	this.visible = original.getVisible();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("VirtualCubeMeasure:");
	    
	    retStr.append(" cubeName = ");
	    retStr.append(cubeName);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" visible = ");
	    retStr.append(visible);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of the cube which the measure belongs to.
             */
    private String /* */ cubeName;
    
    @Accessor
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    @Mutator
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        firePropertyChange("cubeName", oldval, newval);
    }

    /** 
                Unique name of the measure within its cube.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    @Accessor
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    @Mutator
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        firePropertyChange("visible", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New DimensionUsage");
    }
    
    
    /**
     * Creates a new DimensionUsage with all mandatory
     * values passed in.
     */
    @Constructor
    public DimensionUsage(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "source") 
        String source
        , @ConstructorParameter(propertyName = "level") 
        String level
        , @ConstructorParameter(propertyName = "usagePrefix") 
        String usagePrefix
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "foreignKey") 
        String foreignKey
    ) {
        this();
        setName(name);
        
        setSource(source);
    
        setLevel(level);
    
        setUsagePrefix(usagePrefix);
    
        setCaption(caption);
    
        setForeignKey(foreignKey);
    
    }

    
    /**
     * Creates a new DimensionUsage with all
     * attributes copied from the given DimensionUsage.
     */
    public DimensionUsage(DimensionUsage original) {
    	super(original);
    	
    	this.source = original.getSource();
    	
    	this.level = original.getLevel();
    	
    	this.usagePrefix = original.getUsagePrefix();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("DimensionUsage:");
	    
	    retStr.append(" source = ");
	    retStr.append(source);
	    retStr.append(",");
	    
	    retStr.append(" level = ");
	    retStr.append(level);
	    retStr.append(",");
	    
	    retStr.append(" usagePrefix = ");
	    retStr.append(usagePrefix);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** Name of the source dimension. Must be a dimension in
            this schema. Case-sensitive. */
    private String /* */ source;
    
    @Accessor
    public String /* */ getSource() {
        return source;
    }
    
    @Mutator
    public void setSource(String /* */ newval) {
        String /* */ oldval = source;
        source = newval;
        firePropertyChange("source", oldval, newval);
    }

    /** 
                Name of the level to join to. If not specified, joins to the
                lowest level of the dimension.
             */
    private String /* */ level;
    
    @Accessor
    public String /* */ getLevel() {
        return level;
    }
    
    @Mutator
    public void setLevel(String /* */ newval) {
        String /* */ oldval = level;
        level = newval;
        firePropertyChange("level", oldval, newval);
    }

    /** 
                If present, then this is prepended to the Dimension column
                names during the building of collapse dimension aggregates
                allowing 1) different dimension usages to be disambiguated
                during aggregate table recognition and 2) multiple shared
                dimensions that have common column names to be disambiguated.
             */
    private String /* */ usagePrefix;
    
    @Accessor
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    @Mutator
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        firePropertyChange("usagePrefix", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    CubeDimension.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Dimension");
    }
    
    
    /**
     * Creates a new Dimension with all mandatory
     * values passed in.
     */
    @Constructor
    public Dimension(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "type") 
        String type
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "usagePrefix") 
        String usagePrefix
        , @ConstructorParameter(propertyName = "foreignKey") 
        String foreignKey
    ) {
        this();
        setName(name);
        
        setType(type);
    
        setCaption(caption);
    
        setUsagePrefix(usagePrefix);
    
        setCaption(caption);
    
        setForeignKey(foreignKey);
    
    }

    
    /**
     * Creates a new Dimension with all
     * attributes copied from the given Dimension.
     */
    public Dimension(Dimension original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.type = original.getType();
    	
    	this.caption = original.getCaption();
    	
    	this.usagePrefix = original.getUsagePrefix();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Dimension:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" type = ");
	    retStr.append(type);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" usagePrefix = ");
	    retStr.append(usagePrefix);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
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
    
    @Accessor
    public String /* */ getType() {
        return type;
    }
    
    @Mutator
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        firePropertyChange("type", oldval, newval);
    }

    /** 
                A string being displayed instead of the dimensions's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                If present, then this is prepended to the Dimension column
                names during the building of collapse dimension aggregates
                allowing 1) different dimensions to be disambiguated
                during aggregate table recognition.
                This should only be set for private dimensions.
             */
    private String /* */ usagePrefix;
    
    @Accessor
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    @Mutator
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        firePropertyChange("usagePrefix", oldval, newval);
    }

    /**  */
    private final List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addHierarchy(int pos, Hierarchy newChild) {
        hierarchies.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(Hierarchy.class) + pos;
        fireChildAdded(Hierarchy.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Hierarchy removeHierarchy(int pos) {
        Hierarchy removedItem = hierarchies.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Hierarchy.class) + pos;
            fireChildRemoved(Hierarchy.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<Hierarchy> getHierarchies() {
        return Collections.unmodifiableList(hierarchies);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Hierarchy.class));  
        childTypes.addAll(CubeDimension.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(hierarchies);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Hierarchy.class.isAssignableFrom(childClass)) return offset;
        offset += hierarchies.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Hierarchy) {
            int offset = childPositionOffset(Hierarchy.class);
            if (index < 0 || index > hierarchies.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + hierarchies.size());
            }
            addHierarchy(index, (Hierarchy) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Hierarchy) {
            return removeHierarchy((Hierarchy) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Hierarchy");
    }
    
    
    /**
     * Creates a new Hierarchy with all mandatory
     * values passed in.
     */
    @Constructor
    public Hierarchy(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "hasAll") 
        Boolean hasAll
        , @ConstructorParameter(propertyName = "allMemberName") 
        String allMemberName
        , @ConstructorParameter(propertyName = "allMemberCaption") 
        String allMemberCaption
        , @ConstructorParameter(propertyName = "allLevelName") 
        String allLevelName
        , @ConstructorParameter(propertyName = "primaryKey") 
        String primaryKey
        , @ConstructorParameter(propertyName = "primaryKeyTable") 
        String primaryKeyTable
        , @ConstructorParameter(propertyName = "defaultMember") 
        String defaultMember
        , @ConstructorParameter(propertyName = "memberReaderClass") 
        String memberReaderClass
        , @ConstructorParameter(propertyName = "caption") 
        String caption
    ) {
        this();
        setName(name);
        
        setHasAll(hasAll);
    
        setAllMemberName(allMemberName);
    
        setAllMemberCaption(allMemberCaption);
    
        setAllLevelName(allLevelName);
    
        setPrimaryKey(primaryKey);
    
        setPrimaryKeyTable(primaryKeyTable);
    
        setDefaultMember(defaultMember);
    
        setMemberReaderClass(memberReaderClass);
    
        setCaption(caption);
    
    }

    
    /**
     * Creates a new Hierarchy with all
     * attributes copied from the given Hierarchy.
     */
    public Hierarchy(Hierarchy original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.hasAll = original.getHasAll();
    	
    	this.allMemberName = original.getAllMemberName();
    	
    	this.allMemberCaption = original.getAllMemberCaption();
    	
    	this.allLevelName = original.getAllLevelName();
    	
    	this.primaryKey = original.getPrimaryKey();
    	
    	this.primaryKeyTable = original.getPrimaryKeyTable();
    	
    	this.defaultMember = original.getDefaultMember();
    	
    	this.memberReaderClass = original.getMemberReaderClass();
    	
    	this.caption = original.getCaption();
    	
    	this.relation = original.getRelation();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Hierarchy:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" hasAll = ");
	    retStr.append(hasAll);
	    retStr.append(",");
	    
	    retStr.append(" allMemberName = ");
	    retStr.append(allMemberName);
	    retStr.append(",");
	    
	    retStr.append(" allMemberCaption = ");
	    retStr.append(allMemberCaption);
	    retStr.append(",");
	    
	    retStr.append(" allLevelName = ");
	    retStr.append(allLevelName);
	    retStr.append(",");
	    
	    retStr.append(" primaryKey = ");
	    retStr.append(primaryKey);
	    retStr.append(",");
	    
	    retStr.append(" primaryKeyTable = ");
	    retStr.append(primaryKeyTable);
	    retStr.append(",");
	    
	    retStr.append(" defaultMember = ");
	    retStr.append(defaultMember);
	    retStr.append(",");
	    
	    retStr.append(" memberReaderClass = ");
	    retStr.append(memberReaderClass);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" relation = ");
	    retStr.append(relation);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of the hierarchy. If this is not specified, the hierarchy
                has the same name as its dimension.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether this hierarchy has an 'all' member.
             */
    private Boolean /* */ hasAll;
    
    @Accessor
    public Boolean /* */ getHasAll() {
        return hasAll;
    }
    
    @Mutator
    public void setHasAll(Boolean /* */ newval) {
        Boolean /* */ oldval = hasAll;
        hasAll = newval;
        firePropertyChange("hasAll", oldval, newval);
    }

    /** 
                Name of the 'all' member. If this attribute is not specified,
                the all member is named 'All hierarchyName', for
                example, 'All Store'.
             */
    private String /* */ allMemberName;
    
    @Accessor
    public String /* */ getAllMemberName() {
        return allMemberName;
    }
    
    @Mutator
    public void setAllMemberName(String /* */ newval) {
        String /* */ oldval = allMemberName;
        allMemberName = newval;
        firePropertyChange("allMemberName", oldval, newval);
    }

    /** 
                A string being displayed instead as the all member's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ allMemberCaption;
    
    @Accessor
    public String /* */ getAllMemberCaption() {
        return allMemberCaption;
    }
    
    @Mutator
    public void setAllMemberCaption(String /* */ newval) {
        String /* */ oldval = allMemberCaption;
        allMemberCaption = newval;
        firePropertyChange("allMemberCaption", oldval, newval);
    }

    /** 
                Name of the 'all' level. If this attribute is not specified,
                the all member is named '(All)'.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ allLevelName;
    
    @Accessor
    public String /* */ getAllLevelName() {
        return allLevelName;
    }
    
    @Mutator
    public void setAllLevelName(String /* */ newval) {
        String /* */ oldval = allLevelName;
        allLevelName = newval;
        firePropertyChange("allLevelName", oldval, newval);
    }

    /** 
                The name of the column which identifies members, and
                which is referenced by rows in the fact table.
                If not specified, the key of the lowest level is used.
                See also CubeDimension.foreignKey.
             */
    private String /* */ primaryKey;
    
    @Accessor
    public String /* */ getPrimaryKey() {
        return primaryKey;
    }
    
    @Mutator
    public void setPrimaryKey(String /* */ newval) {
        String /* */ oldval = primaryKey;
        primaryKey = newval;
        firePropertyChange("primaryKey", oldval, newval);
    }

    /** 
                The name of the table which contains primaryKey.
                If the hierarchy has only one table, defaults to that;
                it is required.
             */
    private String /* */ primaryKeyTable;
    
    @Accessor
    public String /* */ getPrimaryKeyTable() {
        return primaryKeyTable;
    }
    
    @Mutator
    public void setPrimaryKeyTable(String /* */ newval) {
        String /* */ oldval = primaryKeyTable;
        primaryKeyTable = newval;
        firePropertyChange("primaryKeyTable", oldval, newval);
    }

    /**  */
    private String /* */ defaultMember;
    
    @Accessor
    public String /* */ getDefaultMember() {
        return defaultMember;
    }
    
    @Mutator
    public void setDefaultMember(String /* */ newval) {
        String /* */ oldval = defaultMember;
        defaultMember = newval;
        firePropertyChange("defaultMember", oldval, newval);
    }

    /** 
                Name of the custom member reader class. Must implement
                the mondrian.rolap.MemberReader interface.
             */
    private String /* */ memberReaderClass;
    
    @Accessor
    public String /* */ getMemberReaderClass() {
        return memberReaderClass;
    }
    
    @Mutator
    public void setMemberReaderClass(String /* */ newval) {
        String /* */ oldval = memberReaderClass;
        memberReaderClass = newval;
        firePropertyChange("memberReaderClass", oldval, newval);
    }

    /** 
                A string to be displayed in the user interface.
                If not specified, the hierarchy's name is used.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                The {@link MondrianDef.Table table},
                {@link MondrianDef.Join set of tables},
                {@link MondrianDef.View SQL statement}, or
                {@link MondrianDef.InlineTable inline table}
                which populates this hierarchy.
             */
    private RelationOrJoin /* */ relation;
    
    @NonProperty
    public RelationOrJoin /* */ getRelation() {
        return relation;
    }
    
    @NonProperty
    public void setRelation(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = relation;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (relation != null) {
            fireChildRemoved(RelationOrJoin.class, oldval, overallPosition);
        }
        relation = newval;
        if (relation != null) {
            relation.setParent(this);
            fireChildAdded(RelationOrJoin.class, relation, overallPosition);
        }
	}

    /**  */
    private final List<Level> levels = new ArrayList<Level>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addLevel(int pos, Level newChild) {
        levels.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(Level.class) + pos;
        fireChildAdded(Level.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Level removeLevel(int pos) {
        Level removedItem = levels.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Level.class) + pos;
            fireChildRemoved(Level.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(MemberReaderParameter.class) + pos;
        fireChildAdded(MemberReaderParameter.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public MemberReaderParameter removeMemberReaderParameter(int pos) {
        MemberReaderParameter removedItem = memberReaderParameters.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(MemberReaderParameter.class) + pos;
            fireChildRemoved(MemberReaderParameter.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<MemberReaderParameter> getMemberReaderParameters() {
        return Collections.unmodifiableList(memberReaderParameters);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Level.class, MemberReaderParameter.class,RelationOrJoin.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(levels);
        
        children.addAll(memberReaderParameters);
        
        if (relation != null) {
        	children.add(relation);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Level.class.isAssignableFrom(childClass)) return offset;
        offset += levels.size();
        
        if (MemberReaderParameter.class.isAssignableFrom(childClass)) return offset;
        offset += memberReaderParameters.size();
        
        if (RelationOrJoin.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Level) {
            int offset = childPositionOffset(Level.class);
            if (index < 0 || index > levels.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + levels.size());
            }
            addLevel(index, (Level) child);
        
        } else if (child instanceof MemberReaderParameter) {
            int offset = childPositionOffset(MemberReaderParameter.class);
            if (index < 0 || index > memberReaderParameters.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + memberReaderParameters.size());
            }
            addMemberReaderParameter(index, (MemberReaderParameter) child);
        
        } else if (child instanceof RelationOrJoin) {
            setRelation((RelationOrJoin) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Level) {
            return removeLevel((Level) child);
        
        } else if (child instanceof MemberReaderParameter) {
            return removeMemberReaderParameter((MemberReaderParameter) child);
        
        } else if (child instanceof RelationOrJoin) {
            setRelation(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Hierarchy
/**  */
public static class Level extends OLAPObject {
    
    /**
     * Creates a new Level with all attributes
     * set to their defaults.
     */
    public Level() {
        setName("New Level");
    }
    
    
    /**
     * Creates a new Level with all mandatory
     * values passed in.
     */
    @Constructor
    public Level(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "approxRowCount") 
        String approxRowCount
        , @ConstructorParameter(propertyName = "table") 
        String table
        , @ConstructorParameter(propertyName = "column") 
        String column
        , @ConstructorParameter(propertyName = "nameColumn") 
        String nameColumn
        , @ConstructorParameter(propertyName = "ordinalColumn") 
        String ordinalColumn
        , @ConstructorParameter(propertyName = "parentColumn") 
        String parentColumn
        , @ConstructorParameter(propertyName = "nullParentValue") 
        String nullParentValue
        , @ConstructorParameter(propertyName = "type") 
        String type
        , @ConstructorParameter(propertyName = "uniqueMembers") 
        Boolean uniqueMembers
        , @ConstructorParameter(propertyName = "levelType") 
        String levelType
        , @ConstructorParameter(propertyName = "hideMemberIf") 
        String hideMemberIf
        , @ConstructorParameter(propertyName = "formatter") 
        String formatter
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "captionColumn") 
        String captionColumn
    ) {
        this();
        setName(name);
        
        setApproxRowCount(approxRowCount);
    
        setTable(table);
    
        setColumn(column);
    
        setNameColumn(nameColumn);
    
        setOrdinalColumn(ordinalColumn);
    
        setParentColumn(parentColumn);
    
        setNullParentValue(nullParentValue);
    
        setType(type);
    
        setUniqueMembers(uniqueMembers);
    
        setLevelType(levelType);
    
        setHideMemberIf(hideMemberIf);
    
        setFormatter(formatter);
    
        setCaption(caption);
    
        setCaptionColumn(captionColumn);
    
    }

    
    /**
     * Creates a new Level with all
     * attributes copied from the given Level.
     */
    public Level(Level original) {
    	super(original);
    	
    	this.approxRowCount = original.getApproxRowCount();
    	
    	this.name = original.getName();
    	
    	this.table = original.getTable();
    	
    	this.column = original.getColumn();
    	
    	this.nameColumn = original.getNameColumn();
    	
    	this.ordinalColumn = original.getOrdinalColumn();
    	
    	this.parentColumn = original.getParentColumn();
    	
    	this.nullParentValue = original.getNullParentValue();
    	
    	this.type = original.getType();
    	
    	this.uniqueMembers = original.getUniqueMembers();
    	
    	this.levelType = original.getLevelType();
    	
    	this.hideMemberIf = original.getHideMemberIf();
    	
    	this.formatter = original.getFormatter();
    	
    	this.caption = original.getCaption();
    	
    	this.captionColumn = original.getCaptionColumn();
    	
    	this.keyExp = original.getKeyExp();
    	
    	this.nameExp = original.getNameExp();
    	
    	this.ordinalExp = original.getOrdinalExp();
    	
    	this.parentExp = original.getParentExp();
    	
    	this.closure = original.getClosure();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Level:");
	    
	    retStr.append(" approxRowCount = ");
	    retStr.append(approxRowCount);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" table = ");
	    retStr.append(table);
	    retStr.append(",");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr.append(" nameColumn = ");
	    retStr.append(nameColumn);
	    retStr.append(",");
	    
	    retStr.append(" ordinalColumn = ");
	    retStr.append(ordinalColumn);
	    retStr.append(",");
	    
	    retStr.append(" parentColumn = ");
	    retStr.append(parentColumn);
	    retStr.append(",");
	    
	    retStr.append(" nullParentValue = ");
	    retStr.append(nullParentValue);
	    retStr.append(",");
	    
	    retStr.append(" type = ");
	    retStr.append(type);
	    retStr.append(",");
	    
	    retStr.append(" uniqueMembers = ");
	    retStr.append(uniqueMembers);
	    retStr.append(",");
	    
	    retStr.append(" levelType = ");
	    retStr.append(levelType);
	    retStr.append(",");
	    
	    retStr.append(" hideMemberIf = ");
	    retStr.append(hideMemberIf);
	    retStr.append(",");
	    
	    retStr.append(" formatter = ");
	    retStr.append(formatter);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" captionColumn = ");
	    retStr.append(captionColumn);
	    retStr.append(",");
	    
	    retStr.append(" keyExp = ");
	    retStr.append(keyExp);
	    retStr.append(",");
	    
	    retStr.append(" nameExp = ");
	    retStr.append(nameExp);
	    retStr.append(",");
	    
	    retStr.append(" ordinalExp = ");
	    retStr.append(ordinalExp);
	    retStr.append(",");
	    
	    retStr.append(" parentExp = ");
	    retStr.append(parentExp);
	    retStr.append(",");
	    
	    retStr.append(" closure = ");
	    retStr.append(closure);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                The estimated number of members in this level.
                Setting this property improves the performance of
                MDSCHEMA_LEVELS, MDSCHEMA_HIERARCHIES and
                MDSCHEMA_DIMENSIONS XMLA requests
             */
    private String /* */ approxRowCount;
    
    @Accessor
    public String /* */ getApproxRowCount() {
        return approxRowCount;
    }
    
    @Mutator
    public void setApproxRowCount(String /* */ newval) {
        String /* */ oldval = approxRowCount;
        approxRowCount = newval;
        firePropertyChange("approxRowCount", oldval, newval);
    }

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                The name of the table that the column comes from. If
                this hierarchy is based upon just one table, defaults to
                the name of that table; otherwise, it is required.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ table;
    
    @Accessor
    public String /* */ getTable() {
        return table;
    }
    
    @Mutator
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        firePropertyChange("table", oldval, newval);
    }

    /** 
                The name of the column which holds the unique identifier of
                this level.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the column which holds the user identifier of
                this level.
             */
    private String /* */ nameColumn;
    
    @Accessor
    public String /* */ getNameColumn() {
        return nameColumn;
    }
    
    @Mutator
    public void setNameColumn(String /* */ newval) {
        String /* */ oldval = nameColumn;
        nameColumn = newval;
        firePropertyChange("nameColumn", oldval, newval);
    }

    /** 
                The name of the column which holds member
                ordinals.  If this column is not specified, the
                key column is used for ordering.
             */
    private String /* */ ordinalColumn;
    
    @Accessor
    public String /* */ getOrdinalColumn() {
        return ordinalColumn;
    }
    
    @Mutator
    public void setOrdinalColumn(String /* */ newval) {
        String /* */ oldval = ordinalColumn;
        ordinalColumn = newval;
        firePropertyChange("ordinalColumn", oldval, newval);
    }

    /** 
                The name of the column which references the parent member in
                a parent-child hierarchy.
             */
    private String /* */ parentColumn;
    
    @Accessor
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    @Mutator
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        firePropertyChange("parentColumn", oldval, newval);
    }

    /** 
                Value which identifies null parents in a parent-child
                hierarchy. Typical values are 'NULL' and '0'.
             */
    private String /* */ nullParentValue;
    
    @Accessor
    public String /* */ getNullParentValue() {
        return nullParentValue;
    }
    
    @Mutator
    public void setNullParentValue(String /* */ newval) {
        String /* */ oldval = nullParentValue;
        nullParentValue = newval;
        firePropertyChange("nullParentValue", oldval, newval);
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
    
    @Accessor
    public String /* */ getType() {
        return type;
    }
    
    @Mutator
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        firePropertyChange("type", oldval, newval);
    }

    /** 
                Whether members are unique across all parents. For
                example, zipcodes are unique across all states. The
                first level's members are always unique.
             */
    private Boolean /* */ uniqueMembers;
    
    @Accessor
    public Boolean /* */ getUniqueMembers() {
        return uniqueMembers;
    }
    
    @Mutator
    public void setUniqueMembers(Boolean /* */ newval) {
        Boolean /* */ oldval = uniqueMembers;
        uniqueMembers = newval;
        firePropertyChange("uniqueMembers", oldval, newval);
    }

    /** 
                Whether this is a regular or a time-related level.
                The value makes a difference to time-related functions
                such as YTD (year-to-date).
             */
    private String /* */ levelType;
    
    @Accessor
    public String /* */ getLevelType() {
        return levelType;
    }
    
    @Mutator
    public void setLevelType(String /* */ newval) {
        String /* */ oldval = levelType;
        levelType = newval;
        firePropertyChange("levelType", oldval, newval);
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
    
    @Accessor
    public String /* */ getHideMemberIf() {
        return hideMemberIf;
    }
    
    @Mutator
    public void setHideMemberIf(String /* */ newval) {
        String /* */ oldval = hideMemberIf;
        hideMemberIf = newval;
        firePropertyChange("hideMemberIf", oldval, newval);
    }

    /** 
                Name of a formatter class for the member labels being displayed.
                The class must implement the mondrian.olap.MemberFormatter interface.
             */
    private String /* */ formatter;
    
    @Accessor
    public String /* */ getFormatter() {
        return formatter;
    }
    
    @Mutator
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the level's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                The name of the column which holds the caption for
                members.
             */
    private String /* */ captionColumn;
    
    @Accessor
    public String /* */ getCaptionColumn() {
        return captionColumn;
    }
    
    @Mutator
    public void setCaptionColumn(String /* */ newval) {
        String /* */ oldval = captionColumn;
        captionColumn = newval;
        firePropertyChange("captionColumn", oldval, newval);
    }

    /** 
                The SQL expression used to populate this level's key.
             */
    private KeyExpression /* */ keyExp;
    
    @NonProperty
    public KeyExpression /* */ getKeyExp() {
        return keyExp;
    }
    
    @NonProperty
    public void setKeyExp(KeyExpression /* */ newval) {
        KeyExpression /* */ oldval = keyExp;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(KeyExpression.class);
        if (keyExp != null) {
            fireChildRemoved(KeyExpression.class, oldval, overallPosition);
        }
        keyExp = newval;
        if (keyExp != null) {
            keyExp.setParent(this);
            fireChildAdded(KeyExpression.class, keyExp, overallPosition);
        }
	}

    /** 
                The SQL expression used to populate this level's name. If not
                specified, the level's key is used.
             */
    private NameExpression /* */ nameExp;
    
    @NonProperty
    public NameExpression /* */ getNameExp() {
        return nameExp;
    }
    
    @NonProperty
    public void setNameExp(NameExpression /* */ newval) {
        NameExpression /* */ oldval = nameExp;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(NameExpression.class);
        if (nameExp != null) {
            fireChildRemoved(NameExpression.class, oldval, overallPosition);
        }
        nameExp = newval;
        if (nameExp != null) {
            nameExp.setParent(this);
            fireChildAdded(NameExpression.class, nameExp, overallPosition);
        }
	}

    /** 
                The SQL expression used to populate this level's ordinal.
             */
    private OrdinalExpression /* */ ordinalExp;
    
    @NonProperty
    public OrdinalExpression /* */ getOrdinalExp() {
        return ordinalExp;
    }
    
    @NonProperty
    public void setOrdinalExp(OrdinalExpression /* */ newval) {
        OrdinalExpression /* */ oldval = ordinalExp;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(OrdinalExpression.class);
        if (ordinalExp != null) {
            fireChildRemoved(OrdinalExpression.class, oldval, overallPosition);
        }
        ordinalExp = newval;
        if (ordinalExp != null) {
            ordinalExp.setParent(this);
            fireChildAdded(OrdinalExpression.class, ordinalExp, overallPosition);
        }
	}

    /** 
                The SQL expression used to join to the parent member in a
                parent-child hierarchy.
             */
    private ParentExpression /* */ parentExp;
    
    @NonProperty
    public ParentExpression /* */ getParentExp() {
        return parentExp;
    }
    
    @NonProperty
    public void setParentExp(ParentExpression /* */ newval) {
        ParentExpression /* */ oldval = parentExp;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(ParentExpression.class);
        if (parentExp != null) {
            fireChildRemoved(ParentExpression.class, oldval, overallPosition);
        }
        parentExp = newval;
        if (parentExp != null) {
            parentExp.setParent(this);
            fireChildAdded(ParentExpression.class, parentExp, overallPosition);
        }
	}

    /**  */
    private Closure /* */ closure;
    
    @NonProperty
    public Closure /* */ getClosure() {
        return closure;
    }
    
    @NonProperty
    public void setClosure(Closure /* */ newval) {
        Closure /* */ oldval = closure;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Closure.class);
        if (closure != null) {
            fireChildRemoved(Closure.class, oldval, overallPosition);
        }
        closure = newval;
        if (closure != null) {
            closure.setParent(this);
            fireChildAdded(Closure.class, closure, overallPosition);
        }
	}

    /**  */
    private final List<Property> properties = new ArrayList<Property>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addProperty(int pos, Property newChild) {
        properties.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(Property.class) + pos;
        fireChildAdded(Property.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Property removeProperty(int pos) {
        Property removedItem = properties.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Property.class) + pos;
            fireChildRemoved(Property.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Property.class,KeyExpression.class, NameExpression.class, OrdinalExpression.class, ParentExpression.class, Closure.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(properties);
        
        if (keyExp != null) {
        	children.add(keyExp);
        }
        
        if (nameExp != null) {
        	children.add(nameExp);
        }
        
        if (ordinalExp != null) {
        	children.add(ordinalExp);
        }
        
        if (parentExp != null) {
        	children.add(parentExp);
        }
        
        if (closure != null) {
        	children.add(closure);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Property.class.isAssignableFrom(childClass)) return offset;
        offset += properties.size();
        
        if (KeyExpression.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (NameExpression.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (OrdinalExpression.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (ParentExpression.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (Closure.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Property) {
            int offset = childPositionOffset(Property.class);
            if (index < 0 || index > properties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + properties.size());
            }
            addProperty(index, (Property) child);
        
        } else if (child instanceof KeyExpression) {
            setKeyExp((KeyExpression) child);
        
        } else if (child instanceof NameExpression) {
            setNameExp((NameExpression) child);
        
        } else if (child instanceof OrdinalExpression) {
            setOrdinalExp((OrdinalExpression) child);
        
        } else if (child instanceof ParentExpression) {
            setParentExp((ParentExpression) child);
        
        } else if (child instanceof Closure) {
            setClosure((Closure) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Property) {
            return removeProperty((Property) child);
        
        } else if (child instanceof KeyExpression) {
            setKeyExp(null);
            return true;
        
        } else if (child instanceof NameExpression) {
            setNameExp(null);
            return true;
        
        } else if (child instanceof OrdinalExpression) {
            setOrdinalExp(null);
            return true;
        
        } else if (child instanceof ParentExpression) {
            setParentExp(null);
            return true;
        
        } else if (child instanceof Closure) {
            setClosure(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Closure");
    }
    
    
    /**
     * Creates a new Closure with all mandatory
     * values passed in.
     */
    @Constructor
    public Closure(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "parentColumn") 
        String parentColumn
        , @ConstructorParameter(propertyName = "childColumn") 
        String childColumn
    ) {
        this();
        setName(name);
        
        setParentColumn(parentColumn);
    
        setChildColumn(childColumn);
    
    }

    
    /**
     * Creates a new Closure with all
     * attributes copied from the given Closure.
     */
    public Closure(Closure original) {
    	super(original);
    	
    	this.parentColumn = original.getParentColumn();
    	
    	this.childColumn = original.getChildColumn();
    	
    	this.table = original.getTable();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Closure:");
	    
	    retStr.append(" parentColumn = ");
	    retStr.append(parentColumn);
	    retStr.append(",");
	    
	    retStr.append(" childColumn = ");
	    retStr.append(childColumn);
	    retStr.append(",");
	    
	    retStr.append(" table = ");
	    retStr.append(table);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private String /* */ parentColumn;
    
    @Accessor
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    @Mutator
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        firePropertyChange("parentColumn", oldval, newval);
    }

    /**  */
    private String /* */ childColumn;
    
    @Accessor
    public String /* */ getChildColumn() {
        return childColumn;
    }
    
    @Mutator
    public void setChildColumn(String /* */ newval) {
        String /* */ oldval = childColumn;
        childColumn = newval;
        firePropertyChange("childColumn", oldval, newval);
    }

    /**  */
    private Table /* */ table;
    
    @NonProperty
    public Table /* */ getTable() {
        return table;
    }
    
    @NonProperty
    public void setTable(Table /* */ newval) {
        Table /* */ oldval = table;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Table.class);
        if (table != null) {
            fireChildRemoved(Table.class, oldval, overallPosition);
        }
        table = newval;
        if (table != null) {
            table.setParent(this);
            fireChildAdded(Table.class, table, overallPosition);
        }
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Table.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        if (table != null) {
        	children.add(table);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Table.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Table) {
            setTable((Table) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Table) {
            setTable(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Property");
    }
    
    
    /**
     * Creates a new Property with all mandatory
     * values passed in.
     */
    @Constructor
    public Property(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
        , @ConstructorParameter(propertyName = "type") 
        String type
        , @ConstructorParameter(propertyName = "formatter") 
        String formatter
        , @ConstructorParameter(propertyName = "caption") 
        String caption
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
        setType(type);
    
        setFormatter(formatter);
    
        setCaption(caption);
    
    }

    
    /**
     * Creates a new Property with all
     * attributes copied from the given Property.
     */
    public Property(Property original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.column = original.getColumn();
    	
    	this.type = original.getType();
    	
    	this.formatter = original.getFormatter();
    	
    	this.caption = original.getCaption();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Property:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr.append(" type = ");
	    retStr.append(type);
	    retStr.append(",");
	    
	    retStr.append(" formatter = ");
	    retStr.append(formatter);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /**  */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

    /** 
                Data type of this property:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.
             */
    private String /* */ type;
    
    @Accessor
    public String /* */ getType() {
        return type;
    }
    
    @Mutator
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        firePropertyChange("type", oldval, newval);
    }

    /** 
                Name of a formatter class for the appropriate property value
                being displayed.
                The class must implement the mondrian.olap.PropertyFormatter
                interface.
             */
    private String /* */ formatter;
    
    @Accessor
    public String /* */ getFormatter() {
        return formatter;
    }
    
    @Mutator
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Property
/**  */
public static class Measure extends OLAPObject {
    
    /**
     * Creates a new Measure with all attributes
     * set to their defaults.
     */
    public Measure() {
        setName("New Measure");
    }
    
    
    /**
     * Creates a new Measure with all mandatory
     * values passed in.
     */
    @Constructor
    public Measure(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
        , @ConstructorParameter(propertyName = "datatype") 
        String datatype
        , @ConstructorParameter(propertyName = "formatString") 
        String formatString
        , @ConstructorParameter(propertyName = "aggregator") 
        String aggregator
        , @ConstructorParameter(propertyName = "formatter") 
        String formatter
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "visible") 
        Boolean visible
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
        setDatatype(datatype);
    
        setFormatString(formatString);
    
        setAggregator(aggregator);
    
        setFormatter(formatter);
    
        setCaption(caption);
    
        setVisible(visible);
    
    }

    
    /**
     * Creates a new Measure with all
     * attributes copied from the given Measure.
     */
    public Measure(Measure original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.column = original.getColumn();
    	
    	this.datatype = original.getDatatype();
    	
    	this.formatString = original.getFormatString();
    	
    	this.aggregator = original.getAggregator();
    	
    	this.formatter = original.getFormatter();
    	
    	this.caption = original.getCaption();
    	
    	this.visible = original.getVisible();
    	
    	this.measureExp = original.getMeasureExp();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Measure:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr.append(" datatype = ");
	    retStr.append(datatype);
	    retStr.append(",");
	    
	    retStr.append(" formatString = ");
	    retStr.append(formatString);
	    retStr.append(",");
	    
	    retStr.append(" aggregator = ");
	    retStr.append(aggregator);
	    retStr.append(",");
	    
	    retStr.append(" formatter = ");
	    retStr.append(formatter);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" visible = ");
	    retStr.append(visible);
	    retStr.append(",");
	    
	    retStr.append(" measureExp = ");
	    retStr.append(measureExp);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** Name of this measure. */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Column which is source of this measure's values.
                If not specified, a measure expression must be specified.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

    /** 
                The datatype of this measure:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.

                The default datatype of a measure is
                'Integer' if the measure's aggregator is 'Count',
                otherwise it is 'Numeric'.
             */
    private String /* */ datatype;
    
    @Accessor
    public String /* */ getDatatype() {
        return datatype;
    }
    
    @Mutator
    public void setDatatype(String /* */ newval) {
        String /* */ oldval = datatype;
        datatype = newval;
        firePropertyChange("datatype", oldval, newval);
    }

    /** 
                Format string with which to format cells of this measure. For
                more details, see the mondrian.util.Format class.
             */
    private String /* */ formatString;
    
    @Accessor
    public String /* */ getFormatString() {
        return formatString;
    }
    
    @Mutator
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        firePropertyChange("formatString", oldval, newval);
    }

    /** 
                Aggregation function. Allowed values are "sum", "count", "min",
                "max", "avg", and "distinct-count". ("distinct count" is allowed
                for backwards compatibility, but is deprecated because XML
                enumerated attributes in a DTD cannot legally contain spaces.)
             */
    private String /* */ aggregator;
    
    @Accessor
    public String /* */ getAggregator() {
        return aggregator;
    }
    
    @Mutator
    public void setAggregator(String /* */ newval) {
        String /* */ oldval = aggregator;
        aggregator = newval;
        firePropertyChange("aggregator", oldval, newval);
    }

    /** 
                Name of a formatter class for the appropriate cell being displayed.
                The class must implement the mondrian.olap.CellFormatter interface.
             */
    private String /* */ formatter;
    
    @Accessor
    public String /* */ getFormatter() {
        return formatter;
    }
    
    @Mutator
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        firePropertyChange("formatter", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    @Accessor
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    @Mutator
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        firePropertyChange("visible", oldval, newval);
    }

    /** 
                The SQL expression used to calculate a measure.
                Must be specified if a source column is not specified.
             */
    private MeasureExpression /* */ measureExp;
    
    @NonProperty
    public MeasureExpression /* */ getMeasureExp() {
        return measureExp;
    }
    
    @NonProperty
    public void setMeasureExp(MeasureExpression /* */ newval) {
        MeasureExpression /* */ oldval = measureExp;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(MeasureExpression.class);
        if (measureExp != null) {
            fireChildRemoved(MeasureExpression.class, oldval, overallPosition);
        }
        measureExp = newval;
        if (measureExp != null) {
            measureExp.setParent(this);
            fireChildAdded(MeasureExpression.class, measureExp, overallPosition);
        }
	}

    /**  */
    private final List<CalculatedMemberProperty> memberProperties = new ArrayList<CalculatedMemberProperty>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberPropertie(int pos, CalculatedMemberProperty newChild) {
        memberProperties.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
        fireChildAdded(CalculatedMemberProperty.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CalculatedMemberProperty removeMemberPropertie(int pos) {
        CalculatedMemberProperty removedItem = memberProperties.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
            fireChildRemoved(CalculatedMemberProperty.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            CalculatedMemberProperty.class,MeasureExpression.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(memberProperties);
        
        if (measureExp != null) {
        	children.add(measureExp);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (CalculatedMemberProperty.class.isAssignableFrom(childClass)) return offset;
        offset += memberProperties.size();
        
        if (MeasureExpression.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            int offset = childPositionOffset(CalculatedMemberProperty.class);
            if (index < 0 || index > memberProperties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + memberProperties.size());
            }
            addMemberPropertie(index, (CalculatedMemberProperty) child);
        
        } else if (child instanceof MeasureExpression) {
            setMeasureExp((MeasureExpression) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CalculatedMemberProperty) {
            return removeMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof MeasureExpression) {
            setMeasureExp(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Measure
/**  */
public static class CalculatedMember extends OLAPObject {
    
    /**
     * Creates a new CalculatedMember with all attributes
     * set to their defaults.
     */
    public CalculatedMember() {
        setName("New CalculatedMember");
    }
    
    
    /**
     * Creates a new CalculatedMember with all mandatory
     * values passed in.
     */
    @Constructor
    public CalculatedMember(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "formatString") 
        String formatString
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "formula") 
        String formula
        , @ConstructorParameter(propertyName = "dimension") 
        String dimension
        , @ConstructorParameter(propertyName = "visible") 
        Boolean visible
    ) {
        this();
        setName(name);
        
        setFormatString(formatString);
    
        setCaption(caption);
    
        setFormula(formula);
    
        setDimension(dimension);
    
        setVisible(visible);
    
    }

    
    /**
     * Creates a new CalculatedMember with all
     * attributes copied from the given CalculatedMember.
     */
    public CalculatedMember(CalculatedMember original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.formatString = original.getFormatString();
    	
    	this.caption = original.getCaption();
    	
    	this.formula = original.getFormula();
    	
    	this.dimension = original.getDimension();
    	
    	this.visible = original.getVisible();
    	
    	this.formulaElement = original.getFormulaElement();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CalculatedMember:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" formatString = ");
	    retStr.append(formatString);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" formula = ");
	    retStr.append(formula);
	    retStr.append(",");
	    
	    retStr.append(" dimension = ");
	    retStr.append(dimension);
	    retStr.append(",");
	    
	    retStr.append(" visible = ");
	    retStr.append(visible);
	    retStr.append(",");
	    
	    retStr.append(" formulaElement = ");
	    retStr.append(formulaElement);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of this calculated member.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Format string with which to format cells of this member. For
                more details, see {@link mondrian.util.Format}.
             */
    private String /* */ formatString;
    
    @Accessor
    public String /* */ getFormatString() {
        return formatString;
    }
    
    @Mutator
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        firePropertyChange("formatString", oldval, newval);
    }

    /** 
                A string being displayed instead of the name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this member.
                Equivalent to the Formula sub-element.
             */
    private String /* */ formula;
    
    @Accessor
    public String /* */ getFormula() {
        return formula;
    }
    
    @Mutator
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        firePropertyChange("formula", oldval, newval);
    }

    /** 
                Name of the dimension which this member belongs to.
             */
    private String /* */ dimension;
    
    @Accessor
    public String /* */ getDimension() {
        return dimension;
    }
    
    @Mutator
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        firePropertyChange("dimension", oldval, newval);
    }

    /** 
                Whether this member is visible in the user-interface.
                Default true.
             */
    private Boolean /* */ visible;
    
    @Accessor
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    @Mutator
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        firePropertyChange("visible", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this member.
             */
    private Formula /* */ formulaElement;
    
    @NonProperty
    public Formula /* */ getFormulaElement() {
        return formulaElement;
    }
    
    @NonProperty
    public void setFormulaElement(Formula /* */ newval) {
        Formula /* */ oldval = formulaElement;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Formula.class);
        if (formulaElement != null) {
            fireChildRemoved(Formula.class, oldval, overallPosition);
        }
        formulaElement = newval;
        if (formulaElement != null) {
            formulaElement.setParent(this);
            fireChildAdded(Formula.class, formulaElement, overallPosition);
        }
	}

    /**  */
    private final List<CalculatedMemberProperty> memberProperties = new ArrayList<CalculatedMemberProperty>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberPropertie(int pos, CalculatedMemberProperty newChild) {
        memberProperties.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
        fireChildAdded(CalculatedMemberProperty.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CalculatedMemberProperty removeMemberPropertie(int pos) {
        CalculatedMemberProperty removedItem = memberProperties.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
            fireChildRemoved(CalculatedMemberProperty.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            CalculatedMemberProperty.class,Formula.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(memberProperties);
        
        if (formulaElement != null) {
        	children.add(formulaElement);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (CalculatedMemberProperty.class.isAssignableFrom(childClass)) return offset;
        offset += memberProperties.size();
        
        if (Formula.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            int offset = childPositionOffset(CalculatedMemberProperty.class);
            if (index < 0 || index > memberProperties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + memberProperties.size());
            }
            addMemberPropertie(index, (CalculatedMemberProperty) child);
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CalculatedMemberProperty) {
            return removeMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof Formula) {
            setFormulaElement(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New CalculatedMemberProperty");
    }
    
    
    /**
     * Creates a new CalculatedMemberProperty with all mandatory
     * values passed in.
     */
    @Constructor
    public CalculatedMemberProperty(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "caption") 
        String caption
        , @ConstructorParameter(propertyName = "expression") 
        String expression
        , @ConstructorParameter(propertyName = "value") 
        String value
    ) {
        this();
        setName(name);
        
        setCaption(caption);
    
        setExpression(expression);
    
        setValue(value);
    
    }

    
    /**
     * Creates a new CalculatedMemberProperty with all
     * attributes copied from the given CalculatedMemberProperty.
     */
    public CalculatedMemberProperty(CalculatedMemberProperty original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.caption = original.getCaption();
    	
    	this.expression = original.getExpression();
    	
    	this.value = original.getValue();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CalculatedMemberProperty:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" caption = ");
	    retStr.append(caption);
	    retStr.append(",");
	    
	    retStr.append(" expression = ");
	    retStr.append(expression);
	    retStr.append(",");
	    
	    retStr.append(" value = ");
	    retStr.append(value);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of this member property.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                A string being displayed instead of the Properties's name.
                Can be localized from Properties file using #{propertyname}.
             */
    private String /* */ caption;
    
    @Accessor
    public String /* */ getCaption() {
        return caption;
    }
    
    @Mutator
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        firePropertyChange("caption", oldval, newval);
    }

    /** 
                MDX expression which defines the value of this property.
                If the expression is a constant string, you could enclose it in
                quotes, or just specify the 'value' attribute instead.
             */
    private String /* */ expression;
    
    @Accessor
    public String /* */ getExpression() {
        return expression;
    }
    
    @Mutator
    public void setExpression(String /* */ newval) {
        String /* */ oldval = expression;
        expression = newval;
        firePropertyChange("expression", oldval, newval);
    }

    /** 
                Value of this property.
                If the value is not constant, specify the 'expression' attribute
                instead.
             */
    private String /* */ value;
    
    @Accessor
    public String /* */ getValue() {
        return value;
    }
    
    @Mutator
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        firePropertyChange("value", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New NamedSet");
    }
    
    
    /**
     * Creates a new NamedSet with all mandatory
     * values passed in.
     */
    @Constructor
    public NamedSet(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "formula") 
        String formula
    ) {
        this();
        setName(name);
        
        setFormula(formula);
    
    }

    
    /**
     * Creates a new NamedSet with all
     * attributes copied from the given NamedSet.
     */
    public NamedSet(NamedSet original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.formula = original.getFormula();
    	
    	this.formulaElement = original.getFormulaElement();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("NamedSet:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" formula = ");
	    retStr.append(formula);
	    retStr.append(",");
	    
	    retStr.append(" formulaElement = ");
	    retStr.append(formulaElement);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of this named set.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this set.
                Equivalent to the Formula sub-element.
             */
    private String /* */ formula;
    
    @Accessor
    public String /* */ getFormula() {
        return formula;
    }
    
    @Mutator
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        firePropertyChange("formula", oldval, newval);
    }

    /** 
                MDX expression which gives the value of this set.
             */
    private Formula /* */ formulaElement;
    
    @NonProperty
    public Formula /* */ getFormulaElement() {
        return formulaElement;
    }
    
    @NonProperty
    public void setFormulaElement(Formula /* */ newval) {
        Formula /* */ oldval = formulaElement;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Formula.class);
        if (formulaElement != null) {
            fireChildRemoved(Formula.class, oldval, overallPosition);
        }
        formulaElement = newval;
        if (formulaElement != null) {
            formulaElement.setParent(this);
            fireChildAdded(Formula.class, formulaElement, overallPosition);
        }
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Formula.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        if (formulaElement != null) {
        	children.add(formulaElement);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Formula.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Formula) {
            setFormulaElement(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element NamedSet
/**  */
public static class Formula extends OLAPObject {
    
    /**
     * Creates a new Formula with all attributes
     * set to their defaults.
     */
    public Formula() {
        setName("New Formula");
    }
    
    
    /**
     * Creates a new Formula with all mandatory
     * values passed in.
     */
    @Constructor
    public Formula(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Formula with all
     * attributes copied from the given Formula.
     */
    public Formula(Formula original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Formula:");
	    
		retStr.append("text = ");
		retStr.append(text);
		retStr.append(",");
		
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

	private String text;
	
	@Accessor
	public String getText() {
		return text;
	}
	
	@Mutator
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		firePropertyChange("text", oldval, newval);
	}



    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Formula
/** Not used */
public static class MemberReaderParameter extends OLAPObject {
    
    /**
     * Creates a new MemberReaderParameter with all attributes
     * set to their defaults.
     */
    public MemberReaderParameter() {
        setName("New MemberReaderParameter");
    }
    
    
    /**
     * Creates a new MemberReaderParameter with all mandatory
     * values passed in.
     */
    @Constructor
    public MemberReaderParameter(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "value") 
        String value
    ) {
        this();
        setName(name);
        
        setValue(value);
    
    }

    
    /**
     * Creates a new MemberReaderParameter with all
     * attributes copied from the given MemberReaderParameter.
     */
    public MemberReaderParameter(MemberReaderParameter original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.value = original.getValue();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("MemberReaderParameter:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" value = ");
	    retStr.append(value);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /**  */
    private String /* */ value;
    
    @Accessor
    public String /* */ getValue() {
        return value;
    }
    
    @Mutator
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        firePropertyChange("value", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element MemberReaderParameter
/** A table or a join */
public abstract static class RelationOrJoin extends OLAPObject {
    
    /**
     * Creates a new RelationOrJoin with all attributes
     * set to their defaults.
     */
    public RelationOrJoin() {
    setName("New RelationOrJoin");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public RelationOrJoin(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new RelationOrJoin with all
     * attributes copied from the given RelationOrJoin.
     */
    public RelationOrJoin(RelationOrJoin original) {
    	super(original);
    	
    }
    


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("RelationOrJoin:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of class RelationOrJoin
/** A table, inline table or view */
public abstract static class Relation extends RelationOrJoin {
    
    /**
     * Creates a new Relation with all attributes
     * set to their defaults.
     */
    public Relation() {
    setName("New Relation");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public Relation(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Relation with all
     * attributes copied from the given Relation.
     */
    public Relation(Relation original) {
    	super(original);
    	
    }
    


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Relation:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    RelationOrJoin.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New View");
    }
    
    
    /**
     * Creates a new View with all mandatory
     * values passed in.
     */
    @Constructor
    public View(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "alias") 
        String alias
    ) {
        this();
        setName(name);
        
        setAlias(alias);
    
    }

    
    /**
     * Creates a new View with all
     * attributes copied from the given View.
     */
    public View(View original) {
    	super(original);
    	
    	this.alias = original.getAlias();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("View:");
	    
	    retStr.append(" alias = ");
	    retStr.append(alias);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /**  */
    private String /* */ alias;
    
    @Accessor
    public String /* */ getAlias() {
        return alias;
    }
    
    @Mutator
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        firePropertyChange("alias", oldval, newval);
    }

    /**  */
    private final List<SQL> selects = new ArrayList<SQL>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addSelect(int pos, SQL newChild) {
        selects.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(SQL.class) + pos;
        fireChildAdded(SQL.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public SQL removeSelect(int pos) {
        SQL removedItem = selects.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(SQL.class) + pos;
            fireChildRemoved(SQL.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<SQL> getSelects() {
        return Collections.unmodifiableList(selects);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            SQL.class));  
        childTypes.addAll(Relation.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(selects);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (SQL.class.isAssignableFrom(childClass)) return offset;
        offset += selects.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            int offset = childPositionOffset(SQL.class);
            if (index < 0 || index > selects.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + selects.size());
            }
            addSelect(index, (SQL) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SQL) {
            return removeSelect((SQL) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element View
/**  */
public static class SQL extends OLAPObject {
    
    /**
     * Creates a new SQL with all attributes
     * set to their defaults.
     */
    public SQL() {
        setName("New SQL");
    }
    
    
    /**
     * Creates a new SQL with all mandatory
     * values passed in.
     */
    @Constructor
    public SQL(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "dialect") 
        String dialect
    ) {
        this();
        setName(name);
        
        setDialect(dialect);
    
    }

    
    /**
     * Creates a new SQL with all
     * attributes copied from the given SQL.
     */
    public SQL(SQL original) {
    	super(original);
    	
    	this.dialect = original.getDialect();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("SQL:");
	    
	    retStr.append(" dialect = ");
	    retStr.append(dialect);
	    retStr.append(",");
	    
		retStr.append("text = ");
		retStr.append(text);
		retStr.append(",");
		
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Dialect of SQL the view is intended for.
             */
    private String /* */ dialect;
    
    @Accessor
    public String /* */ getDialect() {
        return dialect;
    }
    
    @Mutator
    public void setDialect(String /* */ newval) {
        String /* */ oldval = dialect;
        dialect = newval;
        firePropertyChange("dialect", oldval, newval);
    }

	private String text;
	
	@Accessor
	public String getText() {
		return text;
	}
	
	@Mutator
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		firePropertyChange("text", oldval, newval);
	}



    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element SQL
/**  */
public static class Join extends RelationOrJoin {
    
    /**
     * Creates a new Join with all attributes
     * set to their defaults.
     */
    public Join() {
        setName("New Join");
    }
    
    
    /**
     * Creates a new Join with all mandatory
     * values passed in.
     */
    @Constructor
    public Join(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "leftAlias") 
        String leftAlias
        , @ConstructorParameter(propertyName = "leftKey") 
        String leftKey
        , @ConstructorParameter(propertyName = "rightAlias") 
        String rightAlias
        , @ConstructorParameter(propertyName = "rightKey") 
        String rightKey
    ) {
        this();
        setName(name);
        
        setLeftAlias(leftAlias);
    
        setLeftKey(leftKey);
    
        setRightAlias(rightAlias);
    
        setRightKey(rightKey);
    
    }

    
    /**
     * Creates a new Join with all
     * attributes copied from the given Join.
     */
    public Join(Join original) {
    	super(original);
    	
    	this.leftAlias = original.getLeftAlias();
    	
    	this.leftKey = original.getLeftKey();
    	
    	this.rightAlias = original.getRightAlias();
    	
    	this.rightKey = original.getRightKey();
    	
    	this.left = original.getLeft();
    	
    	this.right = original.getRight();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Join:");
	    
	    retStr.append(" leftAlias = ");
	    retStr.append(leftAlias);
	    retStr.append(",");
	    
	    retStr.append(" leftKey = ");
	    retStr.append(leftKey);
	    retStr.append(",");
	    
	    retStr.append(" rightAlias = ");
	    retStr.append(rightAlias);
	    retStr.append(",");
	    
	    retStr.append(" rightKey = ");
	    retStr.append(rightKey);
	    retStr.append(",");
	    
	    retStr.append(" left = ");
	    retStr.append(left);
	    retStr.append(",");
	    
	    retStr.append(" right = ");
	    retStr.append(right);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                Defaults to left's alias if left is a table, otherwise
                required.
             */
    private String /* */ leftAlias;
    
    @Accessor
    public String /* */ getLeftAlias() {
        return leftAlias;
    }
    
    @Mutator
    public void setLeftAlias(String /* */ newval) {
        String /* */ oldval = leftAlias;
        leftAlias = newval;
        firePropertyChange("leftAlias", oldval, newval);
    }

    /**  */
    private String /* */ leftKey;
    
    @Accessor
    public String /* */ getLeftKey() {
        return leftKey;
    }
    
    @Mutator
    public void setLeftKey(String /* */ newval) {
        String /* */ oldval = leftKey;
        leftKey = newval;
        firePropertyChange("leftKey", oldval, newval);
    }

    /** 
                Defaults to right's alias if right is a table, otherwise
                required.
             */
    private String /* */ rightAlias;
    
    @Accessor
    public String /* */ getRightAlias() {
        return rightAlias;
    }
    
    @Mutator
    public void setRightAlias(String /* */ newval) {
        String /* */ oldval = rightAlias;
        rightAlias = newval;
        firePropertyChange("rightAlias", oldval, newval);
    }

    /**  */
    private String /* */ rightKey;
    
    @Accessor
    public String /* */ getRightKey() {
        return rightKey;
    }
    
    @Mutator
    public void setRightKey(String /* */ newval) {
        String /* */ oldval = rightKey;
        rightKey = newval;
        firePropertyChange("rightKey", oldval, newval);
    }

    /**  */
    private RelationOrJoin /* */ left;
    
    @NonProperty
    public RelationOrJoin /* */ getLeft() {
        return left;
    }
    
    @NonProperty
    public void setLeft(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = left;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (left != null) {
            fireChildRemoved(RelationOrJoin.class, oldval, overallPosition);
        }
        left = newval;
        if (left != null) {
            left.setParent(this);
            fireChildAdded(RelationOrJoin.class, left, overallPosition);
        }
	}

    /**  */
    private RelationOrJoin /* */ right;
    
    @NonProperty
    public RelationOrJoin /* */ getRight() {
        return right;
    }
    
    @NonProperty
    public void setRight(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = right;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (right != null) {
            fireChildRemoved(RelationOrJoin.class, oldval, overallPosition);
        }
        right = newval;
        if (right != null) {
            right.setParent(this);
            fireChildAdded(RelationOrJoin.class, right, overallPosition);
        }
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            RelationOrJoin.class, RelationOrJoin.class));  
        childTypes.addAll(RelationOrJoin.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        if (left != null) {
        	children.add(left);
        }
        
        if (right != null) {
        	children.add(right);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (RelationOrJoin.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (RelationOrJoin.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
		if (false) {
		
		} else if (child instanceof RelationOrJoin) {
            if (getLeft() == null) {
                setLeft((RelationOrJoin) child);
            } else if (getRight() == null) {
                setRight((RelationOrJoin) child);
            }
        } else {
        	super.addChild(child);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
		if (false) {
			return false;
		} else if (child instanceof RelationOrJoin) {
            if (getLeft() == child) {
            	setLeft(null);
            	return true;
            } else if (getRight() == child) {
                setRight(null);
                return true;
            }
            return false;
        } else {
        	return super.removeChildImpl(child);
        }
			    
    }

} // end of element Join
/**  */
public static class Table extends Relation {
    
    /**
     * Creates a new Table with all attributes
     * set to their defaults.
     */
    public Table() {
        setName("New Table");
    }
    
    
    /**
     * Creates a new Table with all mandatory
     * values passed in.
     */
    @Constructor
    public Table(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "schema") 
        String schema
        , @ConstructorParameter(propertyName = "alias") 
        String alias
    ) {
        this();
        setName(name);
        
        setSchema(schema);
    
        setAlias(alias);
    
    }

    
    /**
     * Creates a new Table with all
     * attributes copied from the given Table.
     */
    public Table(Table original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.schema = original.getSchema();
    	
    	this.alias = original.getAlias();
    	
    	this.filter = original.getFilter();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Table:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" schema = ");
	    retStr.append(schema);
	    retStr.append(",");
	    
	    retStr.append(" alias = ");
	    retStr.append(alias);
	    retStr.append(",");
	    
	    retStr.append(" filter = ");
	    retStr.append(filter);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Optional qualifier for table.
             */
    private String /* */ schema;
    
    @Accessor
    public String /* */ getSchema() {
        return schema;
    }
    
    @Mutator
    public void setSchema(String /* */ newval) {
        String /* */ oldval = schema;
        schema = newval;
        firePropertyChange("schema", oldval, newval);
    }

    /** 
                Alias to be used with this table when it is used to
                form queries. If not specified, defaults to the table
                name, but in any case, must be unique within the
                schema. (You can use the same table in different
                hierarchies, but it must have different aliases.)
             */
    private String /* */ alias;
    
    @Accessor
    public String /* */ getAlias() {
        return alias;
    }
    
    @Mutator
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        firePropertyChange("alias", oldval, newval);
    }

    /** 
          The SQL WHERE clause expression to be appended to any select statement
         */
    private SQL /* */ filter;
    
    @NonProperty
    public SQL /* */ getFilter() {
        return filter;
    }
    
    @NonProperty
    public void setFilter(SQL /* */ newval) {
        SQL /* */ oldval = filter;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(SQL.class);
        if (filter != null) {
            fireChildRemoved(SQL.class, oldval, overallPosition);
        }
        filter = newval;
        if (filter != null) {
            filter.setParent(this);
            fireChildAdded(SQL.class, filter, overallPosition);
        }
	}

    /**  */
    private final List<AggExclude> aggExcludes = new ArrayList<AggExclude>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addAggExclude(int pos, AggExclude newChild) {
        aggExcludes.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(AggExclude.class) + pos;
        fireChildAdded(AggExclude.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggExclude removeAggExclude(int pos) {
        AggExclude removedItem = aggExcludes.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggExclude.class) + pos;
            fireChildRemoved(AggExclude.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(AggTable.class) + pos;
        fireChildAdded(AggTable.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggTable removeAggTable(int pos) {
        AggTable removedItem = aggTables.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggTable.class) + pos;
            fireChildRemoved(AggTable.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<AggTable> getAggTables() {
        return Collections.unmodifiableList(aggTables);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            AggExclude.class, AggTable.class,SQL.class));  
        childTypes.addAll(Relation.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(aggExcludes);
        
        children.addAll(aggTables);
        
        if (filter != null) {
        	children.add(filter);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (AggExclude.class.isAssignableFrom(childClass)) return offset;
        offset += aggExcludes.size();
        
        if (AggTable.class.isAssignableFrom(childClass)) return offset;
        offset += aggTables.size();
        
        if (SQL.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            int offset = childPositionOffset(AggExclude.class);
            if (index < 0 || index > aggExcludes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + aggExcludes.size());
            }
            addAggExclude(index, (AggExclude) child);
        
        } else if (child instanceof AggTable) {
            int offset = childPositionOffset(AggTable.class);
            if (index < 0 || index > aggTables.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + aggTables.size());
            }
            addAggTable(index, (AggTable) child);
        
        } else if (child instanceof SQL) {
            setFilter((SQL) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof AggExclude) {
            return removeAggExclude((AggExclude) child);
        
        } else if (child instanceof AggTable) {
            return removeAggTable((AggTable) child);
        
        } else if (child instanceof SQL) {
            setFilter(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Table
/**  */
public static class InlineTable extends Relation {
    
    /**
     * Creates a new InlineTable with all attributes
     * set to their defaults.
     */
    public InlineTable() {
        setName("New InlineTable");
    }
    
    
    /**
     * Creates a new InlineTable with all mandatory
     * values passed in.
     */
    @Constructor
    public InlineTable(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "alias") 
        String alias
    ) {
        this();
        setName(name);
        
        setAlias(alias);
    
    }

    
    /**
     * Creates a new InlineTable with all
     * attributes copied from the given InlineTable.
     */
    public InlineTable(InlineTable original) {
    	super(original);
    	
    	this.alias = original.getAlias();
    	
    	this.columnDefs = original.getColumnDefs();
    	
    	this.rows = original.getRows();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("InlineTable:");
	    
	    retStr.append(" alias = ");
	    retStr.append(alias);
	    retStr.append(",");
	    
	    retStr.append(" columnDefs = ");
	    retStr.append(columnDefs);
	    retStr.append(",");
	    
	    retStr.append(" rows = ");
	    retStr.append(rows);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                Alias to be used with this table when it is used to
                form queries. If not specified, defaults to the table
                name, but in any case, must be unique within the
                schema. (You can use the same table in different
                hierarchies, but it must have different aliases.)
             */
    private String /* */ alias;
    
    @Accessor
    public String /* */ getAlias() {
        return alias;
    }
    
    @Mutator
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        firePropertyChange("alias", oldval, newval);
    }

    /**  */
    private ColumnDefs /* */ columnDefs;
    
    @NonProperty
    public ColumnDefs /* */ getColumnDefs() {
        return columnDefs;
    }
    
    @NonProperty
    public void setColumnDefs(ColumnDefs /* */ newval) {
        ColumnDefs /* */ oldval = columnDefs;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(ColumnDefs.class);
        if (columnDefs != null) {
            fireChildRemoved(ColumnDefs.class, oldval, overallPosition);
        }
        columnDefs = newval;
        if (columnDefs != null) {
            columnDefs.setParent(this);
            fireChildAdded(ColumnDefs.class, columnDefs, overallPosition);
        }
	}

    /**  */
    private Rows /* */ rows;
    
    @NonProperty
    public Rows /* */ getRows() {
        return rows;
    }
    
    @NonProperty
    public void setRows(Rows /* */ newval) {
        Rows /* */ oldval = rows;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Rows.class);
        if (rows != null) {
            fireChildRemoved(Rows.class, oldval, overallPosition);
        }
        rows = newval;
        if (rows != null) {
            rows.setParent(this);
            fireChildAdded(Rows.class, rows, overallPosition);
        }
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            ColumnDefs.class, Rows.class));  
        childTypes.addAll(Relation.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        if (columnDefs != null) {
        	children.add(columnDefs);
        }
        
        if (rows != null) {
        	children.add(rows);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (ColumnDefs.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        if (Rows.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof ColumnDefs) {
            setColumnDefs((ColumnDefs) child);
        
        } else if (child instanceof Rows) {
            setRows((Rows) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof ColumnDefs) {
            setColumnDefs(null);
            return true;
        
        } else if (child instanceof Rows) {
            setRows(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element InlineTable
/** Holder for an array of ColumnDef elements */
public static class ColumnDefs extends OLAPObject {
    
    /**
     * Creates a new ColumnDefs with all attributes
     * set to their defaults.
     */
    public ColumnDefs() {
        setName("New ColumnDefs");
    }
    
    
    /**
     * Creates a new ColumnDefs with all mandatory
     * values passed in.
     */
    @Constructor
    public ColumnDefs(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new ColumnDefs with all
     * attributes copied from the given ColumnDefs.
     */
    public ColumnDefs(ColumnDefs original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("ColumnDefs:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private final List<ColumnDef> array = new ArrayList<ColumnDef>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addArray(int pos, ColumnDef newChild) {
        array.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(ColumnDef.class) + pos;
        fireChildAdded(ColumnDef.class, newChild, overallPosition);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addArray(ColumnDef newChild) {
        addArray(array.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeArray(ColumnDef removeChild) {
        int pos = array.indexOf(removeChild);
        if (pos != -1) {
            removeArray(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public ColumnDef removeArray(int pos) {
        ColumnDef removedItem = array.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(ColumnDef.class) + pos;
            fireChildRemoved(ColumnDef.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<ColumnDef> getArray() {
        return Collections.unmodifiableList(array);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            ColumnDef.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (ColumnDef.class.isAssignableFrom(childClass)) return offset;
        offset += array.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof ColumnDef) {
            int offset = childPositionOffset(ColumnDef.class);
            if (index < 0 || index > array.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + array.size());
            }
            addArray(index, (ColumnDef) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof ColumnDef) {
            return removeArray((ColumnDef) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New ColumnDef");
    }
    
    
    /**
     * Creates a new ColumnDef with all mandatory
     * values passed in.
     */
    @Constructor
    public ColumnDef(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "type") 
        String type
    ) {
        this();
        setName(name);
        
        setType(type);
    
    }

    
    /**
     * Creates a new ColumnDef with all
     * attributes copied from the given ColumnDef.
     */
    public ColumnDef(ColumnDef original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.type = original.getType();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("ColumnDef:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" type = ");
	    retStr.append(type);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of the column.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Type of the column:
                String, Numeric, Integer, Boolean, Date, Time or Timestamp.
             */
    private String /* */ type;
    
    @Accessor
    public String /* */ getType() {
        return type;
    }
    
    @Mutator
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        firePropertyChange("type", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element ColumnDef
/** Holder for an array of Row elements */
public static class Rows extends OLAPObject {
    
    /**
     * Creates a new Rows with all attributes
     * set to their defaults.
     */
    public Rows() {
        setName("New Rows");
    }
    
    
    /**
     * Creates a new Rows with all mandatory
     * values passed in.
     */
    @Constructor
    public Rows(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Rows with all
     * attributes copied from the given Rows.
     */
    public Rows(Rows original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Rows:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private final List<Row> array = new ArrayList<Row>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addArray(int pos, Row newChild) {
        array.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(Row.class) + pos;
        fireChildAdded(Row.class, newChild, overallPosition);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     *  */
    public void addArray(Row newChild) {
        addArray(array.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeArray(Row removeChild) {
        int pos = array.indexOf(removeChild);
        if (pos != -1) {
            removeArray(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Row removeArray(int pos) {
        Row removedItem = array.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Row.class) + pos;
            fireChildRemoved(Row.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<Row> getArray() {
        return Collections.unmodifiableList(array);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Row.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Row.class.isAssignableFrom(childClass)) return offset;
        offset += array.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Row) {
            int offset = childPositionOffset(Row.class);
            if (index < 0 || index > array.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + array.size());
            }
            addArray(index, (Row) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Row) {
            return removeArray((Row) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Row");
    }
    
    
    /**
     * Creates a new Row with all mandatory
     * values passed in.
     */
    @Constructor
    public Row(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Row with all
     * attributes copied from the given Row.
     */
    public Row(Row original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Row:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private final List<Value> values = new ArrayList<Value>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addValue(int pos, Value newChild) {
        values.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(Value.class) + pos;
        fireChildAdded(Value.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public Value removeValue(int pos) {
        Value removedItem = values.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(Value.class) + pos;
            fireChildRemoved(Value.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<Value> getValues() {
        return Collections.unmodifiableList(values);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            Value.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(values);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (Value.class.isAssignableFrom(childClass)) return offset;
        offset += values.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof Value) {
            int offset = childPositionOffset(Value.class);
            if (index < 0 || index > values.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + values.size());
            }
            addValue(index, (Value) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Value) {
            return removeValue((Value) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Value");
    }
    
    
    /**
     * Creates a new Value with all mandatory
     * values passed in.
     */
    @Constructor
    public Value(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new Value with all
     * attributes copied from the given Value.
     */
    public Value(Value original) {
    	super(original);
    	
    	this.column = original.getColumn();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Value:");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
		retStr.append("text = ");
		retStr.append(text);
		retStr.append(",");
		
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of the column.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

	private String text;
	
	@Accessor
	public String getText() {
		return text;
	}
	
	@Mutator
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		firePropertyChange("text", oldval, newval);
	}



    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Value
/** 
            A definition of an aggregate table for a base fact table.
            This aggregate table must be in the same schema as the
            base fact table.
         */
public abstract static class AggTable extends OLAPObject {
    
    /**
     * Creates a new AggTable with all attributes
     * set to their defaults.
     */
    public AggTable() {
    setName("New AggTable");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public AggTable(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "ignorecase") 
        Boolean ignorecase
    ) {
        this();
        setName(name);
        
        setIgnorecase(ignorecase);
    
    }

    
    /**
     * Creates a new AggTable with all
     * attributes copied from the given AggTable.
     */
    public AggTable(AggTable original) {
    	super(original);
    	
    	this.ignorecase = original.getIgnorecase();
    	
    	this.factcount = original.getFactcount();
    	
    }
    

    /** 
                Whether or not the match should ignore case.
             */
    private Boolean /* */ ignorecase;
    
    @Accessor
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    @Mutator
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        firePropertyChange("ignorecase", oldval, newval);
    }

    /** 
                What does the fact_count column look like.
             */
    private AggFactCount /* */ factcount;
    
    @NonProperty
    public AggFactCount /* */ getFactcount() {
        return factcount;
    }
    
    @NonProperty
    public void setFactcount(AggFactCount /* */ newval) {
        AggFactCount /* */ oldval = factcount;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(AggFactCount.class);
        if (factcount != null) {
            fireChildRemoved(AggFactCount.class, oldval, overallPosition);
        }
        factcount = newval;
        if (factcount != null) {
            factcount.setParent(this);
            fireChildAdded(AggFactCount.class, factcount, overallPosition);
        }
	}

    /**  */
    private final List<AggIgnoreColumn> ignoreColumns = new ArrayList<AggIgnoreColumn>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addIgnoreColumn(int pos, AggIgnoreColumn newChild) {
        ignoreColumns.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(AggIgnoreColumn.class) + pos;
        fireChildAdded(AggIgnoreColumn.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggIgnoreColumn removeIgnoreColumn(int pos) {
        AggIgnoreColumn removedItem = ignoreColumns.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggIgnoreColumn.class) + pos;
            fireChildRemoved(AggIgnoreColumn.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(AggForeignKey.class) + pos;
        fireChildAdded(AggForeignKey.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggForeignKey removeForeignKey(int pos) {
        AggForeignKey removedItem = foreignKeys.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggForeignKey.class) + pos;
            fireChildRemoved(AggForeignKey.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(AggMeasure.class) + pos;
        fireChildAdded(AggMeasure.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggMeasure removeMeasure(int pos) {
        AggMeasure removedItem = measures.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggMeasure.class) + pos;
            fireChildRemoved(AggMeasure.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(AggLevel.class) + pos;
        fireChildAdded(AggLevel.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggLevel removeLevel(int pos) {
        AggLevel removedItem = levels.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggLevel.class) + pos;
            fireChildRemoved(AggLevel.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<AggLevel> getLevels() {
        return Collections.unmodifiableList(levels);
    }
    


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggTable:");
	    
	    retStr.append(" ignorecase = ");
	    retStr.append(ignorecase);
	    retStr.append(",");
	    
	    retStr.append(" factcount = ");
	    retStr.append(factcount);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            AggIgnoreColumn.class, AggForeignKey.class, AggMeasure.class, AggLevel.class,AggFactCount.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(ignoreColumns);
        
        children.addAll(foreignKeys);
        
        children.addAll(measures);
        
        children.addAll(levels);
        
        if (factcount != null) {
        	children.add(factcount);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (AggIgnoreColumn.class.isAssignableFrom(childClass)) return offset;
        offset += ignoreColumns.size();
        
        if (AggForeignKey.class.isAssignableFrom(childClass)) return offset;
        offset += foreignKeys.size();
        
        if (AggMeasure.class.isAssignableFrom(childClass)) return offset;
        offset += measures.size();
        
        if (AggLevel.class.isAssignableFrom(childClass)) return offset;
        offset += levels.size();
        
        if (AggFactCount.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof AggIgnoreColumn) {
            int offset = childPositionOffset(AggIgnoreColumn.class);
            if (index < 0 || index > ignoreColumns.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + ignoreColumns.size());
            }
            addIgnoreColumn(index, (AggIgnoreColumn) child);
        
        } else if (child instanceof AggForeignKey) {
            int offset = childPositionOffset(AggForeignKey.class);
            if (index < 0 || index > foreignKeys.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + foreignKeys.size());
            }
            addForeignKey(index, (AggForeignKey) child);
        
        } else if (child instanceof AggMeasure) {
            int offset = childPositionOffset(AggMeasure.class);
            if (index < 0 || index > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + measures.size());
            }
            addMeasure(index, (AggMeasure) child);
        
        } else if (child instanceof AggLevel) {
            int offset = childPositionOffset(AggLevel.class);
            if (index < 0 || index > levels.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + levels.size());
            }
            addLevel(index, (AggLevel) child);
        
        } else if (child instanceof AggFactCount) {
            setFactcount((AggFactCount) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof AggIgnoreColumn) {
            return removeIgnoreColumn((AggIgnoreColumn) child);
        
        } else if (child instanceof AggForeignKey) {
            return removeForeignKey((AggForeignKey) child);
        
        } else if (child instanceof AggMeasure) {
            return removeMeasure((AggMeasure) child);
        
        } else if (child instanceof AggLevel) {
            return removeLevel((AggLevel) child);
        
        } else if (child instanceof AggFactCount) {
            setFactcount(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of class AggTable
/**  */
public static class AggName extends AggTable {
    
    /**
     * Creates a new AggName with all attributes
     * set to their defaults.
     */
    public AggName() {
        setName("New AggName");
    }
    
    
    /**
     * Creates a new AggName with all mandatory
     * values passed in.
     */
    @Constructor
    public AggName(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "ignorecase") 
        Boolean ignorecase
    ) {
        this();
        setName(name);
        
        setIgnorecase(ignorecase);
    
    }

    
    /**
     * Creates a new AggName with all
     * attributes copied from the given AggName.
     */
    public AggName(AggName original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggName:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                The Table name of a Specific aggregate table.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    AggTable.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggName
/**  */
public static class AggPattern extends AggTable {
    
    /**
     * Creates a new AggPattern with all attributes
     * set to their defaults.
     */
    public AggPattern() {
        setName("New AggPattern");
    }
    
    
    /**
     * Creates a new AggPattern with all mandatory
     * values passed in.
     */
    @Constructor
    public AggPattern(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "pattern") 
        String pattern
        , @ConstructorParameter(propertyName = "ignorecase") 
        Boolean ignorecase
    ) {
        this();
        setName(name);
        
        setPattern(pattern);
    
        setIgnorecase(ignorecase);
    
    }

    
    /**
     * Creates a new AggPattern with all
     * attributes copied from the given AggPattern.
     */
    public AggPattern(AggPattern original) {
    	super(original);
    	
    	this.pattern = original.getPattern();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggPattern:");
	    
	    retStr.append(" pattern = ");
	    retStr.append(pattern);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                A Table pattern used to define a set of aggregate tables.
             */
    private String /* */ pattern;
    
    @Accessor
    public String /* */ getPattern() {
        return pattern;
    }
    
    @Mutator
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        firePropertyChange("pattern", oldval, newval);
    }

    /**  */
    private final List<AggExclude> excludes = new ArrayList<AggExclude>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addExclude(int pos, AggExclude newChild) {
        excludes.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(AggExclude.class) + pos;
        fireChildAdded(AggExclude.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public AggExclude removeExclude(int pos) {
        AggExclude removedItem = excludes.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(AggExclude.class) + pos;
            fireChildRemoved(AggExclude.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<AggExclude> getExcludes() {
        return Collections.unmodifiableList(excludes);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            AggExclude.class));  
        childTypes.addAll(AggTable.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(excludes);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (AggExclude.class.isAssignableFrom(childClass)) return offset;
        offset += excludes.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            int offset = childPositionOffset(AggExclude.class);
            if (index < 0 || index > excludes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + excludes.size());
            }
            addExclude(index, (AggExclude) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof AggExclude) {
            return removeExclude((AggExclude) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggPattern
/**  */
public static class AggExclude extends OLAPObject {
    
    /**
     * Creates a new AggExclude with all attributes
     * set to their defaults.
     */
    public AggExclude() {
        setName("New AggExclude");
    }
    
    
    /**
     * Creates a new AggExclude with all mandatory
     * values passed in.
     */
    @Constructor
    public AggExclude(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "pattern") 
        String pattern
        , @ConstructorParameter(propertyName = "ignorecase") 
        Boolean ignorecase
    ) {
        this();
        setName(name);
        
        setPattern(pattern);
    
        setIgnorecase(ignorecase);
    
    }

    
    /**
     * Creates a new AggExclude with all
     * attributes copied from the given AggExclude.
     */
    public AggExclude(AggExclude original) {
    	super(original);
    	
    	this.pattern = original.getPattern();
    	
    	this.name = original.getName();
    	
    	this.ignorecase = original.getIgnorecase();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggExclude:");
	    
	    retStr.append(" pattern = ");
	    retStr.append(pattern);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" ignorecase = ");
	    retStr.append(ignorecase);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                A Table pattern not to be matched.
             */
    private String /* */ pattern;
    
    @Accessor
    public String /* */ getPattern() {
        return pattern;
    }
    
    @Mutator
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        firePropertyChange("pattern", oldval, newval);
    }

    /** 
                The Table name not to be matched.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Whether or not the match should ignore case.
             */
    private Boolean /* */ ignorecase;
    
    @Accessor
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    @Mutator
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        firePropertyChange("ignorecase", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggExclude
/**  */
public abstract static class AggColumnName extends OLAPObject {
    
    /**
     * Creates a new AggColumnName with all attributes
     * set to their defaults.
     */
    public AggColumnName() {
    setName("New AggColumnName");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public AggColumnName(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new AggColumnName with all
     * attributes copied from the given AggColumnName.
     */
    public AggColumnName(AggColumnName original) {
    	super(original);
    	
    	this.column = original.getColumn();
    	
    }
    

    /** 
                The name of the fact count column.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggColumnName:");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of class AggColumnName
/**  */
public static class AggFactCount extends AggColumnName {
    
    /**
     * Creates a new AggFactCount with all attributes
     * set to their defaults.
     */
    public AggFactCount() {
        setName("New AggFactCount");
    }
    
    
    /**
     * Creates a new AggFactCount with all mandatory
     * values passed in.
     */
    @Constructor
    public AggFactCount(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new AggFactCount with all
     * attributes copied from the given AggFactCount.
     */
    public AggFactCount(AggFactCount original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggFactCount:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    AggColumnName.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggFactCount
/**  */
public static class AggIgnoreColumn extends AggColumnName {
    
    /**
     * Creates a new AggIgnoreColumn with all attributes
     * set to their defaults.
     */
    public AggIgnoreColumn() {
        setName("New AggIgnoreColumn");
    }
    
    
    /**
     * Creates a new AggIgnoreColumn with all mandatory
     * values passed in.
     */
    @Constructor
    public AggIgnoreColumn(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new AggIgnoreColumn with all
     * attributes copied from the given AggIgnoreColumn.
     */
    public AggIgnoreColumn(AggIgnoreColumn original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggIgnoreColumn:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    AggColumnName.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New AggForeignKey");
    }
    
    
    /**
     * Creates a new AggForeignKey with all mandatory
     * values passed in.
     */
    @Constructor
    public AggForeignKey(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "factColumn") 
        String factColumn
        , @ConstructorParameter(propertyName = "aggColumn") 
        String aggColumn
    ) {
        this();
        setName(name);
        
        setFactColumn(factColumn);
    
        setAggColumn(aggColumn);
    
    }

    
    /**
     * Creates a new AggForeignKey with all
     * attributes copied from the given AggForeignKey.
     */
    public AggForeignKey(AggForeignKey original) {
    	super(original);
    	
    	this.factColumn = original.getFactColumn();
    	
    	this.aggColumn = original.getAggColumn();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggForeignKey:");
	    
	    retStr.append(" factColumn = ");
	    retStr.append(factColumn);
	    retStr.append(",");
	    
	    retStr.append(" aggColumn = ");
	    retStr.append(aggColumn);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                The name of the base fact table foreign key.
             */
    private String /* */ factColumn;
    
    @Accessor
    public String /* */ getFactColumn() {
        return factColumn;
    }
    
    @Mutator
    public void setFactColumn(String /* */ newval) {
        String /* */ oldval = factColumn;
        factColumn = newval;
        firePropertyChange("factColumn", oldval, newval);
    }

    /** 
                The name of the aggregate table foreign key.
             */
    private String /* */ aggColumn;
    
    @Accessor
    public String /* */ getAggColumn() {
        return aggColumn;
    }
    
    @Mutator
    public void setAggColumn(String /* */ newval) {
        String /* */ oldval = aggColumn;
        aggColumn = newval;
        firePropertyChange("aggColumn", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggForeignKey
/**  */
public static class AggLevel extends OLAPObject {
    
    /**
     * Creates a new AggLevel with all attributes
     * set to their defaults.
     */
    public AggLevel() {
        setName("New AggLevel");
    }
    
    
    /**
     * Creates a new AggLevel with all mandatory
     * values passed in.
     */
    @Constructor
    public AggLevel(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new AggLevel with all
     * attributes copied from the given AggLevel.
     */
    public AggLevel(AggLevel original) {
    	super(original);
    	
    	this.column = original.getColumn();
    	
    	this.name = original.getName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggLevel:");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                The name of the column mapping to the level name.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the Dimension Hierarchy level.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggLevel
/**  */
public static class AggMeasure extends OLAPObject {
    
    /**
     * Creates a new AggMeasure with all attributes
     * set to their defaults.
     */
    public AggMeasure() {
        setName("New AggMeasure");
    }
    
    
    /**
     * Creates a new AggMeasure with all mandatory
     * values passed in.
     */
    @Constructor
    public AggMeasure(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "column") 
        String column
    ) {
        this();
        setName(name);
        
        setColumn(column);
    
    }

    
    /**
     * Creates a new AggMeasure with all
     * attributes copied from the given AggMeasure.
     */
    public AggMeasure(AggMeasure original) {
    	super(original);
    	
    	this.column = original.getColumn();
    	
    	this.name = original.getName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("AggMeasure:");
	    
	    retStr.append(" column = ");
	    retStr.append(column);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                The name of the column mapping to the measure name.
             */
    private String /* */ column;
    
    @Accessor
    public String /* */ getColumn() {
        return column;
    }
    
    @Mutator
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        firePropertyChange("column", oldval, newval);
    }

    /** 
                The name of the Cube measure.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element AggMeasure
/**  */
public abstract static class Expression extends OLAPObject {
    
    /**
     * Creates a new Expression with all attributes
     * set to their defaults.
     */
    public Expression() {
    setName("New Expression");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public Expression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Expression with all
     * attributes copied from the given Expression.
     */
    public Expression(Expression original) {
    	super(original);
    	
    }
    


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Expression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of class Expression
/**  */
public static class Column extends Expression {
    
    /**
     * Creates a new Column with all attributes
     * set to their defaults.
     */
    public Column() {
        setName("New Column");
    }
    
    
    /**
     * Creates a new Column with all mandatory
     * values passed in.
     */
    @Constructor
    public Column(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "table") 
        String table
    ) {
        this();
        setName(name);
        
        setTable(table);
    
    }

    
    /**
     * Creates a new Column with all
     * attributes copied from the given Column.
     */
    public Column(Column original) {
    	super(original);
    	
    	this.table = original.getTable();
    	
    	this.name = original.getName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Column:");
	    
	    retStr.append(" table = ");
	    retStr.append(table);
	    retStr.append(",");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** 
                Alias of the table which contains this column. Not required if
                the query only has one table.
             */
    private String /* */ table;
    
    @Accessor
    public String /* */ getTable() {
        return table;
    }
    
    @Mutator
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        firePropertyChange("table", oldval, newval);
    }

    /** 
                Name of the column.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Expression.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Column
/** 
            A collection of SQL expressions, one per dialect.
         */
public abstract static class ExpressionView extends Expression {
    
    /**
     * Creates a new ExpressionView with all attributes
     * set to their defaults.
     */
    public ExpressionView() {
    setName("New ExpressionView");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public ExpressionView(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new ExpressionView with all
     * attributes copied from the given ExpressionView.
     */
    public ExpressionView(ExpressionView original) {
    	super(original);
    	
    }
    

    /**  */
    private final List<SQL> expressions = new ArrayList<SQL>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addExpression(int pos, SQL newChild) {
        expressions.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(SQL.class) + pos;
        fireChildAdded(SQL.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public SQL removeExpression(int pos) {
        SQL removedItem = expressions.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(SQL.class) + pos;
            fireChildRemoved(SQL.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<SQL> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }
    


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("ExpressionView:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            SQL.class));  
        childTypes.addAll(Expression.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(expressions);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (SQL.class.isAssignableFrom(childClass)) return offset;
        offset += expressions.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            int offset = childPositionOffset(SQL.class);
            if (index < 0 || index > expressions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + expressions.size());
            }
            addExpression(index, (SQL) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SQL) {
            return removeExpression((SQL) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of class ExpressionView
/**  */
public static class KeyExpression extends ExpressionView {
    
    /**
     * Creates a new KeyExpression with all attributes
     * set to their defaults.
     */
    public KeyExpression() {
        setName("New KeyExpression");
    }
    
    
    /**
     * Creates a new KeyExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public KeyExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new KeyExpression with all
     * attributes copied from the given KeyExpression.
     */
    public KeyExpression(KeyExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("KeyExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element KeyExpression
/**  */
public static class ParentExpression extends ExpressionView {
    
    /**
     * Creates a new ParentExpression with all attributes
     * set to their defaults.
     */
    public ParentExpression() {
        setName("New ParentExpression");
    }
    
    
    /**
     * Creates a new ParentExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public ParentExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new ParentExpression with all
     * attributes copied from the given ParentExpression.
     */
    public ParentExpression(ParentExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("ParentExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element ParentExpression
/**  */
public static class OrdinalExpression extends ExpressionView {
    
    /**
     * Creates a new OrdinalExpression with all attributes
     * set to their defaults.
     */
    public OrdinalExpression() {
        setName("New OrdinalExpression");
    }
    
    
    /**
     * Creates a new OrdinalExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public OrdinalExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new OrdinalExpression with all
     * attributes copied from the given OrdinalExpression.
     */
    public OrdinalExpression(OrdinalExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("OrdinalExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element OrdinalExpression
/**  */
public static class NameExpression extends ExpressionView {
    
    /**
     * Creates a new NameExpression with all attributes
     * set to their defaults.
     */
    public NameExpression() {
        setName("New NameExpression");
    }
    
    
    /**
     * Creates a new NameExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public NameExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new NameExpression with all
     * attributes copied from the given NameExpression.
     */
    public NameExpression(NameExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("NameExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element NameExpression
/**  */
public static class CaptionExpression extends ExpressionView {
    
    /**
     * Creates a new CaptionExpression with all attributes
     * set to their defaults.
     */
    public CaptionExpression() {
        setName("New CaptionExpression");
    }
    
    
    /**
     * Creates a new CaptionExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public CaptionExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new CaptionExpression with all
     * attributes copied from the given CaptionExpression.
     */
    public CaptionExpression(CaptionExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CaptionExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element CaptionExpression
/**  */
public static class MeasureExpression extends ExpressionView {
    
    /**
     * Creates a new MeasureExpression with all attributes
     * set to their defaults.
     */
    public MeasureExpression() {
        setName("New MeasureExpression");
    }
    
    
    /**
     * Creates a new MeasureExpression with all mandatory
     * values passed in.
     */
    @Constructor
    public MeasureExpression(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new MeasureExpression with all
     * attributes copied from the given MeasureExpression.
     */
    public MeasureExpression(MeasureExpression original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("MeasureExpression:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    ExpressionView.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Role");
    }
    
    
    /**
     * Creates a new Role with all mandatory
     * values passed in.
     */
    @Constructor
    public Role(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Role with all
     * attributes copied from the given Role.
     */
    public Role(Role original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.union = original.getUnion();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Role:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" union = ");
	    retStr.append(union);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /**  */
    private final List<SchemaGrant> schemaGrants = new ArrayList<SchemaGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addSchemaGrant(int pos, SchemaGrant newChild) {
        schemaGrants.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(SchemaGrant.class) + pos;
        fireChildAdded(SchemaGrant.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public SchemaGrant removeSchemaGrant(int pos) {
        SchemaGrant removedItem = schemaGrants.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(SchemaGrant.class) + pos;
            fireChildRemoved(SchemaGrant.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<SchemaGrant> getSchemaGrants() {
        return Collections.unmodifiableList(schemaGrants);
    }
    

    /**  */
    private Union /* */ union;
    
    @NonProperty
    public Union /* */ getUnion() {
        return union;
    }
    
    @NonProperty
    public void setUnion(Union /* */ newval) {
        Union /* */ oldval = union;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Union.class);
        if (union != null) {
            fireChildRemoved(Union.class, oldval, overallPosition);
        }
        union = newval;
        if (union != null) {
            union.setParent(this);
            fireChildAdded(Union.class, union, overallPosition);
        }
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            SchemaGrant.class,Union.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(schemaGrants);
        
        if (union != null) {
        	children.add(union);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (SchemaGrant.class.isAssignableFrom(childClass)) return offset;
        offset += schemaGrants.size();
        
        if (Union.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof SchemaGrant) {
            int offset = childPositionOffset(SchemaGrant.class);
            if (index < 0 || index > schemaGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + schemaGrants.size());
            }
            addSchemaGrant(index, (SchemaGrant) child);
        
        } else if (child instanceof Union) {
            setUnion((Union) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SchemaGrant) {
            return removeSchemaGrant((SchemaGrant) child);
        
        } else if (child instanceof Union) {
            setUnion(null);
            return true;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Role
/**  */
public abstract static class Grant extends OLAPObject {
    
    /**
     * Creates a new Grant with all attributes
     * set to their defaults.
     */
    public Grant() {
    setName("New Grant");
    }
    
    
    /**
     * Creates a new  with all mandatory
     * values passed in.
     */
    @Constructor
    public Grant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setAccess(access);
    
    }

    
    /**
     * Creates a new Grant with all
     * attributes copied from the given Grant.
     */
    public Grant(Grant original) {
    	super(original);
    	
    	this.access = original.getAccess();
    	
    }
    

    /** Values correspond to Access. */
    private String /* */ access;
    
    @Accessor
    public String /* */ getAccess() {
        return access;
    }
    
    @Mutator
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        firePropertyChange("access", oldval, newval);
    }


	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Grant:");
	    
	    retStr.append(" access = ");
	    retStr.append(access);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New SchemaGrant");
    }
    
    
    /**
     * Creates a new SchemaGrant with all mandatory
     * values passed in.
     */
    @Constructor
    public SchemaGrant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setAccess(access);
    
    }

    
    /**
     * Creates a new SchemaGrant with all
     * attributes copied from the given SchemaGrant.
     */
    public SchemaGrant(SchemaGrant original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("SchemaGrant:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /**  */
    private final List<CubeGrant> cubeGrants = new ArrayList<CubeGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addCubeGrant(int pos, CubeGrant newChild) {
        cubeGrants.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(CubeGrant.class) + pos;
        fireChildAdded(CubeGrant.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public CubeGrant removeCubeGrant(int pos) {
        CubeGrant removedItem = cubeGrants.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(CubeGrant.class) + pos;
            fireChildRemoved(CubeGrant.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<CubeGrant> getCubeGrants() {
        return Collections.unmodifiableList(cubeGrants);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            CubeGrant.class));  
        childTypes.addAll(Grant.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(cubeGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (CubeGrant.class.isAssignableFrom(childClass)) return offset;
        offset += cubeGrants.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof CubeGrant) {
            int offset = childPositionOffset(CubeGrant.class);
            if (index < 0 || index > cubeGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + cubeGrants.size());
            }
            addCubeGrant(index, (CubeGrant) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CubeGrant) {
            return removeCubeGrant((CubeGrant) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New CubeGrant");
    }
    
    
    /**
     * Creates a new CubeGrant with all mandatory
     * values passed in.
     */
    @Constructor
    public CubeGrant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "cube") 
        String cube
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setCube(cube);
    
        setAccess(access);
    
    }

    
    /**
     * Creates a new CubeGrant with all
     * attributes copied from the given CubeGrant.
     */
    public CubeGrant(CubeGrant original) {
    	super(original);
    	
    	this.cube = original.getCube();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("CubeGrant:");
	    
	    retStr.append(" cube = ");
	    retStr.append(cube);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** The unique name of the cube */
    private String /* */ cube;
    
    @Accessor
    public String /* */ getCube() {
        return cube;
    }
    
    @Mutator
    public void setCube(String /* */ newval) {
        String /* */ oldval = cube;
        cube = newval;
        firePropertyChange("cube", oldval, newval);
    }

    /**  */
    private final List<DimensionGrant> dimensionGrants = new ArrayList<DimensionGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addDimensionGrant(int pos, DimensionGrant newChild) {
        dimensionGrants.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(DimensionGrant.class) + pos;
        fireChildAdded(DimensionGrant.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public DimensionGrant removeDimensionGrant(int pos) {
        DimensionGrant removedItem = dimensionGrants.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(DimensionGrant.class) + pos;
            fireChildRemoved(DimensionGrant.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
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
        int overallPosition = childPositionOffset(HierarchyGrant.class) + pos;
        fireChildAdded(HierarchyGrant.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public HierarchyGrant removeHierarchyGrant(int pos) {
        HierarchyGrant removedItem = hierarchyGrants.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(HierarchyGrant.class) + pos;
            fireChildRemoved(HierarchyGrant.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<HierarchyGrant> getHierarchyGrants() {
        return Collections.unmodifiableList(hierarchyGrants);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            DimensionGrant.class, HierarchyGrant.class));  
        childTypes.addAll(Grant.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(dimensionGrants);
        
        children.addAll(hierarchyGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (DimensionGrant.class.isAssignableFrom(childClass)) return offset;
        offset += dimensionGrants.size();
        
        if (HierarchyGrant.class.isAssignableFrom(childClass)) return offset;
        offset += hierarchyGrants.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof DimensionGrant) {
            int offset = childPositionOffset(DimensionGrant.class);
            if (index < 0 || index > dimensionGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + dimensionGrants.size());
            }
            addDimensionGrant(index, (DimensionGrant) child);
        
        } else if (child instanceof HierarchyGrant) {
            int offset = childPositionOffset(HierarchyGrant.class);
            if (index < 0 || index > hierarchyGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + hierarchyGrants.size());
            }
            addHierarchyGrant(index, (HierarchyGrant) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof DimensionGrant) {
            return removeDimensionGrant((DimensionGrant) child);
        
        } else if (child instanceof HierarchyGrant) {
            return removeHierarchyGrant((HierarchyGrant) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New DimensionGrant");
    }
    
    
    /**
     * Creates a new DimensionGrant with all mandatory
     * values passed in.
     */
    @Constructor
    public DimensionGrant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "dimension") 
        String dimension
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setDimension(dimension);
    
        setAccess(access);
    
    }

    
    /**
     * Creates a new DimensionGrant with all
     * attributes copied from the given DimensionGrant.
     */
    public DimensionGrant(DimensionGrant original) {
    	super(original);
    	
    	this.dimension = original.getDimension();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("DimensionGrant:");
	    
	    retStr.append(" dimension = ");
	    retStr.append(dimension);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** The unique name of the dimension */
    private String /* */ dimension;
    
    @Accessor
    public String /* */ getDimension() {
        return dimension;
    }
    
    @Mutator
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        firePropertyChange("dimension", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Grant.allowedChildTypes;
        
    @NonProperty
    public List<SPObject> getChildren() {
        return super.getChildren();
    }
    
    public boolean allowsChildren() {
        return super.allowsChildren();
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New HierarchyGrant");
    }
    
    
    /**
     * Creates a new HierarchyGrant with all mandatory
     * values passed in.
     */
    @Constructor
    public HierarchyGrant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "hierarchy") 
        String hierarchy
        , @ConstructorParameter(propertyName = "topLevel") 
        String topLevel
        , @ConstructorParameter(propertyName = "bottomLevel") 
        String bottomLevel
        , @ConstructorParameter(propertyName = "rollupPolicy") 
        String rollupPolicy
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setHierarchy(hierarchy);
    
        setTopLevel(topLevel);
    
        setBottomLevel(bottomLevel);
    
        setRollupPolicy(rollupPolicy);
    
        setAccess(access);
    
    }

    
    /**
     * Creates a new HierarchyGrant with all
     * attributes copied from the given HierarchyGrant.
     */
    public HierarchyGrant(HierarchyGrant original) {
    	super(original);
    	
    	this.hierarchy = original.getHierarchy();
    	
    	this.topLevel = original.getTopLevel();
    	
    	this.bottomLevel = original.getBottomLevel();
    	
    	this.rollupPolicy = original.getRollupPolicy();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("HierarchyGrant:");
	    
	    retStr.append(" hierarchy = ");
	    retStr.append(hierarchy);
	    retStr.append(",");
	    
	    retStr.append(" topLevel = ");
	    retStr.append(topLevel);
	    retStr.append(",");
	    
	    retStr.append(" bottomLevel = ");
	    retStr.append(bottomLevel);
	    retStr.append(",");
	    
	    retStr.append(" rollupPolicy = ");
	    retStr.append(rollupPolicy);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        
	    return retStr.toString();
	}

    /** The unique name of the hierarchy */
    private String /* */ hierarchy;
    
    @Accessor
    public String /* */ getHierarchy() {
        return hierarchy;
    }
    
    @Mutator
    public void setHierarchy(String /* */ newval) {
        String /* */ oldval = hierarchy;
        hierarchy = newval;
        firePropertyChange("hierarchy", oldval, newval);
    }

    /** Unique name of the highest level of the hierarchy from which
            this role is allowed to see members. May only be specified if
            the HierarchyGrant.access is "custom". If not
            specified, role can see members up to the top level. */
    private String /* */ topLevel;
    
    @Accessor
    public String /* */ getTopLevel() {
        return topLevel;
    }
    
    @Mutator
    public void setTopLevel(String /* */ newval) {
        String /* */ oldval = topLevel;
        topLevel = newval;
        firePropertyChange("topLevel", oldval, newval);
    }

    /** Unique name of the lowest level of the hierarchy from which
            this role is allowed to see members. May only be specified if
            the HierarchyGrant.access is "custom". If not
            specified, role can see members down to the leaf level. */
    private String /* */ bottomLevel;
    
    @Accessor
    public String /* */ getBottomLevel() {
        return bottomLevel;
    }
    
    @Mutator
    public void setBottomLevel(String /* */ newval) {
        String /* */ oldval = bottomLevel;
        bottomLevel = newval;
        firePropertyChange("bottomLevel", oldval, newval);
    }

    /** Policy which determines how cell values are calculated if
                not all of the children of the current cell are visible to
                the current role. Allowable values are 'full' (the default),
                'partial', and 'hidden'. */
    private String /* */ rollupPolicy;
    
    @Accessor
    public String /* */ getRollupPolicy() {
        return rollupPolicy;
    }
    
    @Mutator
    public void setRollupPolicy(String /* */ newval) {
        String /* */ oldval = rollupPolicy;
        rollupPolicy = newval;
        firePropertyChange("rollupPolicy", oldval, newval);
    }

    /**  */
    private final List<MemberGrant> memberGrants = new ArrayList<MemberGrant>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addMemberGrant(int pos, MemberGrant newChild) {
        memberGrants.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(MemberGrant.class) + pos;
        fireChildAdded(MemberGrant.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public MemberGrant removeMemberGrant(int pos) {
        MemberGrant removedItem = memberGrants.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(MemberGrant.class) + pos;
            fireChildRemoved(MemberGrant.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<MemberGrant> getMemberGrants() {
        return Collections.unmodifiableList(memberGrants);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            MemberGrant.class));  
        childTypes.addAll(Grant.allowedChildTypes);
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(memberGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (MemberGrant.class.isAssignableFrom(childClass)) return offset;
        offset += memberGrants.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof MemberGrant) {
            int offset = childPositionOffset(MemberGrant.class);
            if (index < 0 || index > memberGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + memberGrants.size());
            }
            addMemberGrant(index, (MemberGrant) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof MemberGrant) {
            return removeMemberGrant((MemberGrant) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New MemberGrant");
    }
    
    
    /**
     * Creates a new MemberGrant with all mandatory
     * values passed in.
     */
    @Constructor
    public MemberGrant(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "member") 
        String member
        , @ConstructorParameter(propertyName = "access") 
        String access
    ) {
        this();
        setName(name);
        
        setMember(member);
    
        setAccess(access);
    
    }

    
    /**
     * Creates a new MemberGrant with all
     * attributes copied from the given MemberGrant.
     */
    public MemberGrant(MemberGrant original) {
    	super(original);
    	
    	this.member = original.getMember();
    	
    	this.access = original.getAccess();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("MemberGrant:");
	    
	    retStr.append(" member = ");
	    retStr.append(member);
	    retStr.append(",");
	    
	    retStr.append(" access = ");
	    retStr.append(access);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** The unique name of the member */
    private String /* */ member;
    
    @Accessor
    public String /* */ getMember() {
        return member;
    }
    
    @Mutator
    public void setMember(String /* */ newval) {
        String /* */ oldval = member;
        member = newval;
        firePropertyChange("member", oldval, newval);
    }

    /**  */
    private String /* */ access;
    
    @Accessor
    public String /* */ getAccess() {
        return access;
    }
    
    @Mutator
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        firePropertyChange("access", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Union");
    }
    
    
    /**
     * Creates a new Union with all mandatory
     * values passed in.
     */
    @Constructor
    public Union(
        @ConstructorParameter(propertyName = "name") String name
        
    ) {
        this();
        setName(name);
        
    }

    
    /**
     * Creates a new Union with all
     * attributes copied from the given Union.
     */
    public Union(Union original) {
    	super(original);
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Union:");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private final List<RoleUsage> roleUsages = new ArrayList<RoleUsage>();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * 
     */
    public void addRoleUsage(int pos, RoleUsage newChild) {
        roleUsages.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(RoleUsage.class) + pos;
        fireChildAdded(RoleUsage.class, newChild, overallPosition);
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
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public RoleUsage removeRoleUsage(int pos) {
        RoleUsage removedItem = roleUsages.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(RoleUsage.class) + pos;
            fireChildRemoved(RoleUsage.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List<RoleUsage> getRoleUsages() {
        return Collections.unmodifiableList(roleUsages);
    }
    


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>(
                    Arrays.asList(
            RoleUsage.class));  
        
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List<SPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<SPObject> children = new ArrayList<SPObject>();
        
        children.addAll(roleUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        int offset = 0;
        
        if (RoleUsage.class.isAssignableFrom(childClass)) return offset;
        offset += roleUsages.size();
        
        return offset + super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else if (child instanceof RoleUsage) {
            int offset = childPositionOffset(RoleUsage.class);
            if (index < 0 || index > roleUsages.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + roleUsages.size());
            }
            addRoleUsage(index, (RoleUsage) child);
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof RoleUsage) {
            return removeRoleUsage((RoleUsage) child);
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New RoleUsage");
    }
    
    
    /**
     * Creates a new RoleUsage with all mandatory
     * values passed in.
     */
    @Constructor
    public RoleUsage(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "roleName") 
        String roleName
    ) {
        this();
        setName(name);
        
        setRoleName(roleName);
    
    }

    
    /**
     * Creates a new RoleUsage with all
     * attributes copied from the given RoleUsage.
     */
    public RoleUsage(RoleUsage original) {
    	super(original);
    	
    	this.roleName = original.getRoleName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("RoleUsage:");
	    
	    retStr.append(" roleName = ");
	    retStr.append(roleName);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /**  */
    private String /* */ roleName;
    
    @Accessor
    public String /* */ getRoleName() {
        return roleName;
    }
    
    @Mutator
    public void setRoleName(String /* */ newval) {
        String /* */ oldval = roleName;
        roleName = newval;
        firePropertyChange("roleName", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New UserDefinedFunction");
    }
    
    
    /**
     * Creates a new UserDefinedFunction with all mandatory
     * values passed in.
     */
    @Constructor
    public UserDefinedFunction(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "className") 
        String className
    ) {
        this();
        setName(name);
        
        setClassName(className);
    
    }

    
    /**
     * Creates a new UserDefinedFunction with all
     * attributes copied from the given UserDefinedFunction.
     */
    public UserDefinedFunction(UserDefinedFunction original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.className = original.getClassName();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("UserDefinedFunction:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" className = ");
	    retStr.append(className);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** Name with which the user-defined function will be referenced in MDX expressions. */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Name of the class which implemenets this user-defined function.
                Must implement the mondrian.spi.UserDefinedFunction
                interface.
             */
    private String /* */ className;
    
    @Accessor
    public String /* */ getClassName() {
        return className;
    }
    
    @Mutator
    public void setClassName(String /* */ newval) {
        String /* */ oldval = className;
        className = newval;
        firePropertyChange("className", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
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
        setName("New Parameter");
    }
    
    
    /**
     * Creates a new Parameter with all mandatory
     * values passed in.
     */
    @Constructor
    public Parameter(
        @ConstructorParameter(propertyName = "name") String name
        
        , @ConstructorParameter(propertyName = "description") 
        String description
        , @ConstructorParameter(propertyName = "type") 
        String type
        , @ConstructorParameter(propertyName = "modifiable") 
        Boolean modifiable
        , @ConstructorParameter(propertyName = "defaultValue") 
        String defaultValue
    ) {
        this();
        setName(name);
        
        setDescription(description);
    
        setType(type);
    
        setModifiable(modifiable);
    
        setDefaultValue(defaultValue);
    
    }

    
    /**
     * Creates a new Parameter with all
     * attributes copied from the given Parameter.
     */
    public Parameter(Parameter original) {
    	super(original);
    	
    	this.name = original.getName();
    	
    	this.description = original.getDescription();
    	
    	this.type = original.getType();
    	
    	this.modifiable = original.getModifiable();
    	
    	this.defaultValue = original.getDefaultValue();
    	
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("Parameter:");
	    
	    retStr.append(" name = ");
	    retStr.append(name);
	    retStr.append(",");
	    
	    retStr.append(" description = ");
	    retStr.append(description);
	    retStr.append(",");
	    
	    retStr.append(" type = ");
	    retStr.append(type);
	    retStr.append(",");
	    
	    retStr.append(" modifiable = ");
	    retStr.append(modifiable);
	    retStr.append(",");
	    
	    retStr.append(" defaultValue = ");
	    retStr.append(defaultValue);
	    retStr.append(",");
	    
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    
	    return retStr.toString();
	}

    /** 
                Name of this parameter.
             */
    private String /* */ name;
    
    @Accessor
    public String /* */ getName() {
        return name;
    }
    
    @Mutator
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        firePropertyChange("name", oldval, newval);
    }

    /** 
                Description of this parameter.
             */
    private String /* */ description;
    
    @Accessor
    public String /* */ getDescription() {
        return description;
    }
    
    @Mutator
    public void setDescription(String /* */ newval) {
        String /* */ oldval = description;
        description = newval;
        firePropertyChange("description", oldval, newval);
    }

    /** 
                Indicates the type of this parameter:
                String, Numeric, Integer, Boolean, Date, Time, Timestamp, or Member.
             */
    private String /* */ type;
    
    @Accessor
    public String /* */ getType() {
        return type;
    }
    
    @Mutator
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        firePropertyChange("type", oldval, newval);
    }

    /** 
                If false, statement cannot change the value of this parameter;
                the parameter becomes effectively constant (provided that its default
                value expression always returns the same value).
                Default is true.
             */
    private Boolean /* */ modifiable;
    
    @Accessor
    public Boolean /* */ getModifiable() {
        return modifiable;
    }
    
    @Mutator
    public void setModifiable(Boolean /* */ newval) {
        Boolean /* */ oldval = modifiable;
        modifiable = newval;
        firePropertyChange("modifiable", oldval, newval);
    }

    /** 
                Expression for the default value of this parameter.
             */
    private String /* */ defaultValue;
    
    @Accessor
    public String /* */ getDefaultValue() {
        return defaultValue;
    }
    
    @Mutator
    public void setDefaultValue(String /* */ newval) {
        String /* */ oldval = defaultValue;
        defaultValue = newval;
        firePropertyChange("defaultValue", oldval, newval);
    }


    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes =
    Collections.emptyList();
        
    @NonProperty
    public List<SPObject> getChildren() {
        return Collections.emptyList();
    }
    
    public boolean allowsChildren() {
        return false;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class<? extends SPObject> childClass) {
        return super.childPositionOffset(childClass);
    }
    
    @Override
    public void addChildImpl(SPObject child, int index) {
		
        if (false) {
        
        } else {
            super.addChildImpl(child, index);
        }
			
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChildImpl(child);
        }
			    
    }

} // end of element Parameter
} // end of entire model
