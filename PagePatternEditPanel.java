/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaOptions;
//import com.platformda.syntax.SyntaxEditor;
import com.platformda.iv.tools.auto.SyntaxEditor;
import com.platformda.utility.common.StringBuilderAppender;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JPanel;
import org.openide.util.Exceptions;

/**
 *
 * TODO: add variable table at bottom
 *
 *
 * @author Junyi
 */
public class PagePatternEditPanel extends JPanel {

    EntityPagePattern pagePattern;
    SyntaxEditor textArea = new SyntaxEditor("text/page");

    public PagePatternEditPanel(EntityPagePattern pagePattern) {
        this.pagePattern = pagePattern;
        initComponents();
    }

    private void initComponents() {
        StringBuilder builder = new StringBuilder();
        StringBuilderAppender appender = new StringBuilderAppender(builder);
        try {
            PagePatternLoader.save(pagePattern, appender);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        textArea.setContent(builder.toString());

        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);
        setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE);
    }

    public void stopEditting() {
        String text = textArea.getContent();
        String[] lines = text.split("\n");
        PagePatternLoader loader = new PagePatternLoader();
        EntityPagePattern epp = loader.loadPagePattern(Arrays.asList(lines));
        epp.setDeviceType(pagePattern.getDeviceType());
        epp.setDevicePolarity(pagePattern.getDevicePolarity());
        pagePattern.copyValueOf(epp);
    }
}
