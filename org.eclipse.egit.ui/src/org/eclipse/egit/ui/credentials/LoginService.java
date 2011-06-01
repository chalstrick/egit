/*******************************************************************************
 * Copyright (C) 2010, Jens Baumgart <jens.baumgart@sap.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.ui.credentials;

import java.io.IOException;

import org.eclipse.egit.core.securestorage.EGitCredentials;
import org.eclipse.egit.core.securestorage.PassphraseCredentials;
import org.eclipse.egit.core.securestorage.UserPasswordCredentials;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIText;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements services for interactive login and changing stored
 * credentials.
 */
public class LoginService {

	/**
	 * The method shows a login dialog for a given URI. The user field is taken
	 * from the URI if a user is present in the URI. In this case the user is
	 * not editable.
	 *
	 * @param parent
	 * @param uri
	 * @param promptText
	 * @return credentials, <code>null</code> if the user canceled the dialog.
	 */
	public static EGitCredentials login(Shell parent, URIish uri, final String promptText) {
		LoginDialog dialog = new LoginDialog(parent, uri, promptText);
		if (dialog.open() == Window.OK) {
			EGitCredentials credentials = dialog.getCredentials();
			if (credentials != null && dialog.getStoreInSecureStore())
				storeCredentials(uri, credentials);
			return credentials;
		}
		return null;
	}

	/**
	 * The method shows a login dialog for a given URI. The user has to enter a
	 * passphrase.
	 *
	 * @param parent
	 * @param uri
	 * @param promptText
	 * @return credentials, <code>null</code> if the user canceled the dialog.
	 */
	public static String loginWithPassphrase(Shell parent, URIish uri, final String promptText) {
		LoginWithPassphraseDialog dialog = new LoginWithPassphraseDialog(parent, uri, promptText);
		if (dialog.open() == Window.OK) {
			String passphrase = dialog.getPassphrase();
			if (passphrase != null && dialog.getStoreInSecureStore())
				storePassphrase(uri, passphrase);
			return passphrase;
		}
		return null;
	}

	/**
	 * The method shows a change credentials dialog for a given URI. The user
	 * field is taken from the URI if a user is present in the URI. In this case
	 * the user is not editable.
	 *
	 * @param parent
	 * @param uri
	 * @return credentials, <code>null</code> if the user canceled the dialog.
	 */
	public static EGitCredentials changeCredentials(Shell parent, URIish uri) {
		EGitCredentials oldCredentials = getCredentialsFromSecureStore(uri);
		EGitCredentials credentials;
		if (oldCredentials instanceof UserPasswordCredentials) {
			LoginDialog dialog = new LoginDialog(parent, uri);
			dialog.setChangeCredentials(true);
			dialog.setOldUser(((UserPasswordCredentials) oldCredentials)
						.getUser());
			if (dialog.open() == Window.OK) {
				credentials = dialog.getCredentials();
				if (credentials != null)
					storeCredentials(uri, credentials);
				return credentials;
			}
			return null;
		} else {
			LoginWithPassphraseDialog dialog = new LoginWithPassphraseDialog(parent, uri);
			dialog.setChangeCredentials(true);
			if (dialog.open() == Window.OK) {
				String passphrase = dialog.getPassphrase();
				if (passphrase != null) {
					credentials = new PassphraseCredentials(passphrase);
					storeCredentials(uri, credentials);
				}
				return credentials;
			}
			return null;
		}

	}

	private static void storeCredentials(URIish uri,
			EGitCredentials credentials) {
		try {
			org.eclipse.egit.core.Activator.getDefault().getSecureStore()
					.putCredentials(uri, credentials);
		} catch (StorageException e) {
			Activator.handleError(UIText.LoginService_storingCredentialsFailed, e, true);
		} catch (IOException e) {
			Activator.handleError(UIText.LoginService_storingCredentialsFailed, e, true);
		}
	}

	private static EGitCredentials getCredentialsFromSecureStore(final URIish uri) {
		EGitCredentials credentials = null;
		try {
			credentials = org.eclipse.egit.core.Activator.getDefault().getSecureStore()
					.getCredentials(uri);
		} catch (StorageException e) {
			Activator.logError(
					UIText.LoginService_readingCredentialsFailed, e);
		}
		return credentials;
	}
}
