/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.html.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class HtmlSubsetLanguageTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createNullName() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage(null, 6, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD));
	}

	@Test
	public void createNullBlockTypes() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage("Test", 6, null, ImmutableSet.of(SpanType.BOLD));
	}

	@Test
	public void createNullSpanTypes() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage("Test", 6, ImmutableSet.of(BlockType.PARAGRAPH), null);
	}

	@Test
	public void createInvalidHeadingLevel() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("headingLevel must be between 0 and 6");
		new HtmlSubsetLanguage("Test", -1, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD));
	}

	@Test
	public void create() {
		HtmlSubsetLanguage language = newHtmlSubsetLanguage(BlockType.PARAGRAPH);
		assertEquals("Test", language.getName());
	}

	@Test
	public void supportedBlockTypes() {
		assertEquals(Sets.newHashSet(BlockType.PARAGRAPH, BlockType.CODE),
				newHtmlSubsetLanguage(BlockType.PARAGRAPH, BlockType.CODE).getSupportedBlockTypes());
	}

	@Test
	public void supportedSpanTypes() {
		assertEquals(Sets.newHashSet(SpanType.BOLD, SpanType.EMPHASIS),
				newHtmlSubsetLanguage(SpanType.BOLD, SpanType.EMPHASIS).getSupportedSpanTypes());
	}

	@Test
	public void supportedHeadingLevel() {
		assertSupportedHeadingLevel(0);
		assertSupportedHeadingLevel(1);
		assertSupportedHeadingLevel(5);
		assertSupportedHeadingLevel(6);
		assertEquals(0, newHtmlSubsetLanguageWithHeadingLevel(0).getSupportedHeadingLevel());
	}

	private void assertSupportedHeadingLevel(int level) {
		assertEquals(level, newHtmlSubsetLanguageWithHeadingLevel(level).getSupportedHeadingLevel());
	}

	private HtmlSubsetLanguage newHtmlSubsetLanguageWithHeadingLevel(int level) {
		return new HtmlSubsetLanguage("Test", level, Sets.newHashSet(BlockType.PARAGRAPH), ImmutableSet.<SpanType> of());
	}

	protected HtmlSubsetLanguage newHtmlSubsetLanguage(SpanType... spans) {
		return new HtmlSubsetLanguage("Test", 6, Sets.newHashSet(BlockType.PARAGRAPH), Sets.newHashSet(spans));
	}

	protected HtmlSubsetLanguage newHtmlSubsetLanguage(BlockType... blocks) {
		return new HtmlSubsetLanguage("Test", 6, Sets.newHashSet(blocks), ImmutableSet.<SpanType> of());
	}

	@Test
	public void createDocumentBuilder() {
		DocumentBuilder builder = newHtmlSubsetLanguage(BlockType.PARAGRAPH).createDocumentBuilder(new StringWriter(),
				false);
		assertNotNull(builder);
		assertTrue(builder instanceof HtmlSubsetDocumentBuilder);
	}
}
