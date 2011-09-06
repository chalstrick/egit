/*******************************************************************************
 * Copyright (C) 2007, IBM Corporation and others
 * Copyright (C) 2007, Dave Watson <dwatson@mimvista.com>
 * Copyright (C) 2008, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * Copyright (C) 2008, Google Inc.
 * Copyright (C) 2008, Tor Arne Vestbø <torarnv@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.egit.ui.internal.decorators;

import static org.eclipse.jgit.lib.Repository.stripWorkDir;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.internal.trace.GitTraceLocation;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;

class DecoratableResourceAdapter extends DecoratableResource {

	private final RepositoryMapping mapping;

	private final Repository repository;

	private final boolean trace;

	private IndexDiff cache;

	@SuppressWarnings("fallthrough")
	public DecoratableResourceAdapter(IResource resourceToWrap)
			throws IOException {
		super(resourceToWrap);
		trace = GitTraceLocation.DECORATION.isActive();
		long start = 0;
		if (trace) {
			GitTraceLocation.getTrace().trace(
					GitTraceLocation.DECORATION.getLocation(),
					"Decorate " + resource.getFullPath()); //$NON-NLS-1$
			start = System.currentTimeMillis();
		}
		try {
			mapping = RepositoryMapping.getMapping(resource);
			repository = mapping.getRepository();
			cache = new IndexDiff(repository, Constants.HEAD, new FileTreeIterator(repository));
			cache.diff();

			repositoryName = DecoratableResourceHelper
					.getRepositoryName(repository);
			branch = DecoratableResourceHelper.getShortBranch(repository);

			switch (resource.getType()) {
			case IResource.FILE:
				extractResourceProperties();
				break;
			case IResource.PROJECT:
				tracked = true;
			case IResource.FOLDER:
				extractContainerProperties();
				break;
			}
		} finally {
			if (trace)
				GitTraceLocation
						.getTrace()
						.trace(GitTraceLocation.DECORATION.getLocation(),
								"Decoration took " + (System.currentTimeMillis() - start) //$NON-NLS-1$
										+ " ms"); //$NON-NLS-1$
		}
	}

	private void extractResourceProperties() {
		String repoRelativePath = makeRepoRelative(resource);

		// ignored
		Set<String> untracked = cache.getUntracked();
		tracked = !untracked.contains(repoRelativePath);

		Set<String> added = cache.getAdded();
		Set<String> removed = cache.getRemoved();
		Set<String> changed = cache.getChanged();
		if (added.contains(repoRelativePath)) // added
			staged = Staged.ADDED;
		else if (removed.contains(repoRelativePath)) // removed
			staged = Staged.REMOVED;
		else if (changed.contains(repoRelativePath)) // changed and added into index
			staged = Staged.MODIFIED;
		else
			staged = Staged.NOT_STAGED;

		// conflicting
		Set<String> conflicting = cache.getConflicting();
		conflicts = conflicting.contains(repoRelativePath);

		// locally modified
		Set<String> modified = cache.getModified();
		dirty = modified.contains(repoRelativePath);
	}

	private void extractContainerProperties() {
		String repoRelativePath = makeRepoRelative(resource);

		// ignored
		Set<String> untracked = cache.getUntracked();

		tracked = !containsPrefix(untracked, repoRelativePath);
		if (!tracked)
			return;

		Set<String> added = cache.getAdded();
		Set<String> removed = cache.getRemoved();
		Set<String> changed = cache.getChanged();
		if (containsPrefix(added, repoRelativePath)) // added
			staged = Staged.ADDED;
		else if (containsPrefix(removed, repoRelativePath)) // removed
			staged = Staged.REMOVED;
		else if (containsPrefix(changed, repoRelativePath)) // changed and added into index
			staged = Staged.MODIFIED;
		else
			staged = Staged.NOT_STAGED;

		// conflicting
		Set<String> conflicting = cache.getConflicting();
		conflicts = containsPrefix(conflicting, repoRelativePath);

		// locally modified
		Set<String> modified = cache.getModified();
		dirty = containsPrefix(modified, repoRelativePath);
	}

	private String makeRepoRelative(IResource res) {
		return stripWorkDir(repository.getWorkTree(), res.getLocation().toFile());
	}

	private boolean containsPrefix(Set<String> collection, String prefix) {
		for (String path : collection) {
			if (path.startsWith(prefix))
				return true;
		}

		return false;
	}
}
