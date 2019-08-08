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
 * generated by Xtext 2.14.0-SNAPSHOT
 *  ******************************************************************************
 */
package org.eclipse.emf.ecoretools.ale;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>While</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.While#getCond <em>Cond</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.While#getBlock <em>Block</em>}</li>
 * </ul>
 *
 * @see org.eclipse.emf.ecoretools.ale.AlePackage#getWhile()
 * @model
 * @generated
 */
public interface While extends Statement
{
  /**
   * Returns the value of the '<em><b>Cond</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Cond</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Cond</em>' containment reference.
   * @see #setCond(ExpressionStmt)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getWhile_Cond()
   * @model containment="true"
   * @generated
   */
  ExpressionStmt getCond();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.While#getCond <em>Cond</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Cond</em>' containment reference.
   * @see #getCond()
   * @generated
   */
  void setCond(ExpressionStmt value);

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
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getWhile_Block()
   * @model containment="true"
   * @generated
   */
  Block getBlock();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.While#getBlock <em>Block</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Block</em>' containment reference.
   * @see #getBlock()
   * @generated
   */
  void setBlock(Block value);

} // While
