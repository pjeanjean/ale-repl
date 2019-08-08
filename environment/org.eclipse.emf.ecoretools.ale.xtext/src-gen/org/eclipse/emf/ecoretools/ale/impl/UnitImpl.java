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
package org.eclipse.emf.ecoretools.ale.impl;

import java.lang.String;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.ecoretools.ale.AlePackage;
import org.eclipse.emf.ecoretools.ale.BehavioredClass;
import org.eclipse.emf.ecoretools.ale.Import;
import org.eclipse.emf.ecoretools.ale.Service;
import org.eclipse.emf.ecoretools.ale.Unit;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Unit</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.impl.UnitImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.impl.UnitImpl#getImports <em>Imports</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.impl.UnitImpl#getServices <em>Services</em>}</li>
 *   <li>{@link org.eclipse.emf.ecoretools.ale.impl.UnitImpl#getXtendedClasses <em>Xtended Classes</em>}</li>
 * </ul>
 *
 * @generated
 */
public class UnitImpl extends MinimalEObjectImpl.Container implements Unit
{
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The cached value of the '{@link #getImports() <em>Imports</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getImports()
   * @generated
   * @ordered
   */
  protected EList<Import> imports;

  /**
   * The cached value of the '{@link #getServices() <em>Services</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getServices()
   * @generated
   * @ordered
   */
  protected EList<Service> services;

  /**
   * The cached value of the '{@link #getXtendedClasses() <em>Xtended Classes</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getXtendedClasses()
   * @generated
   * @ordered
   */
  protected EList<BehavioredClass> xtendedClasses;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected UnitImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return AlePackage.Literals.UNIT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName)
  {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, AlePackage.UNIT__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Import> getImports()
  {
    if (imports == null)
    {
      imports = new EObjectContainmentEList<Import>(Import.class, this, AlePackage.UNIT__IMPORTS);
    }
    return imports;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Service> getServices()
  {
    if (services == null)
    {
      services = new EObjectContainmentEList<Service>(Service.class, this, AlePackage.UNIT__SERVICES);
    }
    return services;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<BehavioredClass> getXtendedClasses()
  {
    if (xtendedClasses == null)
    {
      xtendedClasses = new EObjectContainmentEList<BehavioredClass>(BehavioredClass.class, this, AlePackage.UNIT__XTENDED_CLASSES);
    }
    return xtendedClasses;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case AlePackage.UNIT__IMPORTS:
        return ((InternalEList<?>)getImports()).basicRemove(otherEnd, msgs);
      case AlePackage.UNIT__SERVICES:
        return ((InternalEList<?>)getServices()).basicRemove(otherEnd, msgs);
      case AlePackage.UNIT__XTENDED_CLASSES:
        return ((InternalEList<?>)getXtendedClasses()).basicRemove(otherEnd, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case AlePackage.UNIT__NAME:
        return getName();
      case AlePackage.UNIT__IMPORTS:
        return getImports();
      case AlePackage.UNIT__SERVICES:
        return getServices();
      case AlePackage.UNIT__XTENDED_CLASSES:
        return getXtendedClasses();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case AlePackage.UNIT__NAME:
        setName((String)newValue);
        return;
      case AlePackage.UNIT__IMPORTS:
        getImports().clear();
        getImports().addAll((Collection<? extends Import>)newValue);
        return;
      case AlePackage.UNIT__SERVICES:
        getServices().clear();
        getServices().addAll((Collection<? extends Service>)newValue);
        return;
      case AlePackage.UNIT__XTENDED_CLASSES:
        getXtendedClasses().clear();
        getXtendedClasses().addAll((Collection<? extends BehavioredClass>)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case AlePackage.UNIT__NAME:
        setName(NAME_EDEFAULT);
        return;
      case AlePackage.UNIT__IMPORTS:
        getImports().clear();
        return;
      case AlePackage.UNIT__SERVICES:
        getServices().clear();
        return;
      case AlePackage.UNIT__XTENDED_CLASSES:
        getXtendedClasses().clear();
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case AlePackage.UNIT__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case AlePackage.UNIT__IMPORTS:
        return imports != null && !imports.isEmpty();
      case AlePackage.UNIT__SERVICES:
        return services != null && !services.isEmpty();
      case AlePackage.UNIT__XTENDED_CLASSES:
        return xtendedClasses != null && !xtendedClasses.isEmpty();
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuilder result = new StringBuilder(super.toString());
    result.append(" (name: ");
    result.append(name);
    result.append(')');
    return result.toString();
  }

} //UnitImpl
