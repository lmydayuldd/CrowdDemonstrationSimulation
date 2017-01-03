package graphics;

import javax.swing.*;

public class LabelSpinnerPanel extends JPanel {

    private static final long serialVersionUID = 8226660925214936728L;

    private final JLabel label;
    private final JSpinner spinner;

    public LabelSpinnerPanel(int x, int y, double start, double min, double max, double step, String labelName) {
        this.setBounds(x, y, 247, 37);
        this.setOpaque(false);
        this.setLayout(null);
        label = new JLabel(labelName);
        label.setBounds(0, 5, 170, 27);
        add(label);
        SpinnerNumberModel model = new SpinnerNumberModel(start, min, max, step);
        spinner = new JSpinner(model);
        spinner.setBounds(175, 5, 40, 27);
        add(spinner);
    }

    public void changeLabelName(String labelName) {
        label.setText(labelName);
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public int getSpinnerValue() {
        return (Integer) spinner.getValue();
    }

    public double getSpinnerDoubleValue() {
        return (Double) spinner.getValue();
    }

}
