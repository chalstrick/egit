/*******************************************************************************
 * Copyright (C) 2011, Christian Halstrick <christian.halstrick@sap.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.core.securestorage;

/**
 * @author D032780
 *
 */
public class PassphraseCredentials implements EGitCredentials {
	private final String passphrase;

	/**
	 * @param passphrase
	 */
	public PassphraseCredentials(String passphrase) {
		this.passphrase = passphrase;
	}

	/**
	 * @return the passphrase
	 */
	public String getPassphrase() {
		return passphrase;
	}
}
