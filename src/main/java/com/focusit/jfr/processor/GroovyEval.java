package com.focusit.jfr.processor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.spi.EventOrder;
import com.jrockit.mc.flightrecorder.spi.IView;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Created by doki on 26.08.16.
 */
public class GroovyEval extends JFrame
{
    private JPanel rootPanel;
    private JPanel JFRSourcePanel;
    private JTextField sourceFileField;
    private JButton browseButton;
    private JButton loadJfr;
    private JPanel contentPanel;
    private JPanel templatesPanel;
    private JButton stackprocessButton;
    private JButton exceptionprocessButton;
    private JButton helpButton;
    private JButton runButton;
    private RSyntaxTextArea scriptArea;
    private JScrollPane scrollPane1;
    private GroovyEval self;
    private FlightRecording recording;
    private IView view;

    public GroovyEval() throws HeadlessException
    {
        setContentPane(rootPanel);
        pack();
        self = this;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        scriptArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        scriptArea.setAntiAliasingEnabled(true);
        browseButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser dialog = new JFileChooser();
                int ret = dialog.showDialog(self, "Open JFR");
                if (ret == JFileChooser.APPROVE_OPTION)
                {
                    String file = dialog.getSelectedFile().getAbsolutePath();
                    sourceFileField.setText(file);
                }
            }
        });
        loadJfr.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                recording = FlightRecordingLoader.loadFile(new File(sourceFileField.getText()));
                view = recording.createView();
                view.setOrder(EventOrder.ASCENDING);
            }
        });
        runButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GroovyShell shell = new GroovyShell();
                Script script = shell.parse(scriptArea.getText());
                Binding binding = new Binding();
                binding.setProperty("recording", recording);
                binding.setProperty("view", view);
                script.setBinding(binding);
                script.run();
            }
        });
    }

    private void createUIComponents()
    {
        RTextScrollPane pane = new RTextScrollPane(scriptArea);
        pane.setLineNumbersEnabled(true);
        pane.setFoldIndicatorEnabled(true);
        scrollPane1 = pane;
    }
}
