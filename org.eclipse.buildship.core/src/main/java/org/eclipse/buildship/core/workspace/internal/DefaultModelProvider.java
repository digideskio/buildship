/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.connection.ModelResults;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.SingleBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
final class DefaultModelProvider implements ModelProvider {

    private final CompositeBuildModelRepository modelRepository;
    private final SingleBuildModelRepository singleModelRepository;

    public DefaultModelProvider(SingleBuildModelRepository singleModelRepository, CompositeBuildModelRepository modelRepository) {
        this.singleModelRepository = Preconditions.checkNotNull(singleModelRepository);
        this.modelRepository = Preconditions.checkNotNull(modelRepository);
    }

    @Override
    public ModelResults<OmniEclipseProject> fetchEclipseProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchEclipseProjects(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniGradleBuild fetchGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.singleModelRepository.fetchGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.singleModelRepository.fetchEclipseGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.singleModelRepository.fetchBuildEnvironment(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    private final TransientRequestAttributes getTransientRequestAttributes(CancellationToken token, IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener> of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        ImmutableList<org.gradle.tooling.events.ProgressListener> noEventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener> of();
        if (token == null) {
            token = GradleConnector.newCancellationTokenSource().token();
        }
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, noEventListeners, token);
    }

}
