package org.eclipse.xtext.serializer.sequencer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic;

import com.google.inject.ImplementedBy;

@ImplementedBy(BacktrackingSemanticSequencer.class)
public interface ISemanticSequencer {

	void init(ISemanticSequenceAcceptor sequenceAcceptor, ISerializationDiagnostic.Acceptor errorAcceptor);

	void init(ISemanticSequencer sequencer, ISemanticSequenceAcceptor sequenceAcceptor,
			ISerializationDiagnostic.Acceptor errorAcceptor);

	void createSequence(ISerializationContext context, EObject semanticObject);

	/**
	 * @deprecated use {@link #createSequence(ISerializationContext, EObject)}
	 */
	@Deprecated
	void createSequence(EObject context, EObject semanticObject);
}
