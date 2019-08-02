/*******************************************************************************
 * Copyright (c) 2019 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Internal;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.NamedArgument;
import org.eclipse.xtext.RuleCallWithAppendedToken;
import org.eclipse.xtext.impl.RuleCallImpl;

/**
 * @author rhiobet - Initial contribution and API
 */
public class RuleCallWithAppendedTokenImpl extends RuleCallImpl implements RuleCallWithAppendedToken {

	private RuleCallImpl original;
	private String token;


	public RuleCallWithAppendedTokenImpl(RuleCallImpl original, String token) {
		this.original = original;
		this.token = token;
	}


	public String getToken() {
		return this.token;
	}


	@Override
	public EList<Adapter> eAdapters() {
		return this.original.eAdapters();
	}

	@Override
	public String getCardinality() {
		return this.original.getCardinality();
	}

	@Override
	public AbstractRule getRule() {
		return this.original.getRule();
	}

	@Override
	public boolean eDeliver() {
		return this.original.eDeliver();
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		this.original.eSetDeliver(deliver);
	}

	@Override
	public void setCardinality(String value) {
		this.original.setCardinality(value);
	}

	@Override
	public void eNotify(Notification notification) {
		this.original.eNotify(notification);
	}

	@Override
	public void setRule(AbstractRule value) {
		this.original.setRule(value);
	}

	@Override
	public boolean isPredicated() {
		return this.original.isPredicated();
	}

	@Override
	public EList<NamedArgument> getArguments() {
		return this.original.getArguments();
	}

	@Override
	public void setPredicated(boolean value) {
		this.original.setPredicated(value);
	}

	@Override
	public boolean isExplicitlyCalled() {
		return this.original.isExplicitlyCalled();
	}

	@Override
	public boolean isFirstSetPredicated() {
		return this.original.isFirstSetPredicated();
	}

	@Override
	public void setExplicitlyCalled(boolean value) {
		this.original.setExplicitlyCalled(value);
	}

	@Override
	public void setFirstSetPredicated(boolean value) {
		this.original.setFirstSetPredicated(value);
	}

	@Override
	public EClass eClass() {
		return this.original.eClass();
	}

	@Override
	public Resource eResource() {
		return this.original.eResource();
	}

	@Override
	public EObject eContainer() {
		return this.original.eContainer();
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		return this.original.eContainingFeature();
	}

	@Override
	public EReference eContainmentFeature() {
		return this.original.eContainmentFeature();
	}

	@Override
	public EList<EObject> eContents() {
		return this.original.eContents();
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		return this.original.eAllContents();
	}

	@Override
	public boolean eIsProxy() {
		return this.original.eIsProxy();
	}

	@Override
	public EList<EObject> eCrossReferences() {
		return this.original.eCrossReferences();
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		return this.original.eGet(feature);
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		return this.original.eGet(feature, resolve);
	}

	@Override
	public void eSet(EStructuralFeature feature, Object newValue) {
		this.original.eSet(feature, newValue);
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		return this.original.eIsSet(feature);
	}

	@Override
	public void eUnset(EStructuralFeature feature) {
		this.original.eUnset(feature);
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
		return this.original.eInvoke(operation, arguments);
	}

	@Override
	public int hashCode() {
		return this.original.hashCode();
	}

	@Override
	public AbstractRule basicGetRule() {
		return this.original.basicGetRule();
	}

	@Override
	public boolean equals(Object obj) {
		return this.original.equals(obj);
	}

	@Override
	public InternalEObject eInternalContainer() {
		return this.original.eInternalContainer();
	}

	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		return this.original.eInverseRemove(otherEnd, featureID, msgs);
	}

	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		return this.original.eGet(featureID, resolve, coreType);
	}

	@Override
	public void eSet(int featureID, Object newValue) {
		this.original.eSet(featureID, newValue);
	}

	@Override
	public void eUnset(int featureID) {
		this.original.eUnset(featureID);
	}

	@Override
	public boolean eIsSet(int featureID) {
		return this.original.eIsSet(featureID);
	}

	@Override
	public String toString() {
		return this.original.toString();
	}

	@Override
	public String eURIFragmentSegment(EStructuralFeature eStructuralFeature, EObject eObject) {
		return this.original.eURIFragmentSegment(eStructuralFeature, eObject);
	}

	@Override
	public boolean eNotificationRequired() {
		return this.original.eNotificationRequired();
	}

	@Override
	public EObject eObjectForURIFragmentSegment(String uriFragmentSegment) {
		return this.original.eObjectForURIFragmentSegment(uriFragmentSegment);
	}

	@Override
	public URI eProxyURI() {
		return this.original.eProxyURI();
	}

	@Override
	public void eSetProxyURI(URI uri) {
		this.original.eSetProxyURI(uri);
	}

	@Override
	public int eContainerFeatureID() {
		return this.original.eContainerFeatureID();
	}

	@Override
	public void eSetClass(EClass eClass) {
		this.original.eSetClass(eClass);
	}

	@Override
	public Internal eDirectResource() {
		return this.original.eDirectResource();
	}

	@Override
	public Object dynamicGet(int dynamicFeatureID) {
		return this.original.dynamicGet(dynamicFeatureID);
	}

	@Override
	public void dynamicSet(int dynamicFeatureID, Object newValue) {
		this.original.dynamicSet(dynamicFeatureID, newValue);
	}

	@Override
	public void dynamicUnset(int dynamicFeatureID) {
		this.original.dynamicUnset(dynamicFeatureID);
	}

	@Override
	public boolean eContains(EObject eObject) {
		return this.original.eContains(eObject);
	}

	@Override
	public Internal eInternalResource() {
		return this.original.eInternalResource();
	}

	@Override
	public NotificationChain eSetResource(Internal resource, NotificationChain notifications) {
		return this.original.eSetResource(resource, notifications);
	}

	@Override
	public Object eGet(EStructuralFeature eFeature, boolean resolve, boolean coreType) {
		return this.original.eGet(eFeature, resolve, coreType);
	}

	@Override
	public Object eDynamicGet(EStructuralFeature eFeature, boolean resolve) {
		return this.original.eDynamicGet(eFeature, resolve);
	}

	@Override
	public Object eDynamicGet(int featureID, boolean resolve, boolean coreType) {
		return this.original.eDynamicGet(featureID, resolve, coreType);
	}

	@Override
	public Object eOpenGet(EStructuralFeature eFeature, boolean resolve) {
		return this.original.eOpenGet(eFeature, resolve);
	}

	@Override
	public void eDynamicSet(EStructuralFeature eFeature, Object newValue) {
		this.original.eDynamicSet(eFeature, newValue);
	}

	@Override
	public void eDynamicSet(int featureID, Object newValue) {
		this.original.eDynamicSet(featureID, newValue);
	}

	@Override
	public void eOpenSet(EStructuralFeature eFeature, Object newValue) {
		this.original.eOpenSet(eFeature, newValue);
	}

	@Override
	public void eDynamicUnset(EStructuralFeature eFeature) {
		this.original.eDynamicUnset(eFeature);
	}

	@Override
	public void eDynamicUnset(int featureID) {
		this.original.eDynamicUnset(featureID);
	}

	@Override
	public void eOpenUnset(EStructuralFeature eFeature) {
		this.original.eOpenUnset(eFeature);
	}

	@Override
	public boolean eDynamicIsSet(EStructuralFeature eFeature) {
		return this.original.eDynamicIsSet(eFeature);
	}

	@Override
	public boolean eDynamicIsSet(int featureID) {
		return this.original.eDynamicIsSet(featureID);
	}

	@Override
	public boolean eOpenIsSet(EStructuralFeature eFeature) {
		return this.original.eOpenIsSet(eFeature);
	}

	@Override
	public NotificationChain eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID,
			NotificationChain msgs) {
		return this.original.eBasicSetContainer(newContainer, newContainerFeatureID, msgs);
	}

	@Override
	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
		return this.original.eBasicRemoveFromContainer(msgs);
	}

	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		return this.original.eBasicRemoveFromContainerFeature(msgs);
	}

	@Override
	public NotificationChain eDynamicBasicRemoveFromContainer(NotificationChain msgs) {
		return this.original.eDynamicBasicRemoveFromContainer(msgs);
	}

	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class<?> baseClass,
			NotificationChain msgs) {
		return this.original.eInverseAdd(otherEnd, featureID, baseClass, msgs);
	}

	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		return this.original.eInverseAdd(otherEnd, featureID, msgs);
	}

	@Override
	public NotificationChain eDynamicInverseAdd(InternalEObject otherEnd, int featureID, Class<?> inverseClass,
			NotificationChain msgs) {
		return this.original.eDynamicInverseAdd(otherEnd, featureID, inverseClass, msgs);
	}

	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class<?> baseClass,
			NotificationChain msgs) {
		return this.original.eInverseRemove(otherEnd, featureID, baseClass, msgs);
	}

	@Override
	public NotificationChain eDynamicInverseRemove(InternalEObject otherEnd, int featureID, Class<?> inverseClass,
			NotificationChain msgs) {
		return this.original.eDynamicInverseRemove(otherEnd, featureID, inverseClass, msgs);
	}

	@Override
	public EObject eResolveProxy(InternalEObject proxy) {
		return this.original.eResolveProxy(proxy);
	}

	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		return this.original.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		return this.original.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	@Override
	public int eDerivedStructuralFeatureID(EStructuralFeature eStructuralFeature) {
		return this.original.eDerivedStructuralFeatureID(eStructuralFeature);
	}

	@Override
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		return this.original.eDerivedOperationID(baseOperationID, baseClass);
	}

	@Override
	public int eDerivedOperationID(EOperation eOperation) {
		return this.original.eDerivedOperationID(eOperation);
	}

	@Override
	public Setting eSetting(EStructuralFeature eFeature) {
		return this.original.eSetting(eFeature);
	}

	@Override
	public EStore eStore() {
		return this.original.eStore();
	}

	@Override
	public void eSetStore(EStore store) {
		this.original.eSetStore(store);
	}

	@Override
	public Object eVirtualGet(int eDerivedStructuralFeatureID) {
		return this.original.eVirtualGet(eDerivedStructuralFeatureID);
	}

	@Override
	public Object eVirtualGet(int eDerivedStructuralFeatureID, Object defaultValue) {
		return this.original.eVirtualGet(eDerivedStructuralFeatureID, defaultValue);
	}

	@Override
	public boolean eVirtualIsSet(int eDerivedStructuralFeatureID) {
		return this.original.eVirtualIsSet(eDerivedStructuralFeatureID);
	}

	@Override
	public Object eVirtualSet(int eDerivedStructuralFeatureID, Object value) {
		return this.original.eVirtualSet(eDerivedStructuralFeatureID, value);
	}

	@Override
	public Object eVirtualUnset(int eDerivedStructuralFeatureID) {
		return this.original.eVirtualUnset(eDerivedStructuralFeatureID);
	}

	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		return this.original.eInvoke(operationID, arguments);
	}

	@Override
	public Object eDynamicInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		return this.original.eDynamicInvoke(operationID, arguments);
	}

}
