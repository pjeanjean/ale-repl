/**
 * ******************************************************************************
 * Copyright (c) 2017 Inria and Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  *
 * Contributors:
 *     Inria - initial API and implementation
 *  *
 * generated by Xtext 2.14.0
 *  ******************************************************************************
 */
package org.eclipse.emf.ecoretools.ale;

import java.lang.String;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>For Each</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.ForEach#getIterator <em>Iterator</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.ForEach#getCollection <em>Collection</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.ForEach#getBlock <em>Block</em>}</li>
 * </ul>
 *
 * @see org.eclipse.emf.ecoretools.ale.AlePackage#getForEach()
 * @model
 * @generated
 */
public interface ForEach extends Statement
{
  /**
   * Returns the value of the '<em><b>Iterator</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Iterator</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Iterator</em>' attribute.
   * @see #setIterator(String)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getForEach_Iterator()
   * @model
   * @generated
   */
  String getIterator();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.ForEach#getIterator <em>Iterator</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Iterator</em>' attribute.
   * @see #getIterator()
   * @generated
   */
  void setIterator(String value);

  /**
   * Returns the value of the '<em><b>Collection</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Collection</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Collection</em>' containment reference.
   * @see #setCollection(Collection)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getForEach_Collection()
   * @model containment="true"
   * @generated
   */
  Collection getCollection();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.ForEach#getCollection <em>Collection</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Collection</em>' containment reference.
   * @see #getCollection()
   * @generated
   */
  void setCollection(Collection value);

  /**
   * Returns the value of the '<em><b>Block</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Block</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Block</em>' containment reference.
   * @see #setBlock(Block)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getForEach_Block()
   * @model containment="true"
   * @generated
   */
  Block getBlock();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.ForEach#getBlock <em>Block</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Block</em>' containment reference.
   * @see #getBlock()
   * @generated
   */
  void setBlock(Block value);

} // ForEach