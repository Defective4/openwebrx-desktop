package io.github.defective4.sdr.owrxdesktop.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel.Type;

public class ReceiverCache {
    private final Map<String, List<FFTLabel>> labels = new HashMap<>();

    public void clearLabels(String profile) {
        labels.remove(profile);
    }

    public Map<String, List<FFTLabel>> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public void removeLabel(String profile, FFTLabel label) {
        if (labels.containsKey(profile)) labels.get(profile).remove(label);
    }

    public void setLabels(String profile, List<FFTLabel> labels) {
        List<Type> types = labels.stream().map(FFTLabel::type).toList();
        List<FFTLabel> list = this.labels.computeIfAbsent(profile, t -> new ArrayList<>());
        list.stream().filter(label -> types.contains(label.type())).toList().forEach(list::remove);
        list.addAll(labels);
    }

}
