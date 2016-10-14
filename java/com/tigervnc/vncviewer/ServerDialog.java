/* Copyright (C) 2002-2005 RealVNC Ltd.  All Rights Reserved.
 * Copyright (C) 2011-2013 Brian P. Hinz
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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.WindowConstants.*;
import java.util.*;

import com.tigervnc.rfb.*;

class ServerDialog extends Dialog implements
                           ActionListener
{

  @SuppressWarnings({"unchecked","rawtypes"})
  public ServerDialog(OptionsDialog options_,
                      String defaultServerName, CConn cc_) {

    super(true);
    cc = cc_;
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setResizable(false);
    setSize(new Dimension(340, 135));
    setTitle("VNC Viewer: Connection Details");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (VncViewer.nViewers == 1) {
          cc.viewer.exit(1);
        } else {
          ret = false;
          endDialog();
        }
      }
    });

    options = options_;
    getContentPane().setLayout(new GridBagLayout());

    JLabel serverLabel = new JLabel("Server:", JLabel.RIGHT);
    if (UserPreferences.get("ServerDialog", "history") != null) {
      String valueStr = UserPreferences.get("ServerDialog", "history");
      server = new JComboBox(valueStr.split(","));
    } else {
      server = new JComboBox();
    }

    // Hack to set the left inset on editable JComboBox
    if (UIManager.getLookAndFeel().getID() == "Windows") {
      server.setBorder(BorderFactory.createCompoundBorder(server.getBorder(),
        BorderFactory.createEmptyBorder(0,2,0,0)));
    } else if (UIManager.getLookAndFeel().getID() == "Metal") {
      ComboBoxEditor editor = server.getEditor();
      JTextField jtf = (JTextField)editor.getEditorComponent();
      jtf.setBorder(new CompoundBorder(jtf.getBorder(), new EmptyBorder(0,2,0,0)));
    }

    server.setEditable(true);
    editor = server.getEditor();
    editor.getEditorComponent().addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {}
      public void keyReleased(KeyEvent e) {}
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          server.insertItemAt(editor.getItem(), 0);
          server.setSelectedIndex(0);
          commit();
        }
      }
    });

    JPanel topPanel = new JPanel(new GridBagLayout());

    addGBComponent(new JLabel(VncViewer.logoIcon),topPanel, 0, 0, 1, 1, 0, 0, 0, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(5,5,5,15));
    addGBComponent(serverLabel,topPanel, 1, 0, 1, 1, 0, 0, 0, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_END, new Insets(10,0,5,5));
    addGBComponent(server,topPanel, 2, 0, 1, 1, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(10,0,5,40));

    optionsButton = new JButton("Options...");
    aboutButton = new JButton("About...");
    okButton = new JButton("OK");
    cancelButton = new JButton("Cancel");
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.setPreferredSize(new Dimension(340, 40));
    addGBComponent(aboutButton,buttonPanel, 0, 3, 1, 1, 0, 0, 0.2, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0,5,0,5));
    addGBComponent(optionsButton,buttonPanel, 1, 3, 1, 1, 0, 0, 0, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0,5,0,5));
    addGBComponent(okButton,buttonPanel, 2, 3, 1, 1, 0, 0, 0.8, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0,5,0,5));
    addGBComponent(cancelButton,buttonPanel, 3, 3, 1, 1, 0, 0, 0.5, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(0,5,0,5));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = 1;
    gbc.insets = new Insets(0,0,0,0);
    gbc.ipadx = 0;
    gbc.ipady = 0;
    gbc.weightx = 1;
    gbc.weighty = 1;
    getContentPane().add(topPanel,gbc);
    getContentPane().add(buttonPanel);

    server.addActionListener(this);
    optionsButton.addActionListener(this);
    aboutButton.addActionListener(this);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    pack();
  }

  @SuppressWarnings({"unchecked","rawtypes"})
  public void actionPerformed(ActionEvent e) {
    Object s = e.getSource();
    if (s instanceof JButton && (JButton)s == okButton) {
      commit();
    } else if (s instanceof JButton && (JButton)s == cancelButton) {
      if (VncViewer.nViewers == 1)
        cc.viewer.exit(1);
      ret = false;
      endDialog();
    } else if (s instanceof JButton && (JButton)s == optionsButton) {
      options.showDialog(this);
    } else if (s instanceof JButton && (JButton)s == aboutButton) {
      cc.showAbout();
    } else if (s instanceof JComboBox && (JComboBox)s == server) {
      if (e.getActionCommand().equals("comboBoxEdited")) {
        server.insertItemAt(editor.getItem(), 0);
        server.setSelectedIndex(0);
      }
    }
  }

  private void commit() {
    String serverName = (String)server.getSelectedItem();
    if (serverName == null || serverName.equals("")) {
      vlog.error("Invalid servername specified");
      if (VncViewer.nViewers == 1)
        cc.viewer.exit(1);
      ret = false;
      endDialog();
    }
    // set params
    Configuration.setParam("Server", Hostname.getHost(serverName));
    Configuration.setParam("Port", Integer.toString(Hostname.getPort(serverName)));
    // Update the history list
    String valueStr = UserPreferences.get("ServerDialog", "history");
    String t = (valueStr == null) ? "" : valueStr;
    StringTokenizer st = new StringTokenizer(t, ",");
    StringBuffer sb = new StringBuffer().append((String)server.getSelectedItem());
    while (st.hasMoreTokens()) {
      String str = st.nextToken();
      if (!str.equals((String)server.getSelectedItem()) && !str.equals("")) {
        sb.append(',');
        sb.append(str);
      }
    }
    UserPreferences.set("ServerDialog", "history", sb.toString());
    UserPreferences.save("ServerDialog");
    endDialog();
  }

  CConn cc;
  @SuppressWarnings("rawtypes")
  JComboBox server;
  ComboBoxEditor editor;
  JButton aboutButton, optionsButton, okButton, cancelButton;
  OptionsDialog options;
  static LogWriter vlog = new LogWriter("ServerDialog");

}
