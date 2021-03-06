/*******************************************************************************
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * This software is released under GNU General Public License 2.0
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html
 *******************************************************************************/
package edu.umn.ecology.populus.model.soamal;

import edu.umn.ecology.populus.math.NumberMath;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class SOAMALTable extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 6320760428782955706L;
    int dimension = 0;
    Vector[] table;

    /**
     * it is assumed that there will be 4 columns on the left side, so the parameter
     * is for the dimension of the Genotypic Fitness Matrix
     */
    public SOAMALTable() {
        table = new Vector[4];
        for (int i = 0; i < 4; i++)
            table[i] = new Vector(0);

        addDimension(true, true, 0.67, new double[]{0.89});
        addDimension(true, true, 0.12, new double[]{1.00, 0.20});
        addDimension(true, true, 0.21, new double[]{0.89, 0.7, 1.31});
        addDimension(false, false, 0.0d, new double[]{1, 1, 1, 1});
        addDimension(false, false, 0.0d, new double[]{1, 1, 1, 1, 1});
        addDimension(false, false, 0.0d, new double[]{1, 1, 1, 1, 1, 1});

        super.fireTableChanged(new TableModelEvent(this));
    }

    /**
     * matrix.length must be the new dimension
     *
     * @param use
     * @param plot
     * @param initFreq
     * @param matrix
     */
    public void addDimension(boolean use, boolean plot, double initFreq, double[] matrix) {
        dimension++;
        if (dimension != matrix.length) {
            edu.umn.ecology.populus.fileio.Logging.log("matrix.length incorrect");
            dimension--;
            return;
        }
        Vector[] temp = new Vector[4 + dimension];
        for (int i = 0; i < dimension - 1 + 4; i++)
            temp[i] = table[i];
        temp[dimension - 1 + 4] = new Vector(dimension);
        table = temp;

        table[0].add(dimension);
        table[1].add(use);
        table[2].add(plot);
        table[3].add(initFreq);

        for (int i = 0; i < dimension - 1; i++)
            table[4 + dimension - 1].add(matrix[i]);
        //table[4+dimension-1].add("-");
        for (int i = 0; i < dimension; i++)
            table[4 + i].add(matrix[i]);

		this.fireTableStructureChanged();
		//      fireTableChanged(new TableModelEvent(this));
	}
	/**
	 *  Return the name for the column
	 */
	@Override
	public String getColumnName(int column) {
		if(column>3) return ""+(column-3);
		switch (column) {
		case 0:
			return "Allele";
		case 1:
			return "Use";
		case 2:
			return "Plot";
		case 3:
			return "Initial p";
		default:
			return null;
		}
	}

    /**
     * Returns Object.class by default
     */
    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Returns true for anything in columns 1-3
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return false;
        if (columnIndex != 1 && !(Boolean) getValueAt(rowIndex, 1)) return false;
        return columnIndex <= 3 || (Boolean) getValueAt(columnIndex - 4, 1);
    }

    /**
     * make sure that the sum of the frequencies is zero
     *
     * @param aValue
     * @param rowIndex
     * @param columnIndex
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1 && !(Boolean) aValue) {
            table[2].setElementAt(Boolean.FALSE, rowIndex);
            super.fireTableDataChanged();
        }
        if (columnIndex > 3) {
            table[rowIndex + 4].setElementAt(aValue, (columnIndex - 4));
            super.fireTableDataChanged();
        }
        if (columnIndex == 3) {
            double newFreq = (Double) aValue;
            if (newFreq > 1) aValue = 1.0d;
        }
        fireTableDataChanged();
        table[columnIndex].setElementAt(aValue, rowIndex);
        balanceFrequencies(rowIndex);
    }

    @Override
    public int getRowCount() {
        return dimension;
    }

    @Override
    public int getColumnCount() {
        return dimension + 4;
    }

    public int getDataDimension() {
        return dimension;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return table[column].get(row);
    }

    /**
     * the total of the frequencies needs to be maintained at a
     * constant value of zero.
     *
     * @param rowIndex the row that needs to stay the same
     */
    private void balanceFrequencies(int rowIndex) {
        double dF = 0;
        int[] used = this.getUsed();
        double[] freqs = new double[used.length];
        for (int k = 0; k < freqs.length; k++) {
            freqs[k] = (Double) getValueAt(used[k], 3);
            dF += freqs[k];
        }
        dF = 1.0 - dF;
        int i;
        for (i = 0; i < used.length; i++)
            if (used[i] == rowIndex) break;
        while (dF != 0.0) {
            i = (i + 1) % freqs.length;
            freqs[i] += dF;
            if (freqs[i] > 1.0) {
                freqs[i] = 1.0d;
                dF = 0.0;
                for (int j = 0; j < freqs.length; j++)
                    if (j != i) freqs[j] = 0.0d;
            } else if (freqs[i] < 0.0) {
                dF = freqs[i];
                freqs[i] = 0.0;
            } else dF = 0.0;
        }
        Double newValue;
        for (int k = 0; k < freqs.length; k++) {
            newValue = NumberMath.roundSig(freqs[k], 10, 0);
            table[3].setElementAt(newValue, used[k]);
        }
        fireTableDataChanged();
    }

    /**
     * this method is useful for making labels for the matrix data because numbers
     * in the array would correspond to the labels for the data at their index
     *
     * @return
     */
    public int[] getUsed() {
        int[] used;
        int count = 0;

        for (int i = 0; i < dimension; i++)
            if ((Boolean) getValueAt(i, 1)) count++;
        used = new int[count];
        count = 0;
        for (int i = 0; count < used.length; i++)
            if ((Boolean) getValueAt(i, 1)) {
                used[count] = i;
                count++;
            }
        return used;
    }

    /**
     * this method returns an array with information regarding which of the matrix data are meant
     * to be plotted. this information is "relative" to the position in the data matrix returned by
     * getMatrix() and not their position in the table.
     *
     * @return
     */
    public int[] getPlotted() {
        int[] plotted;
        int[] u = getUsed();
        int count = 0;

        for (int j : u)
            if ((Boolean) getValueAt(j, 2))
                count++;
        plotted = new int[count];
        count = 0;
        for (int i = 0; count < plotted.length; i++)
            if ((Boolean) getValueAt(i, 2)) {
                plotted[count] = i;
                count++;
            }
        return plotted;
    }

    synchronized double[] getInitialFrequencies() {
        int[] u = getUsed();
        double[] ifreq = new double[u.length];
        for (int i = 0; i < ifreq.length; i++)
            ifreq[i] = (Double) getValueAt(u[i], 3);
        return ifreq;
    }

    /**
     * the matrix returned by this method "ignores" the positions on the input screen's table
     * and gives only the important data for the graph.
     */
    synchronized double[][] getMatrix() {
        int[] u = getUsed();
        double[][] data = new double[u.length][u.length];
        for (int i = 0; i < data.length; i++)
            for (int j = 0; j < data[i].length; j++)
                data[i][j] = (Double) getValueAt(u[i], u[j] + 4);
        return data;
    }
}
