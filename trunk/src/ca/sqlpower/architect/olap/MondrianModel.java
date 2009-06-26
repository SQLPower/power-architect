
package ca.sqlpower.architect.olap;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


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
        int overallPosition = childPositionOffset(Parameter.class) + pos;
        fireChildAdded(Parameter.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Parameter.class) + pos;
            fireChildRemoved(Parameter.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(Dimension.class) + pos;
        fireChildAdded(Dimension.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Dimension.class) + pos;
            fireChildRemoved(Dimension.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(Cube.class) + pos;
        fireChildAdded(Cube.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Cube.class) + pos;
            fireChildRemoved(Cube.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(VirtualCube.class) + pos;
        fireChildAdded(VirtualCube.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(VirtualCube.class) + pos;
            fireChildRemoved(VirtualCube.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(Role.class) + pos;
        fireChildAdded(Role.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Role.class) + pos;
            fireChildRemoved(Role.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(UserDefinedFunction.class) + pos;
        fireChildAdded(UserDefinedFunction.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(UserDefinedFunction.class) + pos;
            fireChildRemoved(UserDefinedFunction.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<UserDefinedFunction> getUserDefinedFunctions() {
        return Collections.unmodifiableList(userDefinedFunctions);
    }
    

    @Override
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
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Parameter.class) return offset;
        offset += parameters.size();
        
        if (childClass == Dimension.class) return offset;
        offset += dimensions.size();
        
        if (childClass == Cube.class) return offset;
        offset += cubes.size();
        
        if (childClass == VirtualCube.class) return offset;
        offset += virtualCubes.size();
        
        if (childClass == NamedSet.class) return offset;
        offset += namedSets.size();
        
        if (childClass == Role.class) return offset;
        offset += roles.size();
        
        if (childClass == UserDefinedFunction.class) return offset;
        offset += userDefinedFunctions.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Parameter) {
            addParameter((Parameter) child);
        
        } else if (child instanceof Dimension) {
            addDimension((Dimension) child);
        
        } else if (child instanceof Cube) {
            addCube((Cube) child);
        
        } else if (child instanceof VirtualCube) {
            addVirtualCube((VirtualCube) child);
        
        } else if (child instanceof NamedSet) {
            addNamedSet((NamedSet) child);
        
        } else if (child instanceof Role) {
            addRole((Role) child);
        
        } else if (child instanceof UserDefinedFunction) {
            addUserDefinedFunction((UserDefinedFunction) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Parameter) {
            int offset = childPositionOffset(Parameter.class);
            if ((index - offset) < 0 || (index - offset) > parameters.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + parameters.size());
            }
            addParameter(index - offset, (Parameter) child);
        
        } else if (child instanceof Dimension) {
            int offset = childPositionOffset(Dimension.class);
            if ((index - offset) < 0 || (index - offset) > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + dimensions.size());
            }
            addDimension(index - offset, (Dimension) child);
        
        } else if (child instanceof Cube) {
            int offset = childPositionOffset(Cube.class);
            if ((index - offset) < 0 || (index - offset) > cubes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + cubes.size());
            }
            addCube(index - offset, (Cube) child);
        
        } else if (child instanceof VirtualCube) {
            int offset = childPositionOffset(VirtualCube.class);
            if ((index - offset) < 0 || (index - offset) > virtualCubes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + virtualCubes.size());
            }
            addVirtualCube(index - offset, (VirtualCube) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if ((index - offset) < 0 || (index - offset) > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index - offset, (NamedSet) child);
        
        } else if (child instanceof Role) {
            int offset = childPositionOffset(Role.class);
            if ((index - offset) < 0 || (index - offset) > roles.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + roles.size());
            }
            addRole(index - offset, (Role) child);
        
        } else if (child instanceof UserDefinedFunction) {
            int offset = childPositionOffset(UserDefinedFunction.class);
            if ((index - offset) < 0 || (index - offset) > userDefinedFunctions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + userDefinedFunctions.size());
            }
            addUserDefinedFunction(index - offset, (UserDefinedFunction) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
    private RelationOrJoin /* */ fact;
    
    public RelationOrJoin /* */ getFact() {
        return fact;
    }
    
    public void setFact(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = fact;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Relation.class);
        if (fact != null) {
            fireChildRemoved(Relation.class, overallPosition, oldval);
        }
        fact = newval;
        fact.setParent(this);
        fireChildAdded(Relation.class, overallPosition, fact);
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
        fireChildAdded(CubeDimension.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CubeDimension.class) + pos;
            fireChildRemoved(CubeDimension.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(Measure.class) + pos;
        fireChildAdded(Measure.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Measure.class) + pos;
            fireChildRemoved(Measure.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
        fireChildAdded(CalculatedMember.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
            fireChildRemoved(CalculatedMember.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        if (fact != null) {
        	children.add(fact);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == CubeDimension.class) return offset;
        offset += dimensions.size();
        
        if (childClass == Measure.class) return offset;
        offset += measures.size();
        
        if (childClass == CalculatedMember.class) return offset;
        offset += calculatedMembers.size();
        
        if (childClass == NamedSet.class) return offset;
        offset += namedSets.size();
        
        if (childClass == Relation.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeDimension) {
            addDimension((CubeDimension) child);
        
        } else if (child instanceof Measure) {
            addMeasure((Measure) child);
        
        } else if (child instanceof CalculatedMember) {
            addCalculatedMember((CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            addNamedSet((NamedSet) child);
        
        } else if (child instanceof RelationOrJoin) {
            setFact((RelationOrJoin) child);
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeDimension) {
            int offset = childPositionOffset(CubeDimension.class);
            if ((index - offset) < 0 || (index - offset) > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + dimensions.size());
            }
            addDimension(index - offset, (CubeDimension) child);
        
        } else if (child instanceof Measure) {
            int offset = childPositionOffset(Measure.class);
            if ((index - offset) < 0 || (index - offset) > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + measures.size());
            }
            addMeasure(index - offset, (Measure) child);
        
        } else if (child instanceof CalculatedMember) {
            int offset = childPositionOffset(CalculatedMember.class);
            if ((index - offset) < 0 || (index - offset) > calculatedMembers.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + calculatedMembers.size());
            }
            addCalculatedMember(index - offset, (CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if ((index - offset) < 0 || (index - offset) > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index - offset, (NamedSet) child);
        
        } else if (child instanceof Relation) {
            setFact((Relation) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(CubeUsages.class);
        if (cubeUsage != null) {
            fireChildRemoved(CubeUsages.class, overallPosition, oldval);
        }
        cubeUsage = newval;
        cubeUsage.setParent(this);
        fireChildAdded(CubeUsages.class, overallPosition, cubeUsage);
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
        fireChildAdded(VirtualCubeDimension.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(VirtualCubeDimension.class) + pos;
            fireChildRemoved(VirtualCubeDimension.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(VirtualCubeMeasure.class) + pos;
        fireChildAdded(VirtualCubeMeasure.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(VirtualCubeMeasure.class) + pos;
            fireChildRemoved(VirtualCubeMeasure.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
        fireChildAdded(CalculatedMember.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CalculatedMember.class) + pos;
            fireChildRemoved(CalculatedMember.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(NamedSet.class) + pos;
        fireChildAdded(NamedSet.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(NamedSet.class) + pos;
            fireChildRemoved(NamedSet.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<NamedSet> getNamedSets() {
        return Collections.unmodifiableList(namedSets);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensions);
        
        children.addAll(measures);
        
        children.addAll(calculatedMembers);
        
        children.addAll(namedSets);
        
        if (cubeUsage != null) {
        	children.add(cubeUsage);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == VirtualCubeDimension.class) return offset;
        offset += dimensions.size();
        
        if (childClass == VirtualCubeMeasure.class) return offset;
        offset += measures.size();
        
        if (childClass == CalculatedMember.class) return offset;
        offset += calculatedMembers.size();
        
        if (childClass == NamedSet.class) return offset;
        offset += namedSets.size();
        
        if (childClass == CubeUsages.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof VirtualCubeDimension) {
            addDimension((VirtualCubeDimension) child);
        
        } else if (child instanceof VirtualCubeMeasure) {
            addMeasure((VirtualCubeMeasure) child);
        
        } else if (child instanceof CalculatedMember) {
            addCalculatedMember((CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            addNamedSet((NamedSet) child);
        
        } else if (child instanceof CubeUsages) {
            setCubeUsage((CubeUsages) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof VirtualCubeDimension) {
            int offset = childPositionOffset(VirtualCubeDimension.class);
            if ((index - offset) < 0 || (index - offset) > dimensions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + dimensions.size());
            }
            addDimension(index - offset, (VirtualCubeDimension) child);
        
        } else if (child instanceof VirtualCubeMeasure) {
            int offset = childPositionOffset(VirtualCubeMeasure.class);
            if ((index - offset) < 0 || (index - offset) > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + measures.size());
            }
            addMeasure(index - offset, (VirtualCubeMeasure) child);
        
        } else if (child instanceof CalculatedMember) {
            int offset = childPositionOffset(CalculatedMember.class);
            if ((index - offset) < 0 || (index - offset) > calculatedMembers.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + calculatedMembers.size());
            }
            addCalculatedMember(index - offset, (CalculatedMember) child);
        
        } else if (child instanceof NamedSet) {
            int offset = childPositionOffset(NamedSet.class);
            if ((index - offset) < 0 || (index - offset) > namedSets.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + namedSets.size());
            }
            addNamedSet(index - offset, (NamedSet) child);
        
        } else if (child instanceof CubeUsages) {
            setCubeUsage((CubeUsages) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
        fireChildAdded(CubeUsage.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CubeUsage.class) + pos;
            fireChildRemoved(CubeUsage.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<CubeUsage> getCubeUsages() {
        return Collections.unmodifiableList(cubeUsages);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(cubeUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == CubeUsage.class) return offset;
        offset += cubeUsages.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeUsage) {
            addCubeUsage((CubeUsage) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeUsage) {
            int offset = childPositionOffset(CubeUsage.class);
            if ((index - offset) < 0 || (index - offset) > cubeUsages.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + cubeUsages.size());
            }
            addCubeUsage(index - offset, (CubeUsage) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CubeUsage) {
            return removeCubeUsage((CubeUsage) child);
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(Hierarchy.class) + pos;
        fireChildAdded(Hierarchy.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Hierarchy.class) + pos;
            fireChildRemoved(Hierarchy.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<Hierarchy> getHierarchies() {
        return Collections.unmodifiableList(hierarchies);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(hierarchies);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Hierarchy.class) return offset;
        offset += hierarchies.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Hierarchy) {
            addHierarchy((Hierarchy) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Hierarchy) {
            int offset = childPositionOffset(Hierarchy.class);
            if ((index - offset) < 0 || (index - offset) > hierarchies.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + hierarchies.size());
            }
            addHierarchy(index - offset, (Hierarchy) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Hierarchy) {
            return removeHierarchy((Hierarchy) child);
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (relation != null) {
            fireChildRemoved(RelationOrJoin.class, overallPosition, oldval);
        }
        relation = newval;
        relation.setParent(this);
        fireChildAdded(RelationOrJoin.class, overallPosition, relation);
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
        fireChildAdded(Level.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Level.class) + pos;
            fireChildRemoved(Level.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(MemberReaderParameter.class) + pos;
        fireChildAdded(MemberReaderParameter.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(MemberReaderParameter.class) + pos;
            fireChildRemoved(MemberReaderParameter.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<MemberReaderParameter> getMemberReaderParameters() {
        return Collections.unmodifiableList(memberReaderParameters);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(levels);
        
        children.addAll(memberReaderParameters);
        
        if (relation != null) {
        	children.add(relation);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Level.class) return offset;
        offset += levels.size();
        
        if (childClass == MemberReaderParameter.class) return offset;
        offset += memberReaderParameters.size();
        
        if (childClass == RelationOrJoin.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Level) {
            addLevel((Level) child);
        
        } else if (child instanceof MemberReaderParameter) {
            addMemberReaderParameter((MemberReaderParameter) child);
        
        } else if (child instanceof RelationOrJoin) {
            setRelation((RelationOrJoin) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Level) {
            int offset = childPositionOffset(Level.class);
            if ((index - offset) < 0 || (index - offset) > levels.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + levels.size());
            }
            addLevel(index - offset, (Level) child);
        
        } else if (child instanceof MemberReaderParameter) {
            int offset = childPositionOffset(MemberReaderParameter.class);
            if ((index - offset) < 0 || (index - offset) > memberReaderParameters.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + memberReaderParameters.size());
            }
            addMemberReaderParameter(index - offset, (MemberReaderParameter) child);
        
        } else if (child instanceof RelationOrJoin) {
            setRelation((RelationOrJoin) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(KeyExpression.class);
        if (keyExp != null) {
            fireChildRemoved(KeyExpression.class, overallPosition, oldval);
        }
        keyExp = newval;
        keyExp.setParent(this);
        fireChildAdded(KeyExpression.class, overallPosition, keyExp);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(NameExpression.class);
        if (nameExp != null) {
            fireChildRemoved(NameExpression.class, overallPosition, oldval);
        }
        nameExp = newval;
        nameExp.setParent(this);
        fireChildAdded(NameExpression.class, overallPosition, nameExp);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(OrdinalExpression.class);
        if (ordinalExp != null) {
            fireChildRemoved(OrdinalExpression.class, overallPosition, oldval);
        }
        ordinalExp = newval;
        ordinalExp.setParent(this);
        fireChildAdded(OrdinalExpression.class, overallPosition, ordinalExp);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(ParentExpression.class);
        if (parentExp != null) {
            fireChildRemoved(ParentExpression.class, overallPosition, oldval);
        }
        parentExp = newval;
        parentExp.setParent(this);
        fireChildAdded(ParentExpression.class, overallPosition, parentExp);
	}

    /**  */
    private Closure /* */ closure;
    
    public Closure /* */ getClosure() {
        return closure;
    }
    
    public void setClosure(Closure /* */ newval) {
        Closure /* */ oldval = closure;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Closure.class);
        if (closure != null) {
            fireChildRemoved(Closure.class, overallPosition, oldval);
        }
        closure = newval;
        closure.setParent(this);
        fireChildAdded(Closure.class, overallPosition, closure);
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
        fireChildAdded(Property.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Property.class) + pos;
            fireChildRemoved(Property.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
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
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Property.class) return offset;
        offset += properties.size();
        
        if (childClass == KeyExpression.class) return offset;
        offset += 1;
        
        if (childClass == NameExpression.class) return offset;
        offset += 1;
        
        if (childClass == OrdinalExpression.class) return offset;
        offset += 1;
        
        if (childClass == ParentExpression.class) return offset;
        offset += 1;
        
        if (childClass == Closure.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Property) {
            addProperty((Property) child);
        
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
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Property) {
            int offset = childPositionOffset(Property.class);
            if ((index - offset) < 0 || (index - offset) > properties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + properties.size());
            }
            addProperty(index - offset, (Property) child);
        
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
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Table.class);
        if (table != null) {
            fireChildRemoved(Table.class, overallPosition, oldval);
        }
        table = newval;
        table.setParent(this);
        fireChildAdded(Table.class, overallPosition, table);
	}

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        if (table != null) {
        	children.add(table);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Table.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Table) {
            setTable((Table) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Table) {
            setTable((Table) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Table) {
            setTable(null);
            return true;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(MeasureExpression.class);
        if (measureExp != null) {
            fireChildRemoved(MeasureExpression.class, overallPosition, oldval);
        }
        measureExp = newval;
        measureExp.setParent(this);
        fireChildAdded(MeasureExpression.class, overallPosition, measureExp);
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
        fireChildAdded(CalculatedMemberProperty.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
            fireChildRemoved(CalculatedMemberProperty.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberProperties);
        
        if (measureExp != null) {
        	children.add(measureExp);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == CalculatedMemberProperty.class) return offset;
        offset += memberProperties.size();
        
        if (childClass == MeasureExpression.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            addMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof MeasureExpression) {
            setMeasureExp((MeasureExpression) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            int offset = childPositionOffset(CalculatedMemberProperty.class);
            if ((index - offset) < 0 || (index - offset) > memberProperties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + memberProperties.size());
            }
            addMemberPropertie(index - offset, (CalculatedMemberProperty) child);
        
        } else if (child instanceof MeasureExpression) {
            setMeasureExp((MeasureExpression) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CalculatedMemberProperty) {
            return removeMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof MeasureExpression) {
            setMeasureExp(null);
            return true;
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Formula.class);
        if (formulaElement != null) {
            fireChildRemoved(Formula.class, overallPosition, oldval);
        }
        formulaElement = newval;
        formulaElement.setParent(this);
        fireChildAdded(Formula.class, overallPosition, formulaElement);
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
        fireChildAdded(CalculatedMemberProperty.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CalculatedMemberProperty.class) + pos;
            fireChildRemoved(CalculatedMemberProperty.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<CalculatedMemberProperty> getMemberProperties() {
        return Collections.unmodifiableList(memberProperties);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberProperties);
        
        if (formulaElement != null) {
        	children.add(formulaElement);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == CalculatedMemberProperty.class) return offset;
        offset += memberProperties.size();
        
        if (childClass == Formula.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            addMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CalculatedMemberProperty) {
            int offset = childPositionOffset(CalculatedMemberProperty.class);
            if ((index - offset) < 0 || (index - offset) > memberProperties.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + memberProperties.size());
            }
            addMemberPropertie(index - offset, (CalculatedMemberProperty) child);
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CalculatedMemberProperty) {
            return removeMemberPropertie((CalculatedMemberProperty) child);
        
        } else if (child instanceof Formula) {
            setFormulaElement(null);
            return true;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Formula.class);
        if (formulaElement != null) {
            fireChildRemoved(Formula.class, overallPosition, oldval);
        }
        formulaElement = newval;
        formulaElement.setParent(this);
        fireChildAdded(Formula.class, overallPosition, formulaElement);
	}

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        if (formulaElement != null) {
        	children.add(formulaElement);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Formula.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Formula) {
            setFormulaElement((Formula) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Formula) {
            setFormulaElement(null);
            return true;
        
        } else {
            return super.removeChild(child);
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
	
	public String getText() {
		return text;
	}
	
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		pcs.firePropertyChange("text", oldval, newval);
	}


    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(SQL.class) + pos;
        fireChildAdded(SQL.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(SQL.class) + pos;
            fireChildRemoved(SQL.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<SQL> getSelects() {
        return Collections.unmodifiableList(selects);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(selects);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == SQL.class) return offset;
        offset += selects.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            addSelect((SQL) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            int offset = childPositionOffset(SQL.class);
            if ((index - offset) < 0 || (index - offset) > selects.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + selects.size());
            }
            addSelect(index - offset, (SQL) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SQL) {
            return removeSelect((SQL) child);
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getDialect() {
        return dialect;
    }
    
    public void setDialect(String /* */ newval) {
        String /* */ oldval = dialect;
        dialect = newval;
        pcs.firePropertyChange("dialect", oldval, newval);
    }

	private String text;
	
	public String getText() {
		return text;
	}
	
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		pcs.firePropertyChange("text", oldval, newval);
	}


    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (left != null) {
            fireChildRemoved(RelationOrJoin.class, overallPosition, oldval);
        }
        left = newval;
        left.setParent(this);
        fireChildAdded(RelationOrJoin.class, overallPosition, left);
	}

    /**  */
    private RelationOrJoin /* */ right;
    
    public RelationOrJoin /* */ getRight() {
        return right;
    }
    
    public void setRight(RelationOrJoin /* */ newval) {
        RelationOrJoin /* */ oldval = right;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(RelationOrJoin.class);
        if (right != null) {
            fireChildRemoved(RelationOrJoin.class, overallPosition, oldval);
        }
        right = newval;
        right.setParent(this);
        fireChildAdded(RelationOrJoin.class, overallPosition, right);
	}

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        if (left != null) {
        	children.add(left);
        }
        
        if (right != null) {
        	children.add(right);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == RelationOrJoin.class) return offset;
        offset += 1;
        
        if (childClass == RelationOrJoin.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
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
    public void addChild(int index, OLAPObject child) {
		
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
    public boolean removeChild(OLAPObject child) {
		
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
        	return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(SQL.class);
        if (filter != null) {
            fireChildRemoved(SQL.class, overallPosition, oldval);
        }
        filter = newval;
        filter.setParent(this);
        fireChildAdded(SQL.class, overallPosition, filter);
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
        fireChildAdded(AggExclude.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggExclude.class) + pos;
            fireChildRemoved(AggExclude.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(AggTable.class) + pos;
        fireChildAdded(AggTable.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggTable.class) + pos;
            fireChildRemoved(AggTable.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<AggTable> getAggTables() {
        return Collections.unmodifiableList(aggTables);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(aggExcludes);
        
        children.addAll(aggTables);
        
        if (filter != null) {
        	children.add(filter);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == AggExclude.class) return offset;
        offset += aggExcludes.size();
        
        if (childClass == AggTable.class) return offset;
        offset += aggTables.size();
        
        if (childClass == SQL.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            addAggExclude((AggExclude) child);
        
        } else if (child instanceof AggTable) {
            addAggTable((AggTable) child);
        
        } else if (child instanceof SQL) {
            setFilter((SQL) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            int offset = childPositionOffset(AggExclude.class);
            if ((index - offset) < 0 || (index - offset) > aggExcludes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + aggExcludes.size());
            }
            addAggExclude(index - offset, (AggExclude) child);
        
        } else if (child instanceof AggTable) {
            int offset = childPositionOffset(AggTable.class);
            if ((index - offset) < 0 || (index - offset) > aggTables.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + aggTables.size());
            }
            addAggTable(index - offset, (AggTable) child);
        
        } else if (child instanceof SQL) {
            setFilter((SQL) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(ColumnDefs.class);
        if (columnDefs != null) {
            fireChildRemoved(ColumnDefs.class, overallPosition, oldval);
        }
        columnDefs = newval;
        columnDefs.setParent(this);
        fireChildAdded(ColumnDefs.class, overallPosition, columnDefs);
	}

    /**  */
    private Rows /* */ rows;
    
    public Rows /* */ getRows() {
        return rows;
    }
    
    public void setRows(Rows /* */ newval) {
        Rows /* */ oldval = rows;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Rows.class);
        if (rows != null) {
            fireChildRemoved(Rows.class, overallPosition, oldval);
        }
        rows = newval;
        rows.setParent(this);
        fireChildAdded(Rows.class, overallPosition, rows);
	}

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        if (columnDefs != null) {
        	children.add(columnDefs);
        }
        
        if (rows != null) {
        	children.add(rows);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == ColumnDefs.class) return offset;
        offset += 1;
        
        if (childClass == Rows.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof ColumnDefs) {
            setColumnDefs((ColumnDefs) child);
        
        } else if (child instanceof Rows) {
            setRows((Rows) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof ColumnDefs) {
            setColumnDefs((ColumnDefs) child);
        
        } else if (child instanceof Rows) {
            setRows((Rows) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof ColumnDefs) {
            setColumnDefs(null);
            return true;
        
        } else if (child instanceof Rows) {
            setRows(null);
            return true;
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(ColumnDef.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(ColumnDef.class) + pos;
            fireChildRemoved(ColumnDef.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<ColumnDef> getArray() {
        return Collections.unmodifiableList(array);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == ColumnDef.class) return offset;
        offset += array.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof ColumnDef) {
            addArray((ColumnDef) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof ColumnDef) {
            int offset = childPositionOffset(ColumnDef.class);
            if ((index - offset) < 0 || (index - offset) > array.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + array.size());
            }
            addArray(index - offset, (ColumnDef) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof ColumnDef) {
            return removeArray((ColumnDef) child);
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(Row.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Row.class) + pos;
            fireChildRemoved(Row.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<Row> getArray() {
        return Collections.unmodifiableList(array);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(array);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Row.class) return offset;
        offset += array.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Row) {
            addArray((Row) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Row) {
            int offset = childPositionOffset(Row.class);
            if ((index - offset) < 0 || (index - offset) > array.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + array.size());
            }
            addArray(index - offset, (Row) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Row) {
            return removeArray((Row) child);
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(Value.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(Value.class) + pos;
            fireChildRemoved(Value.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<Value> getValues() {
        return Collections.unmodifiableList(values);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(values);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == Value.class) return offset;
        offset += values.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Value) {
            addValue((Value) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof Value) {
            int offset = childPositionOffset(Value.class);
            if ((index - offset) < 0 || (index - offset) > values.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + values.size());
            }
            addValue(index - offset, (Value) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof Value) {
            return removeValue((Value) child);
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

	private String text;
	
	public String getText() {
		return text;
	}
	
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		pcs.firePropertyChange("text", oldval, newval);
	}


    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(AggFactCount.class);
        if (factcount != null) {
            fireChildRemoved(AggFactCount.class, overallPosition, oldval);
        }
        factcount = newval;
        factcount.setParent(this);
        fireChildAdded(AggFactCount.class, overallPosition, factcount);
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
        fireChildAdded(AggIgnoreColumn.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggIgnoreColumn.class) + pos;
            fireChildRemoved(AggIgnoreColumn.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(AggForeignKey.class) + pos;
        fireChildAdded(AggForeignKey.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggForeignKey.class) + pos;
            fireChildRemoved(AggForeignKey.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(AggMeasure.class) + pos;
        fireChildAdded(AggMeasure.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggMeasure.class) + pos;
            fireChildRemoved(AggMeasure.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(AggLevel.class) + pos;
        fireChildAdded(AggLevel.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggLevel.class) + pos;
            fireChildRemoved(AggLevel.class, overallPosition, removedItem);
        }
        return removedItem;
    }

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

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(ignoreColumns);
        
        children.addAll(foreignKeys);
        
        children.addAll(measures);
        
        children.addAll(levels);
        
        if (factcount != null) {
        	children.add(factcount);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == AggIgnoreColumn.class) return offset;
        offset += ignoreColumns.size();
        
        if (childClass == AggForeignKey.class) return offset;
        offset += foreignKeys.size();
        
        if (childClass == AggMeasure.class) return offset;
        offset += measures.size();
        
        if (childClass == AggLevel.class) return offset;
        offset += levels.size();
        
        if (childClass == AggFactCount.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggIgnoreColumn) {
            addIgnoreColumn((AggIgnoreColumn) child);
        
        } else if (child instanceof AggForeignKey) {
            addForeignKey((AggForeignKey) child);
        
        } else if (child instanceof AggMeasure) {
            addMeasure((AggMeasure) child);
        
        } else if (child instanceof AggLevel) {
            addLevel((AggLevel) child);
        
        } else if (child instanceof AggFactCount) {
            setFactcount((AggFactCount) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggIgnoreColumn) {
            int offset = childPositionOffset(AggIgnoreColumn.class);
            if ((index - offset) < 0 || (index - offset) > ignoreColumns.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + ignoreColumns.size());
            }
            addIgnoreColumn(index - offset, (AggIgnoreColumn) child);
        
        } else if (child instanceof AggForeignKey) {
            int offset = childPositionOffset(AggForeignKey.class);
            if ((index - offset) < 0 || (index - offset) > foreignKeys.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + foreignKeys.size());
            }
            addForeignKey(index - offset, (AggForeignKey) child);
        
        } else if (child instanceof AggMeasure) {
            int offset = childPositionOffset(AggMeasure.class);
            if ((index - offset) < 0 || (index - offset) > measures.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + measures.size());
            }
            addMeasure(index - offset, (AggMeasure) child);
        
        } else if (child instanceof AggLevel) {
            int offset = childPositionOffset(AggLevel.class);
            if ((index - offset) < 0 || (index - offset) > levels.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + levels.size());
            }
            addLevel(index - offset, (AggLevel) child);
        
        } else if (child instanceof AggFactCount) {
            setFactcount((AggFactCount) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
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
            return super.removeChild(child);
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
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(AggExclude.class) + pos;
        fireChildAdded(AggExclude.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(AggExclude.class) + pos;
            fireChildRemoved(AggExclude.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<AggExclude> getExcludes() {
        return Collections.unmodifiableList(excludes);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(excludes);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == AggExclude.class) return offset;
        offset += excludes.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            addExclude((AggExclude) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof AggExclude) {
            int offset = childPositionOffset(AggExclude.class);
            if ((index - offset) < 0 || (index - offset) > excludes.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + excludes.size());
            }
            addExclude(index - offset, (AggExclude) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof AggExclude) {
            return removeExclude((AggExclude) child);
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(SQL.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(SQL.class) + pos;
            fireChildRemoved(SQL.class, overallPosition, removedItem);
        }
        return removedItem;
    }

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

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(expressions);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == SQL.class) return offset;
        offset += expressions.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            addExpression((SQL) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SQL) {
            int offset = childPositionOffset(SQL.class);
            if ((index - offset) < 0 || (index - offset) > expressions.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + expressions.size());
            }
            addExpression(index - offset, (SQL) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SQL) {
            return removeExpression((SQL) child);
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(SchemaGrant.class) + pos;
        fireChildAdded(SchemaGrant.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(SchemaGrant.class) + pos;
            fireChildRemoved(SchemaGrant.class, overallPosition, removedItem);
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
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(Union.class);
        if (union != null) {
            fireChildRemoved(Union.class, overallPosition, oldval);
        }
        union = newval;
        union.setParent(this);
        fireChildAdded(Union.class, overallPosition, union);
	}

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(schemaGrants);
        
        if (union != null) {
        	children.add(union);
        }
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == SchemaGrant.class) return offset;
        offset += schemaGrants.size();
        
        if (childClass == Union.class) return offset;
        offset += 1;
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SchemaGrant) {
            addSchemaGrant((SchemaGrant) child);
        
        } else if (child instanceof Union) {
            setUnion((Union) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof SchemaGrant) {
            int offset = childPositionOffset(SchemaGrant.class);
            if ((index - offset) < 0 || (index - offset) > schemaGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + schemaGrants.size());
            }
            addSchemaGrant(index - offset, (SchemaGrant) child);
        
        } else if (child instanceof Union) {
            setUnion((Union) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof SchemaGrant) {
            return removeSchemaGrant((SchemaGrant) child);
        
        } else if (child instanceof Union) {
            setUnion(null);
            return true;
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getAccess() {
        return access;
    }
    
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        pcs.firePropertyChange("access", oldval, newval);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(CubeGrant.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(CubeGrant.class) + pos;
            fireChildRemoved(CubeGrant.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<CubeGrant> getCubeGrants() {
        return Collections.unmodifiableList(cubeGrants);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(cubeGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == CubeGrant.class) return offset;
        offset += cubeGrants.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeGrant) {
            addCubeGrant((CubeGrant) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof CubeGrant) {
            int offset = childPositionOffset(CubeGrant.class);
            if ((index - offset) < 0 || (index - offset) > cubeGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + cubeGrants.size());
            }
            addCubeGrant(index - offset, (CubeGrant) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof CubeGrant) {
            return removeCubeGrant((CubeGrant) child);
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(DimensionGrant.class) + pos;
        fireChildAdded(DimensionGrant.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(DimensionGrant.class) + pos;
            fireChildRemoved(DimensionGrant.class, overallPosition, removedItem);
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
        int overallPosition = childPositionOffset(HierarchyGrant.class) + pos;
        fireChildAdded(HierarchyGrant.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(HierarchyGrant.class) + pos;
            fireChildRemoved(HierarchyGrant.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<HierarchyGrant> getHierarchyGrants() {
        return Collections.unmodifiableList(hierarchyGrants);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(dimensionGrants);
        
        children.addAll(hierarchyGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == DimensionGrant.class) return offset;
        offset += dimensionGrants.size();
        
        if (childClass == HierarchyGrant.class) return offset;
        offset += hierarchyGrants.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof DimensionGrant) {
            addDimensionGrant((DimensionGrant) child);
        
        } else if (child instanceof HierarchyGrant) {
            addHierarchyGrant((HierarchyGrant) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof DimensionGrant) {
            int offset = childPositionOffset(DimensionGrant.class);
            if ((index - offset) < 0 || (index - offset) > dimensionGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + dimensionGrants.size());
            }
            addDimensionGrant(index - offset, (DimensionGrant) child);
        
        } else if (child instanceof HierarchyGrant) {
            int offset = childPositionOffset(HierarchyGrant.class);
            if ((index - offset) < 0 || (index - offset) > hierarchyGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + hierarchyGrants.size());
            }
            addHierarchyGrant(index - offset, (HierarchyGrant) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof DimensionGrant) {
            return removeDimensionGrant((DimensionGrant) child);
        
        } else if (child instanceof HierarchyGrant) {
            return removeHierarchyGrant((HierarchyGrant) child);
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getDimension() {
        return dimension;
    }
    
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        pcs.firePropertyChange("dimension", oldval, newval);
    }

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        int overallPosition = childPositionOffset(MemberGrant.class) + pos;
        fireChildAdded(MemberGrant.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(MemberGrant.class) + pos;
            fireChildRemoved(MemberGrant.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<MemberGrant> getMemberGrants() {
        return Collections.unmodifiableList(memberGrants);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(memberGrants);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == MemberGrant.class) return offset;
        offset += memberGrants.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof MemberGrant) {
            addMemberGrant((MemberGrant) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof MemberGrant) {
            int offset = childPositionOffset(MemberGrant.class);
            if ((index - offset) < 0 || (index - offset) > memberGrants.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + memberGrants.size());
            }
            addMemberGrant(index - offset, (MemberGrant) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof MemberGrant) {
            return removeMemberGrant((MemberGrant) child);
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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
        fireChildAdded(RoleUsage.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(RoleUsage.class) + pos;
            fireChildRemoved(RoleUsage.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List<RoleUsage> getRoleUsages() {
        return Collections.unmodifiableList(roleUsages);
    }
    

    @Override
    public List<OLAPObject> getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List<OLAPObject> children = new ArrayList<OLAPObject>();
        
        children.addAll(roleUsages);
        
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class<? extends OLAPObject> childClass) {
        int offset = 0;
        
        if (childClass == RoleUsage.class) return offset;
        offset += roleUsages.size();
        
        return offset;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof RoleUsage) {
            addRoleUsage((RoleUsage) child);
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else if (child instanceof RoleUsage) {
            int offset = childPositionOffset(RoleUsage.class);
            if ((index - offset) < 0 || (index - offset) > roleUsages.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + offset +
                    "; max=" + roleUsages.size());
            }
            addRoleUsage(index - offset, (RoleUsage) child);
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else if (child instanceof RoleUsage) {
            return removeRoleUsage((RoleUsage) child);
        
        } else {
            return super.removeChild(child);
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
    
    public String /* */ getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String /* */ newval) {
        String /* */ oldval = roleName;
        roleName = newval;
        pcs.firePropertyChange("roleName", oldval, newval);
    }

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
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

    @Override
    public List<OLAPObject> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    
    @Override
    public void addChild(OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(child);
        }
			    
    }

    @Override
    public void addChild(int index, OLAPObject child) {
		
        if (false) {
        
        } else {
            super.addChild(index, child);
        }
			
    }
    
    @Override
    public boolean removeChild(OLAPObject child) {
		
        if (false) {
        	return false;
        
        } else {
            return super.removeChild(child);
        }
			    
    }

} // end of element Parameter
} // end of entire model
