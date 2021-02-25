/*******************************************************************************
 * Copyright (c) 2021 THALES GLOBAL SERVICES.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.diagram.misc.ju.testcases;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.sirius.business.api.preferences.SiriusPreferencesKeys;
import org.eclipse.sirius.business.api.query.DRepresentationQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.viewpoint.DRepresentationDescriptor;
import org.eclipse.sirius.viewpoint.description.DAnnotation;
import org.eclipse.sirius.viewpoint.description.DescriptionFactory;
import org.eclipse.sirius.viewpoint.provider.SiriusEditPlugin;
import org.polarsys.capella.core.diagram.helpers.IRepresentationAnnotationConstants;
import org.polarsys.capella.core.libraries.model.ICapellaModel;
import org.polarsys.capella.core.libraries.utils.ScopeModelWrapper;
import org.polarsys.capella.shared.id.handler.IScope;
import org.polarsys.capella.shared.id.handler.IdManager;
import org.polarsys.capella.test.diagram.common.ju.context.XDFBDiagram;
import org.polarsys.capella.test.diagram.common.ju.wrapper.utils.DiagramHelper;
import org.polarsys.capella.test.framework.api.BasicTestCase;
import org.polarsys.capella.test.framework.context.SessionContext;

/**
 * Test that the interaction gets the correct parent OA when source/target OAs are removed from diagram
 *
 */
public class DAnnotationChangeTest extends BasicTestCase {

    public static final String OA_2 = "f26b78a9-9109-4af6-840c-fc3d050e1f8e"; //$NON-NLS-1$

    public static final String OA_4 = "308bbc45-8cda-45a2-886f-8adc208e3b7b"; //$NON-NLS-1$

    private String projectTestName = "StatusLine";

    @Override
    public List<String> getRequiredTestModels() {
        return Arrays.asList(projectTestName);
    }

    @Override
    public void test() throws Exception {
        // Initialization
        IPreferenceStore preferenceStore = SiriusEditPlugin.getPlugin().getCorePreferenceStore();
        boolean oldAutoRefreshValue = preferenceStore.getBoolean(SiriusPreferencesKeys.PREF_AUTO_REFRESH.name());
        try {
            DiagramHelper.setPreferenceAutoRefresh(false);
            ICapellaModel model = getTestModel(projectTestName);
            IScope scope = new ScopeModelWrapper(model);

            EObject oa_4 = IdManager.getInstance().getEObject(OA_4, scope);
            Session session = getSession(projectTestName);
            SessionContext context = new SessionContext(session);

            // Creation of an OAIB diagram
            XDFBDiagram OAIBDiagram = XDFBDiagram.createDiagram(context, OA_2);

            // Check that the diagram is empty
            EList<DDiagramElement> diagramElements = OAIBDiagram.getDiagram().getDiagramElements();
            assertEquals("There should be 0 diagram elements displayed on this new diagram", 0, diagramElements.size());
            DRepresentationDescriptor representationDescriptor = new DRepresentationQuery(OAIBDiagram.getDiagram()).getRepresentationDescriptor();

            // Add a DAnnotation on the representation descriptor
            session.getTransactionalEditingDomain().getCommandStack().execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

                @Override
                protected void doExecute() {
                    DAnnotation dAnnotation = null;
                    if (representationDescriptor.getEAnnotations().isEmpty()) {
                        dAnnotation = DescriptionFactory.eINSTANCE.createDAnnotation();
                        representationDescriptor.getEAnnotations().add(dAnnotation);
                    } else {
                        dAnnotation = representationDescriptor.getEAnnotations().get(0);
                    }
                    dAnnotation.setSource(IRepresentationAnnotationConstants.ContextualElements);
                    dAnnotation.getReferences().add(oa_4);
                }
            });

            // Check that there is displayed element now
            diagramElements = OAIBDiagram.getDiagram().getDiagramElements();
            assertEquals("There should be 3 diagram elements now displayed", 3, diagramElements.size());
            assertTrue("OA_4 should have been displayed because of the DAnnotation", diagramElements.stream().anyMatch(diagramElement -> diagramElement.getTarget().equals(oa_4)));
            session.save(new NullProgressMonitor());
        } finally {
            DiagramHelper.setPreferenceAutoRefresh(oldAutoRefreshValue);
        }
    }
}
