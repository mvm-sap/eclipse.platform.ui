/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ETabFolder extends CTabFolder {

	boolean webbyStyle = false;

	static final int[] E_TOP_LEFT_CORNER = new int[] {0,5, 1,4, 1,3, 2,2, 3,1, 4,1, 5,0};
	static final int[] E_TOP_RIGHT_CORNER = new int[] {-5,0, -4,1, -3,1, -2,2, -1,3, -1,4, 0,5};

	static final int[] E_TOP_LEFT_CORNER_BORDERLESS = new int[] {};
	static final int[] E_TOP_RIGHT_CORNER_BORDERLESS = new int[] {};
	
	//TODO tabTopMargin and tabBottomMargin aren't the correct values but
	//until make it look more correct until bug #283648 is fixed
	
	int tabTopMargin = 0;  		//The space above the highest (selected) tab
	int tabTopSelectionPadding = 6;  //pad within *selected* tab, above text, below line
	int tabTopPadding = 3;  	//pad within tab, above text, below line
	int tabBottomPadding = 3; 	//bottom margin within tab
	int tabLeftMargin = 2;  	//horizontal spacing left side of each tab
	int tabRightMargin = 0;  	//horizontal spacing left side of each tab
	int leftMargin = 0;  		//first horizontal space
	
	Color topBorderColor;  		// The exterior keyline color of the top tab area
	Color bottomBorderColor;	// The keyline color of the left, right, and bottom of the body
	Color tabBorderColor;		// Keyline color for the tabs themselves
	Color unselectedTabBackgroundColor;
	
/**
 * @param parent
 * @param style
 */
public ETabFolder(Composite parent, int style) {
	super(parent, style);
}

void init(int style) {
	super.init(style);
	
	topBorderColor = getDisplay().getSystemColor(BORDER1_COLOR);
	bottomBorderColor = getDisplay().getSystemColor(BORDER1_COLOR);
	tabBorderColor = getDisplay().getSystemColor(BORDER1_COLOR);
	unselectedTabBackgroundColor = getBackground();
}

public Color getTopBorderColor() {
	return topBorderColor;
}

public void setTopBorderColor(Color color) {
	checkWidget();
	if (topBorderColor == color) return;
	if (color == null) color = getDisplay().getSystemColor(BORDER1_COLOR);
	topBorderColor = color;
	if (selectedIndex > -1) redraw();
}

public Color getBottomBorderColor() {
	return bottomBorderColor;
}

public void setBottomBorderColor(Color color) {
	checkWidget();
	if (bottomBorderColor == color) return;
	if (color == null) color = getDisplay().getSystemColor(BORDER1_COLOR);
	bottomBorderColor = color;
	if (selectedIndex > -1) redraw();
}

public Color getTabBorderColor() {
	return tabBorderColor;
}

public void setTabBorderColor(Color color) {
	checkWidget();
	if (tabBorderColor == color) return;
	if (color == null) color = getDisplay().getSystemColor(BORDER1_COLOR);
	tabBorderColor = color;
	if (selectedIndex > -1) redraw();
}

public Color getUnselectedTabBackgroundColor() {
	return unselectedTabBackgroundColor;
}

public void setUnselectedTabBackgroundColor(Color color) {
	checkWidget();
	if (unselectedTabBackgroundColor == color) return;
	if (color == null) color = getBackground();
	unselectedTabBackgroundColor = color;
	if (selectedIndex > -1) redraw();
}


public int getTabTopMargin() {
	return tabTopMargin;
}

public void setTabTopMargin(int tabTopMargin) {
	this.tabTopMargin = tabTopMargin;
}

public int getTabTopPadding() {
	return tabTopPadding;
}

public void setTabTopPadding(int tabTopPadding) {
	this.tabTopPadding = tabTopPadding;
}

public int getTabBottomPadding() {
	return tabBottomPadding;
}

public void setTabBottomPadding(int tabBottomPadding) {
	this.tabBottomPadding = tabBottomPadding;
}

public int getTabTopSelectionPadding() {
	return tabTopSelectionPadding;
}

public void setTabTopSelectionPadding(int tabTopSelectionPadding) {
	this.tabTopSelectionPadding = tabTopSelectionPadding;
}

public int getTabLeftMargin() {
	return tabLeftMargin;
}

public void setTabLeftMargin(int tabLeftMargin) {
	this.tabLeftMargin = tabLeftMargin;
}

public int getTabRightMargin() {
	return tabRightMargin;
}

public void setTabRightMargin(int tabRightMargin) {
	this.tabRightMargin = tabRightMargin;
}

public boolean getWebbyStyle() {
	return webbyStyle;
}

public void setWebbyStyle(boolean webbyStyle) {
	checkWidget();
	
	if(this.webbyStyle != webbyStyle) {
		this.webbyStyle = webbyStyle;
		updateTabHeight(true);
		if(webbyStyle && single) {
			setSingle(false); //will cause update
			return; //no update needed
		}
		Rectangle rectBefore = getClientArea();
		updateItems();
		Rectangle rectAfter = getClientArea();
		layout();
		if (!rectBefore.equals(rectAfter)) {
			notifyListeners(SWT.Resize, new Event());
		}
		redraw();
	}
}

public ETabItem getETabItem (int index) {
	return (ETabItem) getItem(index);
}

public ETabItem [] getETabItems() {
	//checkWidget();
	ETabItem[] tabItems = new ETabItem [items.length];
	System.arraycopy(items, 0, tabItems, 0, items.length);
	return tabItems;
}



int getTextMidline() {
	int topSpacing = tabTopMargin + getMaxTabTopPadding();
	int textHeight = tabHeight - topSpacing - tabBottomPadding;
	return (textHeight / 2) + topSpacing;
}

void drawBody(Event event) {
	if(! useWebbyStyling()) {
		super.drawBody(event);
		return;
	}
	
	GC gc = event.gc;
	Point size = getSize();
	
	// fill in body
	if (!minimized){
		int width = size.x  - borderLeft - borderRight - 2*highlight_margin;
		int height = size.y - borderTop - borderBottom - tabHeight - highlight_header - highlight_margin;
		// Draw highlight margin
		if (highlight_margin > 0) {
			int[] shape = null;
			if (onBottom) {
				int x1 = borderLeft;
				int y1 = borderTop;
				int x2 = size.x - borderRight;
				int y2 = size.y - borderBottom - tabHeight - highlight_header;
				shape = new int[] {x1,y1, x2,y1, x2,y2, x2-highlight_margin,y2,
						           x2-highlight_margin, y1+highlight_margin, x1+highlight_margin,y1+highlight_margin,
								   x1+highlight_margin,y2, x1,y2};
			} else {	
				int x1 = borderLeft;
				int y1 = borderTop + tabHeight + highlight_header;
				int x2 = size.x - borderRight;
				int y2 = size.y - borderBottom;
				shape = new int[] {x1,y1, x1+highlight_margin,y1, x1+highlight_margin,y2-highlight_margin, 
						           x2-highlight_margin,y2-highlight_margin, x2-highlight_margin,y1,
								   x2,y1, x2,y2, x1,y2};
			}
			// If horizontal gradient, show gradient across the whole area
			if (selectedIndex != -1 && selectionGradientColors != null && selectionGradientColors.length > 1 && !selectionGradientVertical) {
				drawBackground(gc, shape, true);
			} else if (selectedIndex == -1 && gradientColors != null && gradientColors.length > 1 && !gradientVertical) {
				drawBackground(gc, shape, false);
			} else {
				gc.setBackground(selectedIndex == -1 ? getBackground() : selectionBackground);
				gc.fillPolygon(shape);
			}
		}
		//Draw client area
		if ((getStyle() & SWT.NO_BACKGROUND) != 0) {
			gc.setBackground(getBackground());
			gc.fillRectangle(xClient - marginWidth, yClient - marginHeight, width, height);
		}
	} else {
		if ((getStyle() & SWT.NO_BACKGROUND) != 0) {
			int height = borderTop + tabHeight + highlight_header + borderBottom;
			if (size.y > height) {
				gc.setBackground(getParent().getBackground());
				gc.fillRectangle(0, height, size.x, size.y - height);
			}
		}
	}
	
	//draw 1 pixel border around outside
	if (borderLeft > 0) {
		gc.setForeground(bottomBorderColor);
		int x1 = borderLeft - 1;
		int x2 = size.x - borderRight;
		int y1 = onBottom ? borderTop - 1 : borderTop + tabHeight;
		int y2 = onBottom ? size.y - tabHeight - borderBottom - 1 : size.y - borderBottom;
		gc.drawLine(x1, y1, x1, y2); // left
		gc.drawLine(x2, y1, x2, y2); // right
		gc.drawLine(x1, y2, x2, y2); // bottom
	}
}
void drawTabArea(Event event) {
	if(! useWebbyStyling()) {
		super.drawTabArea(event);
		return;
	}

	GC gc = event.gc;
	Point size = getSize();
	int[] shape = null;
	Color borderColor = topBorderColor;

	if (tabHeight == 0) {
		int style = getStyle();
		if ((style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0)
			return;
		int x1 = borderLeft - 1;
		int x2 = size.x - borderRight;
		int y1 = borderTop + highlight_header;
		int y2 = borderTop;
		if (borderLeft > 0 && onBottom)
			y2 -= 1;

		shape = new int[] { x1, y1, x1, y2, x2, y2, x2, y1 };

		// If horizontal gradient, show gradient across the whole area
		if (selectedIndex != -1 && selectionGradientColors != null
				&& selectionGradientColors.length > 1
				&& !selectionGradientVertical) {
			drawBackground(gc, shape, true);
		} else if (selectedIndex == -1 && gradientColors != null
				&& gradientColors.length > 1 && !gradientVertical) {
			drawBackground(gc, shape, false);
		} else {
			gc.setBackground(selectedIndex == -1 ? getBackground()
					: selectionBackground);
			gc.fillPolygon(shape);
		}

		// draw 1 pixel border
		if (borderLeft > 0) {
			gc.setForeground(borderColor);
			gc.drawPolyline(shape);
		}
		return;
	}

	int x = Math.max(0, borderLeft - 1);
	int y = borderTop;
	int width = size.x - borderLeft - borderRight + 1;
	int height = tabHeight - 1;

	// Draw Tab Header
	int[] left, right;
	if ((getStyle() & SWT.BORDER) != 0) {
		left = E_TOP_LEFT_CORNER;
		right = E_TOP_RIGHT_CORNER;
	} else {
		left = E_TOP_LEFT_CORNER_BORDERLESS;
		right = E_TOP_RIGHT_CORNER_BORDERLESS;
	}
	
	shape = new int[left.length + right.length + 4];
	int index = 0;
	shape[index++] = x;
	shape[index++] = y + height + highlight_header + 1;
	for (int i = 0; i < left.length / 2; i++) {
		shape[index++] = x + left[2 * i];
		shape[index++] = y + left[2 * i + 1];
	}
	for (int i = 0; i < right.length / 2; i++) {
		shape[index++] = x + width + right[2 * i];
		shape[index++] = y + right[2 * i + 1];
	}
	shape[index++] = x + width;
	shape[index++] = y + height + highlight_header + 1;

	// Fill in background
	boolean bkSelected = single && selectedIndex != -1;
	drawBackground(gc, shape, bkSelected);
	// Fill in parent background for non-rectangular shape
	Region r = new Region();
	r.add(new Rectangle(x, y, width + 1, height + 1));
	r.subtract(shape);
	gc.setBackground(getParent().getBackground());
	fillRegion(gc, r);
	r.dispose();

	// Draw the unselected tabs.
	for (int i = 0; i < items.length; i++) {
		if (i != selectedIndex
				&& event.getBounds().intersects(items[i].getBounds())) {
			items[i].onPaint(gc, false);
		}
	}

	// Draw selected tab
	if (selectedIndex != -1) {
		CTabItem item = items[selectedIndex];
		item.onPaint(gc, true);
	} else {
		// if no selected tab - draw line across bottom of all tabs
		int x1 = borderLeft;
		int y1 = borderTop + tabHeight;
		int x2 = size.x - borderRight;
		gc.setForeground(borderColor);
		gc.drawLine(x1, y1, x2, y1);
	}

	// Draw Buttons
	drawChevron(gc);
	drawMinimize(gc);
	drawMaximize(gc);

	// Draw border line
	if (borderLeft > 0) {
		RGB outside = getParent().getBackground().getRGB();
		antialias(shape, borderColor.getRGB(), null, outside, gc);
		gc.setForeground(borderColor);
		gc.drawPolyline(shape);
	}
}

int getMaxTabTopPadding() {
	return Math.max(tabTopSelectionPadding, tabTopPadding);
}

boolean updateTabHeight(boolean force){
	if (! useWebbyStyling() || fixedTabHeight != SWT.DEFAULT || items.length > 0) {
		return super.updateTabHeight(force);
	}
	
	int tempHeight = 0;
	GC gc = new GC(this);
	tempHeight = gc.textExtent("Default", ETabItem.FLAGS).y + tabTopMargin + getMaxTabTopPadding() + tabBottomPadding; //$NON-NLS-1$

	gc.dispose();
	tabHeight =  tempHeight;
	notifyListeners(SWT.Resize, new Event());
	return true;
}
	
boolean setItemLocation() {
	if(! useWebbyStyling()) {
		return super.setItemLocation();
	}
	
	boolean changed = false;
	if (items.length == 0) return false;
	int y = borderTop;

	int rightItemEdge = getRightItemEdge();
	int maxWidth = rightItemEdge - borderLeft;
	int width = 0;
	for (int i = 0; i < priority.length; i++) {
		CTabItem item = items[priority[i]];
		width += item.width;
		item.showing = i == 0 ? true : item.width > 0 && width <= maxWidth;
	}
	int x = leftMargin;
	int defaultX = getDisplay().getBounds().width + 10; // off screen
	firstIndex = items.length - 1;
	for (int i = 0; i < items.length; i++) {
		ETabItem item = (ETabItem) items[i];
		if (!item.showing) {
			if (item.x != defaultX) changed = true;
			item.x = defaultX;
		} else {
			firstIndex = Math.min(firstIndex, i);
			if (item.x != x || item.y != y) changed = true;
			x = x + tabLeftMargin;
			item.x = x;
			item.y = y;
			if (i == selectedIndex) {
				int edge = Math.min(item.x + item.width, rightItemEdge);
				item.closeRect.x = edge - CTabItem.RIGHT_MARGIN - BUTTON_SIZE;
				item.y = item.y;
			} else {
				item.closeRect.x = item.x + item.width - CTabItem.RIGHT_MARGIN - BUTTON_SIZE;
				item.y = item.y;
			}
			item.closeRect.y = getTextMidline() - BUTTON_SIZE /2;
			x = x + item.width + tabRightMargin;
		}
	}
	
	return changed;
}


//The space above the selected tab
int getSelectedTabTopOffset() {
	return tabTopMargin + (getMaxTabTopPadding() - tabTopSelectionPadding);
}
//The space above the unselected tab
int getUnselectedTabTopOffset() {
	return tabTopMargin + (getMaxTabTopPadding() - tabTopPadding);
}

private boolean useWebbyStyling() {
	return webbyStyle && ! onBottom && ! single;
}
}
