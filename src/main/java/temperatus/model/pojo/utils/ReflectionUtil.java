package temperatus.model.pojo.utils;

/*******************************************************************************
 * <p>
 * Copyright (c) 2014, 2015 itemis AG and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Jan Köhnlein (itemis AG) - initial API and implementation (#427106)
 * Matthias Wienand (itemis AG) - add methods and javadoc
 *******************************************************************************/


import java.lang.reflect.Field;

/**
 * Allows to access the value of private fields.
 *
 * @author Jan Koehnlein
 * @author mwienand
 *
 */
public class ReflectionUtil {

    /**
     * Returns the value of the specified private field for the given
     * <i>owner</i>.
     *
     * @param <T>
     *            The type of the field value.
     *
     * @param owner
     *            {@link Object} from which the field is read.
     * @param fieldName
     *            Name of the field to read.
     * @return The value of the specified field for the given <i>owner</i>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object owner, String fieldName) {
        Field field = getPrivateField(owner, fieldName);
        try {
            return (T) field.get(owner);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to find the field specified by <i>fieldName</i> in the class
     * hierarchy of the given <i>owner</i>. If the field can be found, it is
     * made accessible, so that its value can be read and written.
     *
     * @param owner
     *            {@link Object} from which the {@link Field} should be
     *            extracted.
     * @param fieldName
     *            The name of the field.
     * @return {@link Field} if it can be found, otherwise <code>null</code>.
     */
    public static Field getPrivateField(Object owner, String fieldName) {
        Class<? extends Object> currentClass = owner.getClass();
        Field field = null;
        do {
            try {
                field = currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
                if (currentClass == null) {
                    return null;
                }
            }
        } while (field == null);
        field.setAccessible(true);
        return field;
    }

    /**
     * Sets the value of the specified private field for the given <i>owner</i>
     * to the given <i>value</i>.
     *
     * @param <T>
     *            The type of the field value.
     *
     * @param owner
     *            {@link Object} for which the field is set.
     * @param fieldName
     *            Name of the field.
     * @param value
     *            New value for the field.
     */
    public static <T> void setFieldValue(Object owner, String fieldName,
                                                T value) {
        Field field = getPrivateField(owner, fieldName);
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}