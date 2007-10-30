/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.undo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;

public class UndoableEditClass extends AbstractUndoableEdit{
	
	private static final Logger logger = Logger.getLogger(UndoableEditClass.class);
	
	private MatchMakerEvent undoEvent;
	private MatchMakerObject mmo;

	public UndoableEditClass(MatchMakerEvent e, MatchMakerObject mmo){
		super();
		undoEvent = e;
		this.mmo = mmo;
	}

	public void undo(){
		super.undo();
		try {
			undoEvent.getSource().setUndoing(true);
		    modifyProperty(undoEvent.getOldValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					undoEvent.getPropertyName(), e);
			throw new CannotUndoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+undoEvent.getPropertyName()+
					" on "+undoEvent.getSource()+" threw exception", e);
			throw new CannotUndoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					undoEvent.getSource(), e);
			throw new CannotUndoException();
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}

	public void redo(){
		super.redo();
		try {
			undoEvent.getSource().setUndoing(true);
		    modifyProperty(undoEvent.getNewValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					undoEvent.getPropertyName(), e);
			throw new CannotUndoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+undoEvent.getPropertyName()+
					" on "+undoEvent.getSource()+" threw exception", e);
			throw new CannotUndoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					undoEvent.getSource(), e);
			throw new CannotUndoException();
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}
	
	private void modifyProperty(Object value) throws IntrospectionException,
	    IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
		// We did this using BeanUtils.copyProperty() before, but the error
		// messages were too vague.
		BeanInfo info = Introspector.getBeanInfo(undoEvent.getSource().getClass());
		
		PropertyDescriptor[] props = info.getPropertyDescriptors();
		for (PropertyDescriptor prop : Arrays.asList(props)) {
		    if (prop.getName().equals(undoEvent.getPropertyName())) {
		        Method writeMethod = prop.getWriteMethod();
		        if (writeMethod != null) {
		        	System.out.println(writeMethod);
		            writeMethod.invoke(undoEvent.getSource(), new Object[] { value });
		        }
		    }
		}
	}
}