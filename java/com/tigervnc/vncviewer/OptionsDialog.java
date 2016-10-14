/* Copyright (C) 2002-2005 RealVNC Ltd.  All Rights Reserved.
 * Copyright (C) 2011-2015 Brian P. Hinz
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
import java.io.File;
import java.text.Format;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.*;

import com.tigervnc.rfb.*;

class OptionsDialog extends Dialog implements
                            ActionListener,
                            ItemListener
{

  private class IntegerTextField extends JFormattedTextField {
    public IntegerTextField(Format format) {
      super(format);
    }
    @Override
    protected void processFocusEvent(final FocusEvent e) {
      if (e.isTemporary())
        return;
      if (e.getID() == FocusEvent.FOCUS_LOST)
        if (getText() == null || getText().isEmpty())
          setValue(null);
      super.processFocusEvent(e);
    }
  }

  // Constants
  // Static variables
  static LogWriter vlog = new LogWriter("OptionsDialog");

  CConn cc;
  JPanel FormatPanel, InputsPanel, MiscPanel, DefaultsPanel, SecPanel, ScreenPanel;
  JCheckBox autoSelect, customCompressLevel, noJpeg;
  @SuppressWarnings({"rawtypes"})
  JComboBox menuKey, compressLevel, qualityLevel, scalingFactor;
  ButtonGroup encodingGroup, colourGroup;
  JRadioButton zrle, hextile, tight, raw;
  JRadioButton fullColour, mediumColour, lowColour, veryLowColour;
  JCheckBox viewOnly, acceptClipboard, sendClipboard, acceptBell;
  JCheckBox desktopSize, fullScreen, fullScreenAllMonitors, shared, useLocalCursor;
  JCheckBox secVeNCrypt, encNone, encTLS, encX509;
  JCheckBox secNone, secVnc, secPlain, secIdent, sendLocalUsername;
  JButton okButton, cancelButton;
  JButton ca, crl;
  JButton cfLoadButton, cfSaveAsButton, defSaveButton, defReloadButton, defClearButton;
  JTextField desktopWidth, desktopHeight;

  @SuppressWarnings({"rawtypes","unchecked"})
  public OptionsDialog(CConn cc_) {
    super(true);
    cc = cc_;
    setResizable(false);
    setTitle("VNC Viewer Options");

    getContentPane().setLayout(
      new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    ButtonGroup encodingGroup = new ButtonGroup();
    ButtonGroup colourGroup = new ButtonGroup();

    // Compression tab
    FormatPanel=new JPanel(new GridBagLayout());

    autoSelect = new JCheckBox("Auto Select");
    autoSelect.addItemListener(this);

    JPanel encodingPanel = new JPanel(new GridBagLayout());
    encodingPanel.setBorder(BorderFactory.createTitledBorder("Preferred encoding"));
    tight = addRadioCheckbox("Tight", encodingGroup, encodingPanel);
    zrle = addRadioCheckbox("ZRLE", encodingGroup, encodingPanel);
    hextile = addRadioCheckbox("Hextile", encodingGroup, encodingPanel);
    raw = addRadioCheckbox("Raw", encodingGroup, encodingPanel);

    JPanel tightPanel = new JPanel(new GridBagLayout());
    customCompressLevel = new JCheckBox("Custom Compression Level");
    customCompressLevel.addItemListener(this);
    Object[] compressionLevels = { 1, 2, 3, 4, 5, 6 };
    compressLevel  = new JComboBox(compressionLevels);
    JLabel compressionLabel = new JLabel("Level (1=fast, 6=best [4-6 are rarely useful])");
    noJpeg = new JCheckBox("Allow JPEG Compression");
    noJpeg.addItemListener(this);
    Object[] qualityLevels = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    qualityLevel  = new JComboBox(qualityLevels);
    JLabel qualityLabel = new JLabel("Quality (0=poor, 9=best)");
    // Hack to set the left inset on editable JComboBox
    if (UIManager.getLookAndFeel().getID() == "Windows") {
      compressLevel.setBorder(BorderFactory.createCompoundBorder(compressLevel.getBorder(),
        BorderFactory.createEmptyBorder(0,1,0,0)));
    } else if (UIManager.getLookAndFeel().getID() == "Metal") {
      ComboBoxEditor editor = compressLevel.getEditor();
      JTextField jtf = (JTextField)editor.getEditorComponent();
      jtf.setBorder(new CompoundBorder(jtf.getBorder(), new EmptyBorder(0,2,0,0)));
    }
    Dimension size = compressLevel.getPreferredSize();
    compressLevel.setEditable(true);
    compressLevel.setPreferredSize(size);
    addGBComponent(customCompressLevel, tightPanel, 0, 0, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(0,0,0,0));
    addGBComponent(compressLevel, tightPanel,       0, 1, 1, 1, 2, 2, 0, 0, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(0,20,0,0));
    addGBComponent(compressionLabel, tightPanel,    1, 1, 1, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(0,5,0,0));
    addGBComponent(noJpeg, tightPanel,              0, 2, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(4,0,0,0));
    addGBComponent(qualityLevel, tightPanel,        0, 3, 1, 1, 2, 2, 0, 0, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(0,20,0,0));
    addGBComponent(qualityLabel, tightPanel,        1, 3, 1, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(0,5,0,0));


    JPanel colourPanel = new JPanel(new GridBagLayout());
    colourPanel.setBorder(BorderFactory.createTitledBorder("Color level"));
    fullColour = addRadioCheckbox("Full (all available colors)", colourGroup, colourPanel);
    mediumColour = addRadioCheckbox("Medium (256 colors)", colourGroup, colourPanel);
    lowColour = addRadioCheckbox("Low (64 colours)", colourGroup, colourPanel);
    veryLowColour = addRadioCheckbox("Very low(8 colors)", colourGroup, colourPanel);

    addGBComponent(autoSelect,FormatPanel,    0, 0, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(4,5,0,5));
    addGBComponent(encodingPanel,FormatPanel, 0, 1, 1, 1, 2, 2, 3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(0,5,0,5));
    addGBComponent(colourPanel,FormatPanel,   1, 1, 1, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_END, new Insets(0,0,0,5));
    addGBComponent(tightPanel,FormatPanel,    0, 2, 2, GridBagConstraints.REMAINDER, 2, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(0,5,0,5));

    // Input tab
    InputsPanel=new JPanel(new GridBagLayout());

    viewOnly = new JCheckBox("View Only (ignore mouse & keyboard)");
    viewOnly.addItemListener(this);
    acceptClipboard = new JCheckBox("Accept clipboard from server");
    acceptClipboard.addItemListener(this);
    sendClipboard = new JCheckBox("Send clipboard to server");
    sendClipboard.addItemListener(this);
    JLabel menuKeyLabel = new JLabel("Menu Key");
    String[] menuKeys = new String[MenuKey.getMenuKeySymbolCount()];
    for (int i = 0; i < MenuKey.getMenuKeySymbolCount(); i++)
      menuKeys[i] = KeyEvent.getKeyText(MenuKey.getMenuKeySymbols()[i].keycode);
    menuKey  = new JComboBox(menuKeys);
    menuKey.addItemListener(this);
    addGBComponent(viewOnly,InputsPanel,        0, 0, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(acceptClipboard,InputsPanel, 0, 1, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(sendClipboard,InputsPanel,   0, 2, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(menuKeyLabel,InputsPanel,    0, 3, 1, GridBagConstraints.REMAINDER, 2, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(8,8,0,5));
    addGBComponent(menuKey,InputsPanel,         1, 3, 1, GridBagConstraints.REMAINDER, 2, 2, 25, 1, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(4,5,0,5));

    // Screen tab
    ScreenPanel=new JPanel(new GridBagLayout());
    desktopSize = new JCheckBox("Resize remote session on connect");
    desktopSize.addItemListener(this);
    desktopSize.setEnabled(!cc.viewer.embed.getValue() &&
                           (cc.viewer.desktopSize.getValue() != null));
    NumberFormat format = NumberFormat.getIntegerInstance();
    format.setMaximumIntegerDigits(5);
    format.setMinimumIntegerDigits(0);
    format.setGroupingUsed(false);
    desktopWidth = new IntegerTextField(format);
    desktopWidth.setColumns(4);
    desktopWidth.setEnabled(desktopSize.isSelected());
    desktopHeight = new IntegerTextField(format);
    desktopHeight.setColumns(4);
    desktopHeight.setEnabled(desktopSize.isSelected());
    JPanel desktopSizePanel = new JPanel();
    desktopSizePanel.add(desktopWidth);
    desktopSizePanel.add(new JLabel("x"));
    desktopSizePanel.add(desktopHeight);
    fullScreen = new JCheckBox("Full-screen mode");
    fullScreen.addItemListener(this);
    fullScreen.setEnabled(!cc.viewer.embed.getValue());
    fullScreenAllMonitors = new JCheckBox("Enable full-screen mode over all monitors");
    fullScreenAllMonitors.addItemListener(this);
    fullScreenAllMonitors.setEnabled(!cc.viewer.embed.getValue());
    JLabel scalingFactorLabel = new JLabel("Scaling Factor");
    Object[] scalingFactors = {
      "Auto", "Fixed Aspect Ratio", "50%", "75%", "95%", "100%", "105%",
      "125%", "150%", "175%", "200%", "250%", "300%", "350%", "400%" };
    scalingFactor = new JComboBox(scalingFactors);
    // Hack to set the left inset on editable JComboBox
    if (UIManager.getLookAndFeel().getID() == "Windows") {
      scalingFactor.setBorder(BorderFactory.createCompoundBorder(scalingFactor.getBorder(),
        BorderFactory.createEmptyBorder(0,1,0,0)));
    } else if (UIManager.getLookAndFeel().getID() == "Metal") {
      ComboBoxEditor sfe = scalingFactor.getEditor();
      JTextField sfeTextField = (JTextField)sfe.getEditorComponent();
      sfeTextField.setBorder(new CompoundBorder(sfeTextField.getBorder(),
                                                new EmptyBorder(0,2,0,0)));
    }
    scalingFactor.setEditable(true);
    scalingFactor.addItemListener(this);
    scalingFactor.setEnabled(!cc.viewer.embed.getValue());
    addGBComponent(desktopSize,ScreenPanel, 0, 0, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(desktopSizePanel,ScreenPanel, 0, 1, 2, 1, 2, 2, 1, 0, GridBagConstraints.REMAINDER, GridBagConstraints.LINE_START, new Insets(0,20,0,0));
    addGBComponent(fullScreen,ScreenPanel, 0, 2, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(0,5,0,5));
    addGBComponent(fullScreenAllMonitors,ScreenPanel, 0, 3, 4, 1, 2, 2, 1, 0, GridBagConstraints.REMAINDER, GridBagConstraints.LINE_START, new Insets(4,25,0,5));
    addGBComponent(scalingFactorLabel,ScreenPanel, 0, 4, 1, GridBagConstraints.REMAINDER, 2, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(8,8,0,5));
    addGBComponent(scalingFactor,ScreenPanel, 1, 4, 1, GridBagConstraints.REMAINDER, 2, 2, 25, 1, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(4,5,0,5));

    // Misc tab
    MiscPanel=new JPanel(new GridBagLayout());

    shared = new JCheckBox("Shared connection (do not disconnect other viewers)");
    shared.addItemListener(this);
    useLocalCursor = new JCheckBox("Render cursor locally");
    useLocalCursor.addItemListener(this);
    acceptBell = new JCheckBox("Beep when requested by the server");
    acceptBell.addItemListener(this);
    addGBComponent(shared,MiscPanel,         0, 0, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(useLocalCursor,MiscPanel, 0, 1, 2, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(4,5,0,5));
    addGBComponent(acceptBell,MiscPanel,     0, 2, 2, 1, 2, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(4,5,0,5));

    // load/save tab
    DefaultsPanel=new JPanel(new GridBagLayout());

    JPanel configPanel = new JPanel(new GridBagLayout());
    configPanel.setBorder(BorderFactory.createTitledBorder("Configuration File"));
    cfLoadButton = new JButton("Load");
    cfLoadButton.addActionListener(this);
    addGBComponent(cfLoadButton,configPanel, 0, 0, 1, 1, 0, 0, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4,8,4,8));
    cfSaveAsButton = new JButton("Save As...");
    cfSaveAsButton.addActionListener(this);
    addGBComponent(cfSaveAsButton,configPanel, 0, 1, 1, 1, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4,8,4,8));

    JPanel defaultsPanel = new JPanel(new GridBagLayout());
    defaultsPanel.setBorder(BorderFactory.createTitledBorder("Defaults"));
    defClearButton = new JButton("Clear");
    defClearButton.addActionListener(this);
    addGBComponent(defClearButton,defaultsPanel, 0, 0, 1, 1, 0, 0, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4,8,4,8));
    defReloadButton = new JButton("Reload");
    defReloadButton.addActionListener(this);
    addGBComponent(defReloadButton,defaultsPanel, 0, 1, 1, 1, 0, 0, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4,8,4,8));
    defSaveButton = new JButton("Save");
    defSaveButton.addActionListener(this);
    addGBComponent(defSaveButton,defaultsPanel, 0, 2, 1, 1, 0, 0, 0, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, new Insets(4,8,4,8));

    addGBComponent(configPanel,DefaultsPanel, 0, 0, 1, GridBagConstraints.REMAINDER, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.PAGE_START, new Insets(4,5,4,5));
    addGBComponent(defaultsPanel,DefaultsPanel, 1, 0, 1, GridBagConstraints.REMAINDER, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.PAGE_START, new Insets(4,0,4,5));

    // security tab
    SecPanel=new JPanel(new GridBagLayout());

    JPanel encryptionPanel = new JPanel(new GridBagLayout());
    encryptionPanel.setBorder(BorderFactory.createTitledBorder("Encryption"));
    encNone = addCheckbox("None", null, encryptionPanel);
    encTLS = addCheckbox("Anonymous TLS", null, encryptionPanel);
    encX509 = addJCheckBox("TLS with X.509 certificates", null, encryptionPanel, new GridBagConstraints(0,2,3,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.REMAINDER,new Insets(0,0,0,0),0,0));

    ca = new JButton("Load CA certificate");
    ca.addActionListener(this);
    crl = new JButton("Load CRL certificate");
    crl.addActionListener(this);
    addGBComponent(ca, encryptionPanel,  0, 3, 1, 1, 2, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.LINE_START, new Insets(2,20,2,2));
    addGBComponent(crl, encryptionPanel, 1, 3, 1, 1, 2, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.LINE_START, new Insets(2,2,2,2));

    JPanel authPanel = new JPanel(new GridBagLayout());
    authPanel.setBorder(BorderFactory.createTitledBorder("Authentication"));
    secNone = addCheckbox("None", null, authPanel);
    secVnc = addCheckbox("Standard VNC", null, authPanel);
    secPlain = addJCheckBox("Plaintext", null, authPanel, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.NONE,new Insets(0,0,0,5),0,0));
    secIdent = addJCheckBox("Ident", null, authPanel, new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.NONE,new Insets(0,0,0,5),0,0));
    sendLocalUsername = new JCheckBox("Send Local Username");
    sendLocalUsername.addItemListener(this);
    addGBComponent(sendLocalUsername, authPanel, 1, 2, 1, 2, 0, 0, 2, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, new Insets(0,20,0,0));

    secVeNCrypt = new JCheckBox("Extended encryption and authentication methods (VeNCrypt)");
    secVeNCrypt.addItemListener(this);
    addGBComponent(secVeNCrypt,SecPanel,     0, 0, 1, 1, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.FIRST_LINE_START, new Insets(4,5,0,30));
    addGBComponent(encryptionPanel,SecPanel, 0, 1, 1, 1, 2, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.LINE_START, new Insets(0,10,2,5));
    addGBComponent(authPanel,SecPanel,       0, 3, 1, 1, 2, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_START, new Insets(2,10,2,5));

    tabPane.add(FormatPanel);
    tabPane.add(ScreenPanel);
    tabPane.add(InputsPanel);
    tabPane.add(MiscPanel);
    tabPane.add(DefaultsPanel);
    tabPane.add(SecPanel);
    tabPane.addTab("Compression", FormatPanel);
    tabPane.addTab("Security", SecPanel);
    tabPane.addTab("Input", InputsPanel);
    tabPane.addTab("Screen", ScreenPanel);
    tabPane.addTab("Misc", MiscPanel);
    tabPane.addTab("Load / Save", DefaultsPanel);
    tabPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

    okButton = new JButton("OK");
    okButton.setPreferredSize(new Dimension(90,30));
    okButton.addActionListener(this);
    cancelButton = new JButton("Cancel");
    cancelButton.setPreferredSize(new Dimension(90,30));
    cancelButton.addActionListener(this);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(okButton);
    buttonPane.add(Box.createRigidArea(new Dimension(5,0)));
    buttonPane.add(cancelButton);
    buttonPane.add(Box.createRigidArea(new Dimension(5,0)));

    this.getContentPane().add(tabPane);
    this.getContentPane().add(buttonPane);

    pack();

  }

  public void initDialog() {
    if (cc != null) cc.setOptions();
    zrle.setEnabled(!autoSelect.isSelected());
    hextile.setEnabled(!autoSelect.isSelected());
    tight.setEnabled(!autoSelect.isSelected());
    raw.setEnabled(!autoSelect.isSelected());
    fullColour.setEnabled(!autoSelect.isSelected());
    mediumColour.setEnabled(!autoSelect.isSelected());
    lowColour.setEnabled(!autoSelect.isSelected());
    veryLowColour.setEnabled(!autoSelect.isSelected());
    compressLevel.setEnabled(customCompressLevel.isSelected());
    qualityLevel.setEnabled(noJpeg.isSelected());
    sendLocalUsername.setEnabled(secVeNCrypt.isEnabled()&&
      (secPlain.isSelected()||secIdent.isSelected()));
  }

  private void updatePreferences() {
    if (autoSelect.isSelected()) {
      UserPreferences.set("global", "AutoSelect", true);
    } else {
      UserPreferences.set("global", "AutoSelect", false);
      if (zrle.isSelected()) {
        UserPreferences.set("global", "PreferredEncoding", "ZRLE");
      } else if (hextile.isSelected()) {
        UserPreferences.set("global", "PreferredEncoding", "hextile");
      } else if (tight.isSelected()) {
        UserPreferences.set("global", "PreferredEncoding", "Tight");
      } else if (raw.isSelected()) {
        UserPreferences.set("global", "PreferredEncoding", "raw");
      }
    }
    if (fullColour.isSelected()) {
      UserPreferences.set("global", "FullColour", true);
    } else {
      UserPreferences.set("global", "FullColour", false);
      if (mediumColour.isSelected()) {
        UserPreferences.set("global", "LowColorLevel", 2);
      } else if (lowColour.isSelected()) {
        UserPreferences.set("global", "LowColorLevel", 1);
      } else if (veryLowColour.isSelected()) {
        UserPreferences.set("global", "LowColorLevel", 0);
      }
    }
    UserPreferences.set("global", "NoJPEG", !noJpeg.isSelected());
    UserPreferences.set("global", "QualityLevel", (Integer)qualityLevel.getSelectedItem());
    UserPreferences.set("global", "CustomCompressLevel", customCompressLevel.isSelected());
    UserPreferences.set("global", "CompressLevel", (Integer)compressLevel.getSelectedItem());
    UserPreferences.set("global", "ViewOnly", viewOnly.isSelected());
    UserPreferences.set("global", "AcceptClipboard", acceptClipboard.isSelected());
    UserPreferences.set("global", "SendClipboard", sendClipboard.isSelected());
    String menuKeyStr = MenuKey.getMenuKeySymbols()[menuKey.getSelectedIndex()].name;
    UserPreferences.set("global", "MenuKey", menuKeyStr);
    String desktopSizeString =
      desktopSize.isSelected() ? desktopWidth.getText() + "x" + desktopHeight.getText() : "";
    UserPreferences.set("global", "DesktopSize", desktopSizeString);
    UserPreferences.set("global", "FullScreen", fullScreen.isSelected());
    UserPreferences.set("global", "FullScreenAllMonitors", fullScreenAllMonitors.isSelected());
    UserPreferences.set("global", "Shared", shared.isSelected());
    UserPreferences.set("global", "UseLocalCursor", useLocalCursor.isSelected());
    UserPreferences.set("global", "AcceptBell", acceptBell.isSelected());
    String scaleString = scalingFactor.getSelectedItem().toString();
    if (scaleString.equalsIgnoreCase("Auto")) {
      UserPreferences.set("global", "ScalingFactor", "Auto");
    } else if(scaleString.equalsIgnoreCase("Fixed Aspect Ratio")) {
      UserPreferences.set("global", "ScalingFactor", "FixedRatio");
    } else {
      scaleString=scaleString.substring(0, scaleString.length()-1);
      UserPreferences.set("global", "ScalingFactor", scaleString);
    }
    UserPreferences.set("viewer", "secVeNCrypt", secVeNCrypt.isSelected());
    UserPreferences.set("viewer", "encNone", encNone.isSelected());
    UserPreferences.set("viewer", "encTLS", encTLS.isSelected());
    UserPreferences.set("viewer", "encX509", encX509.isSelected());
    UserPreferences.set("viewer", "secNone", secNone.isSelected());
    UserPreferences.set("viewer", "secVnc", secVnc.isSelected());
    UserPreferences.set("viewer", "secPlain", secPlain.isSelected());
    UserPreferences.set("viewer", "secIdent", secIdent.isSelected());
    UserPreferences.set("global", "SendLocalUsername", sendLocalUsername.isSelected());
    if (CSecurityTLS.x509ca.getValueStr() != "")
      UserPreferences.set("viewer", "x509ca", CSecurityTLS.x509ca.getValueStr());
    if (CSecurityTLS.x509crl.getValueStr() != "")
      UserPreferences.set("viewer", "x509crl", CSecurityTLS.x509crl.getValueStr());
  }

  private void restorePreferences() {
    autoSelect.setSelected(UserPreferences.getBool("global", "AutoSelect"));
    if (!autoSelect.isSelected()) {
      if (UserPreferences.getBool("global", "FullColour")) {
        fullColour.setSelected(true);
      } else {
        switch (UserPreferences.getInt("global", "LowColorLevel")) {
        case 2:
          mediumColour.setSelected(true);
          break;
        case 1:
          lowColour.setSelected(true);
          break;
        case 0:
          veryLowColour.setSelected(true);
          break;
        }
      }
      String encoding = UserPreferences.get("global", "PreferredEncoding");
      if (encoding != null) {
        switch (Encodings.encodingNum(encoding)) {
        case Encodings.encodingZRLE:
          zrle.setSelected(true);
          break;
        case Encodings.encodingHextile:
          hextile.setSelected(true);
          break;
        case Encodings.encodingRaw:
          raw.setSelected(true);
          break;
        default:
          tight.setSelected(true);
        }
      }
    }
    noJpeg.setSelected(!UserPreferences.getBool("global", "NoJPEG"));
    qualityLevel.setSelectedItem(UserPreferences.getInt("global", "QualityLevel"));
    customCompressLevel.setSelected(UserPreferences.getBool("global", "CustomCompressLevel"));
    compressLevel.setSelectedItem(UserPreferences.getInt("global", "CompressLevel"));
    viewOnly.setSelected(UserPreferences.getBool("global", "ViewOnly"));
    acceptClipboard.setSelected(UserPreferences.getBool("global", "AcceptClipboard"));
    sendClipboard.setSelected(UserPreferences.getBool("global", "SendClipboard"));
    menuKey.setSelectedItem(UserPreferences.get("global", "MenuKey"));
    desktopSize.setSelected(UserPreferences.get("global", "DesktopSize") != null);
    if (desktopSize.isSelected()) {
      String desktopSizeString = UserPreferences.get("global", "DesktopSize");
      desktopWidth.setText(desktopSizeString.split("x")[0]);
      desktopHeight.setText(desktopSizeString.split("x")[1]);
    }
    fullScreen.setSelected(UserPreferences.getBool("global", "FullScreen"));
    fullScreenAllMonitors.setSelected(UserPreferences.getBool("global", "FullScreenAllMonitors"));
    if (shared.isEnabled())
      shared.setSelected(UserPreferences.getBool("global", "Shared"));
    useLocalCursor.setSelected(UserPreferences.getBool("global", "UseLocalCursor"));
    acceptBell.setSelected(UserPreferences.getBool("global", "AcceptBell"));
    String scaleString = UserPreferences.get("global", "ScalingFactor");
    if (scaleString != null) {
      if (scaleString.equalsIgnoreCase("Auto")) {
        scalingFactor.setSelectedItem("Auto");
      } else if (scaleString.equalsIgnoreCase("FixedRatio")) {
        scalingFactor.setSelectedItem("Fixed Aspect Ratio");
      } else {
        scalingFactor.setSelectedItem(scaleString+"%");
      }
    }
    if (secVeNCrypt.isEnabled()) {
      secVeNCrypt.setSelected(UserPreferences.getBool("viewer", "secVeNCrypt", true));
      if (secVeNCrypt.isSelected()) {
        encNone.setSelected(UserPreferences.getBool("viewer", "encNone", true));
        encTLS.setSelected(UserPreferences.getBool("viewer", "encTLS", true));
        encX509.setSelected(UserPreferences.getBool("viewer", "encX509", true));
        secPlain.setSelected(UserPreferences.getBool("viewer", "secPlain", true));
        secIdent.setSelected(UserPreferences.getBool("viewer", "secIdent", true));
        sendLocalUsername.setSelected(UserPreferences.getBool("global", "SendLocalUsername"));
      }
    }
    if (secNone.isEnabled())
      secNone.setSelected(UserPreferences.getBool("viewer", "secNone", true));
    if (secVnc.isEnabled())
      secVnc.setSelected(UserPreferences.getBool("viewer", "secVnc", true));
  }

  JRadioButton addRadioCheckbox(String str, ButtonGroup group, JPanel panel) {
    JRadioButton c = new JRadioButton(str);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    panel.add(c,gbc);
    group.add(c);
    c.addItemListener(this);
    return c;
  }

  JCheckBox addCheckbox(String str, ButtonGroup group, JPanel panel) {
    JCheckBox c = new JCheckBox(str);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    panel.add(c,gbc);
    if (group != null)
      group.add(c);
    c.addItemListener(this);
    return c;
  }

  JCheckBox addJCheckBox(String str, ButtonGroup group, JPanel panel,
      GridBagConstraints gbc) {
    JCheckBox c = new JCheckBox(str);
    panel.add(c,gbc);
    if (group != null)
      group.add(c);
    c.addItemListener(this);

    return c;
  }

  public void endDialog() {
    super.endDialog();
    if (cc.viewport != null && cc.viewport.isVisible()) {
      cc.viewport.toFront();
      cc.viewport.requestFocus();
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object s = e.getSource();
    if (s instanceof JButton && (JButton)s == okButton) {
      if (cc != null) cc.getOptions();
      endDialog();
    } else if (s instanceof JButton && (JButton)s == cancelButton) {
      endDialog();
    } else if (s instanceof JButton && (JButton)s == cfLoadButton) {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle("Path to configuration file");
      fc.setApproveButtonText("OK");
      fc.setFileHidingEnabled(false);
      int ret = fc.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION) {
        String filename = fc.getSelectedFile().toString();
        if (filename != null)
          Configuration.load(filename);
        cc.setOptions();
      }
    } else if (s instanceof JButton && (JButton)s == cfSaveAsButton) {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle("Save current configuration as:");
      fc.setApproveButtonText("OK");
      fc.setFileHidingEnabled(false);
      int ret = fc.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION) {
        String filename = fc.getSelectedFile().toString();
        if (filename != null)
          Configuration.save(filename);
      }
    } else if (s instanceof JButton && (JButton)s == defSaveButton) {
      updatePreferences();
      UserPreferences.save();
    } else if (s instanceof JButton && (JButton)s == defReloadButton) {
      restorePreferences();
    } else if (s instanceof JButton && (JButton)s == defClearButton) {
      UserPreferences.clear();
      cc.setOptions();
    } else if (s instanceof JButton && (JButton)s == ca) {
      JFileChooser fc = new JFileChooser(new File(CSecurityTLS.getDefaultCA()));
      fc.setDialogTitle("Path to X509 CA certificate");
      fc.setApproveButtonText("OK");
      fc.setFileHidingEnabled(false);
      int ret = fc.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION)
        CSecurityTLS.x509ca.setParam(fc.getSelectedFile().toString());
    } else if (s instanceof JButton && (JButton)s == crl) {
      JFileChooser fc = new JFileChooser(new File(CSecurityTLS.getDefaultCRL()));
      fc.setDialogTitle("Path to X509 CRL file");
      fc.setApproveButtonText("OK");
      fc.setFileHidingEnabled(false);
      int ret = fc.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION)
        CSecurityTLS.x509crl.setParam(fc.getSelectedFile().toString());
    }
  }

  public void itemStateChanged(ItemEvent e) {
    Object s = e.getSource();
    if (s instanceof JCheckBox && (JCheckBox)s == autoSelect) {
      zrle.setEnabled(!autoSelect.isSelected());
      hextile.setEnabled(!autoSelect.isSelected());
      tight.setEnabled(!autoSelect.isSelected());
      raw.setEnabled(!autoSelect.isSelected());
      fullColour.setEnabled(!autoSelect.isSelected());
      mediumColour.setEnabled(!autoSelect.isSelected());
      lowColour.setEnabled(!autoSelect.isSelected());
      veryLowColour.setEnabled(!autoSelect.isSelected());
    }
    if (s instanceof JCheckBox && (JCheckBox)s == customCompressLevel) {
      compressLevel.setEnabled(customCompressLevel.isSelected());
    }
    if (s instanceof JCheckBox && (JCheckBox)s == desktopSize) {
      desktopWidth.setEnabled(desktopSize.isSelected());
      desktopHeight.setEnabled(desktopSize.isSelected());
    }
    if (s instanceof JCheckBox && (JCheckBox)s == noJpeg) {
      qualityLevel.setEnabled(noJpeg.isSelected());
    }
    if (s instanceof JCheckBox && (JCheckBox)s == secVeNCrypt) {
      encNone.setEnabled(secVeNCrypt.isSelected());
      encTLS.setEnabled(secVeNCrypt.isSelected());
      encX509.setEnabled(secVeNCrypt.isSelected());
      ca.setEnabled(secVeNCrypt.isSelected());
      crl.setEnabled(secVeNCrypt.isSelected());
      secIdent.setEnabled(secVeNCrypt.isSelected());
      secPlain.setEnabled(secVeNCrypt.isSelected());
      sendLocalUsername.setEnabled(secVeNCrypt.isSelected());
    }
    if (s instanceof JCheckBox && (JCheckBox)s == encNone) {
      secNone.setSelected(encNone.isSelected() &&
        UserPreferences.getBool("viewer", "secNone", true));
      secVnc.setSelected(encNone.isSelected() &&
        UserPreferences.getBool("viewer", "secVnc", true));
    }
    if (s instanceof JCheckBox && (JCheckBox)s == secIdent ||
        s instanceof JCheckBox && (JCheckBox)s == secPlain) {
      sendLocalUsername.setEnabled(secIdent.isSelected()||secPlain.isSelected());
    }
  }

}
