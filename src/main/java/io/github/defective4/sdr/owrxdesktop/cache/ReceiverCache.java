package io.github.defective4.sdr.owrxdesktop.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel.Type;
import io.github.defective4.sdr.owrxdesktop.ui.component.UserBookmark;

public class ReceiverCache {
    private final Map<String, UserBookmark> bookmarks = new HashMap<>();
    private final Map<String, List<FFTLabel>> labels = new HashMap<>();

    public UUID addBookmark(UserBookmark bookmark) {
        UUID uid = UUID.randomUUID();
        bookmarks.put(uid.toString(), bookmark);
        return uid;
    }

    public void clearLabels(String profile) {
        labels.remove(profile);
    }

    public Map<String, UserBookmark> getBookmarksMap() {
        return Collections.unmodifiableMap(bookmarks);
    }

    public Map<String, List<FFTLabel>> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public List<UserBookmark> getUserBookmarks(String profile) {
        return bookmarks.values().stream().filter(bm -> bm.profile().equals(profile)).toList();
    }

    public void removeBookmark(UUID uuid) {
        bookmarks.remove(uuid.toString());
    }

    public void removeLabel(String profile, FFTLabel label) {
        if (labels.containsKey(profile)) labels.get(profile).remove(label);
    }

    public void setLabels(String profile, List<FFTLabel> labels) {
        List<Type> types = labels.stream().map(FFTLabel::type).toList();
        List<FFTLabel> list = this.labels.computeIfAbsent(profile, t -> new ArrayList<>());
        list.stream().filter(label -> types.contains(label.type())).toList().forEach(list::remove);
        List<FFTLabel> flatList = this.labels.values().stream().flatMap(List::stream).toList();
        list.addAll(labels.stream()
                .filter(label -> flatList.stream().noneMatch(flatLabel -> flatLabel.freq() == label.freq())).toList());
    }

}
