/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * The abstract class of MatchMakerObject, it has a listener listens to the change
 * of children, properties and structure, any thing changed in the object will
 * cause auditing information changes.
 *
 * @param <T> The type of this matchmaker object implementation
 * @param <C> The child type of this matchmaker object implementation
 */
public abstract class AbstractMatchMakerObject implements MatchMakerObject
	 {

    private static final Logger logger = Logger.getLogger(AbstractMatchMakerObject.class);

	private MatchMakerObject parent;

	@SuppressWarnings("unchecked")
	private MatchMakerEventSupport eventSupport =
		new MatchMakerEventSupport(this);

	private List<MatchMakerObject> children = new ArrayList<MatchMakerObject>();
	private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;
	@SuppressWarnings(value={"UWF_UNWRITTEN_FIELD"}, justification="Used reflectively by Hibernate")
	private Date createDate;
	private MatchMakerSession matchMakerSession;
	private String name;

	private boolean visible;

	public AbstractMatchMakerObject() {
		visible = true;
	}

	/**
     * Adds the given child object to the end of this MatchMakerObject's child
     * list, then fires an event indicating the insertion took place.
     * <p>
     * Note to subclassers: If you override this method, you must fire the
     * childrenInserted event yourself!
     * 
     * @param child
     *            The child object to add. Must not be null.
     */
	public final void addChild(MatchMakerObject child) {
        addImpl(children.size(), child);
	}

	/**
     * Inserts the given child object at the given position of this
     * MatchMakerObject's child list, then fires an event indicating the
     * insertion took place.
     * <p>
     * Note to subclassers: If you override this method, you must fire the
     * childrenInserted event yourself!
     * 
     * @param index
     *            The position to insert the child at. 0 inserts the child at
     *            the beginning of the list. The given index must be at least 0
     *            but not more than the current child count of this object.
     * @param child
     *            The child object to add. Must not be null.
     */
	public final void addChild(int index, MatchMakerObject child) {
        addImpl(index, child);
	}
	
	protected void addImpl(int index, MatchMakerObject child) {
		logger.debug("addChild: children collection is a "+children.getClass().getName());
        if(child== null) throw new NullPointerException("Cannot add a null child");
		children.add(index, child);
		child.setParent(this);
		List<MatchMakerObject> insertedChildren = new ArrayList<MatchMakerObject>();
		insertedChildren.add(child);
		eventSupport.fireChildrenInserted("children",new int[] {index},insertedChildren);
	}
	
	

    /**
     * Returns the number of children in this MatchMakerObject. For those
     * objects that do not allow children, always returns 0.
     */
	public int getChildCount() {
		return children.size();
	}

    /**
     * Returns the list of children in this object.  The returned list must not
     * be modified by client code (this is not enforced, but the MatchMaker will
     * not work properly if the client code modifies this list directly).  See
     * {@link #addChild(int, MatchMakerObject)} and {@link #removeChild(MatchMakerObject)}
     * for the correct way to manipulate the child list.
     * <p>
     * MatchMakerObjects that do not support children always return an empty
     * list (never null).
     */
	public List<MatchMakerObject> getChildren() {
		return children;
	}

    /**
     * Replaces the list of children with the passed in list.
     * <p>
     * This method should only be used by code that saves and restores
     * MatchMaker projects. At the time of this writing, this includes the
     * Hibernate and XML persistence layers.
     * 
     * @param children
     */
    public void setChildren(List<MatchMakerObject> children){
        this.children = children;
        eventSupport.fireStructureChanged();
    }

	/**
     * Removes the given child and fires a childrenRemoved event.  If the
     * given child is not present in this object, calling this method has
     * no effect (no children are removed and no events are fired).
     * <p>
	 * Note to subclassers: If you override this method, you must fire the
     * ChildrenRemoved yourself.
     *
	 * @param child The child object to remove.  If it is not present in this
     * object's child list, this method call has no effect.
	 */
	public void removeChild(MatchMakerObject child) {
		int childIndex = children.indexOf(child);
        if (childIndex == -1) return;
        int [] removedIndices = {childIndex};
		List<MatchMakerObject> removedChildren = new ArrayList<MatchMakerObject>();
		removedChildren.add(child);
		children.remove(child);
		eventSupport.fireChildrenRemoved("children",removedIndices,removedChildren);
	}

	/**
     * Swaps the elements at the specified positions.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     *
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     */
	public void swapChildren(int i, int j) {
		final List<MatchMakerObject> l = getChildren();
		try {
			startCompoundEdit();
			int less;
			int more;
			if (i < j) {
				less = i;
				more = j;
			} else {
				less = j;
				more = i;
			}
			MatchMakerObject child1 = l.get(less);
			MatchMakerObject child2 = l.get(more);

			removeChild(child1);
			removeChild(child2);

			addChild(less, child2);
			addChild(more, child1);
		} finally {
			endCompoundEdit();
		}
	}
	
	public void moveChild(int from, int to) {
		if (to == from) return;
		final List<MatchMakerObject> l = getChildren();
		MatchMakerObject child = l.get(from);
		try {
			startCompoundEdit();
			removeChild(l.get(from));
			addChild(to, child);
		} finally {
			endCompoundEdit();
		}
	}
	
	public String getLastUpdateAppUser() {
		return lastUpdateAppUser;
	}
	public String getLastUpdateOSUser() {
		return lastUpdateOsUser;
	}
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
    
	/**
	 * Responds to an update by updating the audit information (last update user and date), but
     * only if the object is participating in an ORM session.  The reasons for only doing this
     * in the context of an ORM session are:
     * <ol>
     *  <li>The application username is a property of the ORM session
     *  <li>Updating these properties during unit testing complicates some of the automatic tests
     * </ol>
	 */
	public void registerUpdate() {
		// FIXME Change this to be handled by the daos
		logger.debug("We have registered a change");
		if (matchMakerSession != null){
			lastUpdateDate = new Date();
			lastUpdateOsUser = System.getProperty("user.name");
			lastUpdateAppUser = matchMakerSession.getAppUser();
		}
	}

	public MatchMakerObject getParent() {
		return parent;
	}

	public void setParent(MatchMakerObject parent) {
		MatchMakerObject oldValue = this.parent;
		this.parent = parent;
		eventSupport.firePropertyChange("parent", oldValue, this.parent);
	}

	public void setSession(MatchMakerSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
	}

	/**
	 * Returns this object's session, if it has one. Otherwise, defers
	 * to the parent's getSession() method.
	 */
    public MatchMakerSession getSession() {
        if (getParent() == this) {
            // this check prevents infinite recursion in case of funniness
            throw new IllegalStateException("Something tells me this class belongs to the royal family");
        }
        if (matchMakerSession != null || getParent() == null) {
        	if (logger.isDebugEnabled()) {
        		logger.debug(getClass().getName()+"@"+System.identityHashCode(this)+
        				": Returning session "+matchMakerSession+
        				" (my parent is "+getParent()+")");
        	}
            return matchMakerSession;
        } else {
        	if (logger.isDebugEnabled()) {
        		logger.debug(getClass().getName()+"@"+System.identityHashCode(this)+
        				": looking up the tree");
        	}
            return getParent().getSession();
        }
    }

	public Date getCreateDate() {
		return createDate;
	}

	public boolean allowsChildren() {
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		eventSupport.firePropertyChange("name", oldValue, this.name);
	}

    /**
     * Declared abstract here to force subclasses to implement equals().
     */
	public abstract boolean equals(Object obj);

    /**
     * Declared abstract here to force subclasses to implement hashCode().
     */
	public abstract int hashCode();


	/////// Event stuff ///////

	@Override
	public void addMatchMakerListener(MatchMakerListener l) {
		eventSupport.addMatchMakerListener(l);
	}

	@Override
	public void removeMatchMakerListener(MatchMakerListener l) {
		eventSupport.removeMatchMakerListener(l);
	}

	protected MatchMakerEventSupport getEventSupport() {
		return eventSupport;
	}
	
	public void setVisible(boolean visible) {
		boolean old = this.visible;
		this.visible = visible;
		eventSupport.firePropertyChange("visible", old, visible);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/////// Undo Stuff ///////
	// fires the event as a undo event if this is true;
	private boolean isUndoing;

	public boolean isUndoing() {
		return isUndoing;
	}

	public void setUndoing(boolean isUndoing) {
		this.isUndoing = isUndoing;
	}
	
	/**
	 * Searches through the tree recursively to find if the mmo 
	 * is part of the hierarchy.
	 */
	public boolean hierarchyContains(MatchMakerObject mmo) {
		if (allowsChildren()) {
			if (getChildren().contains(mmo)) {
				return true;
			}
			for (MatchMakerObject child : getChildren()) {
				if (child.hierarchyContains(mmo)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Starts a compound edit so that the whole compound edit can
	 * be undo'ed at the same time. Note that one must call endCompoundEdit after or the
	 * undo listeners will not work properly. Use the following code snippet to ensure 
	 * that endCompoundEdit() is called. <p>
	 * <code> try { 
	 * startCompoundEdit();
	 * ... mutate objects ...
	 * } finally {
	 * endCompoundEdit();
	 * }
	 * </code>
     * @see AbstractMatchMakerObject#endCompoundEdit()
	 */
	public void startCompoundEdit() {
		getEventSupport().firePropertyChange("UNDOSTATE", false, true);
	}
	
	/**
	 * Ends a compound edit.
	 * @see AbstractMatchMakerObject#startCompoundEdit()
	 */
	public void endCompoundEdit() {
		getEventSupport().firePropertyChange("UNDOSTATE", true, false);
	}
}