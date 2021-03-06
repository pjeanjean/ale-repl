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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>rCase</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.rCase#getGuard <em>Guard</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.rCase#getMatch <em>Match</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.rCase#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.emf.ecoretools.ale.AlePackage#getrCase()
 * @model
 * @generated
 */
public interface rCase extends EObject
{
  /**
   * Returns the value of the '<em><b>Guard</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Guard</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Guard</em>' containment reference.
   * @see #setGuard(rType)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getrCase_Guard()
   * @model containment="true"
   * @generated
   */
  rType getGuard();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.rCase#getGuard <em>Guard</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Guard</em>' containment reference.
   * @see #getGuard()
   * @generated
   */
  void setGuard(rType value);

  /**
   * Returns the value of the '<em><b>Match</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Match</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Match</em>' containment reference.
   * @see #setMatch(ExpressionStmt)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getrCase_Match()
   * @model containment="true"
   * @generated
   */
  ExpressionStmt getMatch();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.rCase#getMatch <em>Match</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Match</em>' containment reference.
   * @see #getMatch()
   * @generated
   */
  void setMatch(ExpressionStmt value);

  /**
   * Returns the value of the '<em><b>Value</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Value</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Value</em>' containment reference.
   * @see #setValue(ExpressionStmt)
   * @see org.eclipse.emf.ecoretools.ale.AlePackage#getrCase_Value()
   * @model containment="true"
   * @generated
   */
  ExpressionStmt getValue();

  /**
   * Sets the value of the '{@link org.eclipse.emf.ecoretools.ale.rCase#getValue <em>Value</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Value</em>' containment reference.
   * @see #getValue()
   * @generated
   */
  void setValue(ExpressionStmt value);

} // rCase
