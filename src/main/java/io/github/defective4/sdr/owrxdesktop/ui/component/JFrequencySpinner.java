package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.text.ParseException;
import java.util.Arrays;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class JFrequencySpinner extends JSpinner {

    private static final class FrequencyFormatter extends AbstractFormatter {
        private int val = 0;

        @Override
        public Object stringToValue(String text) throws ParseException {
            String[] split = text.split(" ");
            try {
                if (split.length == 2) {
                    double num = Double.parseDouble(split[0]);
                    String unitStr = split[1];
                    if (unitStr.toLowerCase().endsWith("hz") && unitStr.length() <= 3) {
                        FrequencyUnit unit = FrequencyUnit.valueOf(unitStr.substring(0, 1).toUpperCase());
                        return (int) (num * unit.getMultiplier());
                    }
                } else if (split.length == 1) {
                    return (int) Double.parseDouble(split[0]);
                }
            } catch (IllegalArgumentException e) {}
            return val;
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            val = Math.max(0, (int) value);
            FrequencyUnit u = FrequencyUnit.H;
            for (FrequencyUnit unit : Arrays.stream(FrequencyUnit.values())
                    .sorted((o1, o2) -> o2.getMultiplier() - o1.getMultiplier()).toList()) {
                if (val >= unit.getMultiplier()) {
                    u = unit;
                    break;
                }
            }
            return val / (double) u.getMultiplier() + " " + u;
        }
    }

    private static final class FrequencyFormatterFactory extends AbstractFormatterFactory {
        @Override
        public AbstractFormatter getFormatter(JFormattedTextField tf) {
            return new FrequencyFormatter();
        }
    }

    public JFrequencySpinner() {
        setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        ((NumberEditor) getEditor()).getTextField().setFormatterFactory(new FrequencyFormatterFactory());
    }
}
