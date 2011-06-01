/*******************************************************************************
 * Copyright (C) 2010, Jens Baumgart <jens.baumgart@sap.com>
 * Copyright (C) 2010, Edwin Kempin <edwin.kempin@sap.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.ui.credentials;

import org.eclipse.egit.ui.UIText;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements a login dialog asking for a passphrase. This is used
 * when passphrases are needed to read certificates and private keys during https
 * communication.
 */
class LoginWithPassphraseDialog extends Dialog {

	private Text passphraseText;

	private String passphrase;

	private Button storeCheckbox;

	private boolean storeInSecureStore;

	private final URIish uri;

	private boolean changeCredentials = false;

	private String promptText;

	LoginWithPassphraseDialog(Shell shell, URIish uri) {
		this(shell, uri, null);
	}

	LoginWithPassphraseDialog(Shell shell, URIish uri, final String promptText) {
		super(shell);
		this.uri = uri;
		this.promptText = promptText;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		if (changeCredentials)
			getShell().setText(UIText.LoginDialog_changeCredentials);
		else if (promptText != null && promptText.length() > 0)
			getShell().setText(promptText);
		else
			getShell().setText(UIText.LoginDialog_login);

		Label uriLabel = new Label(composite, SWT.NONE);
		uriLabel.setText(UIText.LoginDialog_repository);
		Text uriText = new Text(composite, SWT.READ_ONLY);
		uriText.setText(uri.toString());

		Label passphraseLabel = new Label(composite, SWT.NONE);
		passphraseLabel.setText(UIText.LoginDialog_passphrase);
		passphraseText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passphraseText);

		if(!changeCredentials) {
			Label storeLabel = new Label(composite, SWT.NONE);
			storeLabel.setText(UIText.LoginDialog_storeInSecureStore);
			storeCheckbox = new Button(composite, SWT.CHECK);
			storeCheckbox.setSelection(true);
		}

		passphraseText.setFocus();

		return composite;
	}

	String getPassphrase() {
		return passphrase;
	}

	boolean getStoreInSecureStore() {
		return storeInSecureStore;
	}

	@Override
	protected void okPressed() {
		if (passphraseText.getText().length() > 0) {
			passphrase = passphraseText.getText();
			if(!changeCredentials)
				storeInSecureStore = storeCheckbox.getSelection();
		}
		super.okPressed();
	}

	void setChangeCredentials(boolean changeCredentials) {
		this.changeCredentials = changeCredentials;
	}
}
