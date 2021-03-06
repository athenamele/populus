/*******************************************************************************
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * This software is released under GNU General Public License 2.0
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html
 *******************************************************************************/
package edu.umn.ecology.populus.model.md;

import edu.umn.ecology.populus.plot.BasicPlotModel;

import java.util.ResourceBundle;

public class MDModel extends BasicPlotModel {
    static final ResourceBundle res = ResourceBundle.getBundle("edu.umn.ecology.populus.model.md.Res");

    @Override
    public Object getModelHelpText() {
        return "MDHELP";
    }

    public MDModel() {
        this.setModelInput(new MDPanel());
    }

    public static String getModelName() {
        return res.getString("Infectious");
    }

    @Override
    protected String getHelpId() {
        return "md.overview";
    }
}
