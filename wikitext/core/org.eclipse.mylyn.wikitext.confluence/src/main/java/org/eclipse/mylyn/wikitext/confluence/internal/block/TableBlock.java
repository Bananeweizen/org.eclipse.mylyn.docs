/*******************************************************************************
 * Copyright (c) 2007, 2017 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.confluence.internal.block;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.confluence.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.parser.Attributes;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.parser.markup.Block;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

/**
 * Table block, matches blocks that start with <code>table. </code> or those that start with a table row.
 *
 * @author David Green
 */
public class TableBlock extends Block {

	private static final List<Class<?>> NESTABLE_CELL_BLOCKS = ImmutableList.of(ListBlock.class);

	private static final Pattern START_PATTERN = Pattern.compile("\\s*(\\|\\|?.*$)"); //$NON-NLS-1$

	private static final Pattern END_OF_CELL_CONTENT_PATTERN = Pattern.compile("((?:(?:[^\\|\\[]*)(?:\\[[^\\]]*\\])?)*)" //$NON-NLS-1$
			+ "(\\|\\|?\\s*)+?"); //$NON-NLS-1$

	private static final Pattern END_OF_ROW_PATTERN = Pattern.compile("^\\|\\|?\\s*$"); //$NON-NLS-1$

	private static final Pattern TABLE_ROW_PATTERN = Pattern
			.compile("\\|(\\|)?\\s*" + "((?:(?:[^\\|\\[]*)(?:\\[[^\\]]*\\])?)*)" //$NON-NLS-1$ //$NON-NLS-2$
					+ "(\\|\\|?\\s*$)?"); //$NON-NLS-1$

	private int blockLineCount = 0;

	private Matcher matcher;

	private BlockType currentCell;

	private boolean nesting = false;

	private boolean rowStarted = false;

	public TableBlock() {
	}

	@Override
	public int processLineContent(String line, int offset) {
		nesting = false;
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			builder.beginBlock(BlockType.TABLE, attributes);
		} else if (markupLanguage.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}

		++blockLineCount;

		if (atEndOfRow(line, offset)) {
			ensureRowClosed();
			return -1;
		}

		int postCellOffset = processCellContent(line, offset);
		return isClosed() ? 0 : processEndOfLine(line, postCellOffset);
	}

	private boolean atEndOfRow(String line, int lineOffset) {
		String restOfLine = line.substring(lineOffset);
		return END_OF_ROW_PATTERN.matcher(restOfLine).find();
	}

	private int processCellContent(String line, int offset) {
		int cellsOffset = 0;
		String restOfline = offset == 0 ? line : line.substring(offset);
		Matcher rowMatcher = TABLE_ROW_PATTERN.matcher(restOfline);
		if (rowMatcher.find()) {
			do {
				ensureCellClosed();
				cellsOffset = startNextCell(rowMatcher);
				String cellContent = rowMatcher.group(2);
				nesting = isNestableCellContent(cellContent);
				if (!nesting) {
					emitMarkup(cellContent, offset + cellsOffset);
					cellsOffset = rowMatcher.end(2);
				}
			} while (!nesting && rowMatcher.find());
		} else {
			setClosed(true);
		}

		return offset + cellsOffset;
	}

	private int startNextCell(Matcher rowMatcher) {
		ensureRowStarted();
		String headerIndicator = rowMatcher.group(1);
		boolean header = "|".equals(headerIndicator); //$NON-NLS-1$
		currentCell = header ? BlockType.TABLE_CELL_HEADER : BlockType.TABLE_CELL_NORMAL;
		builder.beginBlock(currentCell, new Attributes());
		return rowMatcher.start(2);
	}

	private boolean isNestableCellContent(String cellContent) {
		return NESTABLE_CELL_BLOCKS.contains(getConfluenceLanguage().startBlock(cellContent, 0).getClass());
	}

	private void emitMarkup(String text, int lineOffset) {
		getConfluenceLanguage().emitMarkupLine(getParser(), state, lineOffset,
				CharMatcher.WHITESPACE.trimTrailingFrom(text), 0);
	}

	private ConfluenceLanguage getConfluenceLanguage() {
		return (ConfluenceLanguage) getMarkupLanguage();
	}

	private int processEndOfLine(String line, int offset) {
		if (!nesting) {
			ensureRowClosed();
			return -1;
		}
		return offset >= line.length() ? -1 : offset;
	}

	@Override
	public boolean beginNesting() {
		return nesting;
	}

	@Override
	public int findCloseOffset(String line, int lineOffset) {
		Matcher endMatcher = END_OF_CELL_CONTENT_PATTERN.matcher(line);
		if (lineOffset != 0) {
			endMatcher.region(lineOffset, line.length());
		}
		if (endMatcher.find()) {
			return endMatcher.start(2);
		}
		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		if (lineOffset == 0) {
			matcher = START_PATTERN.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}

	private void ensureRowStarted() {
		if (!rowStarted) {
			builder.beginBlock(BlockType.TABLE_ROW, new Attributes());
			rowStarted = true;
		}
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			ensureRowClosed();
			builder.endBlock();
		}
		super.setClosed(closed);
	}

	private void ensureRowClosed() {
		ensureCellClosed();
		if (rowStarted) {
			builder.endBlock();
			rowStarted = false;
		}
	}

	private void ensureCellClosed() {
		if (currentCell != null) {
			builder.endBlock();
			currentCell = null;
		}
	}
}
