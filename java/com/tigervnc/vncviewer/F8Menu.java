/* Copyright (C) 2002-2005 RealVNC Ltd.  All Rights Reserved.
 * Copyright (C) 2011-2014 Brian P. Hinz
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */

package com.tigervnc.vncviewer;

import java.awt.*;
import java.awt.Cursor;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;

import com.tigervnc.rfb.*;

public class F8Menu extends JPopupMenu implements ActionListener {
  public F8Menu(CConn cc_) {
    super("VNC Menu");
    setLightWeightPopupEnabled(false);
    cc = cc_;
    restore    = addMenuItem("Restore",KeyEvent.VK_R);
    restore.setEnabled(!cc.viewer.embed.getValue());
    move       = addMenuItem("Move");
    move.setEnabled(false);
    size       = addMenuItem("Size");
    size.setEnabled(false);
    minimize   = addMenuItem("Minimize", KeyEvent.VK_N);
    minimize.setEnabled(!cc.viewer.embed.getValue());
    maximize   = addMenuItem("Maximize", KeyEvent.VK_X);
    maximize.setEnabled(!cc.viewer.embed.getValue());
    addSeparator();
    exit       = addMenuItem("Close Viewer", KeyEvent.VK_C);
    addSeparator();
    fullScreen = new JCheckBoxMenuItem("Full Screen");
    fullScreen.setMnemonic(KeyEvent.VK_F);
    fullScreen.setSelected(cc.fullScreen);
    fullScreen.addActionListener(this);
    fullScreen.setEnabled(!cc.viewer.embed.getValue());
    add(fullScreen);
    addSeparator();
    clipboard  = addMenuItem("Clipboard...");
    addSeparator();
    f8 = addMenuItem("Send "+KeyEvent.getKeyText(MenuKey.getMenuKeyCode()), MenuKey.getMenuKeyCode());
    ctrlAltDel = addMenuItem("Send Ctrl-Alt-Del");
    addSeparator();
    refresh    = addMenuItem("Refresh Screen", KeyEvent.VK_H);
    addSeparator();
    newConn    = addMenuItem("New connection...", KeyEvent.VK_W);
    newConn.setEnabled(!cc.viewer.embed.getValue());
    options    = addMenuItem("Options...", KeyEvent.VK_O);
    save       = addMenuItem("Save connection info as...", KeyEvent.VK_S);
    info       = addMenuItem("Connection info...", KeyEvent.VK_I);
    about      = addMenuItem("About VncViewer...", KeyEvent.VK_A);
    addSeparator();
    dismiss    = addMenuItem("Dismiss menu");
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  JMenuItem addMenuItem(String str, int mnemonic) {
    JMenuItem item = new JMenuItem(str, mnemonic);
    item.addActionListener(this);
    add(item);
    return item;
  }

  JMenuItem addMenuItem(String str) {
    JMenuItem item = new JMenuItem(str);
    item.addActionListener(this);
    add(item);
    return item;
  }

  boolean actionMatch(ActionEvent ev, JMenuItem item) {
    return ev.getActionCommand().equals(item.getActionCommand());
  }

  public void actionPerformed(ActionEvent ev) {
    if (actionMatch(ev, exit)) {
      cc.close();
    } else if (actionMatch(ev, fullScreen)) {
      cc.toggleFullScreen();
    } else if (actionMatch(ev, restore)) {
      if (cc.fullScreen) cc.toggleFullScreen();
      cc.viewport.setExtendedState(JFrame.NORMAL);
    } else if (actionMatch(ev, minimize)) {
      if (cc.fullScreen) cc.toggleFullScreen();
      cc.viewport.setExtendedState(JFrame.ICONIFIED);
    } else if (actionMatch(ev, maximize)) {
      if (cc.fullScreen) cc.toggleFullScreen();
      cc.viewport.setExtendedState(JFrame.MAXIMIZED_BOTH);
    } else if (actionMatch(ev, clipboard)) {
      cc.clipboardDialog.showDialog(cc.viewport);
    } else if (actionMatch(ev, f8)) {
      cc.writeKeyEvent(MenuKey.getMenuKeySym(), true);
      cc.writeKeyEvent(MenuKey.getMenuKeySym(), false);
    } else if (actionMatch(ev, ctrlAltDel)) {
      cc.writeKeyEvent(Keysyms.Control_L, true);
      cc.writeKeyEvent(Keysyms.Alt_L, true);
      cc.writeKeyEvent(Keysyms.Delete, true);
      cc.writeKeyEvent(Keysyms.Delete, false);
      cc.writeKeyEvent(Keysyms.Alt_L, false);
      cc.writeKeyEvent(Keysyms.Control_L, false);
    } else if (actionMatch(ev, refresh)) {
      cc.refresh();
    } else if (actionMatch(ev, newConn)) {
      VncViewer.newViewer(cc.viewer);
    } else if (actionMatch(ev, options)) {
      cc.options.showDialog(cc.viewport);
    } else if (actionMatch(ev, save)) {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle("Save current configuration as:");
      fc.setApproveButtonText("OK");
      fc.setFileHidingEnabled(false);
      Window fullScreenWindow = Viewport.getFullScreenWindow();
      if (fullScreenWindow != null)
        Viewport.setFullScreenWindow(null);
      int ret = fc.showOpenDialog(cc.viewport);
      if (fullScreenWindow != null)
        Viewport.setFullScreenWindow(fullScreenWindow);
      if (ret == JFileChooser.APPROVE_OPTION) {
        String filename = fc.getSelectedFile().toString();
        if (filename != null)
          Configuration.save(filename);
      }
    } else if (actionMatch(ev, info)) {
      cc.showInfo();
    } else if (actionMatch(ev, about)) {
      cc.showAbout();
    } else if (actionMatch(ev, dismiss)) {
      firePopupMenuCanceled();
    }
  }

  CConn cc;
  JMenuItem restore, move, size, minimize, maximize;
  JMenuItem exit, clipboard, ctrlAltDel, refresh;
  JMenuItem newConn, options, save, info, about, dismiss;
  static JMenuItem f8;
  JCheckBoxMenuItem fullScreen;
  static LogWriter vlog = new LogWriter("F8Menu");
}
