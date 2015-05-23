/*******************************************************************************
 * Copyright (c) 2015 David Green.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.commonmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContext.NamedUriWithTitle;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ProcessingContextTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void empty() {
		ProcessingContext context = ProcessingContext.empty();
		assertNotNull(context);
		assertTrue(context.isEmpty());
	}

	@Test
	public void referenceDefinition() {
		ProcessingContext context = ProcessingContext.builder().referenceDefinition("onE", "/uri", "a title").build();
		assertNotNull(context);
		assertFalse(context.isEmpty());
		assertNotNull(context.namedUriWithTitle("one"));
		assertNotNull(context.namedUriWithTitle("One"));
		NamedUriWithTitle link = context.namedUriWithTitle("ONE");
		assertEquals("onE", link.getName());
		assertEquals("/uri", link.getUri());
		assertEquals("a title", link.getTitle());
		assertNull(context.namedUriWithTitle("Unknown"));

	}

	@Test
	public void referenceDefinitionEmptyName() {
		assertTrue(ProcessingContext.builder().referenceDefinition("", "one", "two").build().isEmpty());
	}

	@Test
	public void referenceDefinitionDuplicate() {
		ProcessingContext context = ProcessingContext.builder()
				.referenceDefinition("a", "/uri", "a title")
				.referenceDefinition("a", "/uri2", "a title2")
				.build();
		NamedUriWithTitle uriWithTitle = context.namedUriWithTitle("a");
		assertEquals("/uri", uriWithTitle.getUri());
	}

}
