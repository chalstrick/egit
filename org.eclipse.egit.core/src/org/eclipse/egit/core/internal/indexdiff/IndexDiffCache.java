package org.eclipse.egit.core.internal.indexdiff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;

/**
 *
 *
 */
public class IndexDiffCache {
	Set<IndexDiffChangedListener> globalListeners = new HashSet<IndexDiffChangedListener>();

	private Map<Repository, IndexDiffCacheEntry> entries = new HashMap<Repository, IndexDiffCacheEntry>();

	/**
	 * @param repository
	 * @return cache entry
	 */
	public IndexDiffCacheEntry getIndexDiffCacheEntry(Repository repository) {
		IndexDiffCacheEntry entry;
		synchronized(entries) {
			entry = entries.get(repository);
			if (entry != null)
				return entry;
			entry = new IndexDiffCacheEntry(repository);
			entries.put(repository, entry);
		}
		return entry;
	}

	/**
	 * @param listener
	 */
	public void addIndexDiffChangedListener(IndexDiffChangedListener listener) {
		synchronized(globalListeners) {
			globalListeners.add(listener);
		}
	}
}
